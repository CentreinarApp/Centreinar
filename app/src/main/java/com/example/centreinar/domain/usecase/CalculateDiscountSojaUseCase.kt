package com.example.centreinar.domain.usecase

import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.util.Utilities
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateDiscountSojaUseCase @Inject constructor(
    private val discountRepo: DiscountRepository,
    private val tools: Utilities
) {

    suspend fun execute(
        sample: InputDiscountSoja,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        val limit = discountRepo.getLimitsByType(
            grain       = sample.grain,
            group       = sample.group,
            tipo        = 1,
            limitSource = sample.limitSource
        )
        return calculate(sample, limit, doesTechnicalLoss, doesClassificationLoss, doesDeduction)
    }

    suspend fun execute(
        sample: InputDiscountSoja,
        limit: Map<String, Float>,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long = calculate(sample, limit, doesTechnicalLoss, doesClassificationLoss, doesDeduction)

    // -------------------------------------------------------------------------
    // Lógica de cálculo
    // -------------------------------------------------------------------------

    private suspend fun calculate(
        sample: InputDiscountSoja,
        limit: Map<String, Float>,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        val lotWeight      = sample.lotWeight
        val lotPrice       = sample.lotPrice
        val storageDays    = sample.daysOfStorage
        val deductionValue = sample.deductionValue
        val pricePerSack   = if (lotWeight > 0) (lotPrice / lotWeight) * 60 else 0f

        // --- IMPUREZAS ---
        val impuritiesLoss      = tools.calculateDifference(sample.foreignMattersAndImpurities, limit["impurities"]!!)
        val impuritiesLossRound = round(classificationLoss(impuritiesLoss, limit["impurities"]!!))
        val impuritiesLossKg    = (impuritiesLossRound / 100) * lotWeight

        // --- UMIDADE ---
        val humidityLoss              = tools.calculateDifference(sample.moisture, limit["moisture"]!!)
        val humidityLossRound         = round(classificationLoss(humidityLoss, limit["moisture"]!!))
        val humidityLossKg            = (humidityLossRound / 100) * (lotWeight - impuritiesLossKg)
        val impuritiesAndHumidityLoss = impuritiesLossKg + humidityLossKg

        // --- QUEBRA TÉCNICA ---
        var technicalLoss = 0f
        if (doesTechnicalLoss && storageDays > 0) {
            technicalLoss = (0.0001f * storageDays) * (lotWeight - impuritiesAndHumidityLoss)
        }

        // --- QUEIMADOS (Qq) ---
        var burntLoss = 0f
        if (sample.burnt > limit["burnt"]!!) {
            val diff = sample.burnt - limit["burnt"]!!
            burntLoss = round(classificationLoss(diff, limit["burnt"]!!))
        }

        // --- ARDIDOS + QUEIMADOS (Qtaq) — cascata: subtrai Qq antes de comparar ---
        var burntOrSourLoss = 0f
        val adjustedBurntOrSour = sample.burntOrSour - burntLoss
        if (adjustedBurntOrSour > limit["burntOrSour"]!!) {
            val diff = adjustedBurntOrSour - limit["burntOrSour"]!!
            burntOrSourLoss = round(classificationLoss(diff, limit["burntOrSour"]!!))
        }

        // --- MOFADOS (Qm) ---
        var moldyLoss = 0f
        if (sample.moldy > limit["moldy"]!!) {
            val diff = sample.moldy - limit["moldy"]!!
            moldyLoss = round(classificationLoss(diff, limit["moldy"]!!))
        }

        // --- AVARIADOS TOTAL (Qta) — cascata: subtrai Qq + Qtaq + Qm antes de comparar ---
        var spoiledLoss = 0f
        val adjustedSpoiled = sample.spoiled - (burntLoss + burntOrSourLoss + moldyLoss)
        if (adjustedSpoiled > limit["spoiled"]!!) {
            val diff = adjustedSpoiled - limit["spoiled"]!!
            spoiledLoss = round(classificationLoss(diff, limit["spoiled"]!!))
        }

        // --- ESVERDEADOS (Qe) ---
        var greenishLoss = 0f
        if (sample.greenish > limit["greenish"]!!) {
            val diff = sample.greenish - limit["greenish"]!!
            greenishLoss = round(classificationLoss(diff, limit["greenish"]!!))
        }

        // --- PARTIDOS / QUEBRADOS / AMASSADOS (Qpqa) ---
        var brokenLoss = 0f
        if (sample.brokenCrackedDamaged > limit["broken"]!!) {
            val diff = sample.brokenCrackedDamaged - limit["broken"]!!
            brokenLoss = round(classificationLoss(diff, limit["broken"]!!))
        }

        // Converter % → Kg
        val burntLossKg       = (burntLoss       / 100) * lotWeight
        val burntOrSourLossKg = (burntOrSourLoss / 100) * lotWeight
        val brokenLossKg      = (brokenLoss      / 100) * lotWeight
        val greenishLossKg    = (greenishLoss    / 100) * lotWeight
        val moldyLossKg       = (moldyLoss       / 100) * lotWeight
        val spoiledLossKg     = (spoiledLoss     / 100) * lotWeight

        // --- DESCONTO DE CLASSIFICAÇÃO ---
        var classificationDiscount = 0f
        if (doesClassificationLoss) {
            classificationDiscount = ((brokenLoss + burntLoss + burntOrSourLoss +
                    moldyLoss + greenishLoss + spoiledLoss) / 100) * lotWeight
        }

        var finalLoss  = impuritiesAndHumidityLoss + technicalLoss + classificationDiscount
        var deduction  = 0f

        // --- DEDUÇÃO ---
        if (doesDeduction && deductionValue > 0) {
            deduction = ((100 - deductionValue) / 100) * classificationDiscount
            finalLoss = finalLoss + deduction - classificationDiscount
        }

        val finalWeight = lotWeight - finalLoss

        // Preços (R$)
        val impuritiesLossPrice            = (impuritiesLossKg / 60) * pricePerSack
        val humidityLossPrice              = (humidityLossKg   / 60) * pricePerSack
        val impuritiesAndHumidityLossPrice = impuritiesLossPrice + humidityLossPrice
        val technicalLossPrice             = if (lotWeight > 0) (lotPrice / lotWeight) * technicalLoss else 0f
        val burntLossPrice                 = (burntLossKg       / 60) * pricePerSack
        val burntOrSourLossPrice           = (burntOrSourLossKg / 60) * pricePerSack
        val brokenLossPrice                = (brokenLossKg      / 60) * pricePerSack
        val greenishLossPrice              = (greenishLossKg    / 60) * pricePerSack
        val moldyLossPrice                 = (moldyLossKg       / 60) * pricePerSack
        val spoiledLossPrice               = (spoiledLossKg     / 60) * pricePerSack
        val classificationDiscountPrice    = burntLossPrice + burntOrSourLossPrice + brokenLossPrice +
                greenishLossPrice + moldyLossPrice + spoiledLossPrice

        var finalDiscountPrice = impuritiesAndHumidityLossPrice + technicalLossPrice + classificationDiscountPrice
        if (doesDeduction && deductionValue > 0) {
            finalDiscountPrice = finalDiscountPrice - classificationDiscountPrice +
                    (lotPrice * (deduction * 100 / lotWeight) / 100)
        }

        val finalWeightPrice = lotPrice - finalDiscountPrice

        val discount = DiscountSoja(
            inputDiscountId                    = sample.id,
            impuritiesLoss                     = impuritiesLossKg,
            humidityLoss                       = humidityLossKg,
            technicalLoss                      = technicalLoss,
            burntLoss                          = burntLossKg,
            burntOrSourLoss                    = burntOrSourLossKg,
            moldyLoss                          = moldyLossKg,
            spoiledLoss                        = spoiledLossKg,
            greenishLoss                       = greenishLossKg,
            brokenLoss                         = brokenLossKg,
            classificationDiscount             = classificationDiscount,
            humidityAndImpuritiesDiscount      = impuritiesAndHumidityLoss,
            burntLossPrice                     = burntLossPrice,
            burntOrSourLossPrice               = burntOrSourLossPrice,
            brokenLossPrice                    = brokenLossPrice,
            greenishLossPrice                  = greenishLossPrice,
            moldyLossPrice                     = moldyLossPrice,
            spoiledLossPrice                   = spoiledLossPrice,
            classificationDiscountPrice        = classificationDiscountPrice,
            humidityAndImpuritiesDiscountPrice = impuritiesAndHumidityLossPrice,
            impuritiesLossPrice                = impuritiesLossPrice,
            humidityLossPrice                  = humidityLossPrice,
            technicalLossPrice                 = technicalLossPrice,
            deductionValue                     = sample.deductionValue,
            deduction                          = deduction,
            finalDiscount                      = finalLoss,
            finalDiscountPrice                 = finalDiscountPrice,
            finalWeightPrice                   = finalWeightPrice,
            finalWeight                        = finalWeight
        )

        return discountRepo.insertDiscount(discount)
    }

    // -------------------------------------------------------------------------
    // Função auxiliar
    // -------------------------------------------------------------------------

    private fun classificationLoss(difValue: Float, endValue: Float): Float =
        (difValue / (100 - endValue)) * 100

    private fun round(value: Float): Float =
        BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP).toFloat()
}