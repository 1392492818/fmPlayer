package com.fm.fmmedia

import android.app.Application
import com.fm.fmmedia.dao.WordRoomDatabase
import com.fm.fmmedia.repository.VideoCategoryRepository
import com.fm.fmmedia.repository.WordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class FmApplication : Application() {
    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val applicationScope = CoroutineScope(SupervisorJob())

    val database by lazy { WordRoomDatabase.getDatabase(this,applicationScope) }
    val repository by lazy { WordRepository(database.wordDao()) }
    val videoCategoryRepository by lazy { VideoCategoryRepository() }
}