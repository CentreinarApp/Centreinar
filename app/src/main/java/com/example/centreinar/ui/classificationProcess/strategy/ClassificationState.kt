package com.example.centreinar.ui.classificationProcess.strategy

import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.util.FieldKeys

// Linha da tabela de dados de entrada da amostra
// Exibida na ClassificationResultScreen acima da OfficialReferenceTable
data class SampleInputRow(
    val label: String,
    val value: String  // formatado com unidade, ex: "500.00 g" ou "14.50 %"
)

// O estado genérico que o ViewModel vai expor para a Tela (UI)
data class ClassificationUIState(
    val isLoading: Boolean = false,
    val limitUsed: BaseLimit? = null,
    val classification: BaseClassification? = null,
    val disqualification: BaseDisqualification? = null,
    val toxicSeeds: List<Pair<String, Int>> = emptyList(),
    val complementaryCards: List<ComplementaryCardData> = emptyList(),
    val allOfficialLimits: List<Any> = emptyList(),
    val sampleLotWeight: Float = 0f,
    val tableRows: List<ClassificationRow> = emptyList(),
    val sampleInputRows: List<SampleInputRow> = emptyList()
)

data class ComplementaryCardData(
    val title: String,
    val subtitle: String,
    val colorType: String // "primary", "secondary", "tertiary"
)

// -------------------------------------------------------------------------
// Payload de limite customizado.
//
// Usa Map<String, Float> com FieldKeys.* como chaves — as mesmas chaves
// que getLimitOfType1Official() retorna e que LimitInputScreen lê.
//
// Chaves obrigatórias: FieldKeys.MOISTURE, FieldKeys.IMPURITIES,
//                      FieldKeys.BROKEN, FieldKeys.SPOILED, FieldKeys.MOLDY
// Chaves opcionais Soja:  FieldKeys.GREENISH, FieldKeys.BURNT, FieldKeys.BURNT_OR_SOUR
// Chaves opcionais Milho: FieldKeys.ARDIDO, FieldKeys.CARUNCHADO
// -------------------------------------------------------------------------
data class CustomLimitPayload(
    val group: Int,
    val limits: Map<String, Float>
) {
    // Helpers para que as strategies acessem os valores sem hard-code de chave.
    val moisture:     Float get() = limits[FieldKeys.MOISTURE]      ?: 0f
    val impurities:   Float get() = limits[FieldKeys.IMPURITIES]    ?: 0f
    val broken:       Float get() = limits[FieldKeys.BROKEN]        ?: 0f
    val moldy:        Float get() = limits[FieldKeys.MOLDY]         ?: 0f
    val spoiled:      Float get() = limits[FieldKeys.SPOILED]       ?: 0f
    // Soja
    val greenish:     Float get() = limits[FieldKeys.GREENISH]      ?: 0f
    val burnt:        Float get() = limits[FieldKeys.BURNT]         ?: 0f
    val burntOrSour:  Float get() = limits[FieldKeys.BURNT_OR_SOUR] ?: 0f
    // Milho
    val ardido:       Float get() = limits[FieldKeys.ARDIDO]        ?: 0f
    val carunchado:   Float get() = limits[FieldKeys.CARUNCHADO]    ?: 0f
}

// O pacote de dados (Payload) que a Tela vai enviar para o ViewModel
sealed class ClassificationPayload {
    data class Soja(
        val sample: SampleSoja,
        val otherColorsWeight: Float,
        val baseWeightCor: Float,
        val isColorDefined: Boolean
    ) : ClassificationPayload()

    data class Milho(
        val sample: SampleMilho,
        val shouldDefineClass: Boolean,
        val shouldDefineGroup: Boolean,
        val weightYellow: Float,
        val weightWhite: Float,
        val weightMixedColors: Float,
        val weightHard: Float,
        val weightDent: Float,
        val weightSemiHard: Float
    ) : ClassificationPayload()
}