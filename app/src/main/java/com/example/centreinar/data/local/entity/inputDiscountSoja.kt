package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey



@Entity(tableName = "input_discount_soja")
data class InputDiscountSoja(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "classificationId") val classificationId: Int?,

    @ColumnInfo(name = "grain")
    val grain: String = "",

    @ColumnInfo(name = "group")
    val group: Int = 0,

    @ColumnInfo(name = "limitSource")
    var limitSource: Int = 0,

    @ColumnInfo(name = "daysOfStorage")
    val daysOfStorage: Int = 0,

    @ColumnInfo(name = "lotWeight")
    val lotWeight: Float = 0.0f,

    @ColumnInfo(name = "lotPrice")
    val lotPrice: Float = 0.0f,

    @ColumnInfo(name = "foreignMattersAndImpurities")
    val foreignMattersAndImpurities: Float = 0.0f,

    @ColumnInfo(name = "humidity")
    val humidity: Float = 0.0f,

    @ColumnInfo(name = "burnt") val burnt: Float,

    @ColumnInfo(name = "burntOrSour") val burntOrSour: Float,

    @ColumnInfo(name = "moldy") val moldy: Float,

    @ColumnInfo(name = "spoiled") val spoiled: Float,

    @ColumnInfo(name = "greenish") val greenish: Float,

    @ColumnInfo(name = "brokenCrackedDamaged") val brokenCrackedDamaged: Float,

    @ColumnInfo(name = "deductionValue")
    val deductionValue: Float = 0.0f,
)


