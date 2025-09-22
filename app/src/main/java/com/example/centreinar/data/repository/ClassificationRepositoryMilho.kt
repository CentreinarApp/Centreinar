package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.SampleMilho

interface ClassificationRepositoryMilho {
    suspend fun classifySample(sample: SampleMilho, limitSource: Int): Long
    suspend fun getSample(id: Int): SampleMilho?
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
}
