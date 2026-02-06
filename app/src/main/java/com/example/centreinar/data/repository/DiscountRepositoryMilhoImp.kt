package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.*
import com.example.centreinar.data.local.entity.*
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton

fun calculateClassificationLossMilho(difValue: Float, endValue: Float) : Float {
    if (endValue >= 100f) return 0f
    return (difValue / ( 100 - endValue )) * 100
}

@Singleton
class DiscountRepositoryMilhoImpl @Inject constructor(
    private val limitDao: LimitMilhoDao,
    private val classificationDao: ClassificationMilhoDao,
    private val sampleDao: SampleMilhoDao,
    private val discountDao: DiscountMilhoDao,
    private val inputDiscountDao: InputDiscountMilhoDao,
    private val tools: Utilities
) : DiscountRepositoryMilho {

    override suspend fun getClassificationById(id: Int): ClassificationMilho? {
        return classificationDao.getById(id)
    }

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
            grain, group, tipo, sample, limit,
            doesTechnicalLoss, doesClassificationLoss, doesDeduction
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
        val pricePerSack = if (lotWeight > 0) (lotPrice / lotWeight) * 60 else 0f

        // Limites
        val limImpurities = limit["impurities"] ?: 1.0f
        val limMoisture = limit["moisture"] ?: 14.0f
        val limBroken = limit["broken"] ?: 3.0f
        val limArdido = limit["ardido"] ?: 1.0f
        val limCarunchado = limit["carunchado"] ?: 2.0f
        val limSpoiledTotal = limit["spoiledTotal"] ?: 6.0f

        // Cálculos de Percentual de Perda (Diferença entre amostra e limite)
        var brokenPerc = tools.calculateDifference(sample.broken, limBroken)
        var ardidoPerc = tools.calculateDifference(sample.ardidos, limArdido)
        var carunchadoPerc = tools.calculateDifference(sample.carunchado, limCarunchado)
        var spoiledPerc = tools.calculateDifference(sample.spoiled, limSpoiledTotal)

        // Aplicação da Fórmula de Perda de Classificação (%)
        if (sample.broken > limBroken) brokenPerc = calculateClassificationLossMilho(brokenPerc, limBroken)
        if (sample.ardidos > limArdido) ardidoPerc = calculateClassificationLossMilho(ardidoPerc, limArdido)
        if (sample.carunchado > limCarunchado) carunchadoPerc = calculateClassificationLossMilho(carunchadoPerc, limCarunchado)
        if (sample.spoiled > limSpoiledTotal) spoiledPerc = calculateClassificationLossMilho(spoiledPerc, limSpoiledTotal)

        // UMIDADE E IMPUREZAS
        val impuritiesDiff = tools.calculateDifference(sample.impurities, limImpurities)
        val moistureDiff = tools.calculateDifference(sample.moisture, limMoisture)

        val impuritiesLossKg = (impuritiesDiff / (100 - limImpurities)) * lotWeight
        val moistureLossKg = (moistureDiff / (100 - limMoisture)) * (lotWeight - impuritiesLossKg)

        // Falha Técnica (KG)
        val technicalLossKg = if (doesTechnicalLoss && storageDays > 0) {
            calculateTechnicalLoss(storageDays, (impuritiesLossKg + moistureLossKg), lotWeight)
        } else 0f

        // Conversão de Defeitos de % para KG
        val brokenLossKg = (brokenPerc * lotWeight / 100)
        val ardidoLossKg = (ardidoPerc * lotWeight / 100)
        val carunchadoLossKg = (carunchadoPerc * lotWeight / 100)
        val spoiledLossKg = (spoiledPerc * lotWeight / 100)

        // Desconto de Classificação Total (KG)
        val classificationDiscountKg = if (doesClassificationLoss) {
            brokenLossKg + ardidoLossKg + carunchadoLossKg + spoiledLossKg
        } else 0f

        // Cálculo de Preços Individuais
        val impuritiesLossPrice = (impuritiesLossKg / 60) * pricePerSack
        val humidityLossPrice = (moistureLossKg / 60) * pricePerSack
        val technicalLossPrice = (technicalLossKg / 60) * pricePerSack
        val brokenLossPrice = (brokenLossKg / 60) * pricePerSack
        val ardidoLossPrice = (ardidoLossKg / 60) * pricePerSack
        val carunchadoLossPrice = (carunchadoLossKg / 60) * pricePerSack
        val spoiledLossPrice = (spoiledLossKg / 60) * pricePerSack

        // Totalização Final
        var finalLossKg = impuritiesLossKg + moistureLossKg + technicalLossKg + classificationDiscountKg
        var deductionKg = 0f

        if (doesDeduction && deductionValue > 0) {
            deductionKg = calculateDeduction(deductionValue, classificationDiscountKg)
            finalLossKg = finalLossKg + deductionKg - classificationDiscountKg
        }

        val finalWeight = lotWeight - finalLossKg
        val finalDiscountPrice = (finalLossKg / 60) * pricePerSack
        val finalWeightPrice = lotPrice - finalDiscountPrice

        // Criação do Objeto com todos os parâmetros exigidos
        val discount = DiscountMilho(
            inputDiscountId = sample.id,

            // Perdas em KG
            impuritiesLoss = impuritiesLossKg,
            humidityLoss = moistureLossKg,
            technicalLoss = technicalLossKg,
            brokenLoss = brokenLossKg,
            ardidoLoss = ardidoLossKg,
            carunchadoLoss = carunchadoLossKg,
            spoiledLoss = spoiledLossKg,

            // Preços
            impuritiesLossPrice = impuritiesLossPrice,
            humidityLossPrice = humidityLossPrice,
            technicalLossPrice = technicalLossPrice,
            brokenLossPrice = brokenLossPrice,
            ardidoLossPrice = ardidoLossPrice,
            carunchadoLossPrice = carunchadoLossPrice,
            spoiledLossPrice = spoiledLossPrice,

            // Totais
            finalDiscount = finalLossKg,
            finalWeight = finalWeight,
            finalDiscountPrice = finalDiscountPrice,
            finalWeightPrice = finalWeightPrice
        )

        return discountDao.insert(discount)
    }

    override suspend fun calculateTechnicalLoss(storageDays: Int, humidityAndImpuritiesLoss: Float, lotWeight: Float): Float {
        return (0.0001f * storageDays) * (lotWeight - humidityAndImpuritiesLoss)
    }

    override suspend fun calculateDeduction(deductionValue: Float, classificationLoss: Float): Float {
        return ((100 - deductionValue) / 100) * classificationLoss
    }

    override suspend fun getDiscountById(id: Long): DiscountMilho? {
        return discountDao.getDiscountById(id.toInt())
    }

    override suspend fun getLimitsByType(grain: String, group: Int, tipo: Int, limitSource: Int): Map<String, Float> {
        val limits = limitDao.getLimitsByType(grain, group, tipo, limitSource)
        val limit = limits.firstOrNull()
        return if (limit != null) {
            mapOf(
                "impurities" to limit.impuritiesUpLim,
                "broken" to limit.brokenUpLim,
                "ardido" to limit.ardidoUpLim,
                "mofado" to limit.mofadoUpLim,
                "carunchado" to limit.carunchadoUpLim,
                "moisture" to limit.moistureUpLim,
                "spoiledTotal" to limit.spoiledTotalUpLim
            )
        } else emptyMap()
    }

    override suspend fun setLimit(grain: String, group: Int, type: Int, impurities: Float, moisture: Float, broken: Float, ardido: Float, mofado: Float, carunchado: Float, spoiledTotal: Float): Long {
        val lastSource = limitDao.getLastSource()
        val limit = LimitMilho(source = lastSource + 1, grain = grain, group = group, type = type, impuritiesUpLim = impurities, moistureUpLim = moisture, brokenUpLim = broken, ardidoUpLim = ardido, mofadoUpLim = mofado, carunchadoUpLim = carunchado, spoiledTotalUpLim = spoiledTotal)
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitMilho? {
        return limitDao.getLimitsByType(grain, group, tipo, source).firstOrNull()
    }

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain, group, 1, 0).firstOrNull()
        return if (limit != null) {
            mapOf("impurities" to limit.impuritiesUpLim, "broken" to limit.brokenUpLim, "ardido" to limit.ardidoUpLim, "mofado" to limit.mofadoUpLim, "carunchado" to limit.carunchadoUpLim, "moisture" to limit.moistureUpLim)
        } else emptyMap()
    }

    override suspend fun getLastClassification(): ClassificationMilho = classificationDao.getLastClassification()

    override suspend fun toInputDiscount(priceBySack: Float, classification: ClassificationMilho, daysOfStorage: Int, deductionValue: Float): InputDiscountMilho {
        val sample = sampleDao.getById(classification.sampleId)
        val lotWeight = sample?.lotWeight ?: 0f
        return InputDiscountMilho(
            grain = classification.grain, group = classification.group, limitSource = 0, classificationId = classification.id,
            daysOfStorage = daysOfStorage, deductionValue = deductionValue, lotWeight = lotWeight, lotPrice = lotWeight * priceBySack / 60,
            impurities = classification.impuritiesPercentage, broken = classification.brokenPercentage, ardidos = classification.ardidoPercentage,
            mofados = classification.mofadoPercentage, carunchado = classification.carunchadoPercentage, spoiled = classification.spoiledTotalPercentage
        ).also { inputDiscountDao.insert(it) }
    }

    override suspend fun getDiscountForClassification(priceBySack: Float, daysOfStorage: Int, deductionValue: Float): DiscountMilho? {
        val lastClassification = getLastClassification()
        val inputDiscount = toInputDiscount(priceBySack, lastClassification, daysOfStorage, deductionValue)
        val id = calculateDiscount(inputDiscount.grain, inputDiscount.group, 1, inputDiscount, true, true, true)
        return discountDao.getDiscountById(id.toInt())
    }

    override suspend fun getLastLimitSource(): Int = limitDao.getLastSource()
    override suspend fun setInputDiscount(inputDiscount: InputDiscountMilho): Long = inputDiscountDao.insert(inputDiscount)
    override suspend fun getLastInputDiscount(): InputDiscountMilho = inputDiscountDao.getLastInputDiscount()
    override suspend fun getSampleById(id: Int): SampleMilho? = sampleDao.getById(id)
}