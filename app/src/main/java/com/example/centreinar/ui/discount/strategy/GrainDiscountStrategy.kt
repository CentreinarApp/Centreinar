package com.example.centreinar.ui.discount.strategy

import android.content.Context

interface GrainDiscountStrategy {
    val grainName: String

    fun getLimitInputFields(classificationId: Int?): List<DiscountInputField>
    fun getDefectInputFields(): List<DiscountInputField>
    fun createDefectsPayload(fieldValues: Map<String, Float>): DiscountDefectsPayload

    suspend fun calculateDiscount(
        defectsPayload: DiscountDefectsPayload,
        financialPayload: FinancialDiscountPayload,
        isOfficial: Boolean
    ): DiscountResult

    suspend fun getBaseLimits(group: Int): Map<String, Float>?
    suspend fun getOfficialTableData(group: Int): Pair<Int, List<Pair<String, List<Float>>>>
    suspend fun getOfficialLimitsList(group: Int): List<Any>
    suspend fun saveCustomLimitData(group: Int, fieldMap: Map<String, Float>)

    /**
     * Exporta o PDF de desconto.
     * [sourceClassificationId]: quando não-nulo, busca a classificação com esse ID
     * e inclui suas páginas antes da página de descontos.
     * Quando nulo, gera somente a página de descontos.
     */
    suspend fun exportDiscountToPdf(context: Context, sourceClassificationId: Int? = null)
}