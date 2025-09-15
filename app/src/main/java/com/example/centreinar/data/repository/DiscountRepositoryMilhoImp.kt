package com.example.centreinar.data.repository

import com.example.centreinar.data.local.dao.DiscountMilhoDao
import com.example.centreinar.data.local.dao.InputDiscountMilhoDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.SampleMilhoDao
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class DiscountRepositoryMilhoImpl @Inject constructor(
    private val limitDao: LimitMilhoDao,
    private val sampleDao: SampleMilhoDao,
    private val discountDao: DiscountMilhoDao,
    private val inputDiscountDao: InputDiscountMilhoDao,
    private val tools: Utilities
) : DiscountRepositoryMilho {

    /**
     * Calcula o desconto para milho usando limites fornecidos.
     *
     * Estratégia:
     * - Calcula perdas por impurezas (diferença entre amostra e limite)
     * - Calcula perdas por defeitos (broken, ardido, mofado, carunchado)
     * - Aplica perda técnica baseada em dias de armazenamento (simples proporção)
     * - Se deductionValue > 0 aplica dedução conforme sua formula disponível
     * - Retorna id do desconto salvo
     */
    override suspend fun calculateDiscount(
        grain: String,
        group: Int,
        tipo: Int,
        sample: InputDiscountMilho,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        // 1) pegar limite do tipo (source fornecido por sample.limitSource)
        val limit: LimitMilho = limitDao.getLimitsBySource(grain, group, sample.limitSource)
            .firstOrNull { it.type == tipo } ?: limitDao.getLimitsBySource(grain, group, sample.limitSource).first()

        val lotWeight = sample.lotWeight
        val storageDays = sample.daysOfStorage
        val deductionValue = sample.deductionValue
        val lotPrice = sample.lotPrice

        // 2) perdas percentuais (positivo = acima do limite)
        val impuritiesLossPerc = tools.calculateDifference(sample.impurities, limit.impuritiesUpLim)
        val brokenLossPerc = tools.calculateDifference(sample.broken, limit.brokenUpLim)
        val ardidoLossPerc = tools.calculateDifference(sample.ardido, limit.ardidoUpLim)
        val mofadoLossPerc = tools.calculateDifference(sample.mofado, limit.mofadoUpLim)
        val carunchadoLossPerc = tools.calculateDifference(sample.carunchado, limit.carunchadoUpLim)

        // 3) converter para perdas em peso (kg)
        val impuritiesLossKg = (impuritiesLossPerc / 100f) * lotWeight
        val brokenLossKg = (brokenLossPerc / 100f) * lotWeight
        val ardidoLossKg = (ardidoLossPerc / 100f) * lotWeight
        val mofadoLossKg = (mofadoLossPerc / 100f) * lotWeight
        val carunchadoLossKg = (carunchadoLossPerc / 100f) * lotWeight

        // 4) perda técnica por armazenamento
        var technicalLossKg = 0.0f
        if (doesTechnicalLoss && storageDays > 0) {
            technicalLossKg = calculateTechnicalLoss(storageDays, impuritiesLossKg + 0f, lotWeight)
        }

        // 5) perda por classificação (soma dos defeitos acima)
        var classificationLossKg = 0.0f
        if (doesClassificationLoss) {
            classificationLossKg = brokenLossKg + ardidoLossKg + mofadoLossKg + carunchadoLossKg
        }

        // 6) dedução (se aplicável)
        var deductionKg = 0.0f
        if (doesDeduction && deductionValue > 0f) {
            deductionKg = calculateDeduction(deductionValue, classificationLossKg)
        }

        var finalLossKg = impuritiesLossKg + technicalLossKg + classificationLossKg
        if (deductionKg > 0f) {
            // conforme sua regra: finalLoss = finalLoss + deduction - classificationLoss (mantendo mesmo raciocínio da soja)
            finalLossKg = finalLossKg + deductionKg - classificationLossKg
        }

        val finalWeightKg = lotWeight - finalLossKg
        val pricePerLot = lotPrice

        // 7) valores monetários (simples: proporção do preço)
        val impuritiesLossPrice = (pricePerLot) * (impuritiesLossKg / lotWeight)
        val technicalLossPrice = (pricePerLot) * (technicalLossKg / lotWeight)
        val classificationLossPrice = (pricePerLot) * (classificationLossKg / lotWeight)
        val deductionPrice = if (deductionKg > 0f) (pricePerLot) * (deductionKg / lotWeight) else 0f

        val finalDiscountPrice = impuritiesLossPrice + technicalLossPrice + classificationLossPrice - deductionPrice
        val finalWeightPrice = pricePerLot - finalDiscountPrice

        // 8) montar DiscountMilho e salvar
        val discount = DiscountMilho(
            inputDiscountId = sample.id,
            impuritiesLoss = impuritiesLossKg,
            brokenLoss = brokenLossKg,
            ardidoLoss = ardidoLossKg,
            mofadoLoss = mofadoLossKg,
            carunchadoLoss = carunchadoLossKg,
            finalDiscount = finalLossKg,
            finalWeight = finalWeightKg,
            finalDiscountPrice = finalDiscountPrice,
            finalWeightPrice = finalWeightPrice
        )

        return discountDao.insert(discount)
    }

    override suspend fun getDiscountById(id: Long): DiscountMilho? {
        return discountDao.getById(id.toInt())
    }

    override suspend fun getLimitsByType(grain: String, group: Int, tipo: Int, limitSource: Int): LimitMilho {
        // retorne o limite do tipo solicitado
        return limitDao.getLimitsBySource(grain, group, limitSource)
            .firstOrNull { it.type == tipo }
            ?: limitDao.getLimitsBySource(grain, group, limitSource).first()
    }

    override suspend fun setInputDiscount(inputDiscount: InputDiscountMilho): Long {
        return inputDiscountDao.insert(inputDiscount)
    }

    override suspend fun getSampleById(id: Int): SampleMilho? {
        return sampleDao.getById(id)
    }

    override suspend fun calculateTechnicalLoss(storageDays: Int, humidityAndImpuritiesLoss: Float, lotWeight: Float): Float {
        // fórmula simples: (0.0001 * storageDays) * (lotWeight - humidityAndImpuritiesLoss)
        return (0.0001f * storageDays) * (lotWeight - humidityAndImpuritiesLoss)
    }

    override suspend fun calculateDeduction(deductionValue: Float, classificationLoss: Float): Float {
        // mesma lógica que você usou para soja: ((100 - deductionValue)/100 * classificationLoss)
        return ((100f - deductionValue) / 100f) * classificationLoss
    }
}

