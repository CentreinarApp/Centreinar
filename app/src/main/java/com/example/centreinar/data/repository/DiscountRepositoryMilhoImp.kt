package com.example.centreinar.data.repository

import android.util.Log
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.dao.*
import com.example.centreinar.data.local.entity.*
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.Map
import kotlin.collections.emptyMap
import kotlin.collections.set
import kotlin.math.max

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

        // Preço por saca (60kg)
        val pricePerSack = if (lotWeight > 0) (lotPrice / lotWeight) * 60 else 0f

        // Limites
        val limImpurities = limit["impurities"] ?: 1.0f
        val limMoisture = limit["humidity"] ?: 14.0f
        val limBroken = limit["broken"] ?: 3.0f
        val limArdido = limit["ardido"] ?: 1.0f
        val limMofado = limit["mofado"] ?: 6.0f
        val limCarunchado = limit["carunchado"] ?: 2.0f
        val limSpoiledTotal = limit["spoiledTotal"] ?: 10.0f

        // DEFEITOS
        var brokenLoss = tools.calculateDifference(sample.broken, limBroken)
        var ardidoLoss = tools.calculateDifference(sample.ardidos, limArdido)
        var mofadoLoss = tools.calculateDifference(sample.mofados, limMofado)
        var carunchadoLoss = tools.calculateDifference(sample.carunchado, limCarunchado)

        val spoiledInput = if (sample.spoiled > 0) {
            sample.spoiled
        } else {
            sample.broken + sample.ardidos + sample.mofados + sample.carunchado
        }

        var spoiledLoss = tools.calculateDifference(
            spoiledInput - brokenLoss - ardidoLoss - mofadoLoss - carunchadoLoss,
            limSpoiledTotal
        )

        if (sample.broken > limBroken) {
            brokenLoss = calculateClassificationLossMilho(brokenLoss, limBroken)
        }
        if (sample.ardidos > limArdido) {
            ardidoLoss = calculateClassificationLossMilho(ardidoLoss, limArdido)
        }
        if (sample.mofados > limMofado) {
            mofadoLoss = calculateClassificationLossMilho(mofadoLoss, limMofado)
        }
        if (sample.carunchado > limCarunchado) {
            carunchadoLoss = calculateClassificationLossMilho(carunchadoLoss, limCarunchado)
        }
        if (
            spoiledInput > limSpoiledTotal ||
            spoiledInput - (brokenLoss + ardidoLoss + mofadoLoss + carunchadoLoss) > limSpoiledTotal
        ) {
            spoiledLoss = calculateClassificationLossMilho(spoiledLoss, limSpoiledTotal)
        }


        // UMIDADE E IMPUREZAS
        val impuritiesDiff = tools.calculateDifference(sample.impurities, limImpurities)
        val moistureDiff = tools.calculateDifference(sample.moisture, limMoisture)

        val impuritiesLossKg =
            (impuritiesDiff / (100 - limImpurities)) * lotWeight

        val moistureLossKg =
            (moistureDiff / (100 - limMoisture)) * (lotWeight - impuritiesLossKg - ((brokenLoss * lotWeight) / 100))

        val impuritiesAndHumidityLoss = impuritiesLossKg + moistureLossKg

        // FALHA TÉCNICA
        var technicalLoss = 0f
        if (doesTechnicalLoss && storageDays > 0) {
            technicalLoss = calculateTechnicalLoss(
                storageDays,
                impuritiesAndHumidityLoss,
                lotWeight
            )
        }


        // DESCONTO DE CLASSIFICAÇÃO
        var classificationDiscountKg = 0f
        if (doesClassificationLoss) {
            classificationDiscountKg =
                ((brokenLoss + ardidoLoss + mofadoLoss + carunchadoLoss + spoiledLoss) / 100) * lotWeight
        }

        // Converte % → KG
        brokenLoss = brokenLoss * lotWeight / 100
        ardidoLoss = ardidoLoss * lotWeight / 100
        mofadoLoss = mofadoLoss * lotWeight / 100
        carunchadoLoss = carunchadoLoss * lotWeight / 100
        spoiledLoss = spoiledLoss * lotWeight / 100

        // TOTALIZAÇÃO
        var finalLoss = impuritiesAndHumidityLoss + technicalLoss + classificationDiscountKg
        var deduction = 0f

        if (doesDeduction && deductionValue > 0) {
            deduction = calculateDeduction(deductionValue, classificationDiscountKg)
            finalLoss = finalLoss + deduction - classificationDiscountKg
        }

        val finalWeight = lotWeight - finalLoss

        // PREÇOS
        val impuritiesLossPrice = (impuritiesLossKg / 60) * pricePerSack
        val humidityLossPrice = (moistureLossKg / 60) * pricePerSack
        val technicalLossPrice = (technicalLoss / 60) * pricePerSack

        val brokenLossPrice = (brokenLoss / 60) * pricePerSack
        val ardidoLossPrice = (ardidoLoss / 60) * pricePerSack
        val mofadoLossPrice = (mofadoLoss / 60) * pricePerSack
        val carunchadoLossPrice = (carunchadoLoss / 60) * pricePerSack
        val spoiledLossPrice = (spoiledLoss / 60) * pricePerSack

        val classificationDiscountPrice =
            brokenLossPrice + ardidoLossPrice + mofadoLossPrice +
                    carunchadoLossPrice + spoiledLossPrice

        var finalDiscountPrice =
            impuritiesLossPrice + humidityLossPrice +
                    technicalLossPrice + classificationDiscountPrice

        if (doesDeduction && deductionValue > 0) {
            finalDiscountPrice =
                finalDiscountPrice - classificationDiscountPrice +
                        (lotPrice * (deduction * 100 / lotWeight) / 100)
        }

        val finalWeightPrice = lotPrice - finalDiscountPrice

        // Salva no banco
        val discount = DiscountMilho(
            inputDiscountId = sample.id,

            impuritiesLoss = impuritiesLossKg,
            humidityLoss = moistureLossKg,
            technicalLoss = technicalLoss,
            brokenLoss = brokenLoss,
            ardidoLoss = ardidoLoss,
            mofadoLoss = mofadoLoss,
            carunchadoLoss = carunchadoLoss,
            spoiledLoss = spoiledLoss,

            fermentedLoss = 0f,
            germinatedLoss = 0f,
            gessadoLoss = 0f,
            immatureLoss = 0f,

            impuritiesLossPrice = impuritiesLossPrice,
            humidityLossPrice = humidityLossPrice,
            technicalLossPrice = technicalLossPrice,
            brokenLossPrice = brokenLossPrice,
            ardidoLossPrice = ardidoLossPrice,
            mofadoLossPrice = mofadoLossPrice,
            carunchadoLossPrice = carunchadoLossPrice,
            spoiledLossPrice = spoiledLossPrice,

            fermentedLossPrice = 0f,
            germinatedLossPrice = 0f,
            gessadoLossPrice = 0f,
            immatureLossPrice = 0f,

            finalDiscount = finalLoss,
            finalWeight = finalWeight,
            finalDiscountPrice = finalDiscountPrice,
            finalWeightPrice = finalWeightPrice
        )

        return discountDao.insert(discount)
    }


    override suspend fun calculateTechnicalLoss(
        storageDays: Int,
        humidityAndImpuritiesLoss: Float,
        lotWeight: Float
    ): Float {
        return (0.0001f * storageDays) * (lotWeight - humidityAndImpuritiesLoss)
    }

    override suspend fun calculateDeduction(
        deductionValue: Float,
        classificationLoss: Float
    ): Float {
        return ((100 - deductionValue) / 100) * classificationLoss
    }

    // --- MÉTODOS DE CONSULTA E SETUP ---

    override suspend fun getDiscountById(id: Long): DiscountMilho? {
        return discountDao.getDiscountById(id.toInt())
    }

    override suspend fun getLimitsByType(
        grain: String,
        group: Int,
        tipo: Int,
        limitSource: Int
    ): Map<String, Float> {
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

    override suspend fun setLimit(
        grain: String,
        group: Int,
        type: Int,
        impurities: Float,
        moisture: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float,
        spoiledTotal: Float
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
            carunchadoUpLim = carunchado,
            spoiledTotalUpLim = spoiledTotal
        )
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitMilho? {
        return limitDao.getLimitsByType(grain, group, tipo, source).firstOrNull()
    }

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain, group, 1, 0).firstOrNull()
        return if (limit != null) {
            mapOf(
                "impurities" to limit.impuritiesUpLim,
                "broken" to limit.brokenUpLim,
                "ardido" to limit.ardidoUpLim,
                "mofado" to limit.mofadoUpLim,
                "carunchado" to limit.carunchadoUpLim,
                "moisture" to limit.moistureUpLim
            )
        } else emptyMap()
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

        val totalSpoiledCalc = classification.brokenPercentage + classification.ardidoPercentage + classification.mofadoPercentage + classification.carunchadoPercentage

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
            carunchado = classification.carunchadoPercentage,

            // Passando a soma calculada como o Total de Avariados
            spoiled = totalSpoiledCalc
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
        val inputDiscount = toInputDiscount(priceBySack, lastClassification, daysOfStorage, deductionValue)
        val id = calculateDiscount(
            grain = inputDiscount.grain,
            group = inputDiscount.group,
            tipo = 1,
            sample = inputDiscount,
            doesTechnicalLoss = true,
            doesClassificationLoss = true,
            doesDeduction = true
        )
        return discountDao.getDiscountById(id.toInt())
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