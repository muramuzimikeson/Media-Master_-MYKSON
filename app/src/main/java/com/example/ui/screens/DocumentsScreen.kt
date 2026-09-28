package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.DocumentItemCard
import com.example.ui.components.EmptyStateView
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentsScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val documents by viewModel.filteredDocuments.collectAsStateWithLifecycle()
    val searchQuery by viewModel.documentSearchQuery.collectAsStateWithLifecycle()
    val activeFilter by viewModel.documentTypeFilter.collectAsStateWithLifecycle()

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importFiles(uris)
        }
    }

    val filterOptions = listOf(
        "ALL" to "All Types",
        "PDF" to "PDFs",
        "MARKDOWN" to "Markdown",
        "CSV" to "Spreadsheets (CSV)",
        "JSON" to "JSON",
        "TEXT" to "Text Notes",
        "CODE" to "Code",
        "FAVORITES" to "Favorites"
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "text/*",
                            "application/json",
                            "text/csv",
                            "application/octet-stream"
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("import_document_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.FileUpload,
                    contentDescription = "Import Document"
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
                onValueChange = { viewModel.documentSearchQuery.value = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .testTag("document_search_input"),
                placeholder = { Text("Search documents, guides, data...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.documentSearchQuery.value = "" }) {
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

            // Horizontal Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                filterOptions.forEach { (key, label) ->
                    val isSelected = activeFilter == key
                    FilterChip(
                        selected = isSelected,
                        onClick = { viewModel.documentTypeFilter.value = key },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.testTag("filter_chip_$key")
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Documents List
            if (documents.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.Description,
                    title = "No Documents Found",
                    subtitle = if (searchQuery.isNotBlank()) "No files match \"$searchQuery\""
                    else "Tap the + button below to import offline PDFs, Markdown, CSV, or Text documents."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(documents, key = { it.id }) { doc ->
                        DocumentItemCard(
                            document = doc,
                            onClick = { viewModel.openDocument(doc) },
                            onToggleFavorite = { viewModel.toggleDocumentFavorite(doc.id) }
                        )
                    }
                }
            }
        }
    }
}
