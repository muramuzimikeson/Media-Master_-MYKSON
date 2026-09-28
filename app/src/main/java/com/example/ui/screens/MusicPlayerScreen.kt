package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Equalizer
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.player.RepeatMode
import com.example.ui.components.AudioVisualizer
import com.example.ui.components.formatDuration
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicPlayerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.audioPlayer.state.collectAsStateWithLifecycle()
    val track = playerState.currentTrack

    BackHandler {
        onBack()
    }

    var showSpeedMenu by remember { mutableStateOf(false) }
    var showTimerMenu by remember { mutableStateOf(false) }
    var showEqualizerMenu by remember { mutableStateOf(false) }

    // Vinyl rotation animation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing)
        ),
        label = "rotation"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = "NOW PLAYING",
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp,
                                fontSize = 11.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = track?.album ?: "Offline Studio",
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Collapse")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            track?.id?.let { viewModel.toggleMediaFavorite(it) }
                        }
                    ) {
                        Icon(
                            imageVector = if (track?.isFavorite == true) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (track?.isFavorite == true) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Vinyl Record Artwork with pulsing aura
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(Color(0xFF0F172A))
                        .border(6.dp, MaterialTheme.colorScheme.surfaceVariant, CircleShape)
                        .rotate(if (playerState.isPlaying) rotation else 0f),
                    contentAlignment = Alignment.Center
                ) {
                    // Vinyl grooves
                    Box(
                        modifier = Modifier
                            .size(160.dp)
                            .border(1.dp, Color(0xFF1E293B), CircleShape)
                    )
                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .border(1.dp, Color(0xFF334155), CircleShape)
                    )
                    // Center label
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MusicNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            // Track Title & Artist
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = track?.title ?: "No Track Selected",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = track?.artist ?: "Unknown Artist",
                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Live Audio Waveform Visualizer
            AudioVisualizer(
                amplitudes = playerState.visualizerAmplitudes,
                isPlaying = playerState.isPlaying,
                modifier = Modifier.padding(vertical = 4.dp)
            )

            // Seek Slider & Timestamps
            Column(modifier = Modifier.fillMaxWidth()) {
                var isSeeking by remember { mutableStateOf(false) }
                var seekPos by remember { mutableFloatStateOf(0f) }

                val currentSliderValue = if (isSeeking) seekPos
                else (playerState.currentPositionMs.toFloat() / playerState.durationMs.coerceAtLeast(1L)).coerceIn(0f, 1f)

                Slider(
                    value = currentSliderValue,
                    onValueChange = {
                        isSeeking = true
                        seekPos = it
                    },
                    onValueChangeFinished = {
                        isSeeking = false
                        viewModel.audioPlayer.seekTo((seekPos * playerState.durationMs).toLong())
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val currentMs = if (isSeeking) (seekPos * playerState.durationMs).toLong() else playerState.currentPositionMs
                    Text(
                        text = formatDuration(currentMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatDuration(playerState.durationMs),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Playback Controls Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Shuffle Button
                IconButton(onClick = { viewModel.audioPlayer.toggleShuffle() }) {
                    Icon(
                        imageVector = Icons.Default.Shuffle,
                        contentDescription = "Shuffle",
                        tint = if (playerState.isShuffle) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Previous Button
                IconButton(
                    onClick = { viewModel.audioPlayer.skipPrevious() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipPrevious,
                        contentDescription = "Previous",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Play / Pause Button (Large Center)
                Surface(
                    onClick = { viewModel.audioPlayer.togglePlayPause() },
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .size(68.dp)
                        .testTag("fullscreen_play_pause_button")
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Next Button
                IconButton(
                    onClick = { viewModel.audioPlayer.skipNext() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.SkipNext,
                        contentDescription = "Next",
                        modifier = Modifier.size(32.dp)
                    )
                }

                // Repeat Button
                IconButton(onClick = { viewModel.audioPlayer.toggleRepeatMode() }) {
                    val (icon, color) = when (playerState.repeatMode) {
                        RepeatMode.REPEAT_ONE -> Pair(Icons.Default.RepeatOne, MaterialTheme.colorScheme.primary)
                        RepeatMode.REPEAT_ALL -> Pair(Icons.Default.Repeat, MaterialTheme.colorScheme.primary)
                        RepeatMode.OFF -> Pair(Icons.Default.Repeat, MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(imageVector = icon, contentDescription = "Repeat", tint = color)
                }
            }

            // Bottom Audio Enhancements Row (Speed, Equalizer, Sleep Timer)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                // Playback Speed
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showSpeedMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("${playerState.playbackSpeed}x", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DropdownMenu(expanded = showSpeedMenu, onDismissRequest = { showSpeedMenu = false }) {
                        listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                            DropdownMenuItem(
                                text = { Text("${spd}x") },
                                onClick = {
                                    viewModel.audioPlayer.setPlaybackSpeed(spd)
                                    showSpeedMenu = false
                                }
                            )
                        }
                    }
                }

                // Equalizer Presets
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showEqualizerMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Equalizer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(playerState.equalizerPreset, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DropdownMenu(expanded = showEqualizerMenu, onDismissRequest = { showEqualizerMenu = false }) {
                        listOf("Normal", "Bass Boost", "Vocal", "Treble", "Rock", "Acoustic").forEach { eq ->
                            DropdownMenuItem(
                                text = { Text(eq) },
                                onClick = {
                                    viewModel.audioPlayer.setEqualizerPreset(eq)
                                    showEqualizerMenu = false
                                }
                            )
                        }
                    }
                }

                // Sleep Timer
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (playerState.sleepTimerRemainingSeconds != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { showTimerMenu = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            val timerText = if (playerState.sleepTimerRemainingSeconds != null) {
                                "${playerState.sleepTimerRemainingSeconds!! / 60}m left"
                            } else "Timer"
                            Text(timerText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    DropdownMenu(expanded = showTimerMenu, onDismissRequest = { showTimerMenu = false }) {
                        listOf(0 to "Off", 15 to "15 minutes", 30 to "30 minutes", 45 to "45 minutes", 60 to "60 minutes").forEach { (mins, label) ->
                            DropdownMenuItem(
                                text = { Text(label) },
                                onClick = {
                                    viewModel.audioPlayer.setSleepTimer(mins)
                                    showTimerMenu = false
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
