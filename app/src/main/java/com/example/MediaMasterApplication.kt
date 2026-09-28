package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.repository.DocumentRepository
import com.example.data.repository.MediaRepository
import com.example.data.sample.SampleDataInitializer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MediaMasterApplication : Application() {
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this) }
    val documentRepository by lazy {
        DocumentRepository(database.documentDao(), database.bookmarkDao())
    }
    val mediaRepository by lazy {
        MediaRepository(database.mediaDao())
    }

    override fun onCreate() {
        super.onCreate()
        applicationScope.launch {
            SampleDataInitializer.seedSampleDataIfNeeded(this@MediaMasterApplication, database)
        }
    }
}
