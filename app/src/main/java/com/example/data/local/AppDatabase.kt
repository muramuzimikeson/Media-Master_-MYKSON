package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.BookmarkEntity
import com.example.data.model.DocumentEntity
import com.example.data.model.MediaEntity
import com.example.data.model.PlaylistEntity

@Database(
    entities = [
        DocumentEntity::class,
        MediaEntity::class,
        BookmarkEntity::class,
        PlaylistEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun documentDao(): DocumentDao
    abstract fun mediaDao(): MediaDao
    abstract fun bookmarkDao(): BookmarkDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mediamaster_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
