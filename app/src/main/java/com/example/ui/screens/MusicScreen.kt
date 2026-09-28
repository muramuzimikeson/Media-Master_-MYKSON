package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun MusicScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val audioList by viewModel.filteredAudio.collectAsStateWithLifecycle()
    val searchQuery by viewModel.musicSearchQuery.collectAsStateWithLifecycle()
    val audioState by viewModel.audioPlayer.state.collectAsStateWithLifecycle()

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
                        arrayOf("audio/*", "application/ogg")
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("import_music_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Import Music"
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
                onValueChange = { viewModel.musicSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("music_search_input"),
                placeholder = { Text("Search songs, artists, albums...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.musicSearchQuery.value = "" }) {
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

            // Play All & Shuffle Buttons
            if (audioList.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            viewModel.playAudio(audioList.first(), audioList)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Play All (${audioList.size})")
                    }

                    OutlinedButton(
                        onClick = {
                            val shuffled = audioList.shuffled()
                            viewModel.playAudio(shuffled.first(), shuffled)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Shuffle All")
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Songs List
            if (audioList.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.MusicNote,
                    title = "No Music Found",
                    subtitle = if (searchQuery.isNotBlank()) "No tracks match \"$searchQuery\""
                    else "Tap the + button to import offline MP3, WAV, FLAC, or AAC songs."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(audioList, key = { it.id }) { track ->
                        MediaItemCard(
                            media = track,
                            isPlaying = audioState.isPlaying && audioState.currentTrack?.id == track.id,
                            onClick = { viewModel.playAudio(track, audioList) },
                            onToggleFavorite = { viewModel.toggleMediaFavorite(track.id) }
                        )
                    }
                }
            }
        }
    }
}
