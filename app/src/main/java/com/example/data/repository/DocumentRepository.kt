package com.example.data.repository

import com.example.data.local.BookmarkDao
import com.example.data.local.DocumentDao
import com.example.data.model.BookmarkEntity
import com.example.data.model.DocumentEntity
import kotlinx.coroutines.flow.Flow

class DocumentRepository(
    private val documentDao: DocumentDao,
    private val bookmarkDao: BookmarkDao
) {
    val allDocuments: Flow<List<DocumentEntity>> = documentDao.getAllDocuments()
    val recentDocuments: Flow<List<DocumentEntity>> = documentDao.getRecentDocuments(6)
    val favoriteDocuments: Flow<List<DocumentEntity>> = documentDao.getFavoriteDocuments()

    fun getDocumentsByType(type: String): Flow<List<DocumentEntity>> =
        documentDao.getDocumentsByType(type)

    fun getDocumentById(id: Long): Flow<DocumentEntity?> =
        documentDao.getDocumentById(id)

    suspend fun getDocumentByIdSync(id: Long): DocumentEntity? =
        documentDao.getDocumentByIdSync(id)

    suspend fun insertDocument(document: DocumentEntity): Long =
        documentDao.insertDocument(document)

    suspend fun updateDocument(document: DocumentEntity) =
        documentDao.updateDocument(document)

    suspend fun deleteDocument(document: DocumentEntity) =
        documentDao.deleteDocument(document)

    fun searchDocuments(query: String): Flow<List<DocumentEntity>> =
        documentDao.searchDocuments(query)

    fun getBookmarks(documentId: Long): Flow<List<BookmarkEntity>> =
        bookmarkDao.getBookmarksForDocument(documentId)

    suspend fun addBookmark(bookmark: BookmarkEntity): Long =
        bookmarkDao.insertBookmark(bookmark)

    suspend fun deleteBookmark(id: Long) =
        bookmarkDao.deleteBookmarkById(id)

    suspend fun updateProgress(id: Long, page: Int, totalPages: Int, progressPercent: Int) {
        val doc = documentDao.getDocumentByIdSync(id) ?: return
        documentDao.updateDocument(
            doc.copy(
                currentPage = page,
                totalPages = if (totalPages > 0) totalPages else doc.totalPages,
                readingProgressPercent = progressPercent,
                lastReadTimestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun toggleFavorite(id: Long) {
        val doc = documentDao.getDocumentByIdSync(id) ?: return
        documentDao.updateDocument(doc.copy(isFavorite = !doc.isFavorite))
    }
}
