package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MediaItemCard
import com.example.ui.viewmodel.MainViewModel

@Composable
fun VideosScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val videos by viewModel.filteredVideos.collectAsStateWithLifecycle()
    val searchQuery by viewModel.videoSearchQuery.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importFiles(uris)
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(
                        arrayOf("video/*")
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("import_video_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Import Video"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Input
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.videoSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("video_search_input"),
                placeholder = { Text("Search offline videos, movies, clips...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.videoSearchQuery.value = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                shape = RoundedCornerShape(14.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (videos.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Videocam,
                    title = "No Videos Found",
                    subtitle = if (searchQuery.isNotBlank()) "No videos match \"$searchQuery\""
                    else "Tap the + button to import offline MP4, MKV, or WEBM videos."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(videos, key = { it.id }) { video ->
                        MediaItemCard(
                            media = video,
                            isPlaying = false,
                            onClick = {
                                viewModel.videoPlayer.prepareAndPlay(video)
                                viewModel.openVideo(video)
                            },
                            onToggleFavorite = { viewModel.toggleMediaFavorite(video.id) }
                        )
                    }
                }
            }
        }
    }
}
