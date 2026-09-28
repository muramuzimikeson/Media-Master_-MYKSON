package com.example.player

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.util.Log
import com.example.data.model.MediaEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import kotlin.math.sin
import kotlin.random.Random

enum class RepeatMode {
    OFF,
    REPEAT_ALL,
    REPEAT_ONE
}

data class AudioPlayerState(
    val currentTrack: MediaEntity? = null,
    val isPlaying: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val repeatMode: RepeatMode = RepeatMode.REPEAT_ALL,
    val isShuffle: Boolean = false,
    val queue: List<MediaEntity> = emptyList(),
    val visualizerAmplitudes: List<Float> = List(16) { 0.1f },
    val sleepTimerRemainingSeconds: Int? = null,
    val equalizerPreset: String = "Normal"
)

class AudioPlayerManager(private val context: Context) {
    private val TAG = "AudioPlayerManager"
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private val _state = MutableStateFlow(AudioPlayerState())
    val state: StateFlow<AudioPlayerState> = _state.asStateFlow()

    private var progressJob: Job? = null
    private var sleepTimerJob: Job? = null
    private var visualizerTick = 0f

    init {
        startProgressTicker()
    }

    fun playTrack(media: MediaEntity, playlist: List<MediaEntity> = emptyList()) {
        try {
            val fullQueue = if (playlist.isNotEmpty()) playlist else listOf(media)
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                val uri = if (!media.filePath.isNullOrEmpty()) {
                    Uri.fromFile(File(media.filePath))
                } else {
                    Uri.parse(media.uriString)
                }
                setDataSource(context, uri)
                prepare()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    playbackParams = PlaybackParams().setSpeed(_state.value.playbackSpeed)
                }
                start()

                setOnCompletionListener {
                    handleTrackCompletion()
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "MediaPlayer error: what=$what, extra=$extra")
                    _state.value = _state.value.copy(isPlaying = false)
                    true
                }
            }

            _state.value = _state.value.copy(
                currentTrack = media,
                isPlaying = true,
                currentPositionMs = 0L,
                durationMs = mediaPlayer?.duration?.toLong()?.coerceAtLeast(0L) ?: media.durationMs,
                queue = fullQueue
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error playing track: ${e.message}", e)
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: return
        if (player.isPlaying) {
            player.pause()
            _state.value = _state.value.copy(isPlaying = false)
        } else {
            player.start()
            _state.value = _state.value.copy(isPlaying = true)
        }
    }

    fun seekTo(positionMs: Long) {
        val player = mediaPlayer ?: return
        val target = positionMs.toInt().coerceIn(0, player.duration)
        player.seekTo(target)
        _state.value = _state.value.copy(currentPositionMs = target.toLong())
    }

    fun skipForward(seconds: Int = 10) {
        val player = mediaPlayer ?: return
        seekTo((player.currentPosition + seconds * 1000).toLong())
    }

    fun skipRewind(seconds: Int = 10) {
        val player = mediaPlayer ?: return
        seekTo((player.currentPosition - seconds * 1000).coerceAtLeast(0).toLong())
    }

    fun skipNext() {
        val currentQueue = _state.value.queue
        if (currentQueue.isEmpty()) return
        val currentIndex = currentQueue.indexOfFirst { it.id == _state.value.currentTrack?.id }
        if (currentIndex != -1) {
            val nextIndex = if (_state.value.isShuffle) {
                Random.nextInt(currentQueue.size)
            } else {
                (currentIndex + 1) % currentQueue.size
            }
            playTrack(currentQueue[nextIndex], currentQueue)
        }
    }

    fun skipPrevious() {
        val currentQueue = _state.value.queue
        if (currentQueue.isEmpty()) return
        val currentIndex = currentQueue.indexOfFirst { it.id == _state.value.currentTrack?.id }
        if (currentIndex != -1) {
            val prevIndex = if (currentIndex - 1 < 0) currentQueue.size - 1 else currentIndex - 1
            playTrack(currentQueue[prevIndex], currentQueue)
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().setSpeed(speed)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to set playback speed: ${e.message}")
            }
        }
    }

    fun toggleRepeatMode() {
        val next = when (_state.value.repeatMode) {
            RepeatMode.OFF -> RepeatMode.REPEAT_ALL
            RepeatMode.REPEAT_ALL -> RepeatMode.REPEAT_ONE
            RepeatMode.REPEAT_ONE -> RepeatMode.OFF
        }
        _state.value = _state.value.copy(repeatMode = next)
    }

    fun toggleShuffle() {
        _state.value = _state.value.copy(isShuffle = !_state.value.isShuffle)
    }

    fun setEqualizerPreset(preset: String) {
        _state.value = _state.value.copy(equalizerPreset = preset)
    }

    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _state.value = _state.value.copy(sleepTimerRemainingSeconds = null)
            return
        }
        var remainingSeconds = minutes * 60
        _state.value = _state.value.copy(sleepTimerRemainingSeconds = remainingSeconds)
        sleepTimerJob = scope.launch {
            while (isActive && remainingSeconds > 0) {
                delay(1000)
                remainingSeconds--
                _state.value = _state.value.copy(sleepTimerRemainingSeconds = remainingSeconds)
            }
            // Timer expired: pause music
            if (_state.value.isPlaying) {
                togglePlayPause()
            }
            _state.value = _state.value.copy(sleepTimerRemainingSeconds = null)
        }
    }

    private fun handleTrackCompletion() {
        when (_state.value.repeatMode) {
            RepeatMode.REPEAT_ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _state.value = _state.value.copy(isPlaying = true)
            }
            RepeatMode.REPEAT_ALL -> {
                skipNext()
            }
            RepeatMode.OFF -> {
                val currentQueue = _state.value.queue
                val currentIndex = currentQueue.indexOfFirst { it.id == _state.value.currentTrack?.id }
                if (currentIndex != -1 && currentIndex + 1 < currentQueue.size) {
                    skipNext()
                } else {
                    _state.value = _state.value.copy(isPlaying = false, currentPositionMs = 0)
                }
            }
        }
    }

    private fun startProgressTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && player.isPlaying) {
                    val pos = player.currentPosition.toLong()
                    val dur = player.duration.toLong().coerceAtLeast(1L)
                    visualizerTick += 0.25f

                    // Generate harmonic wave visualizer amplitudes reacting to playback
                    val amplitudes = List(16) { idx ->
                        val base = sin(visualizerTick * 1.8f + idx * 0.5f) * 0.4f + 0.5f
                        val modulation = sin(visualizerTick * 0.9f + idx * 1.2f) * 0.2f
                        (base + modulation).coerceIn(0.12f, 0.98f)
                    }

                    _state.value = _state.value.copy(
                        currentPositionMs = pos,
                        durationMs = dur,
                        isPlaying = true,
                        visualizerAmplitudes = amplitudes
                    )
                } else if (_state.value.isPlaying) {
                    _state.value = _state.value.copy(isPlaying = false)
                }
                delay(100)
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        sleepTimerJob?.cancel()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}
