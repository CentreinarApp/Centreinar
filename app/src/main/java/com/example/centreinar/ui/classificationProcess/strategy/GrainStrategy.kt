package com.example.centreinar.ui.classificationProcess.strategy

import android.content.Context

data class StrategyResultData(
    val classification: BaseClassification?,
    val disqualification: BaseDisqualification?,
    val limit: BaseLimit?,
    val tableRows: List<ClassificationRow>,
    val toxicSeeds: List<Pair<String, Int>>,
    val cards: List<ComplementaryCardData>,
    val sampleLotWeight: Float = 0f
)

interface GrainStrategy {
    val grainName: String

    // Buscar os limites no banco
    suspend fun getOfficialLimits(group: Int): List<Any>

    suspend fun getBaseLimits(group: Int): Map<String, Float>?

    suspend fun getUiStateData(id: Int): StrategyResultData

    suspend fun classify(payload: ClassificationPayload, isOfficial: Boolean): ClassificationUIState

    suspend fun saveDisqualificationData(
        classificationId: Int,
        badConservation: Int,
        strangeSmell: Int,
        insects: Int,
        toxicGrains: Int,
        toxicSeeds: List<Pair<String, String>>
    )

    suspend fun exportClassificationToPdf(
        context: Context,
        state: ClassificationUIState,
        limits: List<Any>,
        observation: String?,
        isOfficial: Boolean
    )

    suspend fun setCustomLimit(payload: CustomLimitPayload)

    suspend fun deleteCustomLimits()

    fun getTypeLabel(finalType: Int, group: Int): String

    // Método específico de cor
    suspend fun saveColorClass(classificationId: Int, totalWeight: Float, otherColorsWeight: Float) {}
}