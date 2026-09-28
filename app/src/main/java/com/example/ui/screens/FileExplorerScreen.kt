package com.example.ui.screens

import android.content.Context
import android.net.Uri
import android.os.Environment
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EmptyStateView
import com.example.ui.components.formatFileSize
import com.example.ui.viewmodel.MainViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ExplorerFilter {
    ALL,
    DOCUMENTS,
    MUSIC,
    VIDEOS,
    FOLDERS_ONLY
}

enum class ExplorerSort(val label: String) {
    NAME_ASC("Name (A-Z)"),
    DATE_DESC("Date (Newest)"),
    SIZE_DESC("Size (Largest)")
}

data class ExplorerItem(
    val file: File,
    val name: String,
    val isDirectory: Boolean,
    val sizeBytes: Long,
    val lastModified: Long,
    val childCount: Int,
    val icon: ImageVector,
    val badgeColor: Color,
    val badgeLabel: String,
    val isMediaOrDoc: Boolean
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    // Default start directory: App's filesDir (which contains seeded docs and media)
    var currentDir by remember { mutableStateOf(context.filesDir) }
    var dirHistory by remember { mutableStateOf(listOf<File>()) }

    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(ExplorerFilter.ALL) }
    var activeSort by remember { mutableStateOf(ExplorerSort.NAME_ASC) }
    var showSortMenu by remember { mutableStateOf(false) }

    // System SAF picker launcher
    val safPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris: List<Uri> ->
        if (uris.isNotEmpty()) {
            viewModel.importFiles(uris)
        }
    }

    // Handle Android system back button to go up folder hierarchy
    BackHandler {
        if (dirHistory.isNotEmpty()) {
            val previous = dirHistory.last()
            dirHistory = dirHistory.dropLast(1)
            currentDir = previous
        } else {
            onNavigateBack()
        }
    }

    // List and filter files in current directory
    val rawFiles = remember(currentDir) {
        try {
            currentDir.listFiles()?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    val explorerItems = remember(rawFiles, searchQuery, activeFilter, activeSort) {
        val items = rawFiles.map { file ->
            val isDir = file.isDirectory
            val name = file.name
            val lower = name.lowercase()

            val isPdf = lower.endsWith(".pdf")
            val isCsv = lower.endsWith(".csv") || lower.endsWith(".tsv")
            val isMd = lower.endsWith(".md") || lower.endsWith(".markdown")
            val isText = lower.endsWith(".txt") || lower.endsWith(".log")
            val isCode = lower.endsWith(".json") || lower.endsWith(".kt") || lower.endsWith(".java") ||
                    lower.endsWith(".py") || lower.endsWith(".js") || lower.endsWith(".xml") ||
                    lower.endsWith(".html") || lower.endsWith(".css")

            val isAudio = lower.endsWith(".mp3") || lower.endsWith(".wav") ||
                    lower.endsWith(".flac") || lower.endsWith(".aac") ||
                    lower.endsWith(".m4a") || lower.endsWith(".ogg")

            val isVideo = lower.endsWith(".mp4") || lower.endsWith(".mkv") ||
                    lower.endsWith(".webm") || lower.endsWith(".3gp")

            val (icon, color, label) = when {
                isDir -> Triple(Icons.Default.Folder, Color(0xFF38BDF8), "FOLDER")
                isPdf -> Triple(Icons.Default.PictureAsPdf, Color(0xFFEF4444), "PDF")
                isCsv -> Triple(Icons.Default.TableChart, Color(0xFF10B981), "CSV")
                isMd -> Triple(Icons.Default.Description, Color(0xFF06B6D4), "MD")
                isAudio -> Triple(Icons.Default.MusicNote, Color(0xFFA855F7), "AUDIO")
                isVideo -> Triple(Icons.Default.Videocam, Color(0xFFF59E0B), "VIDEO")
                isCode -> Triple(Icons.Default.Code, Color(0xFF8B5CF6), "CODE")
                isText -> Triple(Icons.Default.Description, Color(0xFF3B82F6), "TEXT")
                else -> Triple(Icons.Default.InsertDriveFile, Color(0xFF94A3B8), "FILE")
            }

            val isMediaOrDoc = isPdf || isCsv || isMd || isText || isCode || isAudio || isVideo

            ExplorerItem(
                file = file,
                name = name,
                isDirectory = isDir,
                sizeBytes = if (isDir) 0L else file.length(),
                lastModified = file.lastModified(),
                childCount = if (isDir) file.listFiles()?.size ?: 0 else 0,
                icon = icon,
                badgeColor = color,
                badgeLabel = label,
                isMediaOrDoc = isMediaOrDoc
            )
        }

        // Apply Search Filter
        val searched = if (searchQuery.isBlank()) items else items.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }

        // Apply Category Filter
        val filtered = when (activeFilter) {
            ExplorerFilter.ALL -> searched
            ExplorerFilter.DOCUMENTS -> searched.filter {
                it.isDirectory || it.badgeLabel in listOf("PDF", "CSV", "MD", "TEXT", "CODE")
            }
            ExplorerFilter.MUSIC -> searched.filter { it.isDirectory || it.badgeLabel == "AUDIO" }
            ExplorerFilter.VIDEOS -> searched.filter { it.isDirectory || it.badgeLabel == "VIDEO" }
            ExplorerFilter.FOLDERS_ONLY -> searched.filter { it.isDirectory }
        }

        // Apply Sort (Directories always on top)
        val (dirs, files) = filtered.partition { it.isDirectory }
        val sortedDirs = dirs.sortedBy { it.name.lowercase() }
        val sortedFiles = when (activeSort) {
            ExplorerSort.NAME_ASC -> files.sortedBy { it.name.lowercase() }
            ExplorerSort.DATE_DESC -> files.sortedByDescending { it.lastModified }
            ExplorerSort.SIZE_DESC -> files.sortedByDescending { it.sizeBytes }
        }

        sortedDirs + sortedFiles
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Storage Explorer",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        )
                        Text(
                            text = currentDir.name.ifEmpty { "Storage Root" },
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            if (dirHistory.isNotEmpty()) {
                                val previous = dirHistory.last()
                                dirHistory = dirHistory.dropLast(1)
                                currentDir = previous
                            } else {
                                onNavigateBack()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    // Up directory button
                    val parentFile = currentDir.parentFile
                    if (parentFile != null && parentFile.canRead()) {
                        IconButton(
                            onClick = {
                                dirHistory = dirHistory + currentDir
                                currentDir = parentFile
                            }
                        ) {
                            Icon(Icons.Default.ArrowUpward, contentDescription = "Parent Folder")
                        }
                    }

                    // Sort button
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false }
                        ) {
                            ExplorerSort.entries.forEach { sortOption ->
                                DropdownMenuItem(
                                    text = { Text(sortOption.label) },
                                    onClick = {
                                        activeSort = sortOption
                                        showSortMenu = false
                                    }
                                )
                            }
                        }
                    }

                    // System SAF Browser Shortcut
                    IconButton(
                        onClick = {
                            safPickerLauncher.launch(
                                arrayOf(
                                    "application/pdf",
                                    "text/*",
                                    "audio/*",
                                    "video/*",
                                    "application/json",
                                    "text/csv"
                                )
                            )
                        },
                        modifier = Modifier.testTag("explorer_saf_picker_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FileOpen,
                            contentDescription = "Open System File Picker",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    safPickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "text/*",
                            "audio/*",
                            "video/*",
                            "application/json",
                            "text/csv"
                        )
                    )
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(bottom = 60.dp)
                    .testTag("explorer_fab_system_picker")
            ) {
                Icon(
                    imageVector = Icons.Default.FileOpen,
                    contentDescription = "System SAF Picker"
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Interactive Breadcrumb Path
            BreadcrumbPathBar(
                currentDir = currentDir,
                onSelectDir = { selected ->
                    if (selected != currentDir) {
                        dirHistory = dirHistory + currentDir
                        currentDir = selected
                    }
                }
            )

            // Quick Access Storage Location Chips
            QuickAccessLocationsRow(
                context = context,
                currentDir = currentDir,
                onSelectLocation = { targetDir ->
                    if (targetDir.exists()) {
                        dirHistory = dirHistory + currentDir
                        currentDir = targetDir
                    } else {
                        viewModel.showMessage("Folder does not exist yet: ${targetDir.name}")
                    }
                }
            )

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .testTag("explorer_search_input"),
                placeholder = { Text("Filter files in this directory...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val filters = listOf(
                    ExplorerFilter.ALL to "All Items",
                    ExplorerFilter.DOCUMENTS to "Documents",
                    ExplorerFilter.MUSIC to "Music",
                    ExplorerFilter.VIDEOS to "Videos",
                    ExplorerFilter.FOLDERS_ONLY to "Folders Only"
                )
                filters.forEach { (filter, label) ->
                    val isSelected = activeFilter == filter
                    FilterChip(
                        selected = isSelected,
                        onClick = { activeFilter = filter },
                        label = { Text(label, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // File items list
            if (explorerItems.isEmpty()) {
                EmptyStateView(
                    icon = Icons.Default.FolderOpen,
                    title = "Folder is Empty",
                    subtitle = if (searchQuery.isNotBlank()) "No files match \"$searchQuery\""
                    else "No items found in this directory. Try another folder or use the floating button to browse device storage with the System Picker."
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(explorerItems, key = { it.file.absolutePath }) { item ->
                        ExplorerItemCard(
                            item = item,
                            onClick = {
                                if (item.isDirectory) {
                                    dirHistory = dirHistory + currentDir
                                    currentDir = item.file
                                } else {
                                    viewModel.openLocalFile(item.file)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BreadcrumbPathBar(
    currentDir: File,
    onSelectDir: (File) -> Unit
) {
    val scrollState = rememberScrollState()

    // Build hierarchy chain
    val hierarchy = remember(currentDir) {
        val list = mutableListOf<File>()
        var curr: File? = currentDir
        while (curr != null) {
            list.add(0, curr)
            curr = curr.parentFile
        }
        list
    }

    LaunchedEffect(hierarchy) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            .horizontalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.FolderSpecial,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

        hierarchy.forEachIndexed { index, dir ->
            val isLast = index == hierarchy.lastIndex
            val displayName = if (index == 0 && dir.name.isEmpty()) "Root" else dir.name

            Text(
                text = displayName,
                fontSize = 12.sp,
                fontWeight = if (isLast) FontWeight.Bold else FontWeight.Medium,
                color = if (isLast) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSelectDir(dir) }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            )

            if (!isLast) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
private fun QuickAccessLocationsRow(
    context: Context,
    currentDir: File,
    onSelectLocation: (File) -> Unit
) {
    val locations = remember(context) {
        listOf(
            Triple("App Sandbox", context.filesDir, Icons.Default.FolderSpecial),
            Triple("App Docs", File(context.filesDir, "documents"), Icons.Default.Description),
            Triple("App Media", File(context.filesDir, "media"), Icons.Default.MusicNote),
            Triple(
                "Downloads",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                Icons.Default.Download
            ),
            Triple(
                "Documents",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
                Icons.Default.Description
            ),
            Triple(
                "Music",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC),
                Icons.Default.MusicNote
            ),
            Triple(
                "Movies",
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                Icons.Default.Movie
            ),
            Triple(
                "Storage Root",
                Environment.getExternalStorageDirectory(),
                Icons.Default.PhoneAndroid
            )
        )
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        locations.forEach { (name, file, icon) ->
            val isCurrent = currentDir.absolutePath == file.absolutePath
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = if (isCurrent) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectLocation(file) }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = name,
                        fontSize = 12.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = if (isCurrent) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ExplorerItemCard(
    item: ExplorerItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("explorer_item_${item.name}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Badge
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(item.badgeColor.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = null,
                    tint = item.badgeColor,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // File Name & Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = item.badgeColor.copy(alpha = 0.18f),
                        modifier = Modifier.padding(end = 6.dp)
                    ) {
                        Text(
                            text = item.badgeLabel,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = item.badgeColor,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                        .format(Date(item.lastModified))

                    val detail = if (item.isDirectory) {
                        "${item.childCount} items • $dateStr"
                    } else {
                        "${formatFileSize(item.sizeBytes)} • $dateStr"
                    }

                    Text(
                        text = detail,
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Trailing action / arrow
            if (item.isDirectory) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Open Folder",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            } else if (item.isMediaOrDoc) {
                IconButton(
                    onClick = onClick,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = if (item.badgeLabel == "AUDIO" || item.badgeLabel == "VIDEO") Icons.Default.PlayArrow else Icons.Default.Description,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
