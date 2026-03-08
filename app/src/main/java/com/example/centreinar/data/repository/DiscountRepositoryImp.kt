package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationSoja
import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.dao.ClassificationSojaDao
import com.example.centreinar.data.local.dao.DiscountSojaDao
import com.example.centreinar.data.local.dao.InputDiscountSojaDao
import com.example.centreinar.data.local.dao.LimitSojaDao
import com.example.centreinar.data.local.dao.SampleSojaDao
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton

// Função auxiliar para calcular descontos por defeito
fun calculateClassificationLoss(difValue: Float, endValue: Float): Float {
    return (difValue / (100 - endValue)) * 100
}

private fun round(value: Float): Float {
    return java.math.BigDecimal(value.toString())
        .setScale(2, java.math.RoundingMode.HALF_UP)
        .toFloat()
}

@Singleton
class DiscountRepositoryImpl @Inject constructor(
    private val limitDao: LimitSojaDao,
    private val classificationDao: ClassificationSojaDao,
    private val sampleDao: SampleSojaDao,
    private val discountDao: DiscountSojaDao,
    private val inputDiscountDao: InputDiscountSojaDao,
    private val tools: Utilities
) : DiscountRepository {
    override suspend fun getClassificationById(id: Int): ClassificationSoja? {
        return classificationDao.getById(id)
    }

    override suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountSoja,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        val limit = getLimitsByType(grain, group, tipo, sample.limitSource)
        return calculateDiscount(
            grain = grain,
            group = group,
            tipo = tipo,
            sample = sample,
            limit = limit,
            doesTechnicalLoss = doesTechnicalLoss,
            doesClassificationLoss = doesClassificationLoss,
            doesDeduction = doesDeduction
        )
    }

    override suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountSoja,
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

        // Perdas por umidade e impurezas
        val impuritiesLoss = tools.calculateDifference(sample.foreignMattersAndImpurities, limit["impurities"]!!)
        val humidityLoss = tools.calculateDifference(sample.humidity, limit["humidity"]!!)

        // Arredondando o cálculo
        val impuritiesLossRound = round(calculateClassificationLoss(impuritiesLoss, limit["impurities"]!!))
        val impuritiesLossKg = (impuritiesLossRound / 100) * lotWeight

        // Arredondando o cálculo
        val humidityAndImpuritiesLossRound = round(calculateClassificationLoss(humidityLoss, limit["humidity"]!!))
        val humidityLossKg = (humidityAndImpuritiesLossRound / 100) * (lotWeight - impuritiesLossKg)

        val impuritiesAndHumidityLoss = impuritiesLossKg + humidityLossKg
        var technicalLoss = 0f

        // Falha técnica
        if (doesTechnicalLoss && storageDays > 0) {
            technicalLoss = calculateTechnicalLoss(storageDays, impuritiesAndHumidityLoss, lotWeight)
        }

        // ==========================================================
        // CÁLCULO DE DESCONTOS (CONFORME FLUXOGRAMA OFICIAL)
        // ==========================================================

        // 1. Queimados (Qq)
        var burntLoss = 0f
        if (sample.burnt > limit["burnt"]!!) {
            val diffBurnt = sample.burnt - limit["burnt"]!!
            burntLoss = round(calculateClassificationLoss(diffBurnt, limit["burnt"]!!))
        }

        // 2. Total de Ardidos e Queimados (Qtaq)
        // Regra: "Total ard+que inicial - Qq"
        var burntOrSourLoss = 0f
        val adjustedBurntOrSour = sample.burntOrSour - burntLoss
        if (adjustedBurntOrSour > limit["burntOrSour"]!!) {
            val diffBurntOrSour = adjustedBurntOrSour - limit["burntOrSour"]!!
            burntOrSourLoss = round(calculateClassificationLoss(diffBurntOrSour, limit["burntOrSour"]!!))
        }

        // 3. Mofados (Qm)
        var moldyLoss = 0f
        if (sample.moldy > limit["moldy"]!!) {
            val diffMoldy = sample.moldy - limit["moldy"]!!
            moldyLoss = round(calculateClassificationLoss(diffMoldy, limit["moldy"]!!))
        }

        // 4. Total de Avariados (Qta)
        // Regra: "Total avar. inicial - (Qq + Qtaq + Qm)"
        var spoiledLoss = 0f
        val adjustedSpoiled = sample.spoiled - (burntLoss + burntOrSourLoss + moldyLoss)
        if (adjustedSpoiled > limit["spoiled"]!!) {
            val diffSpoiled = adjustedSpoiled - limit["spoiled"]!!
            spoiledLoss = round(calculateClassificationLoss(diffSpoiled, limit["spoiled"]!!))
        }

        // 5. Esverdeados (Qe)
        var greenishLoss = 0f
        if (sample.greenish > limit["greenish"]!!) {
            val diffGreenish = sample.greenish - limit["greenish"]!!
            greenishLoss = round(calculateClassificationLoss(diffGreenish, limit["greenish"]!!))
        }

        // 6. Partidos, Quebrados e Amassados (Qpqa)
        var brokenLoss = 0f
        if (sample.brokenCrackedDamaged > limit["broken"]!!) {
            val diffBroken = sample.brokenCrackedDamaged - limit["broken"]!!
            brokenLoss = round(calculateClassificationLoss(diffBroken, limit["broken"]!!))
        }

        // ==========================================================
        // Converter para Kg
        // ==========================================================
        val burntLossKg = (burntLoss / 100) * lotWeight
        val burntOrSourLossKg = (burntOrSourLoss / 100) * lotWeight
        val brokenLossKg = (brokenLoss / 100) * lotWeight
        val greenishLossKg = (greenishLoss / 100) * lotWeight
        val moldyLossKg = (moldyLoss / 100) * lotWeight
        val spoiledLossKg = (spoiledLoss / 100) * lotWeight

        // Desconto de classificação
        var classificationDiscount = 0f
        if (doesClassificationLoss) {
            classificationDiscount = ((brokenLoss + burntLoss + burntOrSourLoss + moldyLoss + greenishLoss + spoiledLoss) / 100) * lotWeight
        }

        var finalLoss = impuritiesAndHumidityLoss + technicalLoss + classificationDiscount
        var deduction = 0f

        // Dedução
        if (doesDeduction && deductionValue > 0) {
            deduction = calculateDeduction(deductionValue, classificationDiscount)
            finalLoss = finalLoss + deduction - classificationDiscount
        }

        val finalWeight = lotWeight - finalLoss

        // ==========================================================
        // Preços (R$)
        // ==========================================================
        val impuritiesLossPrice = (impuritiesLossKg / 60) * pricePerSack
        val humidityLossPrice = (humidityLossKg / 60) * pricePerSack
        val impuritiesAndHumidityLossPrice = impuritiesLossPrice + humidityLossPrice
        val technicalLossPrice = (lotPrice / lotWeight) * technicalLoss

        val burntLossPrice = (burntLossKg / 60) * pricePerSack
        val burntOrSourLossPrice = (burntOrSourLossKg / 60) * pricePerSack
        val brokenLossPrice = (brokenLossKg / 60) * pricePerSack
        val greenishLossPrice =  (greenishLossKg / 60) * pricePerSack
        val moldyLossPrice = (moldyLossKg / 60) * pricePerSack
        val spoiledLossPrice = (spoiledLossKg / 60) * pricePerSack

        val classificationDiscountPrice = burntLossPrice + burntOrSourLossPrice + brokenLossPrice +
                greenishLossPrice + moldyLossPrice + spoiledLossPrice

        var finalDiscountPrice = impuritiesAndHumidityLossPrice + technicalLossPrice + classificationDiscountPrice
        if (doesDeduction && deductionValue > 0) {
            finalDiscountPrice = finalDiscountPrice - classificationDiscountPrice + (lotPrice * (deduction * 100 / lotWeight) / 100)
        }

        val finalWeightPrice = lotPrice - finalDiscountPrice

        // ==========================================================
        // Salva a entidade de forma limpa e direta
        // ==========================================================
        val discount = DiscountSoja(
            inputDiscountId = sample.id,

            // Perdas em Kg
            impuritiesLoss = impuritiesLossKg,
            humidityLoss = humidityLossKg,
            technicalLoss = technicalLoss,
            burntLoss = burntLossKg,
            burntOrSourLoss = burntOrSourLossKg,
            moldyLoss = moldyLossKg,
            spoiledLoss = spoiledLossKg,
            greenishLoss = greenishLossKg,
            brokenLoss = brokenLossKg,

            // Totais de Classificação
            classificationDiscount = classificationDiscount,
            humidityAndImpuritiesDiscount = impuritiesAndHumidityLoss,

            // Valores Financeiros
            burntLossPrice = burntLossPrice,
            burntOrSourLossPrice = burntOrSourLossPrice,
            brokenLossPrice = brokenLossPrice,
            greenishLossPrice = greenishLossPrice,
            moldyLossPrice = moldyLossPrice,
            spoiledLossPrice = spoiledLossPrice,
            classificationDiscountPrice = classificationDiscountPrice,
            humidityAndImpuritiesDiscountPrice = impuritiesAndHumidityLossPrice,
            impuritiesLossPrice = impuritiesLossPrice,
            humidityLossPrice = humidityLossPrice,
            technicalLossPrice = technicalLossPrice,

            // Dedução e Totais
            deductionValue = sample.deductionValue, // Valor original digitado (%)
            deduction = deduction,                  // Desconto calculado em Kg
            finalDiscount = finalLoss,
            finalDiscountPrice = finalDiscountPrice,
            finalWeightPrice = finalWeightPrice,
            finalWeight = finalWeight
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

    override suspend fun calculateDeduction(deductionValue: Float, classificationLoss: Float): Float {
        return ((100 - deductionValue) / 100) * classificationLoss
    }

    override suspend fun getDiscountById(id: Long): DiscountSoja? {
        return discountDao.getDiscountById(id.toInt())
    }

    override suspend fun getLimitsByType(
        grain: String,
        group: Int,
        tipo: Int,
        limitSource: Int
    ): Map<String, Float> {
        val limit: LimitSoja? = try {
            limitDao.getLimitsByType(grain, group, tipo, limitSource)
        } catch (e: Exception) {
            null
        }

        return if (limit != null) {
            mapOf(
                "impurities" to limit.impuritiesUpLim,
                "humidity" to limit.moistureUpLim,
                "broken" to limit.brokenCrackedDamagedUpLim,
                "greenish" to limit.greenishUpLim,
                "burnt" to limit.burntUpLim,
                "burntOrSour" to limit.burntOrSourUpLim,
                "moldy" to limit.moldyUpLim,
                "spoiled" to limit.spoiledTotalUpLim
            )
        } else {
            emptyMap()
        }
    }

    override suspend fun setLimit(
        grain: String,
        group: Int,
        type: Int,
        impurities: Float,
        moisture: Float,
        brokenCrackedDamaged: Float,
        greenish: Float,
        burnt: Float,
        burntOrSour: Float,
        moldy: Float,
        spoiled: Float
    ): Long {
        val lastSource = limitDao.getLastSource()
        val source = lastSource + 1
        val limit = LimitSoja(
            source = source,
            grain = grain,
            group = group,
            type = type,
            impuritiesLowerLim = 0f,
            impuritiesUpLim = impurities,
            moistureLowerLim = 0f,
            moistureUpLim = moisture,
            brokenCrackedDamagedLowerLim = 0f,
            brokenCrackedDamagedUpLim = brokenCrackedDamaged,
            greenishLowerLim = 0f,
            greenishUpLim = greenish,
            burntLowerLim = 0f,
            burntUpLim = burnt,
            burntOrSourLowerLim = 0f,
            burntOrSourUpLim = burntOrSour,
            moldyLowerLim = 0f,
            moldyUpLim = moldy,
            spoiledTotalLowerLim = 0f,
            spoiledTotalUpLim = spoiled
        )
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitSoja? {
        return limitDao.getLimitsByType(grain, group, tipo, source)
    }

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain, group, 1, 0)
        return if (limit != null) {
            mapOf(
                "impuritiesLowerLim" to limit.impuritiesLowerLim,
                "impuritiesUpLim" to limit.impuritiesUpLim,
                "moistureLowerLim" to limit.moistureLowerLim,
                "moistureUpLim" to limit.moistureUpLim,
                "brokenLowerLim" to limit.brokenCrackedDamagedLowerLim,
                "brokenUpLim" to limit.brokenCrackedDamagedUpLim,
                "greenishLowerLim" to limit.greenishLowerLim,
                "greenishUpLim" to limit.greenishUpLim,
                "burntLowerLim" to limit.burntLowerLim,
                "burntUpLim" to limit.burntUpLim,
                "burntOrSourLowerLim" to limit.burntOrSourLowerLim,
                "burntOrSourUpLim" to limit.burntOrSourUpLim,
                "moldyLowerLim" to limit.moldyLowerLim,
                "moldyUpLim" to limit.moldyUpLim,
                "spoiledTotalLowerLim" to limit.spoiledTotalLowerLim,
                "spoiledTotalUpLim" to limit.spoiledTotalUpLim
            )
        } else {
            emptyMap()
        }
    }

    override suspend fun getLastClassification(): ClassificationSoja {
        return classificationDao.getLastClassification()
    }

    override suspend fun toInputDiscount(
        priceBySack: Float,
        classification: ClassificationSoja,
        daysOfStorage: Int,
        deductionValue: Float
    ): InputDiscountSoja {
        val sample = sampleDao.getById(classification.sampleId)
        val lotWeight = sample?.lotWeight ?: 0f

        val inputDiscount = InputDiscountSoja(
            grain = classification.grain,
            group = classification.group,
            limitSource = 0,
            classificationId = classification.id,
            daysOfStorage = daysOfStorage,
            deductionValue = deductionValue,
            lotWeight = lotWeight,
            lotPrice = lotWeight * priceBySack / 60,
            foreignMattersAndImpurities = classification.impuritiesPercentage,
            humidity = sample?.humidity ?: 0f,
            burnt = classification.burntPercentage,
            burntOrSour = classification.burntOrSourPercentage,
            moldy = classification.moldyPercentage,
            spoiled = classification.spoiledPercentage,
            greenish = classification.greenishPercentage,
            brokenCrackedDamaged = classification.brokenCrackedDamagedPercentage
        )

        inputDiscountDao.insert(inputDiscount)
        return inputDiscount
    }

    override suspend fun getDiscountForClassification(
        priceBySack: Float,
        daysOfStorage: Int,
        deductionValue: Float
    ): DiscountSoja? {
        val classification = getLastClassification()
        val inputDiscount = toInputDiscount(priceBySack, classification, daysOfStorage, deductionValue)
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

    override suspend fun setInputDiscount(inputDiscount: InputDiscountSoja): Long {
        return inputDiscountDao.insert(inputDiscount)
    }

    override suspend fun getLastInputDiscount(): InputDiscountSoja {
        return inputDiscountDao.getLastInputDiscount()
    }

    override suspend fun getSampleById(id: Int): SampleSoja? {
        return sampleDao.getById(id)
    }
}