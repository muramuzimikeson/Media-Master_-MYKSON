package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DocumentType(val displayName: String, val extensionBadge: String) {
    PDF("PDF Document", "PDF"),
    MARKDOWN("Markdown", "MD"),
    TEXT("Plain Text", "TXT"),
    CSV("Spreadsheet / CSV", "CSV"),
    JSON("JSON Data", "JSON"),
    CODE("Source Code", "CODE"),
    LOG("Log File", "LOG"),
    OTHER("Document", "DOC");

    companion object {
        fun fromExtensionOrMime(filename: String, mimeType: String? = null): DocumentType {
            val lower = filename.lowercase()
            return when {
                lower.endsWith(".pdf") || mimeType?.contains("pdf") == true -> PDF
                lower.endsWith(".md") || lower.endsWith(".markdown") -> MARKDOWN
                lower.endsWith(".csv") || lower.endsWith(".tsv") -> CSV
                lower.endsWith(".json") -> JSON
                lower.endsWith(".txt") -> TEXT
                lower.endsWith(".log") -> LOG
                lower.endsWith(".kt") || lower.endsWith(".java") || lower.endsWith(".py") ||
                lower.endsWith(".js") || lower.endsWith(".html") || lower.endsWith(".xml") ||
                lower.endsWith(".c") || lower.endsWith(".cpp") || lower.endsWith(".css") -> CODE
                else -> OTHER
            }
        }
    }
}

@Entity(tableName = "documents")
data class DocumentEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val uriString: String,
    val filePath: String? = null,
    val fileType: String = DocumentType.TEXT.name,
    val fileSizeBytes: Long = 0,
    val lastReadTimestamp: Long = System.currentTimeMillis(),
    val totalPages: Int = 1,
    val currentPage: Int = 0,
    val scrollOffset: Int = 0,
    val readingProgressPercent: Int = 0,
    val isFavorite: Boolean = false,
    val tags: String = ""
)
