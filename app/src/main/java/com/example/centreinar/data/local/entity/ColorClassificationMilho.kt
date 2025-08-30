package com.example.centreinar.data.local.entity
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

class ColorClassificationMilho {
    @Entity(tableName = "color_classification_milho")
    data class ColorClassificationMilho(
        @PrimaryKey(autoGenerate = true)
        val id: Int = 0,

        @ColumnInfo(name = "grain") val grain: String = "milho",

        @ColumnInfo(name = "classificationId") val classificationId: Int,

        @ColumnInfo(name = "yellowPercentage") val yellowPercentage: Float,

        @ColumnInfo(name = "otherColorPercentage") val otherColorPercentage: Float,

        @ColumnInfo(name = "class") val framingClass: String = "" // amarela, branca, cores, misturada
    )
}