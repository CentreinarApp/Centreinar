package com.example.centreinar.domain.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.*

interface DiscountRepositoryMilho {
    suspend fun getClassificationById(id: Int): ClassificationMilho?

    suspend fun insertDiscount(discount: DiscountMilho): Long

    suspend fun getDiscountById(id: Long): DiscountMilho?

    suspend fun setInputDiscount(inputDiscount: InputDiscountMilho): Long

    suspend fun getLastInputDiscount(): InputDiscountMilho

    // Limites
    suspend fun getLimitsByType(
        grain: String,
        group: Int,
        tipo: Int,
        limitSource: Int
    ): Map<String, Float>

    suspend fun setLimit(
        grain: String,
        group: Int,
        type: Int,
        impurities: Float,
        moisture: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float,
        spoiledTotal: Float
    ): Long

    suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitMilho?
    suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float>
    suspend fun getLastLimitSource(): Int

    // Auxiliares
    suspend fun getLastClassification(): ClassificationMilho
    suspend fun toInputDiscount(
        priceBySack: Float,
        classification: ClassificationMilho,
        daysOfStorage: Int,
        deductionValue: Float
    ): InputDiscountMilho
    suspend fun getSampleById(id: Int): SampleMilho?
}