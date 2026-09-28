package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.MediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Query("SELECT * FROM media_items ORDER BY addedTimestamp DESC")
    fun getAllMedia(): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type ORDER BY addedTimestamp DESC")
    fun getMediaByType(type: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type AND isFavorite = 1 ORDER BY addedTimestamp DESC")
    fun getFavoriteMedia(type: String): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE mediaType = :type AND lastPlayedTimestamp > 0 ORDER BY lastPlayedTimestamp DESC LIMIT :limit")
    fun getRecentPlayedMedia(type: String, limit: Int = 10): Flow<List<MediaEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    fun getMediaById(id: Long): Flow<MediaEntity?>

    @Query("SELECT * FROM media_items WHERE id = :id LIMIT 1")
    suspend fun getMediaByIdSync(id: Long): MediaEntity?

    @Query("SELECT * FROM media_items WHERE uriString = :uri LIMIT 1")
    suspend fun getMediaByUri(uri: String): MediaEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: MediaEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMediaList(mediaList: List<MediaEntity>)

    @Update
    suspend fun updateMedia(media: MediaEntity)

    @Delete
    suspend fun deleteMedia(media: MediaEntity)

    @Query("DELETE FROM media_items WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("SELECT * FROM media_items WHERE mediaType = :type AND (title LIKE '%' || :query || '%' OR artist LIKE '%' || :query || '%') ORDER BY addedTimestamp DESC")
    fun searchMedia(query: String, type: String): Flow<List<MediaEntity>>

    @Query("UPDATE media_items SET lastPlayedTimestamp = :timestamp, lastPositionMs = :position, playCount = playCount + 1 WHERE id = :id")
    suspend fun updatePlaybackStats(id: Long, timestamp: Long, position: Long)

    @Query("UPDATE media_items SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
