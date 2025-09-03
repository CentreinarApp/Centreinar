package com.example.centreinar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


    @Entity(tableName = "sample_milho")
    data class SampleMilho(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,
        @ColumnInfo(name = "grain") val grain: String = "milho",
        @ColumnInfo(name = "group") val group: Int = 0,
        @ColumnInfo(name = "lotWeight") val lotWeight: Float = 0.0f,
        @ColumnInfo(name = "sampleWeight") val sampleWeight: Float = 0.0f,
        @ColumnInfo(name = "cleanWeight") val cleanWeight: Float = 0.0f,
        @ColumnInfo(name = "impurities") val impurities: Float = 0.0f,
        @ColumnInfo(name = "broken") val broken: Float = 0.0f,
        @ColumnInfo(name = "carunchado") val carunchado: Float = 0.0f,
        @ColumnInfo(name = "ardido") val ardido: Float = 0.0f,
        @ColumnInfo(name = "mofado") val mofado: Float = 0.0f,
        @ColumnInfo(name = "fermented") val fermented: Float = 0.0f,
        @ColumnInfo(name = "germinated") val germinated: Float = 0.0f,
        @ColumnInfo(name = "immature") val immature: Float = 0.0f,
        @ColumnInfo(name = "gessado") val gessado: Float = 0.0f
    )
