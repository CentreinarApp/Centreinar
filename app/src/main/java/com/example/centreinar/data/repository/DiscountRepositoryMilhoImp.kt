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
        val limit = getLimitsByType(grain, group, tipo, sample.limitSource)
        return calculateDiscount(
            grain, group, tipo, sample,
            limit,
            doesTechnicalLoss,
            doesClassificationLoss,
            doesDeduction
        )
    }

    override suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountMilho,
        limit: Map<String, Float>,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        val lotWeight = sample.lotWeight
        val lotPrice = sample.lotPrice
        val storageDays = sample.daysOfStorage
        val deductionValue = sample.deductionValue

        // perdas por defeitos
        var impuritiesLoss = tools.calculateDifference(sample.impurities, limit["impurities"]!!)
        var brokenLoss = tools.calculateDifference(sample.broken, limit["broken"]!!)
        var ardidoLoss = tools.calculateDifference(sample.ardidos, limit["ardido"]!!)
        var mofadoLoss = tools.calculateDifference(sample.mofados, limit["mofado"]!!)
        var carunchadoLoss = tools.calculateDifference(sample.carunchado, limit["carunchado"]!!)

        var classificationDiscount = 0f
        if (doesClassificationLoss) {
            classificationDiscount =
                ((impuritiesLoss + brokenLoss + ardidoLoss + mofadoLoss + carunchadoLoss) / 100) * lotWeight
        }

        var technicalLoss = 0f
        if (doesTechnicalLoss && storageDays > 0) {
            technicalLoss = calculateTechnicalLoss(storageDays, impuritiesLoss, lotWeight)
        }

        var finalLoss = classificationDiscount + technicalLoss
        var deduction = 0f

        if (doesDeduction && deductionValue > 0) {
            deduction = calculateDeduction(deductionValue, classificationDiscount)
            finalLoss = finalLoss + deduction - classificationDiscount
        }

        val finalWeight = lotWeight - finalLoss

        // preços
        val impuritiesLossPrice = lotPrice * impuritiesLoss / 100
        val brokenLossPrice = lotPrice * brokenLoss / 100
        val ardidoLossPrice = lotPrice * ardidoLoss / 100
        val mofadoLossPrice = lotPrice * mofadoLoss / 100
        val carunchadoLossPrice = lotPrice * carunchadoLoss / 100

        val classificationDiscountPrice =
            impuritiesLossPrice + brokenLossPrice + ardidoLossPrice + mofadoLossPrice + carunchadoLossPrice

        val technicalLossPrice = (lotPrice / lotWeight) * technicalLoss

        var finalDiscountPrice = classificationDiscountPrice + technicalLossPrice
        if (doesDeduction && deductionValue > 0) {
            finalDiscountPrice =
                finalDiscountPrice - classificationDiscountPrice + (lotPrice * (deduction * 100 / lotWeight) / 100)
        }

        val finalWeightPrice = lotPrice - finalDiscountPrice

        // converter perdas percentuais para peso (kg)
        impuritiesLoss = impuritiesLoss * lotWeight / 100
        brokenLoss = brokenLoss * lotWeight / 100
        ardidoLoss = ardidoLoss * lotWeight / 100
        mofadoLoss = mofadoLoss * lotWeight / 100
        carunchadoLoss = carunchadoLoss * lotWeight / 100

        val discount = DiscountMilho(
            inputDiscountId = sample.id,
            impuritiesLoss = impuritiesLoss,
            humidityLoss = 0f, // milho não tem desconto por umidade
            technicalLoss = technicalLoss,
            brokenLoss = brokenLoss,
            ardidoLoss = ardidoLoss,
            mofadoLoss = mofadoLoss,
            carunchadoLoss = carunchadoLoss,
            fermentedLoss = 0f,
            germinatedLoss = 0f,
            gessadoLoss = 0f,
            finalDiscount = finalLoss,
            finalWeight = finalWeight
        )

        return discountDao.insert(discount)
    }

    override suspend fun calculateTechnicalLoss(
        storageDays: Int,
        impuritiesLoss: Float,
        lotWeight: Float
    ): Float {
        return (0.0001f * storageDays) * (lotWeight - impuritiesLoss)
    }

    override suspend fun calculateDeduction(
        deductionValue: Float,
        classificationLoss: Float
    ): Float {
        return ((100 - deductionValue) / 100) * classificationLoss
    }

    override suspend fun getDiscountById(id: Long): DiscountMilho? {
        return discountDao.getDiscountById(id.toInt())
    }

    override suspend fun getLimitsByType(
        grain: String,
        group: Int,
        tipo: Int,
        limitSource: Int
    ): Map<String, Float> {
        val limit = limitDao.getLimitsBySource(grain, group, limitSource).first()
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
        val lastSource = limitDao.getLastSource()
        val source = lastSource + 1
        val limit = LimitMilho(
            source = source,
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

    override suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitMilho {
        return limitDao.getLimitsBySource(grain, group, source).first()
    }

    override suspend fun getLimitOfType1Official(
        group: Int,
        grain: String
    ): Map<String, Float> {
        val limit = limitDao.getLimitsBySource(grain, group, 0).first()
        return mapOf(
            "impurities" to limit.impuritiesUpLim,
            "broken" to limit.brokenUpLim,
            "ardido" to limit.ardidoUpLim,
            "mofado" to limit.mofadoUpLim,
            "carunchado" to limit.carunchadoUpLim,
            "moisture" to limit.moistureUpLim
        )
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
            carunchado = classification.carunchadoPercentage
        )

        inputDiscountDao.insert(inputDiscount)
        return inputDiscount
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
