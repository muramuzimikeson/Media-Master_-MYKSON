package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bookmarks")
data class BookmarkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val documentId: Long,
    val pageNumber: Int = 0,
    val title: String,
    val snippet: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "playlists")
data class PlaylistEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val mediaType: String = MediaType.AUDIO.name,
    val createdTimestamp: Long = System.currentTimeMillis()
)
