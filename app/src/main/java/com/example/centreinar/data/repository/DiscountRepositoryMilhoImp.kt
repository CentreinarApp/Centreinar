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

    override suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountMilho,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        val limit: LimitMilho =
            limitDao.getLimitsBySource(grain, group, sample.limitSource).first()

        val impuritiesLoss = tools.calculateDifference(sample.impurities, limit.impuritiesUpLim)
        val brokenLoss = tools.calculateDifference(sample.broken, limit.brokenUpLim)
        val ardidoLoss = tools.calculateDifference(sample.ardidos, limit.ardidoUpLim)
        val mofadoLoss = tools.calculateDifference(sample.mofados, limit.mofadoUpLim)
        val carunchadoLoss = tools.calculateDifference(sample.carunchado, limit.carunchadoUpLim)

        // desconto total
        val classificationDiscount =
            (impuritiesLoss + brokenLoss + ardidoLoss + mofadoLoss + carunchadoLoss) / 100 * sample.lotWeight

        val finalWeight = sample.lotWeight - classificationDiscount

        val discount = DiscountMilho(
            inputDiscountId = sample.id,
            impuritiesLoss = impuritiesLoss,
            humidityLoss = 0f, // não tem humidity em milho
            technicalLoss = if (doesTechnicalLoss) 0f else 0f, // pode ajustar depois
            brokenLoss = brokenLoss,
            ardidoLoss = ardidoLoss,
            mofadoLoss = mofadoLoss,
            carunchadoLoss = carunchadoLoss, // ✅ AGORA PASSA CERTO
            fermentedLoss = 0f,
            germinatedLoss = 0f,
            gessadoLoss = 0f,
            finalDiscount = classificationDiscount,
            finalWeight = finalWeight
        )

        return discountDao.insert(discount)
    }

    override suspend fun getDiscountById(id: Long): DiscountMilho? {
        return discountDao.getDiscountById(id.toInt())
    }

    override suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitMilho {
        return limitDao.getLimitsBySource(grain, group, source).first()
    }

    override suspend fun getLastClassification(): ClassificationMilho {
        return classificationDao.getLastClassification()
    }

    override suspend fun toInputDiscount(
        priceBySack: Float,
        classification: ClassificationMilho,
        daysOfStorage: Int,
        deductionValue: Float
    ): InputDiscountMilho {
        val sample = sampleDao.getById(classification.sampleId)
        val lotWeight = sample?.lotWeight ?: 0f

        val inputDiscount = InputDiscountMilho(
            grain = classification.grain,
            group = classification.group,
            limitSource = 0,
            classificationId = classification.id,
            daysOfStorage = daysOfStorage,
            deductionValue = deductionValue,
            lotWeight = lotWeight,
            lotPrice = lotWeight * priceBySack / 60,
            impurities = classification.impuritiesPercentage,
            broken = classification.brokenPercentage,
            ardidos = classification.ardidoPercentage,
            mofados = classification.mofadoPercentage,
            carunchado = classification.carunchadoPercentage, // ✅ compatível
        )

        inputDiscountDao.insert(inputDiscount)
        return inputDiscount
    }

    override suspend fun getLimitsByType(
        grain: String,
        group: Int,
        tipo: Int,
        limitSource: Int
    ): Map<String, Float> {
        val limit = limitDao.getLimitsBySource(grain, group, limitSource).firstOrNull()
            ?: return emptyMap()
        return mapOf(
            "impurities" to limit.impuritiesUpLim,
            "broken" to limit.brokenUpLim,
            "ardido" to limit.ardidoUpLim,
            "mofado" to limit.mofadoUpLim,
            "carunchado" to limit.carunchadoUpLim,
            "moisture" to limit.moistureUpLim
        )
    }

    override suspend fun setLimit(
        grain: String,
        group: Int,
        type: Int,
        impurities: Float,
        moisture: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float
    ): Long {
        val limit = LimitMilho(
            source = 0,
            grain = grain,
            group = group,
            type = type,
            impuritiesUpLim = impurities,
            moistureUpLim = moisture,
            brokenUpLim = broken,
            ardidoUpLim = ardido,
            mofadoUpLim = mofado,
            carunchadoUpLim = carunchado
        )
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsBySource(grain, group, 0).firstOrNull() ?: return emptyMap()
        return mapOf(
            "impurities" to limit.impuritiesUpLim,
            "broken" to limit.brokenUpLim,
            "ardido" to limit.ardidoUpLim,
            "mofado" to limit.mofadoUpLim,
            "carunchado" to limit.carunchadoUpLim,
            "moisture" to limit.moistureUpLim
        )
    }

    override suspend fun getDiscountForClassification(
        priceBySack: Float,
        daysOfStorage: Int,
        deductionValue: Float
    ): DiscountMilho? {
        val lastClassification = getLastClassification()
        val inputDiscount =
            toInputDiscount(priceBySack, lastClassification, daysOfStorage, deductionValue)
        return discountDao.getDiscountById(inputDiscount.id)
    }

    override suspend fun getLastLimitSource(): Int {
        return limitDao.getLastSource()
    }

    override suspend fun setInputDiscount(inputDiscount: InputDiscountMilho): Long {
        return inputDiscountDao.insert(inputDiscount)
    }

    override suspend fun getLastInputDiscount(): InputDiscountMilho {
        return inputDiscountDao.getLastInputDiscount()
    }

    override suspend fun getSampleById(id: Int): SampleMilho? {
        return sampleDao.getById(id)
    }
}
