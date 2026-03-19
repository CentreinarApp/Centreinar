package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.centreinar.ui.classificationProcess.strategy.BaseClassification
import com.example.centreinar.util.FieldKeys

@Entity(tableName = "classification_milho")
data class ClassificationMilho(
    @PrimaryKey(autoGenerate = true) override val id: Int = 0,

    @ColumnInfo(name = "grain") val grain: String = "milho",
    @ColumnInfo(name = "group") val group: Int = 0, // duro, dentado, semiduro, misturado
    @ColumnInfo(name = "sampleId") val sampleId: Int = 0,

    // --- Defeitos (% Porcentagens) ---
    @ColumnInfo(name = "brokenPercentage") val brokenPercentage: Float = 0.0f,
    @ColumnInfo(name = "moisturePercentage") override val moisturePercentage: Float = 0.0f,
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

    // Tipos Individuais
    @ColumnInfo(name = "impuritiesType") val impuritiesType: Int = 0,
    @ColumnInfo(name = "brokenType") val brokenType: Int = 0,
    @ColumnInfo(name = "ardidoType") val ardidoType: Int = 0,
    @ColumnInfo(name = "mofadoType") val mofadoType: Int = 0,
    @ColumnInfo(name = "fermentedType") val fermentedType: Int = 0,
    @ColumnInfo(name = "germinatedType") val germinatedType: Int = 0,
    @ColumnInfo(name = "immatureType") val immatureType: Int = 0,
    @ColumnInfo(name = "gessadoType") val gessadoType: Int = 0,
    @ColumnInfo(name = "carunchadoType") val carunchadoType: Int = 0,
    @ColumnInfo(name = "spoiledTotalType") val spoiledTotalType: Int = 0,

    // --- Resultado Final ---
    @ColumnInfo(name = "finalType") override val finalType: Int = 0,
    @ColumnInfo(name = "isDisqualified") val isDisqualified: Boolean = false
) : BaseClassification {

    // Converte os defeitos em um map para carregar os valores para classificação -> descontos
    override fun toDefectsMap(): Map<String, Float> = mapOf(
        FieldKeys.IMPURITIES to impuritiesPercentage,
        FieldKeys.BROKEN     to brokenPercentage,
        FieldKeys.ARDIDO     to ardidoPercentage,
        FieldKeys.MOLDY      to mofadoPercentage,
        FieldKeys.CARUNCHADO to carunchadoPercentage,
        FieldKeys.SPOILED    to spoiledTotalPercentage,
        FieldKeys.FERMENTED  to fermentedPercentage,
        FieldKeys.GERMINATED to germinatedPercentage,
        FieldKeys.GESSADO    to gessadoPercentage
    )
}