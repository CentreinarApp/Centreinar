package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = "classification"
)
data class Classification(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "grain")
    val grain: String = "",

    @ColumnInfo(name = "group")
    val group: Int = 0,

    @ColumnInfo(name = "sample")
    val sampleId: Int = 0,

    @ColumnInfo(name = "foreignMattersPercentage")
    val foreignMattersPercentage: Float = 0.0f,

    @ColumnInfo(name = "brokenCrackedDamagedPercentage")
    val brokenCrackedDamagedPercentage: Float = 0.0f,

    @ColumnInfo(name = "greenishPercentage")
    val greenishPercentage:Float = 0.0f,

    @ColumnInfo(name = "moldyPercentage")
    val moldyPercentage: Float = 0.0f,

    @ColumnInfo(name = "burntPercentage")
    val burntPercentage: Float = 0.0f,

    @ColumnInfo(name = "burntOrSourPercentage")  // Fixed wrong column name
    val burntOrSourPercentage: Float = 0.0f,

    @ColumnInfo(name = "spoiledPercentage")
    val spoiledPercentage: Float = 0.0f,

    @ColumnInfo(name = "damagedPercentage")
    val damagedPercentage: Float = 0.0f,

    @ColumnInfo(name = "sourPercentage")
    val sourPercentage: Float = 0.0f,

    @ColumnInfo(name = "fermentedPercentage")
    val fermentedPercentage: Float = 0.0f,

    @ColumnInfo(name = "germinatedPercentage")
    val germinatedPercentage: Float = 0.0f,

    @ColumnInfo(name = "immaturePercentage")
    val immaturePercentage: Float = 0.0f,

    @ColumnInfo(name = "shriveledPercentage")
    val shriveledPercentage: Float = 0.0f,

    @ColumnInfo(name = "foreignMatters")
    val foreignMatters: Int = 0,

    @ColumnInfo(name = "brokenCrackedDamaged")
    val brokenCrackedDamaged: Int = 0,

    @ColumnInfo(name = "greenish")
    val greenish: Int = 0,

    @ColumnInfo(name = "moldy")
    val moldy: Int = 0,

    @ColumnInfo(name = "burnt")
    val burnt: Int = 0,

    @ColumnInfo(name = "burntOrSour")
    val burntOrSour: Int = 0,

    @ColumnInfo(name = "spoiled")
    val spoiled: Int = 0,

    @ColumnInfo(name = "finalType")
    val finalType: Int = 0
)