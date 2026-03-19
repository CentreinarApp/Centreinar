package com.example.centreinar.ui.classificationProcess.strategy

import android.content.Context
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.domain.model.LimitField

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

    val descriptor: GrainDescriptor

    val grainName: String get() = descriptor.name

    fun getLimitFields(): List<LimitField>

    fun getSampleInputRows(state: ClassificationUIState): List<SampleInputRow>

    fun buildPayload(state: ClassificationInputState): ClassificationPayload

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

    suspend fun saveColorClass(
        classificationId: Int,
        totalWeight: Float,
        otherColorsWeight: Float
    ) {}
}