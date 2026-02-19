package com.example.centreinar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// DAOs
import com.example.centreinar.data.local.dao.ClassificationMilhoDao
import com.example.centreinar.data.local.dao.ClassificationSojaDao
import com.example.centreinar.data.local.dao.ColorClassificationMilhoDao
import com.example.centreinar.data.local.dao.ColorClassificationSojaDao
import com.example.centreinar.data.local.dao.DiscountMilhoDao
import com.example.centreinar.data.local.dao.DiscountSojaDao
import com.example.centreinar.data.local.dao.DisqualificationMilhoDao
import com.example.centreinar.data.local.dao.DisqualificationSojaDao
import com.example.centreinar.data.local.dao.InputDiscountMilhoDao
import com.example.centreinar.data.local.dao.InputDiscountSojaDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.LimitSojaDao
import com.example.centreinar.data.local.dao.SampleMilhoDao
import com.example.centreinar.data.local.dao.SampleSojaDao

// Entities
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.data.local.entity.ColorClassificationMilho
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.DiscountSoja
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.dao.ToxicSeedMilhoDao
import com.example.centreinar.data.local.dao.ToxicSeedSojaDao
import com.example.centreinar.data.local.entities.ToxicSeedSoja
import com.example.centreinar.data.local.entity.ToxicSeedMilho

@Database(
    entities = [
        LimitMilho::class,
        LimitSoja::class,
        ClassificationMilho::class,
        ClassificationSoja::class,
        SampleMilho::class,
        SampleSoja::class,
        DiscountMilho::class,
        DiscountSoja::class,
        InputDiscountMilho::class,
        InputDiscountSoja::class,
        ColorClassificationMilho::class,
        ColorClassificationSoja::class,
        DisqualificationMilho::class,
        DisqualificationSoja::class,
        ToxicSeedSoja::class,
        ToxicSeedMilho::class
    ],
    version = 5,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun limitMilhoDao(): LimitMilhoDao
    abstract fun limitSojaDao(): LimitSojaDao

    abstract fun toxicSeedSojaDao(): ToxicSeedSojaDao

    abstract fun toxicSeedMilhoDao(): ToxicSeedMilhoDao

    abstract fun classificationMilhoDao(): ClassificationMilhoDao
    abstract fun classificationSojaDao(): ClassificationSojaDao

    abstract fun sampleMilhoDao(): SampleMilhoDao
    abstract fun sampleSojaDao(): SampleSojaDao

    abstract fun discountMilhoDao(): DiscountMilhoDao
    abstract fun discountSojaDao(): DiscountSojaDao

    abstract fun inputDiscountMilhoDao(): InputDiscountMilhoDao
    abstract fun inputDiscountSojaDao(): InputDiscountSojaDao

    abstract fun colorClassificationMilhoDao(): ColorClassificationMilhoDao
    abstract fun colorClassificationSojaDao(): ColorClassificationSojaDao

    abstract fun disqualificationMilhoDao(): DisqualificationMilhoDao
    abstract fun disqualificationSojaDao(): DisqualificationSojaDao
}

