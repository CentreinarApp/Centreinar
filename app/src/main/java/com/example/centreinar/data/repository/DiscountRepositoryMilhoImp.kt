package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.*
import com.example.centreinar.data.local.entity.*
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscountRepositoryMilhoImpl @Inject constructor(
    private val limitDao: LimitMilhoDao,
    private val classificationDao: ClassificationMilhoDao,
    private val sampleDao: SampleMilhoDao,
    private val discountDao: DiscountMilhoDao,
    private val inputDiscountDao: InputDiscountMilhoDao,
    private val tools: Utilities
) : DiscountRepositoryMilho {

    override suspend fun getClassificationById(id: Int): ClassificationMilho? =
        classificationDao.getById(id)


    override suspend fun insertDiscount(discount: DiscountMilho): Long =
        discountDao.insert(discount)

    override suspend fun getDiscountById(id: Long): DiscountMilho? =
        discountDao.getDiscountById(id.toInt())

    override suspend fun setInputDiscount(inputDiscount: InputDiscountMilho): Long =
        inputDiscountDao.insert(inputDiscount)

    override suspend fun getLastInputDiscount(): InputDiscountMilho =
        inputDiscountDao.getLastInputDiscount()

    // -------------------------------------------------------------------------
    // Limites
    // -------------------------------------------------------------------------

    override suspend fun getLimitsByType(
        grain: String,
        group: Int,
        tipo: Int,
        limitSource: Int
    ): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain, group, tipo, limitSource).firstOrNull()
            ?: return emptyMap()
        return mapOf(
            "impurities"   to limit.impuritiesUpLim,
            "broken"       to limit.brokenUpLim,
            "ardido"       to limit.ardidoUpLim,
            "mofado"       to limit.mofadoUpLim,
            "carunchado"   to limit.carunchadoUpLim,
            "moisture"     to limit.moistureUpLim,
            "spoiledTotal" to limit.spoiledTotalUpLim
        )
    }

    override suspend fun setLimit(
        grain: String, group: Int, type: Int, impurities: Float, moisture: Float,
        broken: Float, ardido: Float, mofado: Float, carunchado: Float, spoiledTotal: Float
    ): Long {
        val lastSource = limitDao.getLastSource()
        val limit = LimitMilho(
            source = lastSource + 1, grain = grain, group = group, type = type,
            impuritiesUpLim = impurities, moistureUpLim = moisture,
            brokenUpLim = broken, ardidoUpLim = ardido, mofadoUpLim = mofado,
            carunchadoUpLim = carunchado, spoiledTotalUpLim = spoiledTotal
        )
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitMilho? =
        limitDao.getLimitsByType(grain, group, tipo, source).firstOrNull()

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain, group, 1, 0).firstOrNull()
            ?: return emptyMap()
        return mapOf(
            "impurities"   to limit.impuritiesUpLim,
            "broken"       to limit.brokenUpLim,
            "ardido"       to limit.ardidoUpLim,
            "mofado"       to limit.mofadoUpLim,
            "carunchado"   to limit.carunchadoUpLim,
            "moisture"     to limit.moistureUpLim
        )
    }

    override suspend fun getLastLimitSource(): Int = limitDao.getLastSource()

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    override suspend fun getLastClassification(): ClassificationMilho =
        classificationDao.getLastClassification()

    override suspend fun toInputDiscount(
        priceBySack: Float,
        classification: ClassificationMilho,
        daysOfStorage: Int,
        deductionValue: Float
    ): InputDiscountMilho {
        val sample    = sampleDao.getById(classification.sampleId)
        val lotWeight = sample?.lotWeight ?: 0f
        return InputDiscountMilho(
            grain            = classification.grain,
            group            = classification.group,
            limitSource      = 0,
            classificationId = classification.id,
            daysOfStorage    = daysOfStorage,
            deductionValue   = deductionValue,
            lotWeight        = lotWeight,
            lotPrice         = lotWeight * priceBySack / 60,
            impurities       = classification.impuritiesPercentage,
            broken           = classification.brokenPercentage,
            ardidos          = classification.ardidoPercentage,
            mofados          = classification.mofadoPercentage,
            carunchado       = classification.carunchadoPercentage,
            spoiled          = classification.spoiledTotalPercentage
        ).also { inputDiscountDao.insert(it) }
    }

    override suspend fun getSampleById(id: Int): SampleMilho? = sampleDao.getById(id)
}