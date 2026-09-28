package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DocumentEntity
import com.example.data.model.MediaEntity
import com.example.ui.components.DocumentItemCard
import com.example.ui.components.MediaItemCard
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel

@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.allDocuments.collectAsStateWithLifecycle()
    val audioItems by viewModel.allAudio.collectAsStateWithLifecycle()
    val videoItems by viewModel.allVideos.collectAsStateWithLifecycle()
    val recentDocs by viewModel.recentDocuments.collectAsStateWithLifecycle()
    val recentAudio by viewModel.recentAudio.collectAsStateWithLifecycle()
    val audioState by viewModel.audioPlayer.state.collectAsStateWithLifecycle()

    // File picker launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importFiles(uris)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("home_screen_content"),
        contentPadding = PaddingValues(bottom = 90.dp)
    ) {
        // App Header Banner
        item {
            HeaderBanner(
                onBrowseFiles = { viewModel.navigateTo(AppScreen.FILE_EXPLORER) },
                onImportClick = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "text/*",
                            "audio/*",
                            "video/*",
                            "application/json",
                            "text/csv"
                        )
                    )
                }
            )
        }

        // Quick Stats row
        item {
            QuickStatsRow(
                docsCount = documents.size,
                audioCount = audioItems.size,
                videoCount = videoItems.size,
                onNavigateDocs = { viewModel.navigateTo(AppScreen.DOCUMENTS) },
                onNavigateMusic = { viewModel.navigateTo(AppScreen.MUSIC) },
                onNavigateVideos = { viewModel.navigateTo(AppScreen.VIDEOS) }
            )
        }

        // Continue Reading section (if any document available)
        if (recentDocs.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Continue Reading",
                    actionLabel = "View All",
                    onActionClick = { viewModel.navigateTo(AppScreen.DOCUMENTS) }
                )
            }
            items(recentDocs.take(2)) { doc ->
                DocumentItemCard(
                    document = doc,
                    onClick = { viewModel.openDocument(doc) },
                    onToggleFavorite = { viewModel.toggleDocumentFavorite(doc.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Quick Audio section
        if (audioItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Offline Audio Studio",
                    actionLabel = "View All",
                    onActionClick = { viewModel.navigateTo(AppScreen.MUSIC) }
                )
            }
            items(audioItems.take(3)) { track ->
                MediaItemCard(
                    media = track,
                    isPlaying = audioState.isPlaying && audioState.currentTrack?.id == track.id,
                    onClick = { viewModel.playAudio(track, audioItems) },
                    onToggleFavorite = { viewModel.toggleMediaFavorite(track.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }

        // Featured Videos section
        if (videoItems.isNotEmpty()) {
            item {
                SectionHeader(
                    title = "Offline Cinema Videos",
                    actionLabel = "View All",
                    onActionClick = { viewModel.navigateTo(AppScreen.VIDEOS) }
                )
            }
            items(videoItems.take(2)) { video ->
                MediaItemCard(
                    media = video,
                    isPlaying = false,
                    onClick = {
                        viewModel.videoPlayer.prepareAndPlay(video)
                        viewModel.openVideo(video)
                    },
                    onToggleFavorite = { viewModel.toggleMediaFavorite(video.id) },
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun HeaderBanner(
    onBrowseFiles: () -> Unit,
    onImportClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "OFFLINE MASTER",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = "100% On-Device",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "MediaMaster_#MYKSON#",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Text(
                    text = "Read all document types & play high-fidelity music and videos completely offline.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 6.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onBrowseFiles,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_browse_storage_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FolderOpen,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Browse Storage")
                    }

                    OutlinedButton(
                        onClick = onImportClick,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("home_import_files_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileUpload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Import Files")
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    docsCount: Int,
    audioCount: Int,
    videoCount: Int,
    onNavigateDocs: () -> Unit,
    onNavigateMusic: () -> Unit,
    onNavigateVideos: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = "Documents",
            count = "$docsCount",
            icon = Icons.Default.Description,
            color = Color(0xFF06B6D4),
            onClick = onNavigateDocs,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Music",
            count = "$audioCount",
            icon = Icons.Default.MusicNote,
            color = Color(0xFF8B5CF6),
            onClick = onNavigateMusic,
            modifier = Modifier.weight(1f)
        )
        StatCard(
            title = "Videos",
            count = "$videoCount",
            icon = Icons.Default.Videocam,
            color = Color(0xFFF59E0B),
            onClick = onNavigateVideos,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp
            ),
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = actionLabel,
            style = MaterialTheme.typography.bodySmall.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            ),
            modifier = Modifier
                .clickable { onActionClick() }
                .padding(4.dp)
        )
    }
}
