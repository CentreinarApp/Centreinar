package com.example.centreinar.data.repository

import android.util.Log
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.ClassificationMilhoDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.SampleMilhoDao
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationRepositoryMilhoImpl @Inject constructor(
    private val limitDao: LimitMilhoDao,
    private val classificationDao: ClassificationMilhoDao,
    private val sampleDao: SampleMilhoDao,
    private val tools: Utilities
) : ClassificationRepositoryMilho {
    override suspend fun classifySample(sample: SampleMilho, limitSource: Int): Long {
        // Busca os limites no banco (Retorna uma lista com Limite Tipo 1, Tipo 2 e Tipo 3)
        val limitsList = limitDao.getLimitsBySource(
            grain = sample.grain,
            limitSource = limitSource,
            group = sample.group
        )

        if (limitsList.isNullOrEmpty()) {
            throw IllegalStateException("Limites não encontrados para Milho.")
        }

        // Separa os objetos de limite para facilitar a comparação
        val l1 = limitsList.find { it.type == 1 }
        val l2 = limitsList.find { it.type == 2 }
        val l3 = limitsList.find { it.type == 3 }

        // Se faltar algum limite na tabela, usamos o que tem ou padrão alto
        if (l1 == null || l2 == null || l3 == null) {
            throw IllegalStateException("Tabela de limites incompleta (precisa de Tipo 1, 2 e 3).")
        }

        // Cálculos de Porcentagem
        val cleanWeight = if (sample.cleanWeight > 0f) sample.cleanWeight else (sample.sampleWeight - sample.impurities)

        val pImpurities = tools.calculateDefectPercentage(sample.impurities, cleanWeight)
        val pBroken = tools.calculateDefectPercentage(sample.broken, cleanWeight)
        val pArdido = tools.calculateDefectPercentage(sample.ardido, cleanWeight)
        val pMofado = tools.calculateDefectPercentage(sample.mofado, cleanWeight)
        val pCarunchado = tools.calculateDefectPercentage(sample.carunchado, cleanWeight)

        val pFermented = tools.calculateDefectPercentage(sample.fermented, cleanWeight)
        val pGerminated = tools.calculateDefectPercentage(sample.germinated, cleanWeight)
        val pImmature = tools.calculateDefectPercentage(sample.immature, cleanWeight)
        val pGessado = tools.calculateDefectPercentage(sample.gessado, cleanWeight)

        // Total Avariados
        val pSpoiledTotal = pArdido + pMofado + pFermented + pGerminated + pImmature + pGessado

        // DEFINIÇÃO DOS TIPOS INDIVIDUAIS ---
        // Função local para determinar o tipo baseado nos 3 limites
        fun getIndividualType(value: Float, limit1: Float, limit2: Float, limit3: Float): Int {
            return when {
                value <= limit1 -> 1
                value <= limit2 -> 2
                value <= limit3 -> 3
                else -> 7 // Fora de Tipo
            }
        }

        // Guarda os tipos dos defeitos...
        val typeImpurities = getIndividualType(pImpurities, l1.impuritiesUpLim, l2.impuritiesUpLim, l3.impuritiesUpLim)
        val typeBroken = getIndividualType(pBroken, l1.brokenUpLim, l2.brokenUpLim, l3.brokenUpLim)
        val typeArdido = getIndividualType(pArdido, l1.ardidoUpLim, l2.ardidoUpLim, l3.ardidoUpLim)
        val typeCarunchado = getIndividualType(pCarunchado, l1.carunchadoUpLim, l2.carunchadoUpLim, l3.carunchadoUpLim)
        val typeSpoiledTotal = getIndividualType(pSpoiledTotal, l1.spoiledTotalUpLim, l2.spoiledTotalUpLim, l3.spoiledTotalUpLim)

        // Define o Tipo Final (O maior entre eles)
        val allTypes = listOf(typeImpurities, typeBroken, typeArdido, typeCarunchado, typeSpoiledTotal)
        var finalType = allTypes.maxOrNull() ?: 1

        // Salva no Banco
        val sampleId = sampleDao.insert(sample) // Long

        val classification = ClassificationMilho(
            sampleId =  sampleId.toInt(),
            grain = sample.grain,
            group = sample.group,

            // Valores Float
            moisturePercentage = 0f, // TODO: Ver como fica a umidade na tabela (!!!MUDAR!!!)
            impuritiesPercentage = pImpurities,
            brokenPercentage = pBroken,
            ardidoPercentage = pArdido,
            mofadoPercentage = pMofado,
            carunchadoPercentage = pCarunchado,
            fermentedPercentage = pFermented,
            germinatedPercentage = pGerminated,
            immaturePercentage = pImmature,
            gessadoPercentage = pGessado,
            spoiledTotalPercentage = pSpoiledTotal,

            // --- Salvando os valores dos tipos de defeito ---
            impuritiesType = typeImpurities,
            brokenType = typeBroken,
            ardidoType = typeArdido,
            carunchadoType = typeCarunchado,
            spoiledTotalType = typeSpoiledTotal,

            finalType = finalType
        )

        return classificationDao.insert(classification)
    }

    override suspend fun getSample(id: Int): SampleMilho? = sampleDao.getById(id)

    override suspend fun setSample(
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
    ): SampleMilho {
        val sample = SampleMilho(
            grain = grain,
            group = group,
            lotWeight = 0f,
            sampleWeight = sampleWeight,
            cleanWeight = sampleWeight,
            impurities = impurities,
            broken = broken,
            carunchado = carunchado,
            ardido = ardido,
            mofado = mofado,
            fermented = fermented,
            germinated = germinated,
            immature = immature,
            gessado = gessado
        )
        sampleDao.insert(sample)
        return sample
    }

    override suspend fun setSample(sample: SampleMilho): Long = sampleDao.insert(sample)

    override suspend fun getClassification(id: Int): ClassificationMilho? {
        return classificationDao.getById(id)
    }

    override suspend fun getLastLimitSource(): Int {
        return try {
            limitDao.getLastSource()
        } catch (e: Exception) {
            0
        }
    }

    override suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitMilho? {
        val limits = limitDao.getLimitsByType(grain, group, tipo, source)
        return limits.firstOrNull()
    }

    override suspend fun getLimitOfType1Official(
        group: Int,
        grain: String
    ): Map<String, Float> {
        val limit: LimitMilho? = limitDao.getLimitsByType(
            grain = grain,
            group = group,
            tipo = 1, // Força Tipo 1
            limitSource = 0 // Força Oficial
        ).firstOrNull()

        return if (limit != null) {
            mapOf(
                "impuritiesUpLim" to limit.impuritiesUpLim,
                "moistureUpLim" to limit.moistureUpLim,
                "brokenUpLim" to limit.brokenUpLim,
                "ardidoUpLim" to limit.ardidoUpLim,
                "mofadoUpLim" to limit.mofadoUpLim,
                "carunchadoUpLim" to limit.carunchadoUpLim,
                "spoiledTotalUpLim" to limit.spoiledTotalUpLim
            )
        } else {
            Log.w(
                "RepoMilho",
                "Limites oficiais Milho (Source 0) não encontrados para Grão: $grain, Grupo: $group."
            )
            emptyMap()
        }
    }
}