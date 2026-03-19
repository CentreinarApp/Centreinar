package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.*
import com.example.centreinar.data.local.entity.*
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.Utilities
import com.example.centreinar.util.roundToTwoDecimals
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationRepositoryMilhoImpl @Inject constructor(
    private val limitDao: LimitMilhoDao,
    private val classificationDao: ClassificationMilhoDao,
    private val colorClassificationDao: ColorClassificationMilhoDao,
    private val sampleDao: SampleMilhoDao,
    private val disqualificationMilhoDao: DisqualificationMilhoDao,
    private val toxicSeedMilhoDao: ToxicSeedMilhoDao,
    private val tools: Utilities
) : ClassificationRepositoryMilho {

    override suspend fun getLimitsByGroup(grain: String, group: Int, source: Int): List<LimitMilho> {
        return limitDao.getLimitsBySource(grain, source, group)
    }

    override suspend fun getLastClassificationId(grain: String): Int? {
        return classificationDao.getLastIdByGrain(grain)
    }

    override suspend fun classifySample(sample: SampleMilho, limitSource: Int, lastDisq: DisqualificationMilho): Long {
        val limitsList = limitDao.getLimitsBySource(
            grain = sample.grain,
            limitSource = limitSource,
            group = sample.group
        )

        if (limitsList.isNullOrEmpty()) throw IllegalStateException("Limites não encontrados para Milho.")

        val l1 = limitsList.find { it.type == 1 }
        val l2 = limitsList.find { it.type == 2 }
        val l3 = limitsList.find { it.type == 3 }

        if (l1 == null || l2 == null || l3 == null)
            throw IllegalStateException("Tabela de limites incompleta (precisa de Tipo 1, 2 e 3).")

        val cleanWeight = if (sample.cleanWeight > 0f) sample.cleanWeight
        else (sample.sampleWeight - (sample.impurities + sample.broken))

        val sumSpoiledTotalWeights = sample.ardido + sample.mofado + sample.fermented +
                sample.germinated + sample.immature + sample.gessado

        val pImpurities  = tools.calculateDefectPercentage(sample.impurities,  sample.sampleWeight).roundToTwoDecimals()
        val pBroken      = tools.calculateDefectPercentage(sample.broken,       sample.sampleWeight).roundToTwoDecimals()
        val pArdido      = tools.calculateDefectPercentage(sample.ardido,       cleanWeight).roundToTwoDecimals()
        val pMofado      = tools.calculateDefectPercentage(sample.mofado,       cleanWeight).roundToTwoDecimals()
        val pCarunchado  = tools.calculateDefectPercentage(sample.carunchado,   cleanWeight).roundToTwoDecimals()
        val pFermented   = tools.calculateDefectPercentage(sample.fermented,    cleanWeight).roundToTwoDecimals()
        val pGerminated  = tools.calculateDefectPercentage(sample.germinated,   cleanWeight).roundToTwoDecimals()
        val pImmature    = tools.calculateDefectPercentage(sample.immature,     cleanWeight).roundToTwoDecimals()
        val pGessado     = tools.calculateDefectPercentage(sample.gessado,      cleanWeight).roundToTwoDecimals()
        val pSpoiledTotal = tools.calculateDefectPercentage(sumSpoiledTotalWeights, cleanWeight).roundToTwoDecimals()

        fun getIndividualType(value: Float, limit1: Float, limit2: Float, limit3: Float): Int = when {
            value <= limit1 -> 1
            value <= limit2 -> 2
            value <= limit3 -> 3
            else            -> 7
        }

        val typeImpurities   = getIndividualType(pImpurities,   l1.impuritiesUpLim,   l2.impuritiesUpLim,   l3.impuritiesUpLim)
        val typeBroken       = getIndividualType(pBroken,        l1.brokenUpLim,        l2.brokenUpLim,        l3.brokenUpLim)
        val typeArdido       = getIndividualType(pArdido,        l1.ardidoUpLim,        l2.ardidoUpLim,        l3.ardidoUpLim)
        val typeCarunchado   = getIndividualType(pCarunchado,    l1.carunchadoUpLim,    l2.carunchadoUpLim,    l3.carunchadoUpLim)
        val typeSpoiledTotal = getIndividualType(pSpoiledTotal,  l1.spoiledTotalUpLim,  l2.spoiledTotalUpLim,  l3.spoiledTotalUpLim)

        val finalType = listOf(typeImpurities, typeBroken, typeArdido, typeCarunchado, typeSpoiledTotal).maxOrNull() ?: 1

        var isDisqualify = lastDisq.badConservation == 1 || lastDisq.strangeSmell == 1 ||
                lastDisq.insects == 1 || lastDisq.toxicGrains == 1

        val sampleId = sampleDao.insert(sample)

        val classification = ClassificationMilho(
            sampleId              = sampleId.toInt(),
            grain                 = sample.grain,
            group                 = sample.group,
            moisturePercentage    = sample.moisture,
            impuritiesPercentage  = pImpurities,
            brokenPercentage      = pBroken,
            ardidoPercentage      = pArdido,
            mofadoPercentage      = pMofado,
            carunchadoPercentage  = pCarunchado,
            fermentedPercentage   = pFermented,
            germinatedPercentage  = pGerminated,
            immaturePercentage    = pImmature,
            gessadoPercentage     = pGessado,
            spoiledTotalPercentage = pSpoiledTotal,
            impuritiesType        = typeImpurities,
            brokenType            = typeBroken,
            ardidoType            = typeArdido,
            carunchadoType        = typeCarunchado,
            spoiledTotalType      = typeSpoiledTotal,
            mofadoType            = -1,
            fermentedType         = -1,
            germinatedType        = -1,
            immatureType          = -1,
            gessadoType           = -1,
            finalType             = finalType,
            isDisqualified        = isDisqualify
        )

        return classificationDao.insert(classification)
    }

    override suspend fun getSample(id: Int): SampleMilho? = sampleDao.getById(id)

    override suspend fun setSample(
        grain: String, group: Int, sampleWeight: Float, broken: Float, impurities: Float,
        carunchado: Float, ardido: Float, mofado: Float, fermented: Float,
        germinated: Float, immature: Float, gessado: Float
    ): SampleMilho {
        val sample = SampleMilho(
            grain = grain, group = group, lotWeight = 0f, sampleWeight = sampleWeight,
            cleanWeight = sampleWeight, impurities = impurities, broken = broken,
            carunchado = carunchado, ardido = ardido, mofado = mofado, fermented = fermented,
            germinated = germinated, immature = immature, gessado = gessado
        )
        sampleDao.insert(sample)
        return sample
    }

    override suspend fun setSample(sample: SampleMilho): Long = sampleDao.insert(sample)

    override suspend fun getClassification(id: Int): ClassificationMilho? = classificationDao.getById(id)

    override suspend fun getLastLimitSource(): Int {
        return try { limitDao.getLastSource() } catch (e: Exception) { 0 }
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitMilho? {
        return limitDao.getLimitsByType(grain, group, tipo, source).firstOrNull()
    }

    // -------------------------------------------------------------------------
    // Retorna os limites do Tipo 1 oficial usando FieldKeys.* como chaves.
    // A LimitInputScreen lê essas chaves para preencher os campos de prefill.
    // -------------------------------------------------------------------------
    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain, group, 1, 0).firstOrNull()
            ?: return emptyMap()
        return mapOf(
            FieldKeys.MOISTURE   to limit.moistureUpLim,
            FieldKeys.IMPURITIES to limit.impuritiesUpLim,
            FieldKeys.BROKEN     to limit.brokenUpLim,
            FieldKeys.ARDIDO     to limit.ardidoUpLim,
            FieldKeys.MOLDY      to limit.mofadoUpLim,
            FieldKeys.CARUNCHADO to limit.carunchadoUpLim,
            FieldKeys.SPOILED    to limit.spoiledTotalUpLim
        )
    }

    override suspend fun setLimit(
        grain: String, group: Int, tipo: Int, impurities: Float, moisture: Float,
        broken: Float, ardido: Float, mofado: Float, spoiledTotal: Float, carunchado: Float
    ) {
        val lastSource = getLastLimitSource()
        val newSource  = if (lastSource == 0) 1 else lastSource + 1

        val tiposParaSalvar = if (tipo != 0) listOf(tipo) else listOf(1, 2, 3)

        tiposParaSalvar.forEach { t ->
            val limit = LimitMilho(
                source = newSource, grain = grain, group = group, type = t,
                impuritiesUpLim  = impurities,
                moistureUpLim    = moisture,
                brokenUpLim      = broken,
                ardidoUpLim      = ardido,
                mofadoUpLim      = mofado,
                spoiledTotalUpLim = spoiledTotal,
                carunchadoUpLim  = carunchado
            )
            limitDao.insertLimit(limit)
        }
    }

    override suspend fun deleteCustomLimits() {
        limitDao.deleteCustomLimits()
    }

    override suspend fun insertColorClassificationMilho(colorEntity: ColorClassificationMilho) {
        colorClassificationDao.insert(colorEntity)
    }

    override suspend fun getLastColorClassMilho(): ColorClassificationMilho? {
        return colorClassificationDao.getLastColorClass()
    }

    override suspend fun getLastDisqualificationId(): Int? {
        return disqualificationMilhoDao.getLastDisqualificationId()
    }

    override suspend fun insertDisqualification(disqualification: DisqualificationMilho): Long {
        return disqualificationMilhoDao.insert(disqualification)
    }

    override suspend fun getLastDisqualification(): DisqualificationMilho? {
        return disqualificationMilhoDao.getLastDisqualification()
    }

    override suspend fun updateClassificationIdOnDisqualification(disqualificationId: Int, classificationId: Int) {
        disqualificationMilhoDao.updateClassificationId(disqualificationId, classificationId)
    }

    override suspend fun getDisqualificationByClassificationId(classificationId: Int): DisqualificationMilho? {
        return disqualificationMilhoDao.getByClassificationId(classificationId)
    }

    override suspend fun getToxicSeedsByDisqualificationId(disqualificationId: Int): List<ToxicSeedMilho> {
        return toxicSeedMilhoDao.getToxicSeedsByDisqualificationId(disqualificationId)
    }

    override suspend fun insertToxicSeeds(seeds: List<ToxicSeedMilho>) {
        toxicSeedMilhoDao.insertAll(seeds)
    }

    override suspend fun getColorClassification(classifId: Long): ColorClassificationMilho? {
        return colorClassificationDao.getByClassificationId(classifId)
    }
}