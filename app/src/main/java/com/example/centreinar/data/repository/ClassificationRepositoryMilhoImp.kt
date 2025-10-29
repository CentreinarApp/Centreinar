package com.example.centreinar.data.repository

import android.util.Log // Adicionar import de Log
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.ClassificationMilhoDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.SampleMilhoDao
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.util.Utilities
import com.example.centreinar.data.repository.ClassificationRepositoryMilho
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationRepositoryMilhoImpl @Inject constructor(
    private val limitDao: LimitMilhoDao,
    private val classificationDao: ClassificationMilhoDao,
    private val sampleDao: SampleMilhoDao,
    private val tools: Utilities
) : ClassificationRepositoryMilho {
    /**
     * Classifica uma amostra de milho com base nos limites e defeitos calculados.
     * Agora com verificação segura para evitar crash quando limites não existem.
     */
    override suspend fun classifySample(sample: SampleMilho, limitSource: Int): Long {
        val limitsList = limitDao.getLimitsBySource(sample.grain, sample.group, limitSource)

        // 🚨 VERIFICAÇÃO CRÍTICA DE SEGURANÇA
        if (limitsList.isNullOrEmpty()) {
            Log.e(
                "ClassificationRepoMilho",
                "Nenhum limite encontrado para Milho: grain=${sample.grain}, group=${sample.group}, source=$limitSource"
            )
            throw IllegalStateException(
                "Não foram encontrados limites de classificação para o grão ${sample.grain} (grupo ${sample.group})."
            )
        }

        val limit = limitsList.first()

        val cleanWeight = if (sample.cleanWeight > 0f) sample.cleanWeight else sample.sampleWeight

        val percentageImpurities = tools.calculateDefectPercentage(sample.impurities, cleanWeight)
        val percentageBroken = tools.calculateDefectPercentage(sample.broken, cleanWeight)
        val percentageArdido = tools.calculateDefectPercentage(sample.ardido, cleanWeight)
        val percentageMofado = tools.calculateDefectPercentage(sample.mofado, cleanWeight)
        val percentageCarunchado = tools.calculateDefectPercentage(sample.carunchado, cleanWeight)

        val finalType = tools.defineFinalTypeMilho(
            impurities = percentageImpurities,
            broken = percentageBroken,
            ardido = percentageArdido,
            mofado = percentageMofado,
            carunchado = percentageCarunchado,
            limits = limitsList
        )

        val anyExceeds = (
                percentageImpurities > limit.impuritiesUpLim ||
                        percentageBroken > limit.brokenUpLim ||
                        percentageArdido > limit.ardidoUpLim ||
                        percentageMofado > limit.mofadoUpLim ||
                        percentageCarunchado > limit.carunchadoUpLim
                )

        val computedFinalType = if (anyExceeds) 0 else finalType

        val classification = ClassificationMilho(
            grain = sample.grain,
            group = sample.group,
            sampleId = sample.id,
            impuritiesPercentage = percentageImpurities,
            brokenPercentage = percentageBroken,
            carunchadoPercentage = percentageCarunchado,
            ardidoPercentage = percentageArdido,
            mofadoPercentage = percentageMofado,
            fermentedPercentage = sample.fermented,
            germinatedPercentage = sample.germinated,
            immaturePercentage = sample.immature,
            gessadoPercentage = sample.gessado,
            finalType = computedFinalType
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

    // 🔹 Retorna a classificação existente
    override suspend fun getClassification(id: Int): ClassificationMilho? {
        return classificationDao.getById(id)
    }

    // 🔹 Busca o último source de limite (0 = oficial, 1 = personalizado, etc.)
    override suspend fun getLastLimitSource(): Int {
        return try {
            limitDao.getLastSource()
        } catch (e: Exception) {
            0
        }
    }

    // 🔹 Busca um limite específico
    override suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitMilho? {
        return limitDao.getLimitsBySource(grain, group, source).firstOrNull()
    }

    // 🔹 Busca limites oficiais (source = 0)
    override suspend fun getLimitOfType1Official(
        group: Int,
        grain: String
    ): Map<String, Float> {
        val limit: LimitMilho? = limitDao.getLimitsBySource(grain, group, 0).firstOrNull()

        return if (limit != null) {
            mapOf(
                "impuritiesUpLim" to limit.impuritiesUpLim,
                "moistureUpLim" to limit.moistureUpLim,
                "brokenUpLim" to limit.brokenUpLim,
                "ardidoUpLim" to limit.ardidoUpLim,
                "mofadoUpLim" to limit.mofadoUpLim,
                "carunchadoUpLim" to limit.carunchadoUpLim
            )
        } else {
            Log.w(
                "RepoMilho",
                "Limites oficiais Milho (Source 0) não encontrados para Grão: $grain, Grupo: $group. Retornando mapa vazio."
            )
            emptyMap()
        }
    }
}
