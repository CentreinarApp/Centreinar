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
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DiscountRepositoryImpl @Inject constructor(
    private val limitDao: LimitSojaDao,
    private val classificationDao: ClassificationSojaDao,
    private val sampleDao: SampleSojaDao,
    private val discountDao: DiscountSojaDao,
    private val inputDiscountDao: InputDiscountSojaDao,
) : DiscountRepository {

    override suspend fun getClassificationById(id: Int): ClassificationSoja? =
        classificationDao.getById(id)


    override suspend fun insertDiscount(discount: DiscountSoja): Long =
        discountDao.insert(discount)

    override suspend fun getDiscountById(id: Long): DiscountSoja? =
        discountDao.getDiscountById(id.toInt())

    override suspend fun setInputDiscount(inputDiscount: InputDiscountSoja): Long =
        inputDiscountDao.insert(inputDiscount)

    override suspend fun getLastInputDiscount(): InputDiscountSoja =
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
        val limit: LimitSoja? = try {
            limitDao.getLimitsByType(grain, group, tipo, limitSource)
        } catch (e: Exception) { null }

        return if (limit != null) mapOf(
            "impurities"  to limit.impuritiesUpLim,
            "moisture"    to limit.moistureUpLim,
            "broken"      to limit.brokenCrackedDamagedUpLim,
            "greenish"    to limit.greenishUpLim,
            "burnt"       to limit.burntUpLim,
            "burntOrSour" to limit.burntOrSourUpLim,
            "moldy"       to limit.moldyUpLim,
            "spoiled"     to limit.spoiledTotalUpLim
        ) else emptyMap()
    }

    override suspend fun setLimit(
        grain: String, group: Int, type: Int,
        impurities: Float, moisture: Float, brokenCrackedDamaged: Float,
        greenish: Float, burnt: Float, burntOrSour: Float, moldy: Float, spoiled: Float
    ): Long {
        val source = limitDao.getLastSource() + 1
        val limit = LimitSoja(
            source = source, grain = grain, group = group, type = type,
            impuritiesLowerLim = 0f, impuritiesUpLim = impurities,
            moistureLowerLim = 0f, moistureUpLim = moisture,
            brokenCrackedDamagedLowerLim = 0f, brokenCrackedDamagedUpLim = brokenCrackedDamaged,
            greenishLowerLim = 0f, greenishUpLim = greenish,
            burntLowerLim = 0f, burntUpLim = burnt,
            burntOrSourLowerLim = 0f, burntOrSourUpLim = burntOrSour,
            moldyLowerLim = 0f, moldyUpLim = moldy,
            spoiledTotalLowerLim = 0f, spoiledTotalUpLim = spoiled
        )
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitSoja? =
        limitDao.getLimitsByType(grain, group, tipo, source)

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain, group, 1, 0)
            ?: return emptyMap()
        return mapOf(
            "impurities"  to limit.impuritiesUpLim,
            "moisture"    to limit.moistureUpLim,  // chave "humidity" — preservada do cálculo original
            "broken"      to limit.brokenCrackedDamagedUpLim,
            "greenish"    to limit.greenishUpLim,
            "burnt"       to limit.burntUpLim,
            "burntOrSour" to limit.burntOrSourUpLim,
            "moldy"       to limit.moldyUpLim,
            "spoiled"     to limit.spoiledTotalUpLim
        )
    }

    override suspend fun getLastLimitSource(): Int = limitDao.getLastSource()

    // -------------------------------------------------------------------------
    // Auxiliares
    // -------------------------------------------------------------------------

    override suspend fun getLastClassification(): ClassificationSoja =
        classificationDao.getLastClassification()

    override suspend fun toInputDiscount(
        priceBySack: Float,
        classification: ClassificationSoja,
        daysOfStorage: Int,
        deductionValue: Float
    ): InputDiscountSoja {
        val sample    = sampleDao.getById(classification.sampleId)
        val lotWeight = sample?.lotWeight ?: 0f
        return InputDiscountSoja(
            grain                       = classification.grain,
            group                       = classification.group,
            limitSource                 = 0,
            classificationId            = classification.id,
            daysOfStorage               = daysOfStorage,
            deductionValue              = deductionValue,
            lotWeight                   = lotWeight,
            lotPrice                    = lotWeight * priceBySack / 60,
            foreignMattersAndImpurities = classification.impuritiesPercentage,
            moisture                    = sample?.moisture ?: 0f,
            burnt                       = classification.burntPercentage,
            burntOrSour                 = classification.burntOrSourPercentage,
            moldy                       = classification.moldyPercentage,
            spoiled                     = classification.spoiledPercentage,
            greenish                    = classification.greenishPercentage,
            brokenCrackedDamaged        = classification.brokenCrackedDamagedPercentage
        ).also { inputDiscountDao.insert(it) }
    }

    override suspend fun getSampleById(id: Int): SampleSoja? = sampleDao.getById(id)
}