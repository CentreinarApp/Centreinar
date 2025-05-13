package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Disqualification")
data class Disqualification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "classificationId")
    val classificationId:Int,
    @ColumnInfo(name = "badConservation")
    val badConservation:Boolean,
    @ColumnInfo(name = "graveDefectSum")
    val graveDefectSum:Boolean,
    @ColumnInfo(name = "strangeSmell")
    val strangeSmell:Boolean,
    @ColumnInfo(name = "insects")
    val insects:Boolean,
    @ColumnInfo(name = "toxicGrains")
    val toxicGrains:Boolean,
)
