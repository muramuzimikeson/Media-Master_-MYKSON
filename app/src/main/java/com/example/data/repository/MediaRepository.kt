package com.example.data.repository

import com.example.data.local.MediaDao
import com.example.data.model.MediaEntity
import com.example.data.model.MediaType
import kotlinx.coroutines.flow.Flow

class MediaRepository(private val mediaDao: MediaDao) {
    val allAudio: Flow<List<MediaEntity>> = mediaDao.getMediaByType(MediaType.AUDIO.name)
    val allVideos: Flow<List<MediaEntity>> = mediaDao.getMediaByType(MediaType.VIDEO.name)
    val favoriteAudio: Flow<List<MediaEntity>> = mediaDao.getFavoriteMedia(MediaType.AUDIO.name)
    val favoriteVideos: Flow<List<MediaEntity>> = mediaDao.getFavoriteMedia(MediaType.VIDEO.name)
    val recentAudio: Flow<List<MediaEntity>> = mediaDao.getRecentPlayedMedia(MediaType.AUDIO.name, 8)
    val recentVideos: Flow<List<MediaEntity>> = mediaDao.getRecentPlayedMedia(MediaType.VIDEO.name, 8)

    fun getMediaById(id: Long): Flow<MediaEntity?> = mediaDao.getMediaById(id)

    suspend fun getMediaByIdSync(id: Long): MediaEntity? = mediaDao.getMediaByIdSync(id)

    suspend fun insertMedia(media: MediaEntity): Long = mediaDao.insertMedia(media)

    suspend fun insertMediaList(list: List<MediaEntity>) = mediaDao.insertMediaList(list)

    suspend fun updateMedia(media: MediaEntity) = mediaDao.updateMedia(media)

    suspend fun deleteMedia(media: MediaEntity) = mediaDao.deleteMedia(media)

    fun searchAudio(query: String): Flow<List<MediaEntity>> =
        mediaDao.searchMedia(query, MediaType.AUDIO.name)

    fun searchVideos(query: String): Flow<List<MediaEntity>> =
        mediaDao.searchMedia(query, MediaType.VIDEO.name)

    suspend fun recordPlayback(id: Long, positionMs: Long) {
        mediaDao.updatePlaybackStats(id, System.currentTimeMillis(), positionMs)
    }

    suspend fun toggleFavorite(id: Long) {
        val item = mediaDao.getMediaByIdSync(id) ?: return
        mediaDao.setFavorite(id, !item.isFavorite)
    }
}
