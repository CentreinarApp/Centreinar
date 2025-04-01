package com.example.centreinar

import android.content.Context
import androidx.room.Room

object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "grains_db"  // Changed to more appropriate name
            )
                .createFromAsset("database/innit.db")
                .fallbackToDestructiveMigration()  // Added migration strategy
                .build()

            INSTANCE = instance
            instance
        }
    }
}