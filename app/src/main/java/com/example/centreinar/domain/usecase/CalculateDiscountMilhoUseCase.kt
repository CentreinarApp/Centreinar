package com.example.centreinar.domain.usecase

import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

class CalculateDiscountMilhoUseCase @Inject constructor(
    private val discountRepo: DiscountRepositoryMilho
) {

    suspend fun execute(
        sample: InputDiscountMilho,
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
        sample: InputDiscountMilho,
        limit: Map<String, Float>,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long = calculate(sample, limit, doesTechnicalLoss, doesClassificationLoss, doesDeduction)

    // -------------------------------------------------------------------------
    // Lógica de cálculo
    // -------------------------------------------------------------------------

    private suspend fun calculate(
        sample: InputDiscountMilho,
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

        // Limites
        val limImpurities   = limit["impurities"]   ?: 1.0f
        val limMoisture     = limit["moisture"]      ?: 14.0f
        val limBroken       = limit["broken"]        ?: 3.0f
        val limArdido       = limit["ardido"]        ?: 1.0f
        val limCarunchado   = limit["carunchado"]    ?: 2.0f
        val limSpoiledTotal = limit["spoiledTotal"]  ?: 6.0f

        // --- IMPUREZAS ---
        var impuritiesPerc = 0f
        if (sample.impurities > limImpurities) {
            val diff = sample.impurities - limImpurities
            impuritiesPerc = round(classificationLoss(diff, limImpurities))
        }
        val impuritiesLossKg = (impuritiesPerc / 100) * lotWeight

        // --- QUEBRADOS ---
        var brokenPerc = 0f
        if (sample.broken > limBroken) {
            val diff = sample.broken - limBroken
            brokenPerc = round(classificationLoss(diff, limBroken))
        }
        val brokenLossKg = (brokenPerc / 100) * (lotWeight - impuritiesLossKg)

        // --- UMIDADE ---
        var moisturePerc = 0f
        if (sample.moisture > limMoisture) {
            val diff = sample.moisture - limMoisture
            moisturePerc = round(classificationLoss(diff, limMoisture))
        }
        val moistureLossKg = (moisturePerc / 100) * (lotWeight - impuritiesLossKg - brokenLossKg)

        // --- ARDIDOS ---
        var ardidoLoss = 0f
        if (sample.ardidos > limArdido) {
            val diff = sample.ardidos - limArdido
            ardidoLoss = round(classificationLoss(diff, limArdido))
        }

        // --- CARUNCHADOS ---
        var carunchadoLoss = 0f
        if (sample.carunchado > limCarunchado) {
            val diff = sample.carunchado - limCarunchado
            carunchadoLoss = round(classificationLoss(diff, limCarunchado))
        }

        // --- AVARIADOS TOTAL ---
        var spoiledLoss = 0f
        val spoiledAfterArdido = sample.spoiled - ardidoLoss
        if (spoiledAfterArdido > limSpoiledTotal) {
            val diff = spoiledAfterArdido - limSpoiledTotal
            spoiledLoss = round(classificationLoss(diff, limSpoiledTotal))
        }

        // Converter % → Kg
        val ardidoLossKg    = (ardidoLoss    / 100) * lotWeight
        val carunchadoLossKg = (carunchadoLoss / 100) * lotWeight
        val spoiledLossKg   = (spoiledLoss   / 100) * lotWeight

        // --- QUEBRA TÉCNICA ---
        val technicalLossKg = if (doesTechnicalLoss && storageDays > 0) {
            round(technicalLoss(storageDays, impuritiesLossKg + moistureLossKg, lotWeight))
        } else 0f

        // --- DESCONTO DE CLASSIFICAÇÃO ---
        val classificationDiscountKg = if (doesClassificationLoss) {
            brokenLossKg + ardidoLossKg + carunchadoLossKg + spoiledLossKg
        } else 0f

        var finalLossKg = impuritiesLossKg + moistureLossKg + technicalLossKg + classificationDiscountKg
        var deductionKg = 0f

        if (doesDeduction && deductionValue > 0) {
            deductionKg = deduction(deductionValue, classificationDiscountKg)
            finalLossKg = finalLossKg - classificationDiscountKg + deductionKg
        }

        val finalWeight = lotWeight - finalLossKg

        // Preços
        val impuritiesLossPrice         = (impuritiesLossKg        / 60) * pricePerSack
        val humidityLossPrice           = (moistureLossKg          / 60) * pricePerSack
        val technicalLossPrice          = (technicalLossKg         / 60) * pricePerSack
        val classificationDiscountPrice = (classificationDiscountKg / 60) * pricePerSack
        val brokenLossPrice             = (brokenLossKg            / 60) * pricePerSack
        val ardidoLossPrice             = (ardidoLossKg            / 60) * pricePerSack
        val carunchadoLossPrice         = (carunchadoLossKg        / 60) * pricePerSack
        val spoiledLossPrice            = (spoiledLossKg           / 60) * pricePerSack
        val finalDiscountPrice          = (finalLossKg             / 60) * pricePerSack
        val finalWeightPrice            = lotPrice - finalDiscountPrice

        val discount = DiscountMilho(
            inputDiscountId             = sample.id,
            impuritiesLoss              = impuritiesLossKg,
            humidityLoss                = moistureLossKg,
            technicalLoss               = technicalLossKg,
            brokenLoss                  = brokenLossKg,
            ardidoLoss                  = ardidoLossKg,
            carunchadoLoss              = carunchadoLossKg,
            spoiledLoss                 = spoiledLossKg,
            deduction                   = deductionKg,
            classificationDiscount      = classificationDiscountKg,
            impuritiesLossPrice         = impuritiesLossPrice,
            humidityLossPrice           = humidityLossPrice,
            technicalLossPrice          = technicalLossPrice,
            classificationDiscountPrice = classificationDiscountPrice,
            brokenLossPrice             = brokenLossPrice,
            ardidoLossPrice             = ardidoLossPrice,
            carunchadoLossPrice         = carunchadoLossPrice,
            spoiledLossPrice            = spoiledLossPrice,
            deductionPrice              = (deductionKg / 60) * pricePerSack,
            finalDiscount               = finalLossKg,
            finalWeight                 = finalWeight,
            finalDiscountPrice          = finalDiscountPrice,
            finalWeightPrice            = finalWeightPrice
        )

        return discountRepo.insertDiscount(discount)
    }

    // -------------------------------------------------------------------------
    // Funções de cálculo puras
    // -------------------------------------------------------------------------

    private fun classificationLoss(diff: Float, lim: Float): Float {
        if (lim >= 100f) return 0f
        return (diff / (100f - lim)) * 100f
    }

    private fun technicalLoss(storageDays: Int, humidityAndImpuritiesLoss: Float, lotWeight: Float): Float {
        return (0.0001f * storageDays) * (lotWeight - humidityAndImpuritiesLoss)
    }

    private fun deduction(deductionValue: Float, classificationLoss: Float): Float {
        return ((100f - deductionValue) / 100f) * classificationLoss
    }

    private fun round(value: Float): Float =
        BigDecimal(value.toString()).setScale(2, RoundingMode.HALF_UP).toFloat()
}