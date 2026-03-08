
package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.centreinar.ui.classificationProcess.strategy.BaseDisqualification


@Entity(
    tableName = "disqualification_soja"
)
data class DisqualificationSoja(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(name = "classificationId") val classificationId: Int?,
    @ColumnInfo(name = "badConservation") override val badConservation: Int,
    @ColumnInfo(name = "graveDefectSum") val graveDefectSum: Int,
    @ColumnInfo(name = "strangeSmell") override val strangeSmell: Int,
    @ColumnInfo(name = "insects") override val insects: Int,
    @ColumnInfo(name = "toxicGrains") override val toxicGrains: Int,
) : BaseDisqualification





