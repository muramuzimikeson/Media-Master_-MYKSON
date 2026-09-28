package com.example.ui.screens

import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.player.VideoAspectRatio
import com.example.ui.components.formatDuration
import com.example.ui.viewmodel.MainViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val playerState by viewModel.videoPlayer.state.collectAsStateWithLifecycle()
    val video = playerState.currentVideo

    BackHandler {
        viewModel.videoPlayer.release()
        onBack()
    }

    var showControls by remember { mutableStateOf(true) }
    var showSpeedMenu by remember { mutableStateOf(false) }

    // Auto-hide controls after 4 seconds of inactivity
    LaunchedEffect(showControls, playerState.isPlaying) {
        if (showControls && playerState.isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
            .testTag("video_player_container")
    ) {
        // Video Surface
        val videoAspectRatioModifier = when (playerState.aspectRatio) {
            VideoAspectRatio.RATIO_16_9 -> Modifier.aspectRatio(16f / 9f)
            VideoAspectRatio.RATIO_4_3 -> Modifier.aspectRatio(4f / 3f)
            VideoAspectRatio.FILL -> Modifier.fillMaxSize()
            VideoAspectRatio.FIT -> {
                if (playerState.videoWidth > 0 && playerState.videoHeight > 0) {
                    Modifier.aspectRatio(playerState.videoWidth.toFloat() / playerState.videoHeight.toFloat())
                } else {
                    Modifier.fillMaxSize()
                }
            }
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    SurfaceView(context).apply {
                        holder.addCallback(object : SurfaceHolder.Callback {
                            override fun surfaceCreated(holder: SurfaceHolder) {
                                viewModel.videoPlayer.attachSurface(holder.surface)
                                if (video != null && !playerState.isPrepared) {
                                    viewModel.videoPlayer.prepareAndPlay(video, holder.surface)
                                }
                            }

                            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

                            override fun surfaceDestroyed(holder: SurfaceHolder) {}
                        })
                    }
                },
                modifier = videoAspectRatioModifier
            )
        }

        // Loading indicator
        if (!playerState.isPrepared) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Controls Overlay
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                // Top Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.TopCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent)
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = {
                            viewModel.videoPlayer.release()
                            onBack()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = video?.title ?: "Offline Video",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Aspect Ratio Button
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .clickable { viewModel.videoPlayer.toggleAspectRatio() }
                            .padding(end = 8.dp)
                    ) {
                        Text(
                            text = playerState.aspectRatio.label,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    // Speed selector
                    Box {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.2f),
                            modifier = Modifier.clickable { showSpeedMenu = true }
                        ) {
                            Text(
                                text = "${playerState.playbackSpeed}x",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                        DropdownMenu(
                            expanded = showSpeedMenu,
                            onDismissRequest = { showSpeedMenu = false }
                        ) {
                            listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f).forEach { spd ->
                                DropdownMenuItem(
                                    text = { Text("${spd}x") },
                                    onClick = {
                                        viewModel.videoPlayer.setPlaybackSpeed(spd)
                                        showSpeedMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Center Playback Controls
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Rewind 10s
                    IconButton(
                        onClick = { viewModel.videoPlayer.skipRewind(10) },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Rewind 10s",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Big Play/Pause Button
                    Surface(
                        onClick = { viewModel.videoPlayer.togglePlayPause() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .size(72.dp)
                            .testTag("video_play_pause_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    // Forward 10s
                    IconButton(
                        onClick = { viewModel.videoPlayer.skipForward(10) },
                        modifier = Modifier.size(52.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Forward 10s",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Bottom Seekbar & Controls
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                            )
                        )
                        .padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
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
                            viewModel.videoPlayer.seekTo((seekPos * playerState.durationMs).toLong())
                        },
                        colors = SliderDefaults.colors(
                            thumbColor = MaterialTheme.colorScheme.primary,
                            activeTrackColor = MaterialTheme.colorScheme.primary,
                            inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val currentMs = if (isSeeking) (seekPos * playerState.durationMs).toLong() else playerState.currentPositionMs
                        Text(
                            text = "${formatDuration(currentMs)} / ${formatDuration(playerState.durationMs)}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.weight(1f))

                        // Loop toggle
                        IconButton(
                            onClick = { viewModel.videoPlayer.toggleLooping() },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Repeat,
                                contentDescription = "Loop",
                                tint = if (playerState.isLooping) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
            }
        }
    }
}
