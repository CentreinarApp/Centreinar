package com.example.centreinar
import android.content.Context
import androidx.room.Room
import com.example.centreinar.AppDatabase

object DatabaseProvider {
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
        return INSTANCE ?: synchronized(this) {
            val instance = Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "limits"
            ).createFromAsset("database/innit.db")
            .build()

            INSTANCE = instance
            instance
        }
    }
}
