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

    @ColumnInfo(name = "burntOrSour")  // Fixed wrong column name
    val burntOrSour: Int = 0,

    @ColumnInfo(name = "spoiled")
    val spoiled: Int = 0,

    @ColumnInfo(name = "finalType")
    val finalType: Int = 0
) {
    // Secondary constructor with default values
    constructor() : this(0, "", 0, 0, 0, 0, 0, 0, 0, 0, 0)
}
