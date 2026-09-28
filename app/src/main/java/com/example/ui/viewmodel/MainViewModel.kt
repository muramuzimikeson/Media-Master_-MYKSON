package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.BookmarkEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentType
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import com.example.data.repository.DocumentRepository
import com.example.data.repository.MediaRepository
import com.example.document.CsvTableData
import com.example.document.DocumentParser
import com.example.document.DocumentTtsManager
import com.example.document.PdfRendererManager
import com.example.document.SearchMatch
import com.example.player.AudioPlayerManager
import com.example.player.VideoPlayerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class AppScreen {
    HOME,
    DOCUMENTS,
    MUSIC,
    VIDEOS,
    FILE_EXPLORER,
    DOCUMENT_READER,
    FULLSCREEN_MUSIC,
    FULLSCREEN_VIDEO
}

enum class ReaderTheme(val displayName: String) {
    NIGHT("Night Obsidian"),
    SEPIA("Warm Sepia"),
    LIGHT("Clean Day")
}

data class DocumentReaderUiState(
    val document: DocumentEntity? = null,
    val isLoading: Boolean = false,
    val rawText: String = "",
    val csvData: CsvTableData? = null,
    val currentPdfPage: Int = 0,
    val totalPdfPages: Int = 0,
    val currentPdfBitmap: Bitmap? = null,
    val theme: ReaderTheme = ReaderTheme.NIGHT,
    val fontSizeSp: Int = 16,
    val searchQuery: String = "",
    val searchMatches: List<SearchMatch> = emptyList(),
    val currentSearchIndex: Int = 0,
    val bookmarks: List<BookmarkEntity> = emptyList(),
    val isTtsSpeaking: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val TAG = "MainViewModel"
    private val database = AppDatabase.getDatabase(application)
    val documentRepo = DocumentRepository(database.documentDao(), database.bookmarkDao())
    val mediaRepo = MediaRepository(database.mediaDao())

    val audioPlayer = AudioPlayerManager(application)
    val videoPlayer = VideoPlayerManager(application)
    val pdfRenderer = PdfRendererManager(application)
    val ttsManager = DocumentTtsManager(application)

    // Navigation
    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // Filters and Search
    val documentSearchQuery = MutableStateFlow("")
    val documentTypeFilter = MutableStateFlow("ALL")

    val musicSearchQuery = MutableStateFlow("")
    val videoSearchQuery = MutableStateFlow("")

    // Data streams
    val allDocuments = documentRepo.allDocuments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val recentDocuments = documentRepo.recentDocuments.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allAudio = mediaRepo.allAudio.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val allVideos = mediaRepo.allVideos.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val recentAudio = mediaRepo.recentAudio.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )
    val recentVideos = mediaRepo.recentVideos.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    // Filtered Documents
    val filteredDocuments: StateFlow<List<DocumentEntity>> = combine(
        allDocuments,
        documentTypeFilter,
        documentSearchQuery
    ) { docs, filter, query ->
        docs.filter { doc ->
            val matchesFilter = when (filter) {
                "ALL" -> true
                "FAVORITES" -> doc.isFavorite
                else -> doc.fileType.equals(filter, ignoreCase = true)
            }
            val matchesQuery = query.isBlank() || doc.title.contains(query, ignoreCase = true) ||
                    doc.tags.contains(query, ignoreCase = true)
            matchesFilter && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Audio
    val filteredAudio: StateFlow<List<MediaEntity>> = combine(
        allAudio,
        musicSearchQuery
    ) { songs, query ->
        if (query.isBlank()) songs else songs.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Video
    val filteredVideos: StateFlow<List<MediaEntity>> = combine(
        allVideos,
        videoSearchQuery
    ) { videos, query ->
        if (query.isBlank()) videos else videos.filter {
            it.title.contains(query, ignoreCase = true) || it.artist.contains(query, ignoreCase = true)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Document Reader State
    private val _readerState = MutableStateFlow(DocumentReaderUiState())
    val readerState: StateFlow<DocumentReaderUiState> = _readerState.asStateFlow()

    // Status / Message snackbar
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    fun clearMessage() {
        _userMessage.value = null
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
    }

    fun openDocument(doc: DocumentEntity) {
        viewModelScope.launch {
            _readerState.value = DocumentReaderUiState(
                document = doc,
                isLoading = true,
                currentPdfPage = doc.currentPage
            )
            _currentScreen.value = AppScreen.DOCUMENT_READER

            if (doc.fileType == DocumentType.PDF.name) {
                val opened = pdfRenderer.openPdf(doc.uriString, doc.filePath)
                if (opened) {
                    val page = doc.currentPage.coerceIn(0, (pdfRenderer.pageCount - 1).coerceAtLeast(0))
                    val bitmap = pdfRenderer.renderPage(page)
                    _readerState.value = _readerState.value.copy(
                        isLoading = false,
                        totalPdfPages = pdfRenderer.pageCount,
                        currentPdfPage = page,
                        currentPdfBitmap = bitmap
                    )
                } else {
                    _readerState.value = _readerState.value.copy(
                        isLoading = false,
                        rawText = "Failed to open PDF document. The file may be password protected or corrupted."
                    )
                }
            } else {
                val content = DocumentParser.readDocumentContent(
                    getApplication(),
                    doc.uriString,
                    doc.filePath
                )
                val csv = if (doc.fileType == DocumentType.CSV.name) {
                    DocumentParser.parseCsv(content)
                } else null

                val formatted = if (doc.fileType == DocumentType.JSON.name) {
                    DocumentParser.formatJson(content)
                } else content

                _readerState.value = _readerState.value.copy(
                    isLoading = false,
                    rawText = formatted,
                    csvData = csv
                )
            }

            // Load bookmarks
            loadBookmarksForDocument(doc.id)
            // Update last read
            documentRepo.updateProgress(doc.id, doc.currentPage, doc.totalPages, doc.readingProgressPercent)
        }
    }

    fun changePdfPage(pageIndex: Int) {
        viewModelScope.launch {
            val total = _readerState.value.totalPdfPages
            if (pageIndex in 0 until total) {
                val bitmap = pdfRenderer.renderPage(pageIndex)
                val progress = (((pageIndex + 1).toFloat() / total) * 100).toInt()
                _readerState.value = _readerState.value.copy(
                    currentPdfPage = pageIndex,
                    currentPdfBitmap = bitmap
                )
                _readerState.value.document?.let { doc ->
                    documentRepo.updateProgress(doc.id, pageIndex, total, progress)
                }
            }
        }
    }

    fun setReaderTheme(theme: ReaderTheme) {
        _readerState.value = _readerState.value.copy(theme = theme)
    }

    fun setReaderFontSize(sizeSp: Int) {
        _readerState.value = _readerState.value.copy(fontSizeSp = sizeSp.coerceIn(12, 32))
    }

    fun searchInDocument(query: String) {
        val content = _readerState.value.rawText
        val matches = DocumentParser.searchMatches(content, query)
        _readerState.value = _readerState.value.copy(
            searchQuery = query,
            searchMatches = matches,
            currentSearchIndex = 0
        )
    }

    fun toggleTts() {
        val currentText = if (_readerState.value.document?.fileType == DocumentType.PDF.name) {
            "Reading page ${_readerState.value.currentPdfPage + 1} of ${_readerState.value.totalPdfPages} in ${_readerState.value.document?.title}"
        } else {
            _readerState.value.rawText
        }
        ttsManager.toggleSpeak(currentText)
        _readerState.value = _readerState.value.copy(isTtsSpeaking = !_readerState.value.isTtsSpeaking)
    }

    fun toggleDocumentFavorite(id: Long) {
        viewModelScope.launch {
            documentRepo.toggleFavorite(id)
        }
    }

    fun toggleMediaFavorite(id: Long) {
        viewModelScope.launch {
            mediaRepo.toggleFavorite(id)
        }
    }

    private fun loadBookmarksForDocument(documentId: Long) {
        viewModelScope.launch {
            documentRepo.getBookmarks(documentId).collect { bookmarks ->
                _readerState.value = _readerState.value.copy(bookmarks = bookmarks)
            }
        }
    }

    fun addBookmark(note: String = "") {
        viewModelScope.launch {
            val doc = _readerState.value.document ?: return@launch
            val page = _readerState.value.currentPdfPage
            val bookmark = BookmarkEntity(
                documentId = doc.id,
                pageNumber = page,
                title = if (doc.fileType == DocumentType.PDF.name) "Page ${page + 1}" else "Bookmark",
                snippet = if (note.isNotBlank()) note else "Saved position in ${doc.title}"
            )
            documentRepo.addBookmark(bookmark)
            showMessage("Bookmark saved for Page ${page + 1}")
        }
    }

    fun deleteBookmark(id: Long) {
        viewModelScope.launch {
            documentRepo.deleteBookmark(id)
            showMessage("Bookmark removed")
        }
    }

    fun playAudio(track: MediaEntity, playlist: List<MediaEntity> = emptyList()) {
        audioPlayer.playTrack(track, playlist)
        viewModelScope.launch {
            mediaRepo.recordPlayback(track.id, 0L)
        }
    }

    fun openVideo(video: MediaEntity) {
        _currentScreen.value = AppScreen.FULLSCREEN_VIDEO
        viewModelScope.launch {
            mediaRepo.recordPlayback(video.id, 0L)
        }
    }

    /**
     * Imports files selected via SAF OpenDocument / OpenMultipleDocuments
     */
    fun importFiles(uris: List<Uri>) {
        viewModelScope.launch {
            val context: Context = getApplication()
            var importedDocsCount = 0
            var importedMediaCount = 0

            withContext(Dispatchers.IO) {
                uris.forEach { uri ->
                    try {
                        val fileName = getFileNameFromUri(context, uri) ?: "Imported_File_${System.currentTimeMillis()}"
                        val fileSize = getFileSizeFromUri(context, uri)
                        val mimeType = context.contentResolver.getType(uri)

                        val lower = fileName.lowercase()
                        val isAudio = lower.endsWith(".mp3") || lower.endsWith(".wav") ||
                                lower.endsWith(".flac") || lower.endsWith(".aac") ||
                                lower.endsWith(".m4a") || lower.endsWith(".ogg") ||
                                mimeType?.startsWith("audio/") == true

                        val isVideo = lower.endsWith(".mp4") || lower.endsWith(".mkv") ||
                                lower.endsWith(".webm") || lower.endsWith(".3gp") ||
                                mimeType?.startsWith("video/") == true

                        if (isAudio) {
                            val media = MediaEntity(
                                title = fileName.substringBeforeLast("."),
                                artist = "Imported Audio",
                                album = "Offline Library",
                                durationMs = 0L,
                                uriString = uri.toString(),
                                filePath = null,
                                mediaType = MediaType.AUDIO.name,
                                fileSizeBytes = fileSize
                            )
                            mediaRepo.insertMedia(media)
                            importedMediaCount++
                        } else if (isVideo) {
                            val media = MediaEntity(
                                title = fileName.substringBeforeLast("."),
                                artist = "Imported Video",
                                album = "Offline Library",
                                durationMs = 0L,
                                uriString = uri.toString(),
                                filePath = null,
                                mediaType = MediaType.VIDEO.name,
                                fileSizeBytes = fileSize
                            )
                            mediaRepo.insertMedia(media)
                            importedMediaCount++
                        } else {
                            val docType = DocumentType.fromExtensionOrMime(fileName, mimeType)
                            val doc = DocumentEntity(
                                title = fileName,
                                uriString = uri.toString(),
                                filePath = null,
                                fileType = docType.name,
                                fileSizeBytes = fileSize,
                                totalPages = if (docType == DocumentType.PDF) 1 else 1,
                                tags = docType.extensionBadge
                            )
                            documentRepo.insertDocument(doc)
                            importedDocsCount++
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error importing URI $uri: ${e.message}", e)
                    }
                }
            }

            val resultMsg = buildString {
                if (importedDocsCount > 0) append("$importedDocsCount document(s) ")
                if (importedMediaCount > 0) {
                    if (isNotEmpty()) append("and ")
                    append("$importedMediaCount media file(s) ")
                }
                append("imported offline!")
            }
            showMessage(if (importedDocsCount + importedMediaCount > 0) resultMsg else "Failed to import selected files.")
        }
    }

    private fun getFileNameFromUri(context: Context, uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val nameIdx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIdx >= 0) {
                        name = it.getString(nameIdx)
                    }
                }
            }
        }
        if (name == null) {
            name = uri.path
            val cut = name?.lastIndexOf('/')
            if (cut != null && cut != -1) {
                name = name?.substring(cut + 1)
            }
        }
        return name
    }

    private fun getFileSizeFromUri(context: Context, uri: Uri): Long {
        var size: Long = 0
        try {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0) {
                        size = cursor.getLong(sizeIndex)
                    }
                }
            }
        } catch (ignored: Exception) {}
        return size
    }

    fun openLocalFile(file: File) {
        if (!file.exists() || file.isDirectory) return
        val fileName = file.name
        val lower = fileName.lowercase()
        val isAudio = lower.endsWith(".mp3") || lower.endsWith(".wav") ||
                lower.endsWith(".flac") || lower.endsWith(".aac") ||
                lower.endsWith(".m4a") || lower.endsWith(".ogg")
        val isVideo = lower.endsWith(".mp4") || lower.endsWith(".mkv") ||
                lower.endsWith(".webm") || lower.endsWith(".3gp")

        viewModelScope.launch {
            if (isAudio) {
                val existing = database.mediaDao().getMediaByUri(Uri.fromFile(file).toString())
                val targetMedia = existing ?: MediaEntity(
                    title = fileName.substringBeforeLast("."),
                    artist = "Local Storage",
                    album = file.parentFile?.name ?: "Offline",
                    durationMs = 0L,
                    uriString = Uri.fromFile(file).toString(),
                    filePath = file.absolutePath,
                    mediaType = MediaType.AUDIO.name,
                    fileSizeBytes = file.length()
                ).also {
                    val id = mediaRepo.insertMedia(it)
                }
                playAudio(targetMedia)
                showMessage("Playing: ${targetMedia.title}")
            } else if (isVideo) {
                val existing = database.mediaDao().getMediaByUri(Uri.fromFile(file).toString())
                val targetMedia = existing ?: MediaEntity(
                    title = fileName.substringBeforeLast("."),
                    artist = "Local Video",
                    album = file.parentFile?.name ?: "Offline",
                    durationMs = 0L,
                    uriString = Uri.fromFile(file).toString(),
                    filePath = file.absolutePath,
                    mediaType = MediaType.VIDEO.name,
                    fileSizeBytes = file.length()
                ).also {
                    mediaRepo.insertMedia(it)
                }
                videoPlayer.prepareAndPlay(targetMedia)
                openVideo(targetMedia)
            } else {
                val existing = database.documentDao().getDocumentByUri(Uri.fromFile(file).toString())
                val docType = DocumentType.fromExtensionOrMime(fileName)
                val targetDoc = existing ?: DocumentEntity(
                    title = fileName,
                    uriString = Uri.fromFile(file).toString(),
                    filePath = file.absolutePath,
                    fileType = docType.name,
                    fileSizeBytes = file.length(),
                    totalPages = 1,
                    tags = docType.extensionBadge
                ).also {
                    documentRepo.insertDocument(it)
                }
                openDocument(targetDoc)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.release()
        videoPlayer.release()
        pdfRenderer.close()
        ttsManager.release()
    }
}
