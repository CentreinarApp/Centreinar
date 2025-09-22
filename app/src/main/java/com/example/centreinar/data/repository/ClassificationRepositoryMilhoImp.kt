package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.ClassificationMilhoDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.SampleMilhoDao
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.domain.repository.ClassificationRepositoryMilho
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
        val limitsList = limitDao.getLimitsBySource(sample.grain, sample.group, limitSource)
        val limit = limitsList.firstOrNull() ?: throw Exception("Limites não encontrados para este sample")

        val cleanWeight = sample.sampleWeight

        // percentuais (usa Utilities que você já tem)
        val percentageImpurities = tools.calculateDefectPercentage(sample.impurities, cleanWeight)
        val percentageBroken = tools.calculateDefectPercentage(sample.broken, cleanWeight)
        val percentageArdido = tools.calculateDefectPercentage(sample.ardido, cleanWeight)
        val percentageMofado = tools.calculateDefectPercentage(sample.mofado, cleanWeight)
        val percentageCarunchado = tools.calculateDefectPercentage(sample.carunchado, cleanWeight)

        // lógica simples para finalType: se passar qualquer limite => desclassificado (0), senão tipo 1.
        // Você pode substituir por regra mais complexa depois.
        val finalType = if (
            percentageImpurities > limit.impuritiesUpLim ||
            percentageBroken > limit.brokenUpLim ||
            percentageArdido > limit.ardidoUpLim ||
            percentageMofado > limit.mofadoUpLim ||
            percentageCarunchado > limit.carunchadoUpLim
        ) 0 else 1

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
            finalType = finalType
        )

        return classificationDao.insert(classification)
    }

    override suspend fun getSample(id: Int): SampleMilho? {
        return sampleDao.getById(id)
    }

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
            sampleWeight = sampleWeight,
            lotWeight = 0f,
            cleanWeight = sampleWeight,
            broken = broken,
            impurities = impurities,
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
}





