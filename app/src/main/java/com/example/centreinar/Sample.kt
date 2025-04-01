package com.example.centreinar
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/*
Esverdeado → Greenish
Partido e quebrado e danificado → Broken, cracked, and damaged
Queimados → Burnt
Ardidos → Sour
Mofados → Moldy
Fermentados → Fermented
Germinados → Germinated
Imaturos (chochos) → Immature (shrivelled)
Danificados por insetos → Insect-damaged
Gessado → starchy
Matérias estranhas e Impurezas → foreign matter and Impurities
 */

@Entity(tableName = "sample")
data class Sample(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "grain")
    val grain: String ="",

    @ColumnInfo(name = "group")
    val group: Int = 0,

    @ColumnInfo(name = "weight")
    val weight: Float = 0.0f,

    @ColumnInfo(name = "foreignMattersAndImpurities")
    val foreignMattersAndImpurities: Float = 0.0f,

    @ColumnInfo(name = "humidity")
    val humidity: Float = 0.0f,

    @ColumnInfo(name = "greenish")
    val greenish: Float = 0.0f,

    @ColumnInfo(name = "brokenCrackedDamaged")
    val brokenCrackedDamaged: Float = 0.0f,

    @ColumnInfo(name = "burnt")
    val burnt: Float = 0.0f,

    @ColumnInfo(name = "sour")
    val sour: Float = 0.0f,

    @ColumnInfo(name = "moldy")
    val moldy: Float = 0.0f,

    @ColumnInfo(name = "fermented")
    val fermented: Float = 0.0f,

    @ColumnInfo(name = "germinated")
    val germinated: Float = 0.0f,

    @ColumnInfo(name = "immature")
    val immature: Float = 0.0f
) {
    // Secondary constructor with default values
    constructor() : this(0, " ", 0, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f)
}