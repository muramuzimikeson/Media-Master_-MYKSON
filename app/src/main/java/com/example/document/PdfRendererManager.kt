package com.example.document

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Rect
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.util.Log
import android.util.LruCache
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class PdfRendererManager(private val context: Context) {
    private val TAG = "PdfRendererManager"
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var pdfRenderer: PdfRenderer? = null
    private var currentDocumentPath: String? = null

    // Cache recently viewed page bitmaps
    private val pageCache = object : LruCache<Int, Bitmap>(16) {}

    var pageCount: Int = 0
        private set

    suspend fun openPdf(uriString: String, filePath: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            close()
            val fileToOpen: File = if (!filePath.isNullOrEmpty() && File(filePath).exists()) {
                File(filePath)
            } else {
                // Copy stream from URI to cache file if needed
                val uri = Uri.parse(uriString)
                val tempFile = File(context.cacheDir, "temp_render_${System.currentTimeMillis()}.pdf")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    FileOutputStream(tempFile).use { output ->
                        input.copyTo(output)
                    }
                }
                tempFile
            }

            fileDescriptor = ParcelFileDescriptor.open(fileToOpen, ParcelFileDescriptor.MODE_READ_ONLY)
            val pfd = fileDescriptor ?: return@withContext false
            pdfRenderer = PdfRenderer(pfd)
            pageCount = pdfRenderer?.pageCount ?: 0
            currentDocumentPath = fileToOpen.absolutePath
            Log.i(TAG, "Opened PDF with $pageCount pages")
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed opening PDF: ${e.message}", e)
            false
        }
    }

    suspend fun renderPage(pageIndex: Int, targetWidth: Int = 1080): Bitmap? = withContext(Dispatchers.IO) {
        if (pdfRenderer == null || pageIndex < 0 || pageIndex >= pageCount) {
            return@withContext null
        }

        // Check cache
        val cached = pageCache.get(pageIndex)
        if (cached != null && !cached.isRecycled) {
            return@withContext cached
        }

        try {
            val renderer = pdfRenderer ?: return@withContext null
            val page = renderer.openPage(pageIndex)
            val pageWidth = page.width
            val pageHeight = page.height

            val scale = (targetWidth.toFloat() / pageWidth).coerceIn(1f, 3.5f)
            val bitmapWidth = (pageWidth * scale).toInt()
            val bitmapHeight = (pageHeight * scale).toInt()

            val bitmap = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888)
            // Draw clean white background behind PDF vector page
            val canvas = android.graphics.Canvas(bitmap)
            canvas.drawColor(Color.WHITE)

            val matrix = Matrix().apply {
                postScale(scale, scale)
            }
            page.render(bitmap, null, matrix, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()

            pageCache.put(pageIndex, bitmap)
            bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Error rendering page $pageIndex: ${e.message}", e)
            null
        }
    }

    fun close() {
        try {
            pageCache.evictAll()
            pdfRenderer?.close()
            pdfRenderer = null
            fileDescriptor?.close()
            fileDescriptor = null
            pageCount = 0
            currentDocumentPath = null
        } catch (ignored: Exception) {}
    }
}
