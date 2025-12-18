package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationSoja
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.domain.model.LimitCategory

interface ClassificationRepository {

    suspend fun classifySample(sample: SampleSoja, limitSource: Int): Long

    suspend fun getSample(id: Int): SampleSoja?


    suspend fun setSample(
        grain: String,
        group: Int,
        sampleWeight: Float,
        lotWeight: Float,
        foreignMattersAndImpurities: Float,
        humidity: Float,
        greenish: Float,
        brokenCrackedDamaged: Float,
        burnt: Float,
        sour: Float,
        moldy: Float,
        fermented: Float,
        germinated: Float,
        immature: Float,
        shriveled: Float,
        damaged: Float,
        cleanWeight: Float
    ): SampleSoja

    suspend fun setSample(sample: SampleSoja): Long

    suspend fun getClassification(id: Int): ClassificationSoja?

    suspend fun getLimitsForGrain(
        grain: String,
        group: Int,
        limitSource: Int
    ): Map<String, List<LimitCategory>>

    suspend fun setClass(
        grain: String,
        classificationId: Int,
        totalWeight: Float,
        otherColors: Float
    ): ColorClassificationSoja

    // ✅ ATUALIZADO: classificationId agora é Int? (Anulável) para evitar crashes
    suspend fun setDisqualification(
        classificationId: Int?,
        badConservation: Int,
        graveDefectSum: Int,
        strangeSmell: Int,
        toxicGrains: Int,
        insects: Int
    ): Long

    suspend fun setLimit(
        grain: String,
        group: Int,
        type: Int,
        impurities: Float,
        moisture: Float,
        brokenCrackedDamaged: Float,
        greenish: Float,
        burnt: Float,
        burntOrSour: Float,
        moldy: Float,
        spoiled: Float
    ): Long

    suspend fun getLastLimitSource(): Int
    suspend fun getLastColorClass(): ColorClassificationSoja?
    suspend fun getDisqualificationByClassificationId(idClassification: Int): DisqualificationSoja?
    suspend fun updateDisqualification(classificationId: Int, finalType: Int)
    suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float>
    suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitSoja?

    //  Garantir que o parâmetro opcional seja aceito
    suspend fun getObservations(idClassification: Int, colorClass: ColorClassificationSoja? = null): String
}