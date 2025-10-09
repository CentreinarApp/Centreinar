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

@Singleton
class DiscountRepositoryImpl @Inject constructor(
    private val limitDao: LimitSojaDao,
    private val classificationDao: ClassificationSojaDao,
    private val sampleDao: SampleSojaDao,
    private val discountDao: DiscountSojaDao,
    private val inputDiscountDao: InputDiscountSojaDao,
    private val tools: Utilities
) : DiscountRepository {

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

        // perdas por umidade e impurezas
        val impuritiesLoss = tools.calculateDifference(sample.foreignMattersAndImpurities, limit["impurities"]!!)
        val humidityLoss = tools.calculateDifference(sample.humidity, limit["humidity"]!!)

        val impuritiesLossKg = (impuritiesLoss / 100) * lotWeight
        val humidityLossKg = (humidityLoss / 100) * (lotWeight - impuritiesLossKg)

        var impuritiesAndHumidityLoss = impuritiesLossKg + humidityLossKg
        var technicalLoss = 0f

        if (doesTechnicalLoss && storageDays > 0) {
            technicalLoss = calculateTechnicalLoss(storageDays, impuritiesAndHumidityLoss, lotWeight)
        }

        // perdas por defeitos
        var burntLoss = tools.calculateDifference(sample.burnt, limit["burnt"]!!)
        var burntOrSourLoss = tools.calculateDifference(sample.burntOrSour - burntLoss, limit["burntOrSour"]!!)
        var moldyLoss = tools.calculateDifference(sample.moldy, limit["moldy"]!!)
        var spoiledLoss = tools.calculateDifference(sample.spoiled - moldyLoss - burntLoss - burntOrSourLoss, limit["spoiled"]!!)
        var greenishLoss = tools.calculateDifference(sample.greenish, limit["greenish"]!!)
        var brokenLoss = tools.calculateDifference(sample.brokenCrackedDamaged, limit["broken"]!!)

        var classificationDiscount = 0f
        if (doesClassificationLoss) {
            classificationDiscount = ((brokenLoss + burntLoss + burntOrSourLoss + moldyLoss + greenishLoss + spoiledLoss) / 100) * lotWeight
        }

        var finalLoss = impuritiesAndHumidityLoss + technicalLoss + classificationDiscount
        var deduction = 0f

        if (doesDeduction && deductionValue > 0) {
            deduction = calculateDeduction(deductionValue, classificationDiscount)
            finalLoss = finalLoss + deduction - classificationDiscount
        }

        val finalWeight = lotWeight - finalLoss

        // preços
        val impuritiesLossPrice = (lotPrice * impuritiesLoss / 100)
        val humidityLossPrice = ((lotPrice - impuritiesLossPrice) * humidityLoss / 100)
        val impuritiesAndHumidityLossPrice = impuritiesLossPrice + humidityLossPrice
        val technicalLossPrice = (lotPrice / lotWeight) * technicalLoss

        val burntLossPrice = lotPrice * burntLoss / 100
        val burntOrSourLossPrice = lotPrice * burntOrSourLoss / 100
        val brokenLossPrice = lotPrice * brokenLoss / 100
        val greenishLossPrice = lotPrice * greenishLoss / 100
        val moldyLossPrice = lotPrice * moldyLoss / 100
        val spoiledLossPrice = lotPrice * spoiledLoss / 100

        val classificationDiscountPrice = burntLossPrice + burntOrSourLossPrice + brokenLossPrice +
                greenishLossPrice + moldyLossPrice + spoiledLossPrice

        var finalDiscountPrice = impuritiesAndHumidityLossPrice + technicalLossPrice + classificationDiscountPrice
        if (doesDeduction && deductionValue > 0) {
            finalDiscountPrice = finalDiscountPrice - classificationDiscountPrice + (lotPrice * (deduction * 100 / lotWeight) / 100)
        }

        val finalWeightPrice = lotPrice - finalDiscountPrice

        // converter perdas percentuais para peso (kg)
        burntLoss = burntLoss * lotWeight / 100
        burntOrSourLoss = burntOrSourLoss * lotWeight / 100
        brokenLoss = brokenLoss * lotWeight / 100
        greenishLoss = greenishLoss * lotWeight / 100
        moldyLoss = moldyLoss * lotWeight / 100
        spoiledLoss = spoiledLoss * lotWeight / 100

        val discount = DiscountSoja(
            inputDiscountId = sample.id,
            impuritiesLoss = impuritiesLossKg,
            humidityLoss = humidityLossKg,
            technicalLoss = technicalLoss,
            burntLoss = burntLoss,
            burntOrSourLoss = burntOrSourLoss,
            moldyLoss = moldyLoss,
            spoiledLoss = spoiledLoss,
            classificationDiscount = classificationDiscount,
            greenishLoss = greenishLoss,
            brokenLoss = brokenLoss,
            burntLossPrice = burntLossPrice,
            burntOrSourLossPrice = burntOrSourLossPrice,
            brokenLossPrice = brokenLossPrice,
            greenishLossPrice = greenishLossPrice,
            moldyLossPrice = moldyLossPrice,
            spoiledLossPrice = spoiledLossPrice,
            classificationDiscountPrice = classificationDiscountPrice,
            humidityAndImpuritiesDiscountPrice = impuritiesAndHumidityLossPrice,
            humidityAndImpuritiesDiscount = impuritiesAndHumidityLoss,
            deductionValue = deduction,
            deduction = deduction,
            finalDiscount = finalLoss,
            finalDiscountPrice = finalDiscountPrice,
            impuritiesLossPrice = impuritiesLossPrice,
            humidityLossPrice = humidityLossPrice,
            technicalLossPrice = technicalLossPrice,
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
        // Tenta buscar o limite e lida com o caso de ser nulo ou exceção.
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

    // CORREÇÃO: Assinatura alterada para retornar LimitSoja? (anulável)
    override suspend fun getLimit(
        grain: String,
        group: Int,
        tipo: Int,
        source: Int
    ): LimitSoja? { // <-- MUDANÇA AQUI
        return limitDao.getLimitsByType(grain, group, tipo, source)
    }

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain, group, 1, 0)
        return mapOf(
            "impuritiesLowerLim" to limit!!.impuritiesLowerLim, // ATENÇÃO: Aqui estou usando !! para manter seu código original, mas a função chamadora deve tratar o nulo
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
            foreignMattersAndImpurities = classification.foreignMattersPercentage,
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