package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "classification_milho")
data class ClassificationMilho(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,

    @ColumnInfo(name = "grain") val grain: String = "milho",
    @ColumnInfo(name = "group") val group: Int = 0, // duro, dentado, semiduro, misturado
    @ColumnInfo(name = "sampleId") val sampleId: Int = 0,

    // --- Defeitos (% Porcentagens) ---
    @ColumnInfo(name = "brokenPercentage") val brokenPercentage: Float = 0.0f,
    @ColumnInfo(name = "moisturePercentage") val moisturePercentage: Float = 0.0f,
    @ColumnInfo(name = "impuritiesPercentage") val impuritiesPercentage: Float = 0.0f,
    @ColumnInfo(name = "carunchadoPercentage") val carunchadoPercentage: Float = 0.0f,
    @ColumnInfo(name = "ardidoPercentage") val ardidoPercentage: Float = 0.0f,
    @ColumnInfo(name = "mofadoPercentage") val mofadoPercentage: Float = 0.0f,
    @ColumnInfo(name = "fermentedPercentage") val fermentedPercentage: Float = 0.0f,
    @ColumnInfo(name = "germinatedPercentage") val germinatedPercentage: Float = 0.0f,
    @ColumnInfo(name = "immaturePercentage") val immaturePercentage: Float = 0.0f,
    @ColumnInfo(name = "gessadoPercentage") val gessadoPercentage: Float = 0.0f,

    // Soma total dos avariados
    @ColumnInfo(name = "spoiledTotalPercentage") val spoiledTotalPercentage: Float = 0.0f,

    // Tipos Individuais ---
    // Ex: 1 = Tipo 1, 2 = Tipo 2, 7 = Fora de Tipo, etc.
    @ColumnInfo(name = "impuritiesType") val impuritiesType: Int = 0,
    @ColumnInfo(name = "brokenType") val brokenType: Int = 0,
    @ColumnInfo(name = "ardidoType") val ardidoType: Int = 0,
    @ColumnInfo(name = "mofadoType") val mofadoType: Int = 0,
    @ColumnInfo(name = "carunchadoType") val carunchadoType: Int = 0,
    @ColumnInfo(name = "spoiledTotalType") val spoiledTotalType: Int = 0,

    // --- Resultado Final ---
    @ColumnInfo(name = "finalType") val finalType: Int = 0
)