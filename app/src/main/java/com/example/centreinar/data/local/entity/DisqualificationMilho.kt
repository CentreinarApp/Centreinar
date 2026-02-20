package com.example.centreinar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


    @Entity(tableName = "disqualification_milho")
    data class DisqualificationMilho(
        @PrimaryKey(autoGenerate = true) val id: Int = 0,

        @ColumnInfo(name = "classificationId") val classificationId: Int?,
        @ColumnInfo(name = "badConservation") val badConservation: Int,
        @ColumnInfo(name = "insects") val insects: Int,
        @ColumnInfo(name = "strangeSmell") val strangeSmell: Int,
        @ColumnInfo(name = "toxicGrains") val toxicGrains: Int
    )
