package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.centreinar.ui.classificationProcess.strategy.BaseClassification
import com.example.centreinar.util.FieldKeys

@Entity(tableName = "classification_soja")
data class ClassificationSoja(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,

    @ColumnInfo(name = "grain") val grain: String = "",
    @ColumnInfo(name = "group") val group: Int = 0,
    @ColumnInfo(name = "sampleId") val sampleId: Int = 0,

    // --- Defeitos (% Porcentagens) ---
    @ColumnInfo(name = "moisturePercentage") override val moisturePercentage: Float = 0.0f,
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

    // Tipos Individuais
    @ColumnInfo(name = "fermented") val fermentedType: Int = 0,
    @ColumnInfo(name = "germinated") val germinatedType: Int = 0,
    @ColumnInfo(name = "immature") val immatureType: Int = 0,
    @ColumnInfo(name = "shriveled") val shriveledType: Int = 0,
    @ColumnInfo(name = "sour") val sourType: Int = 0,
    @ColumnInfo(name = "impuritiesType") val impuritiesType: Int = 0,
    @ColumnInfo(name = "brokenCrackedDamagedType") val brokenCrackedDamagedType: Int = 0,
    @ColumnInfo(name = "greenishType") val greenishType: Int = 0,
    @ColumnInfo(name = "moldyType") val moldyType: Int = 0,
    @ColumnInfo(name = "burntType") val burntType: Int = 0,
    @ColumnInfo(name = "burntOrSourType") val burntOrSourType: Int = 0,
    @ColumnInfo(name = "spoiledType") val spoiledType: Int = 0,

    // --- Resultado Final ---
    @ColumnInfo(name = "finalType") override val finalType: Int = 0,
    @ColumnInfo(name = "isDisqualified") val isDisqualified: Boolean = false
) : BaseClassification {

    // Converte os defeitos em um map para carregar os valores para classificação -> descontos
    override fun toDefectsMap(): Map<String, Float> = mapOf(
        FieldKeys.IMPURITIES   to impuritiesPercentage,
        FieldKeys.BROKEN       to brokenCrackedDamagedPercentage,
        FieldKeys.GREENISH     to greenishPercentage,
        FieldKeys.MOLDY        to moldyPercentage,
        FieldKeys.BURNT        to burntPercentage,
        FieldKeys.BURNT_OR_SOUR to burntOrSourPercentage,
        FieldKeys.SPOILED      to spoiledPercentage,
        FieldKeys.ARDIDO       to sourPercentage
    )
}