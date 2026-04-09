package com.example.centreinar.ui.discount.strategy

import android.content.Context
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.domain.model.LimitField

interface GrainDiscountStrategy {

    val descriptor: GrainDescriptor

    val grainName: String get() = descriptor.name

    /**
     * Declara os campos de limite de tolerância que este grão utiliza,
     * na ordem em que devem aparecer na tela.
     *
     * Mesmo contrato de GrainStrategy.getLimitFields() — a LimitInputScreen
     * é genérica e usa esta lista independente do fluxo (classificação ou desconto).
     */
    fun getLimitFields(): List<LimitField>

    fun getDiscountInputRows(
        defectsPayload: DiscountDefectsPayload,
        financial: FinancialDiscountPayload
    ): List<DiscountInputRow>

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
    suspend fun getLimitsForDisplay(group: Int, isOfficial: Boolean): List<Any>
    suspend fun saveCustomLimitData(group: Int, fieldMap: Map<String, Float>)

    suspend fun exportDiscountToPdf(context: Context, sourceClassificationId: Int? = null)
}