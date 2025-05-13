package com.example.centreinar

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Limit::class,
        Classification::class,
        Sample::class,
        Discount::class,
        InputDiscount::class,
        ColorClassification::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun limitDao(): LimitDao
    abstract fun classificationDao(): ClassificationDao
    abstract fun sampleDao(): SampleDao
    abstract fun discountDao(): DiscountDao
    abstract fun inputDiscountDao(): InputDiscountDao
    abstract fun colorClassificationDao():ColorClassificationDao
}