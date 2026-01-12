package com.example.centreinar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "input_discount_milho")
data class InputDiscountMilho(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "classificationId") val classificationId: Int?,
    @ColumnInfo(name = "grain") val grain: String = "milho",
    @ColumnInfo(name = "group") val group: Int = 0,
    @ColumnInfo(name = "limitSource") var limitSource: Int = 0,
    @ColumnInfo(name = "daysOfStorage") val daysOfStorage: Int = 0,
    @ColumnInfo(name = "lotWeight") val lotWeight: Float = 0.0f,
    @ColumnInfo(name = "lotPrice") val lotPrice: Float = 0.0f,

    // Defeitos Básicos
    @ColumnInfo(name = "impurities") val impurities: Float = 0.0f,
    @ColumnInfo(name = "moisture") val moisture: Float = 0.0f,
    @ColumnInfo(name = "broken") val broken: Float = 0.0f,
    @ColumnInfo(name = "ardidos") val ardidos: Float = 0.0f,
    @ColumnInfo(name = "mofados") val mofados: Float = 0.0f,
    @ColumnInfo(name = "carunchado") val carunchado: Float = 0.0f,
    @ColumnInfo(name = "spoiled") val spoiled: Float = 0.0f,
    @ColumnInfo(name = "fermented") val fermented: Float = 0.0f,
    @ColumnInfo(name = "germinated") val germinated: Float = 0.0f,
    @ColumnInfo(name = "gessado") val gessado: Float = 0.0f,

    @ColumnInfo(name = "deductionValue") val deductionValue: Float = 0.0f
)