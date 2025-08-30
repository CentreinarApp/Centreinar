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

class SampleSoja {
@Entity(tableName = "sample_soja")
data class SampleSoja(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "grain")
    var grain: String ="",

    @ColumnInfo(name = "group")
    var group: Int = 0,

    @ColumnInfo(name = "lotWeight")
    val lotWeight: Float = 0.0f,

    @ColumnInfo(name = "sampleWeight")
    val sampleWeight: Float = 0.0f,

    @ColumnInfo(name = "cleanWeight")
    var cleanWeight: Float = 0.0f,

    @ColumnInfo(name = "foreignMattersAndImpurities")
    val foreignMattersAndImpurities: Float = 0.0f,

    @ColumnInfo(name = "humidity")
    val humidity: Float = 0.0f,

    @ColumnInfo(name = "greenish")
    val greenish: Float = 0.0f,

    @ColumnInfo(name = "brokenCrackedDamaged")
    val brokenCrackedDamaged: Float = 0.0f,

    @ColumnInfo(name = "damaged")
    val damaged: Float = 0.0f,

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
    val immature: Float = 0.0f,

    @ColumnInfo(name = "shriveled")
    val shriveled: Float = 0.0f,
)



}
