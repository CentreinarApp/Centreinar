package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "ColorClassificationSoja")
data class ColorClassificationSoja(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "grain")
    val grain: String = "",

    @ColumnInfo(name = "classificationId")
    val classificationId: Int,

    @ColumnInfo(name = "yellowPercentage")
    val yellowPercentage: Float,

    @ColumnInfo(name = "otherColorPercentage")
    val otherColorPercentage: Float,

    @ColumnInfo(name = "class")
    val framingClass: String = "",
)
