package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.collections.map


@Entity(
    tableName = "classification_soja"
)
data class ClassificationSoja(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(name = "grain") val grain: String = "",
    @ColumnInfo(name = "group") val group: Int = 0,
    @ColumnInfo(name = "sampleId") val sampleId: Int = 0,

    // --- Defeitos (% Porcentagens) ---
    @ColumnInfo(name = "moisturePercentage") val moisturePercentage: Float = 0.0f,
    @ColumnInfo(name = "impuritiesPercentage") val impuritiesPercentage: Float = 0.0f,
    @ColumnInfo(name = "brokenCrackedDamagedPercentage") val brokenCrackedDamagedPercentage: Float = 0.0f,
    @ColumnInfo(name = "greenishPercentage") val greenishPercentage: Float = 0.0f,
    @ColumnInfo(name = "moldyPercentage") val moldyPercentage: Float = 0.0f,
    @ColumnInfo(name = "burntPercentage") val burntPercentage: Float = 0.0f,
    @ColumnInfo(name = "burntOrSourPercentage") val burntOrSourPercentage: Float = 0.0f,
    @ColumnInfo(name = "damagedPercentage") val damagedPercentage: Float = 0.0f,
    @ColumnInfo(name = "sourPercentage") val sourPercentage: Float = 0.0f,
    @ColumnInfo(name = "fermentedPercentage") val fermentedPercentage: Float = 0.0f,
    @ColumnInfo(name = "germinatedPercentage") val germinatedPercentage: Float = 0.0f,
    @ColumnInfo(name = "immaturePercentage") val immaturePercentage: Float = 0.0f,
    @ColumnInfo(name = "shriveledPercentage") val shriveledPercentage: Float = 0.0f,

    // Soma total dos avariados
    @ColumnInfo(name = "spoiledPercentage") val spoiledPercentage: Float = 0.0f,

    // Tipos Individuais ---
    // Ex: 1 = Tipo 1, 2 = Tipo 2, 7 = Fora de Tipo, etc.
    @ColumnInfo(name = "fermented") val fermented: Int = 0,
    @ColumnInfo(name = "germinated") val germinated: Int = 0,
    @ColumnInfo(name = "immature") val immature: Int = 0,
    @ColumnInfo(name = "shriveled") val shriveled: Int = 0,
    @ColumnInfo(name = "sour") val sour: Int = 0,
    @ColumnInfo(name = "impuritiesType") val impuritiesType: Int = 0,
    @ColumnInfo(name = "brokenCrackedDamagedType") val brokenCrackedDamagedType: Int = 0,
    @ColumnInfo(name = "greenishType") val greenishType: Int = 0,
    @ColumnInfo(name = "moldyType") val moldyType: Int = 0,
    @ColumnInfo(name = "burntType") val burntType: Int = 0,
    @ColumnInfo(name = "burntOrSourType") val burntOrSourType: Int = 0,
    @ColumnInfo(name = "spoiledType") val spoiledType: Int = 0,

    // --- Resultado Final ---
    @ColumnInfo(name = "finalType") val finalType: Int = 0
)



