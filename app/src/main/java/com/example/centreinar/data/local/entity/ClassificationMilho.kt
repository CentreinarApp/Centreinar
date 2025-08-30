package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classification_milho")
data class ClassificationMilho(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "grain")
    val grain: String = "",

    @ColumnInfo(name = "group")
    val group: Int = 0,

    @ColumnInfo(name = "sample")
    val sampleId: Int = 0,

    @ColumnInfo(name = "moldyPercentage")
    val moldyPercentage: Float = 0.0f,

    @ColumnInfo(name = "burntPercentage") // Ardidos
    val burntPercentage: Float = 0.0f,

    @ColumnInfo(name = "fermentedPercentage")
    val fermentedPercentage: Float = 0.0f,

    @ColumnInfo(name = "germinatedPercentage")
    val germinatedPercentage: Float = 0.0f,

    @ColumnInfo(name = "insectDamagedPercentage") // Carunchados
    val insectDamagedPercentage: Float = 0.0f,

    @ColumnInfo(name = "immaturePercentage") // Chochos ou Imaturos
    val immaturePercentage: Float = 0.0f,

    @ColumnInfo(name = "chalkyPercentage") // Gessados
    val chalkyPercentage: Float = 0.0f,

    @ColumnInfo(name = "moldy")
    val moldy: Int = 0,

    @ColumnInfo(name = "burnt") // Ardidos
    val burnt: Int = 0,

    @ColumnInfo(name = "fermented")
    val fermented: Int = 0,

    @ColumnInfo(name = "germinated")
    val germinated: Int = 0,

    @ColumnInfo(name = "insectDamaged")
    val insectDamaged: Int = 0,

    @ColumnInfo(name = "immature") // Chochos ou Imaturos
    val immature: Int = 0,

    @ColumnInfo(name = "chalky") // Gessados
    val chalky: Int = 0,

    @ColumnInfo(name = "finalType")
    val finalType: Int = 0
)
