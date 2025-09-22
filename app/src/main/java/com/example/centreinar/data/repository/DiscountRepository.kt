package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationSoja
import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja

interface DiscountRepository {

    suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountSoja,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long

    suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountSoja,
        limit: Map<String, Float>,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long

    suspend fun calculateTechnicalLoss(
        storageDays: Int,
        humidityAndImpuritiesLoss: Float,
        lotWeight: Float
    ): Float

    suspend fun calculateDeduction(
        deductionValue: Float,
        classificationLoss: Float
    ): Float

    suspend fun getDiscountById(id: Long): DiscountSoja?

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
        brokenCrackedDamaged: Float,
        greenish: Float,
        burnt: Float,
        burntOrSour: Float,
        moldy: Float,
        spoiled: Float
    ): Long

    suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitSoja

    suspend fun getLimitOfType1Official(
        group: Int,
        grain: String
    ): Map<String, Float>

    suspend fun getLastClassification(): ClassificationSoja

    suspend fun toInputDiscount(
        priceBySack: Float,
        classification: ClassificationSoja,
        daysOfStorage: Int,
        deductionValue: Float
    ): InputDiscountSoja

    suspend fun getDiscountForClassification(
        priceBySack: Float,
        daysOfStorage: Int,
        deductionValue: Float
    ): DiscountSoja?

    suspend fun getLastLimitSource(): Int

    suspend fun setInputDiscount(inputDiscount: InputDiscountSoja): Long

    suspend fun getLastInputDiscount(): InputDiscountSoja

    suspend fun getSampleById(id: Int): SampleSoja?
}
