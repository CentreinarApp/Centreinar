package com.example.centreinar.domain.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.*

interface DiscountRepositoryMilho {

    suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountMilho,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long

    suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountMilho,
        limit: Map<String, Float>,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long

    suspend fun calculateTechnicalLoss(
        storageDays: Int,
        impuritiesLoss: Float,
        lotWeight: Float
    ): Float

    suspend fun calculateDeduction(
        deductionValue: Float,
        classificationLoss: Float
    ): Float

    suspend fun getDiscountById(id: Long): DiscountMilho?

    // FUNÇÃO QUE O COMPILADOR ESTÁ RECLAMANDO (Contrato)
    suspend fun getLimitsByType(
        grain: String,
        group: Int,
        tipo: Int,
        limitSource: Int
    ): Map<String, Float> // Mantemos o tipo Map<String, Float> para o cálculo

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

    suspend fun getLastClassification(): ClassificationMilho

    suspend fun toInputDiscount(
        priceBySack: Float,
        classification: ClassificationMilho,
        daysOfStorage: Int,
        deductionValue: Float
    ): InputDiscountMilho

    suspend fun getDiscountForClassification(
        priceBySack: Float,
        daysOfStorage: Int,
        deductionValue: Float
    ): DiscountMilho?

    suspend fun getLastLimitSource(): Int

    suspend fun setInputDiscount(inputDiscount: InputDiscountMilho): Long

    suspend fun getLastInputDiscount(): InputDiscountMilho

    suspend fun getSampleById(id: Int): SampleMilho?
}