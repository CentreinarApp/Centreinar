package com.example.centreinar.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

// DAOs
import com.example.centreinar.data.local.dao.ClassificationMilhoDAO
import com.example.centreinar.data.local.dao.ClassificationSojaDAO
import com.example.centreinar.data.local.dao.ColorClassificationMilhoDAO
import com.example.centreinar.data.local.dao.ColorClassificationSojaDAO
import com.example.centreinar.data.local.dao.DiscountMilhoDAO
import com.example.centreinar.data.local.dao.DiscountSojaDAO
import com.example.centreinar.data.local.dao.DisqualificationMilhoDAO
import com.example.centreinar.data.local.dao.DisqualificationSojaDAO
import com.example.centreinar.data.local.dao.InputDiscountMilhoDAO
import com.example.centreinar.data.local.dao.InputDiscountSojaDAO
import com.example.centreinar.data.local.dao.LimitMilhoDAO
import com.example.centreinar.data.local.dao.LimitSojaDAO
import com.example.centreinar.data.local.dao.SampleMilhoDAO
import com.example.centreinar.data.local.dao.SampleSojaDAO

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
        DisqualificationSoja::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun limitMilhoDao(): LimitMilhoDAO
    abstract fun limitSojaDao(): LimitSojaDAO

    abstract fun classificationMilhoDao(): ClassificationMilhoDAO
    abstract fun classificationSojaDao(): ClassificationSojaDAO

    abstract fun sampleMilhoDao(): SampleMilhoDAO
    abstract fun sampleSojaDao(): SampleSojaDAO

    abstract fun discountMilhoDao(): DiscountMilhoDAO
    abstract fun discountSojaDao(): DiscountSojaDAO

    abstract fun inputDiscountMilhoDao(): InputDiscountMilhoDAO
    abstract fun inputDiscountSojaDao(): InputDiscountSojaDAO

    abstract fun colorClassificationMilhoDao(): ColorClassificationMilhoDAO
    abstract fun colorClassificationSojaDao(): ColorClassificationSojaDAO

    abstract fun disqualificationMilhoDao(): DisqualificationMilhoDAO
    abstract fun disqualificationSojaDao(): DisqualificationSojaDAO
}

