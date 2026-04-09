package com.example.centreinar.ui.classificationProcess.strategy

import android.content.Context
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.domain.model.ClassificationInputField
import com.example.centreinar.domain.model.LimitField

data class StrategyResultData(
    val classification: BaseClassification?,
    val disqualification: BaseDisqualification?,
    val limit: BaseLimit?,
    val tableRows: List<ClassificationRow>,
    val toxicSeeds: List<Pair<String, Int>>,
    val cards: List<ComplementaryCardData>,
    val sampleLotWeight: Float = 0f
)

interface GrainStrategy {
    // Descreve o grão para a UI (nome, cor, flags de seções visíveis).
    val descriptor: GrainDescriptor
    val grainName: String get() = descriptor.name
    val cleanWeightLabel: String

    // -------------------------------------------------------------------------
    // Factory de Payload
    //
    // A tela coleta os campos em ClassificationInputState e chama buildPayload().
    // A strategy monta o ClassificationPayload concreto (Soja ou Milho).
    // A tela nunca importa SampleSoja, SampleMilho ou ClassificationPayload.*
    // -------------------------------------------------------------------------

    /**
     * Retorna as linhas da tabela de dados de entrada da amostra.
     * Cada strategy retorna os campos relevantes para o seu grão
     * (pesos em gramas + defeitos em gramas).
     * A tela exibe sem saber qual grão é.
     */
    fun getSampleInputRows(state: ClassificationUIState): List<SampleInputRow>

    fun buildPayload(state: ClassificationInputState): ClassificationPayload

    // -------------------------------------------------------------------------
    // Limites
    // -------------------------------------------------------------------------

    /**
     * Retorna os campos editáveis de limite para este grão, na ordem de exibição.
     */
    /**
     * Retorna os campos de input para a tela de classificação, agrupados por aba.
     * A tela genérica renderiza dinamicamente — sem hardcode de campos por grão.
     */
    fun getClassificationInputFields(): List<ClassificationInputField>

    fun getLimitFields(): List<LimitField>

    suspend fun getOfficialLimits(group: Int): List<Any>

    suspend fun getBaseLimits(group: Int): Map<String, Float>?

    // -------------------------------------------------------------------------
    // Classificação
    // -------------------------------------------------------------------------

    suspend fun getUiStateData(id: Int): StrategyResultData

    suspend fun classify(payload: ClassificationPayload, isOfficial: Boolean): ClassificationUIState

    suspend fun saveDisqualificationData(
        classificationId: Int,
        badConservation: Int,
        strangeSmell: Int,
        insects: Int,
        toxicGrains: Int,
        toxicSeeds: List<Pair<String, String>>
    )

    suspend fun exportClassificationToPdf(
        context: Context,
        state: ClassificationUIState,
        limits: List<Any>,
        observation: String?,
        isOfficial: Boolean
    )

    suspend fun setCustomLimit(payload: CustomLimitPayload)

    suspend fun deleteCustomLimits()

    fun getTypeLabel(finalType: Int, group: Int): String

    // -------------------------------------------------------------------------
    // Opcionais com implementação padrão
    // -------------------------------------------------------------------------

    /** Apenas grãos que têm classificação por cor precisam sobrescrever. */
    suspend fun saveColorClass(
        classificationId: Int,
        totalWeight: Float,
        otherColorsWeight: Float
    ) {}

    fun calculateDisplayCleanWeight(
        sampleWeight: Float,
        foreignMatters: Float,
        fieldValues: Map<String, Float>
    ): Float = (sampleWeight - foreignMatters).coerceAtLeast(0f)
}