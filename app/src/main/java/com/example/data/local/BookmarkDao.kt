package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.model.BookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT * FROM bookmarks WHERE documentId = :documentId ORDER BY pageNumber ASC")
    fun getBookmarksForDocument(documentId: Long): Flow<List<BookmarkEntity>>

    @Query("SELECT * FROM bookmarks ORDER BY timestamp DESC")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity): Long

    @Query("DELETE FROM bookmarks WHERE id = :id")
    suspend fun deleteBookmarkById(id: Long)

    @Query("DELETE FROM bookmarks WHERE documentId = :documentId AND pageNumber = :pageNumber")
    suspend fun deleteBookmarkByPage(documentId: Long, pageNumber: Int)
}
