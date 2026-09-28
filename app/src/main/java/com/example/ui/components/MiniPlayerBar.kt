package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.player.AudioPlayerState

@Composable
fun MiniPlayerBar(
    playerState: AudioPlayerState,
    onTogglePlayPause: () -> Unit,
    onSkipNext: () -> Unit,
    onClickBar: () -> Unit,
    modifier: Modifier = Modifier
) {
    val track = playerState.currentTrack

    AnimatedVisibility(
        visible = track != null,
        enter = slideInVertically { it },
        exit = slideOutVertically { it },
        modifier = modifier
    ) {
        if (track != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onClickBar() }
                    .testTag("mini_player_bar"),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                tonalElevation = 8.dp,
                shadowElevation = 10.dp
            ) {
                Column {
                    // Thin progress indicator on top of mini player
                    val progress = if (playerState.durationMs > 0) {
                        (playerState.currentPositionMs.toFloat() / playerState.durationMs).coerceIn(0f, 1f)
                    } else 0f

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.5.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Album / Icon avatar
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MusicNote,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Title & Artist
                        Column(
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = track.title,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = track.artist,
                                style = MaterialTheme.typography.bodySmall.copy(
                                    fontSize = 12.sp
                                ),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        // Play/Pause button
                        IconButton(
                            onClick = onTogglePlayPause,
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primary)
                                .testTag("mini_player_play_pause_button")
                        ) {
                            Icon(
                                imageVector = if (playerState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (playerState.isPlaying) "Pause" else "Play",
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Skip next button
                        IconButton(
                            onClick = onSkipNext,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "Next Track",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
