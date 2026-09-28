package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.DocumentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    @Query("SELECT * FROM documents ORDER BY lastReadTimestamp DESC")
    fun getAllDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents ORDER BY lastReadTimestamp DESC LIMIT :limit")
    fun getRecentDocuments(limit: Int = 10): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE fileType = :type ORDER BY lastReadTimestamp DESC")
    fun getDocumentsByType(type: String): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE isFavorite = 1 ORDER BY lastReadTimestamp DESC")
    fun getFavoriteDocuments(): Flow<List<DocumentEntity>>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    fun getDocumentById(id: Long): Flow<DocumentEntity?>

    @Query("SELECT * FROM documents WHERE id = :id LIMIT 1")
    suspend fun getDocumentByIdSync(id: Long): DocumentEntity?

    @Query("SELECT * FROM documents WHERE uriString = :uri LIMIT 1")
    suspend fun getDocumentByUri(uri: String): DocumentEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: DocumentEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocuments(documents: List<DocumentEntity>)

    @Update
    suspend fun updateDocument(document: DocumentEntity)

    @Delete
    suspend fun deleteDocument(document: DocumentEntity)

    @Query("DELETE FROM documents WHERE id = :id")
    suspend fun deleteDocumentById(id: Long)

    @Query("SELECT * FROM documents WHERE title LIKE '%' || :query || '%' ORDER BY lastReadTimestamp DESC")
    fun searchDocuments(query: String): Flow<List<DocumentEntity>>
}
