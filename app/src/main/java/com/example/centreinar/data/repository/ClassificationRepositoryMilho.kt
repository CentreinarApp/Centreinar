package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.ColorClassificationMilho
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.domain.model.LimitCategory

interface ClassificationRepositoryMilho {
    suspend fun classifySample(sample: SampleMilho, limitSource: Int): Long
    suspend fun getSample(id: Int): SampleMilho?
    suspend fun setSample(
        grain: String,
        group: Int,
        sampleWeight: Float,
        lotWeight: Float,
        impurities: Float,
        humidity: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float
    ): SampleMilho
    suspend fun setSample(sample: SampleMilho): Long
    suspend fun getClassification(id: Int): ClassificationMilho?
    suspend fun getLimitsForGrain(grain: String, group: Int, limitSource: Int): Map<String, List<LimitCategory>>
    suspend fun setClass(grain: String, classificationId: Int, totalWeight: Float, otherColors: Float): ColorClassificationMilho
    suspend fun setDisqualification(
        classificationId: Int,
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
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float
    ): Long
    suspend fun getLastLimitSource(): Int
    suspend fun getLastColorClass(): ColorClassificationMilho?
    suspend fun getDisqualificationByClassificationId(idClassification: Int): DisqualificationMilho?
    suspend fun updateDisqualification(classificationId: Int, finalType: Int)
    suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float>
    suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitMilho
    suspend fun getObservations(idClassification: Int, colorClass: ColorClassificationMilho? = null): String
}
