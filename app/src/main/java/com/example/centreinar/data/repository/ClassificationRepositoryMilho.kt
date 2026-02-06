package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho

interface ClassificationRepositoryMilho {

    /**
     * Classifica uma amostra e retorna o ID da nova classificação.
     */
    suspend fun classifySample(sample: SampleMilho, limitSource: Int): Long

    /**
     * Obtém uma amostra de milho pelo ID.
     */
    suspend fun getSample(id: Int): SampleMilho?

    /**
     * Cria e salva uma nova amostra com os parâmetros fornecidos.
     */
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

    /**
     * Insere uma amostra já criada e retorna o ID gerado.
     */
    suspend fun setSample(sample: SampleMilho): Long

    /**
     * Retorna a classificação pelo ID (para o ViewModel usar após inserir)
     */
    suspend fun getClassification(id: Int): ClassificationMilho?

    /**
     * Retorna o último valor de limitSource salvo na tabela de limites.
     */
    suspend fun getLastLimitSource(): Int

    /**
     * Retorna um limite de milho específico pelo ID, grupo e source.
     */
    suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitMilho? // <-- NOVO MÉTODO NECESSÁRIO

    /**
     * Retorna os limites oficiais (Type 1, Source 0) em formato de mapa.
     */
    suspend fun getLimitOfType1Official(
        group: Int,
        grain: String
    ): Map<String, Float> // <-- NOVO MÉTODO NECESSÁRIO

    suspend fun setLimit(
        grain: String,
        group: Int,
        tipo: Int,
        impurities: Float,
        moisture: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        spoiledTotal: Float,
        carunchado: Float
    )

    suspend fun deleteCustomLimits()
}