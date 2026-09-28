package com.example.data.sample

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.DocumentEntity
import com.example.data.model.DocumentType
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object SampleDataInitializer {
    private const val TAG = "SampleDataInitializer"
    private const val PREFS_NAME = "mediamaster_prefs"
    private const val KEY_SEEDED = "is_database_seeded_v1"

    suspend fun seedSampleDataIfNeeded(context: Context, database: AppDatabase) = withContext(Dispatchers.IO) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) {
            return@withContext
        }

        try {
            val docsDir = File(context.filesDir, "documents").apply { mkdirs() }
            val mediaDir = File(context.filesDir, "media").apply { mkdirs() }

            // 1. Create Sample PDFs
            val manualPdf = File(docsDir, "MediaMaster_User_Guide.pdf")
            createManualPdf(manualPdf)

            val strategyPdf = File(docsDir, "The_Art_of_Strategy_Volume_I.pdf")
            createStrategyPdf(strategyPdf)

            // 2. Create Sample Markdown
            val markdownFile = File(docsDir, "Android_Clean_Architecture_Handbook.md")
            markdownFile.writeText(
                """
                # MediaMaster Modern Architecture Handbook
                
                Welcome to **MediaMaster_#MYKSON#**, your high-performance universal offline media and document companion.
                
                ## Key Capabilities
                - **Universal Document Reader**: Native support for PDF rendering, Markdown formatting, CSV interactive tables, JSON trees, and code syntax.
                - **Offline Audio Studio**: High-fidelity playback with dynamic waveforms, custom equalizer presets, sleep timers, and queue management.
                - **Offline Cinema Engine**: Video playback with smooth seeking, aspect ratio switching (16:9, 4:3, Fill), and speed regulation (0.5x to 2.0x).
                - **Text-to-Speech (TTS)**: Offline listening capability for all documents.
                
                ---
                
                ## Supported Formats Matrix
                
                | Category | Supported File Formats |
                | :--- | :--- |
                | **Documents** | `.pdf`, `.md`, `.txt`, `.csv`, `.tsv`, `.json`, `.xml`, `.log`, `.kt`, `.java`, `.py` |
                | **Music & Audio** | `.mp3`, `.wav`, `.flac`, `.aac`, `.m4a`, `.ogg` |
                | **Video & Cinema** | `.mp4`, `.mkv`, `.webm`, `.3gp` |
                
                ## Kotlin Coroutines Code Example
                ```kotlin
                // Asynchronous reactive state flow pattern
                val documentState: StateFlow<UiState> = repository.allDocuments
                    .map { UiState.Success(it) }
                    .stateIn(
                        scope = viewModelScope,
                        started = SharingStarted.WhileSubscribed(5000),
                        initialValue = UiState.Loading
                    )
                ```
                
                > "Simplicity is prerequisite for reliability." — Edsger W. Dijkstra
                
                ### Reading Checklist
                - [x] Configure offline storage
                - [x] Test PDF page renderer
                - [x] Test Audio equalizer & waveform
                - [x] Test Video playback controls
                """.trimIndent()
            )

            // 3. Create Sample CSV
            val csvFile = File(docsDir, "Global_Economic_Analytics_2026.csv")
            csvFile.writeText(
                """
                Country,Region,GDP_Growth_Pct,Inflation_Rate,Tech_Index,Status,Fiscal_Quarter
                United States,North America,2.6,2.8,94.2,Expanding,Q3-2026
                Germany,Europe,1.4,2.2,88.5,Stable,Q3-2026
                Japan,Asia Pacific,1.8,2.0,91.0,Moderate,Q3-2026
                Singapore,Asia Pacific,3.5,2.1,96.8,High Growth,Q3-2026
                United Kingdom,Europe,1.5,2.5,87.4,Stable,Q3-2026
                Canada,North America,2.1,2.7,85.6,Expanding,Q3-2026
                South Korea,Asia Pacific,2.9,2.3,93.4,Expanding,Q3-2026
                Switzerland,Europe,1.9,1.4,95.1,Stable,Q3-2026
                Australia,Oceania,2.4,2.9,86.2,Expanding,Q3-2026
                Norway,Europe,2.0,2.6,90.4,Stable,Q3-2026
                Sweden,Europe,1.7,2.1,91.8,Expanding,Q3-2026
                Brazil,South America,3.1,4.2,74.5,Developing,Q3-2026
                India,South Asia,6.8,4.5,82.3,Accelerated,Q3-2026
                """.trimIndent()
            )

            // 4. Create Sample JSON
            val jsonFile = File(docsDir, "Application_Configuration_Spec.json")
            jsonFile.writeText(
                """
                {
                  "appName": "MediaMaster_#MYKSON#",
                  "version": "1.0.0",
                  "offlineEngine": {
                    "audio": {
                      "sampleRate": 44100,
                      "channels": 2,
                      "equalizerBands": 5,
                      "bassBoostStrength": 650,
                      "defaultPlaybackSpeed": 1.0
                    },
                    "video": {
                      "hardwareAcceleration": true,
                      "defaultAspectRatio": "FIT_SCREEN",
                      "skipIntervalSeconds": 10
                    },
                    "documentReader": {
                      "defaultFontSizeSp": 16,
                      "nightModeInversion": true,
                      "textToSpeechSpeed": 1.0,
                      "tableColumnDivider": true
                    }
                  },
                  "security": {
                    "storageIsolation": "SANDBOXED_INTERNAL",
                    "telemetryEnabled": false,
                    "offlineStrict": true
                  }
                }
                """.trimIndent()
            )

            // 5. Create Sample Text
            val textFile = File(docsDir, "Creative_Notes_and_Ideas.txt")
            textFile.writeText(
                """
                MediaMaster Creative Notes
                ==========================
                Date: 2026-09-28
                Subject: Digital Freedom & Local Offline Media
                
                The philosophy of modern software design emphasizes total offline sovereignty:
                1. No dependency on cloud servers for opening documents.
                2. Instant zero-latency audio playback without buffering spinners.
                3. Crystal-clear video playback with intuitive touch-driven velocity controls.
                4. Accessible document digestion through multi-mode text-to-speech.
                
                Remember: The best productivity suite is the one that works seamlessly in airplane mode.
                """.trimIndent()
            )

            // Insert Documents into Database
            val docEntities = listOf(
                DocumentEntity(
                    title = "MediaMaster User Guide & Technical Manual",
                    uriString = Uri.fromFile(manualPdf).toString(),
                    filePath = manualPdf.absolutePath,
                    fileType = DocumentType.PDF.name,
                    fileSizeBytes = manualPdf.length(),
                    totalPages = 3,
                    currentPage = 0,
                    readingProgressPercent = 0,
                    isFavorite = true,
                    tags = "Guide,Manual,PDF"
                ),
                DocumentEntity(
                    title = "The Art of Strategy - Classical Treatise",
                    uriString = Uri.fromFile(strategyPdf).toString(),
                    filePath = strategyPdf.absolutePath,
                    fileType = DocumentType.PDF.name,
                    fileSizeBytes = strategyPdf.length(),
                    totalPages = 3,
                    currentPage = 0,
                    readingProgressPercent = 0,
                    isFavorite = false,
                    tags = "Philosophy,Books,Classic"
                ),
                DocumentEntity(
                    title = "Android Architecture & Kotlin Handbook",
                    uriString = Uri.fromFile(markdownFile).toString(),
                    filePath = markdownFile.absolutePath,
                    fileType = DocumentType.MARKDOWN.name,
                    fileSizeBytes = markdownFile.length(),
                    totalPages = 1,
                    currentPage = 0,
                    readingProgressPercent = 25,
                    isFavorite = true,
                    tags = "Code,Markdown,Kotlin"
                ),
                DocumentEntity(
                    title = "Global Economic Analytics 2026",
                    uriString = Uri.fromFile(csvFile).toString(),
                    filePath = csvFile.absolutePath,
                    fileType = DocumentType.CSV.name,
                    fileSizeBytes = csvFile.length(),
                    totalPages = 1,
                    currentPage = 0,
                    readingProgressPercent = 10,
                    isFavorite = false,
                    tags = "Data,CSV,Finance"
                ),
                DocumentEntity(
                    title = "System Configuration Specification",
                    uriString = Uri.fromFile(jsonFile).toString(),
                    filePath = jsonFile.absolutePath,
                    fileType = DocumentType.JSON.name,
                    fileSizeBytes = jsonFile.length(),
                    totalPages = 1,
                    currentPage = 0,
                    readingProgressPercent = 0,
                    isFavorite = false,
                    tags = "Config,JSON"
                ),
                DocumentEntity(
                    title = "Creative Notes & Local Media Philosophy",
                    uriString = Uri.fromFile(textFile).toString(),
                    filePath = textFile.absolutePath,
                    fileType = DocumentType.TEXT.name,
                    fileSizeBytes = textFile.length(),
                    totalPages = 1,
                    currentPage = 0,
                    readingProgressPercent = 50,
                    isFavorite = false,
                    tags = "Notes,Text"
                )
            )
            database.documentDao().insertDocuments(docEntities)

            // 6. Copy Sample Offline Audio from assets
            val audio1 = File(mediaDir, "Midnight_Lofi_Chill.wav")
            copyAssetFile(context, "audio_lofi.wav", audio1)

            val audio2 = File(mediaDir, "Cyberpunk_Pulse_Drive.wav")
            copyAssetFile(context, "audio_cyber.wav", audio2)

            val audio3 = File(mediaDir, "Acoustic_Dawn_Serenade.wav")
            copyAssetFile(context, "audio_acoustic.wav", audio3)

            val mediaEntities = mutableListOf(
                MediaEntity(
                    title = "Midnight Lo-Fi Echoes",
                    artist = "Master Acoustics",
                    album = "Offline Sanctuary",
                    durationMs = 5_000,
                    uriString = Uri.fromFile(audio1).toString(),
                    filePath = audio1.absolutePath,
                    mediaType = MediaType.AUDIO.name,
                    fileSizeBytes = audio1.length(),
                    isFavorite = true,
                    genre = "Lo-Fi Beats"
                ),
                MediaEntity(
                    title = "Cyberpunk Pulse Drive",
                    artist = "Neon Matrix",
                    album = "Synthetic City",
                    durationMs = 5_000,
                    uriString = Uri.fromFile(audio2).toString(),
                    filePath = audio2.absolutePath,
                    mediaType = MediaType.AUDIO.name,
                    fileSizeBytes = audio2.length(),
                    isFavorite = false,
                    genre = "Electronic Synth"
                ),
                MediaEntity(
                    title = "Acoustic Dawn Serenade",
                    artist = "Horizon Harmonic",
                    album = "Mellow Moments",
                    durationMs = 5_000,
                    uriString = Uri.fromFile(audio3).toString(),
                    filePath = audio3.absolutePath,
                    mediaType = MediaType.AUDIO.name,
                    fileSizeBytes = audio3.length(),
                    isFavorite = true,
                    genre = "Acoustic Melody"
                )
            )

            // 7. Initialize Sample Video from assets
            val videoFile = File(mediaDir, "MediaMaster_Showcase_HD.mp4")
            copyAssetFile(context, "sample_video.mp4", videoFile)

            if (videoFile.exists() && videoFile.length() > 1000) {
                mediaEntities.add(
                    MediaEntity(
                        title = "MediaMaster Offline Cinema Showcase",
                        artist = "MediaMaster Studio",
                        album = "Offline Demos",
                        durationMs = 5_000,
                        uriString = Uri.fromFile(videoFile).toString(),
                        filePath = videoFile.absolutePath,
                        mediaType = MediaType.VIDEO.name,
                        fileSizeBytes = videoFile.length(),
                        isFavorite = true,
                        genre = "Visual Demo"
                    )
                )
            }

            database.mediaDao().insertMediaList(mediaEntities)

            prefs.edit().putBoolean(KEY_SEEDED, true).apply()
            Log.i(TAG, "Sample data seeded successfully!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed seeding sample data: ${e.message}", e)
        }
    }

    private fun createManualPdf(outputFile: File) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 points

        val headerPaint = Paint().apply {
            color = Color.parseColor("#0891B2")
            textSize = 24f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val subPaint = Paint().apply {
            color = Color.parseColor("#475569")
            textSize = 14f
            isAntiAlias = true
        }

        val bodyPaint = Paint().apply {
            color = Color.parseColor("#1E293B")
            textSize = 12f
            isAntiAlias = true
        }

        val bulletPaint = Paint().apply {
            color = Color.parseColor("#0EA5E9")
            textSize = 13f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val cardBgPaint = Paint().apply {
            color = Color.parseColor("#F1F5F9")
            style = Paint.Style.FILL
        }

        // Page 1: Overview
        var page = document.startPage(pageInfo)
        var canvas = page.canvas
        canvas.drawRoundRect(40f, 40f, 555f, 150f, 16f, 16f, cardBgPaint)
        canvas.drawText("MediaMaster_#MYKSON#", 60f, 85f, headerPaint)
        canvas.drawText("Complete User Manual & Offline Architecture Guide", 60f, 115f, subPaint)
        canvas.drawText("Document Version 1.0  |  100% Offline Engineered", 60f, 135f, subPaint)

        var y = 190f
        canvas.drawText("Chapter 1: Offline Universal Reading Engine", 60f, y, headerPaint.apply { textSize = 18f })
        y += 30f
        canvas.drawText("MediaMaster includes high-precision native renderers for modern media and document workflows.", 60f, y, bodyPaint)
        y += 20f
        canvas.drawText("Unlike standard web-bound viewers, all parsing, caching, and playback happens entirely on-device.", 60f, y, bodyPaint)

        y += 40f
        canvas.drawText("Core Features:", 60f, y, bulletPaint)
        y += 25f
        canvas.drawText("• Native PDF Vector Rendering: Zoom, page thumbnails, text-to-speech aloud reading.", 75f, y, bodyPaint)
        y += 20f
        canvas.drawText("• Markdown & Code Viewer: Monospace font, code block separation, rich typography.", 75f, y, bodyPaint)
        y += 20f
        canvas.drawText("• CSV Spreadsheet Grid: View structured datasets as clean sortable data tables.", 75f, y, bodyPaint)
        y += 20f
        canvas.drawText("• Persistent Bookmarks: Mark any page or section with single-tap instant resume.", 75f, y, bodyPaint)

        y += 50f
        canvas.drawRoundRect(40f, y, 555f, y + 160f, 12f, 12f, cardBgPaint)
        canvas.drawText("Offline Reading Tips:", 60f, y + 35f, bulletPaint)
        canvas.drawText("1. Use the Reading Theme selector (Light, Sepia, Night) for optimal eye comfort.", 60f, y + 65f, bodyPaint)
        canvas.drawText("2. Tap the 'Speaker' icon on any text or document to listen via Text-to-Speech.", 60f, y + 95f, bodyPaint)
        canvas.drawText("3. Use the Search Bar inside the reader to locate matching words instantly.", 60f, y + 125f, bodyPaint)

        document.finishPage(page)

        // Page 2: Audio & Video
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        page = document.startPage(pageInfo2)
        canvas = page.canvas

        canvas.drawText("Chapter 2: Offline Audio Studio", 60f, 60f, headerPaint.apply { textSize = 18f })
        y = 100f
        canvas.drawText("The MediaMaster Audio Engine gives you studio-grade controls directly on your phone:", 60f, y, bodyPaint)
        y += 30f
        canvas.drawText("• Real-Time Visualizer: Animated audio spectrum reacts dynamically to track dynamics.", 75f, y, bodyPaint)
        y += 22f
        canvas.drawText("• Multi-Band Equalizer: Select presets for Bass Boost, Vocal Clarity, Treble, and Rock.", 75f, y, bodyPaint)
        y += 22f
        canvas.drawText("• Sleep Timer: Automatically pause playback after 15, 30, 45, or 60 minutes.", 75f, y, bodyPaint)
        y += 22f
        canvas.drawText("• Playback Speed: Fine-tune listening rate from 0.5x to 2.0x for podcasts or study.", 75f, y, bodyPaint)

        y += 50f
        canvas.drawText("Chapter 3: Cinema Video Engine", 60f, y, headerPaint.apply { textSize = 18f })
        y += 35f
        canvas.drawText("• Full gesture controls for instant seeking (10s skip forward/backward).", 75f, y, bodyPaint)
        y += 22f
        canvas.drawText("• Aspect ratio toggle adapts letterboxed content to fill your modern screen.", 75f, y, bodyPaint)
        y += 22f
        canvas.drawText("• Zero background network tracking ensures 100% privacy and battery longevity.", 75f, y, bodyPaint)

        document.finishPage(page)

        // Page 3: Summary
        val pageInfo3 = PdfDocument.PageInfo.Builder(595, 842, 3).create()
        page = document.startPage(pageInfo3)
        canvas = page.canvas

        canvas.drawText("Chapter 4: Importing Your Files", 60f, 60f, headerPaint.apply { textSize = 18f })
        y = 100f
        canvas.drawText("Tap the floating '+' action button or 'Import' on any screen to open files from:", 60f, y, bodyPaint)
        y += 25f
        canvas.drawText("1. Device Internal Storage & SD Cards", 75f, y, bodyPaint)
        y += 20f
        canvas.drawText("2. Downloads Folder", 75f, y, bodyPaint)
        y += 20f
        canvas.drawText("3. Storage Access Framework (SAF) File Provider", 75f, y, bodyPaint)

        y += 60f
        canvas.drawRoundRect(40f, y, 555f, y + 100f, 12f, 12f, cardBgPaint)
        canvas.drawText("Designed exclusively for MediaMaster_#MYKSON#", 60f, y + 45f, bulletPaint)
        canvas.drawText("Crafted with Android Jetpack Compose and modern Kotlin coroutines.", 60f, y + 75f, subPaint)

        document.finishPage(page)

        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
    }

    private fun createStrategyPdf(outputFile: File) {
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()

        val titlePaint = Paint().apply {
            color = Color.parseColor("#B45309")
            textSize = 22f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textPaint = Paint().apply {
            color = Color.parseColor("#334155")
            textSize = 12f
            isAntiAlias = true
        }

        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        canvas.drawText("The Art of Strategy — Volume I", 60f, 70f, titlePaint)
        var y = 110f
        canvas.drawText("Section 1: Laying Plans", 60f, y, titlePaint.apply { textSize = 16f })
        y += 30f
        canvas.drawText("The moral law causes the people to be in complete accord with their ruler,", 60f, y, textPaint)
        y += 18f
        canvas.drawText("so that they will follow him regardless of their lives, undismayed by any danger.", 60f, y, textPaint)
        y += 25f
        canvas.drawText("Heaven signifies night and day, cold and heat, times and seasons.", 60f, y, textPaint)
        y += 18f
        canvas.drawText("Earth comprises distances, great and small; danger and security; open ground and narrow passes.", 60f, y, textPaint)

        y += 40f
        canvas.drawText("Section 2: Waging War", 60f, y, titlePaint.apply { textSize = 16f })
        y += 30f
        canvas.drawText("In the operations of war, where there are in the field a thousand swift chariots,", 60f, y, textPaint)
        y += 18f
        canvas.drawText("as many heavy chariots, and a hundred thousand mail-clad soldiers, with provisions", 60f, y, textPaint)
        y += 18f
        canvas.drawText("enough to carry them a thousand li, the expenditure at home and at the front will be great.", 60f, y, textPaint)

        document.finishPage(page)

        // Page 2
        val pageInfo2 = PdfDocument.PageInfo.Builder(595, 842, 2).create()
        page = document.startPage(pageInfo2)
        canvas = page.canvas

        canvas.drawText("Section 3: Attack by Stratagem", 60f, 70f, titlePaint.apply { textSize = 16f })
        y = 110f
        canvas.drawText("In the practical art of war, the best thing of all is to take the enemy's country whole and intact;", 60f, y, textPaint)
        y += 18f
        canvas.drawText("to shatter and destroy it is not so good. So, too, it is better to recapture an army entire", 60f, y, textPaint)
        y += 18f
        canvas.drawText("than to destroy it, to capture a regiment, a detachment or a company entire than to destroy them.", 60f, y, textPaint)
        y += 30f
        canvas.drawText("Hence to fight and conquer in all your battles is not supreme excellence;", 60f, y, textPaint)
        y += 18f
        canvas.drawText("supreme excellence consists in breaking the enemy's resistance without fighting.", 60f, y, textPaint)

        document.finishPage(page)

        FileOutputStream(outputFile).use { fos ->
            document.writeTo(fos)
        }
        document.close()
    }

    private fun copyAssetFile(context: Context, assetName: String, destination: File) {
        if (!destination.exists() || destination.length() < 100) {
            try {
                context.assets.open(assetName).use { input ->
                    FileOutputStream(destination).use { output ->
                        input.copyTo(output)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed copying asset $assetName: ${e.message}")
            }
        }
    }
}
