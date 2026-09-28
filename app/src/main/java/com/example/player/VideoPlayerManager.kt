package com.example.player

import android.content.Context
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.net.Uri
import android.os.Build
import android.util.Log
import android.view.Surface
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

enum class VideoAspectRatio(val label: String) {
    FIT("Fit Screen"),
    FILL("Crop / Fill"),
    RATIO_16_9("16:9"),
    RATIO_4_3("4:3")
}

data class VideoPlayerState(
    val currentVideo: MediaEntity? = null,
    val isPlaying: Boolean = false,
    val isPrepared: Boolean = false,
    val currentPositionMs: Long = 0L,
    val durationMs: Long = 0L,
    val playbackSpeed: Float = 1.0f,
    val aspectRatio: VideoAspectRatio = VideoAspectRatio.FIT,
    val videoWidth: Int = 0,
    val videoHeight: Int = 0,
    val isLooping: Boolean = false,
    val controlsVisible: Boolean = true
)

class VideoPlayerManager(private val context: Context) {
    private val TAG = "VideoPlayerManager"
    private var mediaPlayer: MediaPlayer? = null
    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var progressJob: Job? = null

    private val _state = MutableStateFlow(VideoPlayerState())
    val state: StateFlow<VideoPlayerState> = _state.asStateFlow()

    private var currentSurface: Surface? = null

    init {
        startTicker()
    }

    fun attachSurface(surface: Surface) {
        currentSurface = surface
        try {
            mediaPlayer?.setSurface(surface)
        } catch (e: Exception) {
            Log.e(TAG, "Error attaching surface: ${e.message}")
        }
    }

    fun prepareAndPlay(media: MediaEntity, surface: Surface? = null) {
        try {
            if (surface != null) {
                currentSurface = surface
            }
            mediaPlayer?.release()
            mediaPlayer = MediaPlayer().apply {
                currentSurface?.let { setSurface(it) }
                val uri = if (!media.filePath.isNullOrEmpty()) {
                    Uri.fromFile(File(media.filePath))
                } else {
                    Uri.parse(media.uriString)
                }
                setDataSource(context, uri)

                setOnPreparedListener { mp ->
                    _state.value = _state.value.copy(
                        isPrepared = true,
                        isPlaying = true,
                        durationMs = mp.duration.toLong().coerceAtLeast(0L),
                        videoWidth = mp.videoWidth,
                        videoHeight = mp.videoHeight
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        mp.playbackParams = PlaybackParams().setSpeed(_state.value.playbackSpeed)
                    }
                    mp.start()
                }

                setOnVideoSizeChangedListener { _, w, h ->
                    if (w > 0 && h > 0) {
                        _state.value = _state.value.copy(videoWidth = w, videoHeight = h)
                    }
                }

                setOnCompletionListener {
                    if (_state.value.isLooping) {
                        seekTo(0)
                        start()
                    } else {
                        _state.value = _state.value.copy(isPlaying = false, currentPositionMs = _state.value.durationMs)
                    }
                }

                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Video player error: what=$what, extra=$extra")
                    _state.value = _state.value.copy(isPlaying = false)
                    true
                }

                prepareAsync()
            }

            _state.value = _state.value.copy(
                currentVideo = media,
                isPlaying = false,
                isPrepared = false,
                currentPositionMs = 0L
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error playing video: ${e.message}", e)
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

    fun setPlaybackSpeed(speed: Float) {
        _state.value = _state.value.copy(playbackSpeed = speed)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().setSpeed(speed)
            } catch (e: Exception) {
                Log.e(TAG, "Failed setting video playback speed: ${e.message}")
            }
        }
    }

    fun toggleAspectRatio() {
        val next = when (_state.value.aspectRatio) {
            VideoAspectRatio.FIT -> VideoAspectRatio.FILL
            VideoAspectRatio.FILL -> VideoAspectRatio.RATIO_16_9
            VideoAspectRatio.RATIO_16_9 -> VideoAspectRatio.RATIO_4_3
            VideoAspectRatio.RATIO_4_3 -> VideoAspectRatio.FIT
        }
        _state.value = _state.value.copy(aspectRatio = next)
    }

    fun toggleLooping() {
        val next = !_state.value.isLooping
        _state.value = _state.value.copy(isLooping = next)
        mediaPlayer?.isLooping = next
    }

    fun setControlsVisibility(visible: Boolean) {
        _state.value = _state.value.copy(controlsVisible = visible)
    }

    private fun startTicker() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                val player = mediaPlayer
                if (player != null && _state.value.isPrepared) {
                    try {
                        val pos = player.currentPosition.toLong()
                        val dur = player.duration.toLong().coerceAtLeast(0L)
                        _state.value = _state.value.copy(
                            currentPositionMs = pos,
                            durationMs = dur,
                            isPlaying = player.isPlaying
                        )
                    } catch (ignored: Exception) {}
                }
                delay(200)
            }
        }
    }

    fun release() {
        progressJob?.cancel()
        try {
            mediaPlayer?.release()
        } catch (ignored: Exception) {}
        mediaPlayer = null
        currentSurface = null
        _state.value = VideoPlayerState()
    }
}
