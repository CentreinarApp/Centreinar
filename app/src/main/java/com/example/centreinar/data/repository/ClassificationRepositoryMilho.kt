package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
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
     * 🟢 Retorna uma classificação específica pelo ID.
     * (usada após a inserção para buscar os dados completos)
     */
    suspend fun getClassification(id: Int): ClassificationMilho?

    /**
     * 🟢 Retorna o último valor de limitSource salvo na tabela de limites.
     * (usada para definir a origem dos limites quando não for oficial)
     */
    suspend fun getLastLimitSource(): Int
}
