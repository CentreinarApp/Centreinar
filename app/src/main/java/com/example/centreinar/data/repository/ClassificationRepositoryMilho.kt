package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.ColorClassificationMilho
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.local.entity.ToxicSeedMilho

interface ClassificationRepositoryMilho {

    suspend fun classifySample(sample: SampleMilho, limitSource: Int, lastDisq: DisqualificationMilho): Long

    suspend fun getSample(id: Int): SampleMilho?

    suspend fun getLastClassificationId(grain: String): Int?

    suspend fun setSample(
        grain: String,
        group: Int,
        sampleWeight: Float,
        broken: Float,
        impurities: Float,
        carunchado: Float,
        ardido: Float,
        mofado: Float,
        fermented: Float,
        germinated: Float,
        immature: Float,
        gessado: Float
    ): SampleMilho

    suspend fun setSample(sample: SampleMilho): Long

    suspend fun getClassification(id: Int): ClassificationMilho?

    suspend fun getLastDisqualification(): DisqualificationMilho?

    suspend fun updateClassificationIdOnDisqualification(
        disqualificationId: Int,
        classificationId: Int
    )

    suspend fun getLastDisqualificationId(): Int?

    suspend fun insertDisqualification(disqualification: DisqualificationMilho): Long

    suspend fun getDisqualificationByClassificationId(classificationId: Int): DisqualificationMilho?

    suspend fun getToxicSeedsByDisqualificationId(disqualificationId: Int): List<ToxicSeedMilho>

    suspend fun insertToxicSeeds(seeds: List<ToxicSeedMilho>)

    suspend fun getLastLimitSource(): Int

    suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitMilho?

    suspend fun getLimitOfType1Official(
        group: Int,
        grain: String
    ): Map<String, Float>

    suspend fun setLimit(
        grain: String,
        group: Int,
        tipo: Int,
        impurities: Float,
        moisture: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        spoiledTotal: Float,
        carunchado: Float
    )

    suspend fun getLimitsByGroup(grain: String, group: Int, source: Int): List<LimitMilho>

    suspend fun deleteCustomLimits()

    suspend fun insertColorClassificationMilho(colorEntity: ColorClassificationMilho)

    suspend fun getLastColorClassMilho(): ColorClassificationMilho?

    suspend fun getColorClassification(classifId: Long): ColorClassificationMilho?
}