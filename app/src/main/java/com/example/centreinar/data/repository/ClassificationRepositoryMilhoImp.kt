package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.ClassificationMilhoDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.SampleMilhoDao
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
        // Obtém a lista de limites para o grain/group/source solicitado ou milho ou soja
        val limitsList = limitDao.getLimitsBySource(sample.grain, sample.group, limitSource)
        val limit = limitsList.firstOrNull()
            ?: throw Exception("Limites não encontrados para: grain=${sample.grain}, group=${sample.group}, source=$limitSource")

        // Usa cleanWeight baseado na sua entidade (sampleWeight ou cleanWeight)
        // Preferimos usar sample.cleanWeight se estiver preenchido (você preenche cleanWeight ao criar amostra).
        val cleanWeight = if (sample.cleanWeight > 0f) sample.cleanWeight else sample.sampleWeight

        // Calcula percentuais (defeito/cleanWeight * 100) ja fazendo o arredondamento correto
        val percentageImpurities = tools.calculateDefectPercentage(sample.impurities, cleanWeight)
        val percentageBroken = tools.calculateDefectPercentage(sample.broken, cleanWeight)
        val percentageArdido = tools.calculateDefectPercentage(sample.ardido, cleanWeight)
        val percentageMofado = tools.calculateDefectPercentage(sample.mofado, cleanWeight)
        val percentageCarunchado = tools.calculateDefectPercentage(sample.carunchado, cleanWeight)

        // Determina tipo final usando a regra do pior defeito (Utilities.defineFinalTypeMilho)
        val finalType = tools.defineFinalTypeMilho(
            impurities = percentageImpurities,
            broken = percentageBroken,
            ardido = percentageArdido,
            mofado = percentageMofado,
            carunchado = percentageCarunchado,
            limits = limitsList
        )

        // Verifica desclassificação

        val anyExceeds = (percentageImpurities > limit.impuritiesUpLim
                || percentageBroken > limit.brokenUpLim
                || percentageArdido > limit.ardidoUpLim
                || percentageMofado > limit.mofadoUpLim
                || percentageCarunchado > limit.carunchadoUpLim)

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
            fermentedPercentage = sample.fermented, // se você prefere percentual, calcule com tools
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
}
