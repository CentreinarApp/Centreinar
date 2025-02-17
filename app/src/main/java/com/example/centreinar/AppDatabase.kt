package com.example.centreinar
import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.centreinar.Limit
import com.example.centreinar.LimitDao


@Database(entities = [Limit::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun limitDao(): LimitDao
}