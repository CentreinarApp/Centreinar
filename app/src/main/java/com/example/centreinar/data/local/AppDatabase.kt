package com.example.centreinar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.centreinar.Classification
import com.example.centreinar.ColorClassification
import com.example.centreinar.Discount
import com.example.centreinar.Disqualification
import com.example.centreinar.InputDiscount
import com.example.centreinar.Limit
import com.example.centreinar.Sample
import com.example.centreinar.data.local.dao.ClassificationDao
import com.example.centreinar.data.local.dao.ColorClassificationDao
import com.example.centreinar.data.local.dao.DiscountDao
import com.example.centreinar.data.local.dao.DisqualificationDao
import com.example.centreinar.data.local.dao.InputDiscountDao
import com.example.centreinar.data.local.dao.LimitDao
import com.example.centreinar.data.local.dao.SampleDao

@Database(
    entities = [
        Limit::class,
        Classification::class,
        Sample::class,
        Discount::class,
        InputDiscount::class,
        ColorClassification::class,
        Disqualification::class
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
    abstract fun colorClassificationDao(): ColorClassificationDao
    abstract fun disqualificationDao(): DisqualificationDao
}