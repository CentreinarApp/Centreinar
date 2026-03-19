package com.example.centreinar.ui.discount.strategy

data class DiscountInputField(
    val key: String,
    val label: String,
    val value: String = "",
    val isReadOnly: Boolean = false,
    val tabGroup: Int = 0
)

// Linha da tabela de dados de entrada usados no cálculo do desconto
// Exibida na DiscountResultsScreen acima da OfficialReferenceTable
data class DiscountInputRow(
    val label: String,
    val value: String  // formatado com unidade, ex: "14.50 %" ou "500.00 kg"
)

data class DiscountUIState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val limitInputFields: List<DiscountInputField> = emptyList(),
    val officialTableColumnsCount: Int = 0,
    val officialTableRows: List<Pair<String, List<Float>>> = emptyList(),
    val discountInputRows: List<DiscountInputRow> = emptyList()
)

data class FinancialDiscountPayload(
    val priceBySack: Float,
    val lotWeight: Float,
    val group: Int,
    val daysOfStorage: Int,
    val doesTechnicalLoss: Boolean,
    val deductionValue: Float,
    val doesDeduction: Boolean,
    val sourceClassificationId: Int? = null
)

sealed class DiscountDefectsPayload {
    data class Soja(
        val moisture: Float,
        val impurities: Float,
        val greenish: Float,
        val broken: Float,
        val spoiled: Float,
        val burnt: Float,
        val burntOrSour: Float,
        val moldy: Float
    ) : DiscountDefectsPayload()

    data class Milho(
        val moisture: Float,
        val impurities: Float,
        val broken: Float,
        val ardido: Float,
        val carunchado: Float,
        val spoiled: Float,
        val mofados: Float,
        val fermented: Float,
        val germinated: Float,
        val gessado: Float
    ) : DiscountDefectsPayload()
}

// Modelo genérico de resultado
data class DiscountResultRow(
    val label: String,
    val massKg: Float,
    val valueRS: Float
)

data class DiscountResult(
    val summaryRows: List<DiscountResultRow>,
    val detailRows: List<DiscountResultRow>,
    val limitRows: List<Pair<String, List<Float>>> = emptyList(),
    val limitHeaders: List<String> = emptyList()
)