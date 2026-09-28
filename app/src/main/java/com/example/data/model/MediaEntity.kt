package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MediaType {
    AUDIO,
    VIDEO
}

@Entity(tableName = "media_items")
data class MediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val artist: String = "Unknown Artist",
    val album: String = "Offline Storage",
    val durationMs: Long = 0,
    val uriString: String,
    val filePath: String? = null,
    val mediaType: String = MediaType.AUDIO.name,
    val fileSizeBytes: Long = 0,
    val addedTimestamp: Long = System.currentTimeMillis(),
    val lastPlayedTimestamp: Long = 0,
    val lastPositionMs: Long = 0,
    val playCount: Int = 0,
    val isFavorite: Boolean = false,
    val genre: String = "General"
)
