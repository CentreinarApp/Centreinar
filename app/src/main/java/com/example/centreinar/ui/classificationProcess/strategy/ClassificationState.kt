package com.example.centreinar.ui.classificationProcess.strategy

import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entity.SampleMilho

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
    val tableRows: List<ClassificationRow> = emptyList()
)

data class ComplementaryCardData(
    val title: String,
    val subtitle: String,
    val colorType: String // "primary", "secondary", "tertiary"
)

// Empacota os dados para salvar um limite manual
data class CustomLimitPayload(
    val group: Int,
    val impurities: Float,
    val moisture: Float,
    val brokenCrackedDamaged: Float,
    val greenish: Float = 0f,       // Específico Soja
    val burnt: Float = 0f,          // Específico Soja
    val burntOrSour: Float,
    val moldy: Float,
    val spoiled: Float,
    val carunchado: Float = 0f      // Específico Milho
)

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

