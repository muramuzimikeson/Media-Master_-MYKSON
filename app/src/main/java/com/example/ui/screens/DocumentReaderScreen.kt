package com.example.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FormatSize
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.model.DocumentType
import com.example.ui.components.CsvTableView
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkOnSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightOnSurface
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NightBackground
import com.example.ui.theme.NightSurface
import com.example.ui.theme.NightText
import com.example.ui.theme.SepiaBackground
import com.example.ui.theme.SepiaSurface
import com.example.ui.theme.SepiaText
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MainViewModel
import com.example.ui.viewmodel.ReaderTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentReaderScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val readerState by viewModel.readerState.collectAsStateWithLifecycle()
    val doc = readerState.document

    BackHandler {
        onBack()
    }

    // Colors according to ReaderTheme
    val (bgColor, surfaceColor, textColor) = when (readerState.theme) {
        ReaderTheme.NIGHT -> Triple(NightBackground, NightSurface, NightText)
        ReaderTheme.SEPIA -> Triple(SepiaBackground, SepiaSurface, SepiaText)
        ReaderTheme.LIGHT -> Triple(LightBackground, LightSurface, LightOnSurface)
    }

    var showThemeMenu by remember { mutableStateOf(false) }
    var showFontSizeMenu by remember { mutableStateOf(false) }
    var showSearchBar by remember { mutableStateOf(false) }
    var searchInput by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = doc?.title ?: "Document Reader",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (doc?.fileType == DocumentType.PDF.name && readerState.totalPdfPages > 0) {
                            Text(
                                text = "Page ${readerState.currentPdfPage + 1} of ${readerState.totalPdfPages}",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Text(
                                text = "${doc?.fileType ?: "DOCUMENT"} • Offline Mode",
                                style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // TTS Read-Aloud Button
                    IconButton(
                        onClick = { viewModel.toggleTts() },
                        modifier = Modifier.testTag("reader_tts_button")
                    ) {
                        Icon(
                            imageVector = if (readerState.isTtsSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                            contentDescription = "Read Aloud",
                            tint = if (readerState.isTtsSpeaking) Color(0xFFEF4444) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Search Button
                    IconButton(onClick = { showSearchBar = !showSearchBar }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search"
                        )
                    }

                    // Bookmark Button
                    IconButton(onClick = { viewModel.addBookmark() }) {
                        Icon(
                            imageVector = Icons.Default.BookmarkAdd,
                            contentDescription = "Add Bookmark"
                        )
                    }

                    // Font Size Menu
                    Box {
                        IconButton(onClick = { showFontSizeMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Font Size"
                            )
                        }
                        DropdownMenu(
                            expanded = showFontSizeMenu,
                            onDismissRequest = { showFontSizeMenu = false }
                        ) {
                            listOf(14 to "Small", 16 to "Medium", 18 to "Large", 22 to "Extra Large").forEach { (size, label) ->
                                DropdownMenuItem(
                                    text = { Text("$label (${size}sp)") },
                                    onClick = {
                                        viewModel.setReaderFontSize(size)
                                        showFontSizeMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // Theme selector menu
                    Box {
                        IconButton(onClick = { showThemeMenu = true }) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Themes"
                            )
                        }
                        DropdownMenu(
                            expanded = showThemeMenu,
                            onDismissRequest = { showThemeMenu = false }
                        ) {
                            ReaderTheme.entries.forEach { theme ->
                                DropdownMenuItem(
                                    text = { Text(theme.displayName) },
                                    onClick = {
                                        viewModel.setReaderTheme(theme)
                                        showThemeMenu = false
                                    }
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = surfaceColor,
                    titleContentColor = textColor,
                    navigationIconContentColor = textColor,
                    actionIconContentColor = textColor
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(bgColor)
        ) {
            // Search Input Drawer if toggled
            if (showSearchBar) {
                Surface(
                    color = surfaceColor,
                    shadowElevation = 4.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchInput,
                            onValueChange = {
                                searchInput = it
                                viewModel.searchInDocument(it)
                            },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Find in document...") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = textColor,
                                unfocusedTextColor = textColor
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        if (readerState.searchMatches.isNotEmpty()) {
                            Text(
                                text = "${readerState.searchMatches.size} found",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        IconButton(onClick = {
                            showSearchBar = false
                            searchInput = ""
                            viewModel.searchInDocument("")
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Close", tint = textColor)
                        }
                    }
                }
            }

            // Speaking indicator banner if active
            if (readerState.isTtsSpeaking) {
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.VolumeUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Reading document aloud...",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Stop",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(4.dp)
                        )
                    }
                }
            }

            // Main Reader Content Container
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                if (readerState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else if (doc?.fileType == DocumentType.PDF.name) {
                    // PDF Page Viewer
                    PdfPageView(
                        bitmap = readerState.currentPdfBitmap,
                        currentPage = readerState.currentPdfPage,
                        totalPages = readerState.totalPdfPages,
                        onPrevPage = { viewModel.changePdfPage(readerState.currentPdfPage - 1) },
                        onNextPage = { viewModel.changePdfPage(readerState.currentPdfPage + 1) },
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (doc?.fileType == DocumentType.CSV.name && readerState.csvData != null) {
                    // CSV Spreadsheet Table Viewer
                    CsvTableView(
                        data = readerState.csvData!!,
                        textColor = textColor,
                        surfaceColor = surfaceColor,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Markdown / Text / Code / JSON Viewer
                    SelectionContainer {
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(16.dp)
                        ) {
                            val isCode = doc?.fileType == DocumentType.CODE.name ||
                                    doc?.fileType == DocumentType.JSON.name

                            Text(
                                text = readerState.rawText,
                                color = textColor,
                                fontSize = readerState.fontSizeSp.sp,
                                lineHeight = (readerState.fontSizeSp * 1.5).sp,
                                fontFamily = if (isCode) FontFamily.Monospace else FontFamily.Default
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PdfPageView(
    bitmap: Bitmap?,
    currentPage: Int,
    totalPages: Int,
    onPrevPage: () -> Unit,
    onNextPage: () -> Unit,
    modifier: Modifier = Modifier
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformState = rememberTransformableState { zoomChange, panChange, _ ->
        scale = (scale * zoomChange).coerceIn(1f, 4f)
        offset = if (scale > 1f) offset + panChange else Offset.Zero
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .transformable(state = transformState)
    ) {
        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "PDF Page ${currentPage + 1}",
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y
                    )
            )
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Rendering page...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        // Floating Bottom Page Navigation Pill
        if (totalPages > 1) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.92f),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onPrevPage,
                        enabled = currentPage > 0,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Previous Page")
                    }

                    Text(
                        text = "${currentPage + 1} / $totalPages",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        modifier = Modifier.padding(horizontal = 12.dp)
                    )

                    IconButton(
                        onClick = onNextPage,
                        enabled = currentPage < totalPages - 1,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next Page")
                    }
                }
            }
        }
    }
}
