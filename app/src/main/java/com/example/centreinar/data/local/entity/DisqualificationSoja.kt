
package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(
    tableName = "disqualification_soja"
)
data class DisqualificationSoja(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "classificationId")
    val classificationId: Int?,
    @ColumnInfo(name = "badConservation")
    val badConservation: Int,
    @ColumnInfo(name = "graveDefectSum")
    val graveDefectSum: Int,
    @ColumnInfo(name = "strangeSmell")
    val strangeSmell: Int,
    @ColumnInfo(name = "insects")
    val insects: Int,
    @ColumnInfo(name = "toxicGrains")
    val toxicGrains: Int,
)




