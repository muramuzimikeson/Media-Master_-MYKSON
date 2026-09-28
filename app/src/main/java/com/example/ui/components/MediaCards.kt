package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentType
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType

@Composable
fun DocumentItemCard(
    document: DocumentEntity,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (icon, badgeColor, badgeLabel) = when (document.fileType) {
        DocumentType.PDF.name -> Triple(Icons.Default.PictureAsPdf, Color(0xFFEF4444), "PDF")
        DocumentType.CSV.name -> Triple(Icons.Default.TableChart, Color(0xFF10B981), "CSV")
        DocumentType.JSON.name, DocumentType.CODE.name -> Triple(Icons.Default.Code, Color(0xFF8B5CF6), "CODE")
        DocumentType.MARKDOWN.name -> Triple(Icons.Default.Description, Color(0xFF06B6D4), "MD")
        else -> Triple(Icons.Default.Description, Color(0xFF3B82F6), "DOC")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("document_card_${document.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(badgeColor.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = badgeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Title and info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = document.title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = badgeColor.copy(alpha = 0.2f),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = badgeLabel,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = badgeColor,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        val sizeStr = formatFileSize(document.fileSizeBytes)
                        Text(
                            text = if (document.totalPages > 1) "${document.totalPages} pgs • $sizeStr" else sizeStr,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Favorite icon button
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (document.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (document.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Reading progress bar if started
            if (document.readingProgressPercent > 0) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LinearProgressIndicator(
                        progress = { document.readingProgressPercent / 100f },
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${document.readingProgressPercent}%",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun MediaItemCard(
    media: MediaEntity,
    isPlaying: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isVideo = media.mediaType == MediaType.VIDEO.name
    val accentColor = if (isVideo) Color(0xFFF59E0B) else Color(0xFF06B6D4)
    val icon = if (isVideo) Icons.Default.Videocam else Icons.Default.MusicNote

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .testTag("media_card_${media.id}"),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Media Icon thumbnail
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = media.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${media.artist} • ${formatDuration(media.durationMs)}",
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Favorite button
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (media.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (media.isFavorite) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Play action button
            IconButton(
                onClick = onClick,
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyStateView(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

fun formatDuration(durationMs: Long): String {
    if (durationMs <= 0) return "00:00"
    val totalSeconds = durationMs / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format("%02d:%02d", minutes, seconds)
}

fun formatFileSize(bytes: Long): String {
    if (bytes <= 0) return "0 KB"
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    return if (mb >= 1.0) {
        String.format("%.1f MB", mb)
    } else {
        String.format("%.0f KB", kb)
    }
}
