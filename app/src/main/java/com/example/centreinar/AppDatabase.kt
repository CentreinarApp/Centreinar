package com.example.centreinar

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Limit::class,
        Classification::class,
        Sample::class
    ],
    version = 2,  // Incremented version number
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun limitDao(): LimitDao
    abstract fun classificationDao(): ClassificationDao
    abstract fun sampleDao(): SampleDao
}