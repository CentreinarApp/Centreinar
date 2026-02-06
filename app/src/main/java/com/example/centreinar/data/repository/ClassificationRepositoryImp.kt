package com.example.centreinar.data.repository

import android.util.Log
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.dao.*
import com.example.centreinar.domain.model.LimitCategory
import com.example.centreinar.util.Utilities
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationRepositoryImpl @Inject constructor(
    private val limitDao: LimitSojaDao,
    private val classificationDao: ClassificationSojaDao,
    private val sampleDao: SampleSojaDao,
    private val tools: Utilities,
    private val colorClassificationDao: ColorClassificationSojaDao,
    private val disqualificationDao: DisqualificationSojaDao
) : ClassificationRepository {

    override suspend fun classifySample(sample: SampleSoja, limitSource: Int): Long {
        val limitMap = getLimitsForGrain(sample.grain, sample.group, limitSource)
        val sampleId = setSample(sample)

        val cleanWeightGrams = if (sample.cleanWeight > 0f) sample.cleanWeight
        else sample.sampleWeight - sample.foreignMattersAndImpurities

        val safeCleanWeight = if (cleanWeightGrams > 0f) cleanWeightGrams else sample.sampleWeight

        val percentageImpurities = tools.calculateDefectPercentage(sample.foreignMattersAndImpurities, sample.sampleWeight)
        val percentageBroken = tools.calculateDefectPercentage(sample.brokenCrackedDamaged, safeCleanWeight)
        val percentageGreenish = tools.calculateDefectPercentage(sample.greenish, safeCleanWeight)
        val percentageMoldy = tools.calculateDefectPercentage(sample.moldy, safeCleanWeight)
        val percentageBurnt = tools.calculateDefectPercentage(sample.burnt, safeCleanWeight)
        val percentageSour = tools.calculateDefectPercentage(sample.sour, safeCleanWeight)
        val percentageBurntOrSour = percentageBurnt + percentageSour

        val sumDefectWeights = sample.moldy + sample.fermented + sample.sour + sample.burnt +
                sample.germinated + sample.immature + sample.shriveled + sample.damaged

        val percentageSpoiled = tools.calculateDefectPercentage(sumDefectWeights, safeCleanWeight)
        val percentageDamaged = tools.calculateDefectPercentage(sample.damaged, safeCleanWeight)
        val percentageFermented = tools.calculateDefectPercentage(sample.fermented, safeCleanWeight)
        val percentageGerminated = tools.calculateDefectPercentage(sample.germinated, safeCleanWeight)
        val percentageImmature = tools.calculateDefectPercentage(sample.immature, safeCleanWeight)
        val percentageShriveled = tools.calculateDefectPercentage(sample.shriveled, safeCleanWeight)

        val impuritiesType = tools.findCategoryForValue(limitMap["impurities"]?.map { it.lowerL to it.upperL } ?: emptyList(), percentageImpurities)
        val brokenType = tools.findCategoryForValue(limitMap["broken"]?.map { it.lowerL to it.upperL } ?: emptyList(), percentageBroken)
        val greenishType = tools.findCategoryForValue(limitMap["greenish"]?.map { it.lowerL to it.upperL } ?: emptyList(), percentageGreenish)
        val moldyType = tools.findCategoryForValue(limitMap["moldy"]?.map { it.lowerL to it.upperL } ?: emptyList(), percentageMoldy)
        val burntType = tools.findCategoryForValue(limitMap["burnt"]?.map { it.lowerL to it.upperL } ?: emptyList(), percentageBurnt)
        val burntOrSourType = tools.findCategoryForValue(limitMap["burntOrSour"]?.map { it.lowerL to it.upperL } ?: emptyList(), percentageBurntOrSour)
        val spoiledType = tools.findCategoryForValue(limitMap["spoiled"]?.map { it.lowerL to it.upperL } ?: emptyList(), percentageSpoiled)

        var finalType = listOf(brokenType, greenishType, moldyType, burntType, burntOrSourType, spoiledType, impuritiesType).maxOrNull() ?: 1

        val graveDefectsSum = percentageBurntOrSour + percentageMoldy
        var isDisqualify = false
        if (sample.group == 1 && graveDefectsSum > 12f) isDisqualify = true
        if (sample.group == 2 && graveDefectsSum > 40f) isDisqualify = true
        if (isDisqualify) finalType = 0

        val classification = ClassificationSoja(
            grain = sample.grain, group = sample.group, sampleId = sampleId.toInt(),
            moisturePercentage = sample.moisture, foreignMattersPercentage = percentageImpurities,
            brokenCrackedDamagedPercentage = percentageBroken, greenishPercentage = percentageGreenish,
            moldyPercentage = percentageMoldy, burntPercentage = percentageBurnt,
            burntOrSourPercentage = percentageBurntOrSour, spoiledPercentage = percentageSpoiled,
            damagedPercentage = percentageDamaged, sourPercentage = percentageSour,
            fermentedPercentage = percentageFermented, germinatedPercentage = percentageGerminated,
            immaturePercentage = percentageImmature, shriveledPercentage = percentageShriveled,
            foreignMatters = impuritiesType, brokenCrackedDamaged = brokenType,
            greenish = greenishType, moldy = moldyType, burnt = burntType,
            burntOrSour = burntOrSourType, spoiled = spoiledType,
            fermented = 0, germinated = 0, immature = 0, shriveled = 0, sour = 0, finalType = finalType,
        )

        return classificationDao.insert(classification)
    }

    override suspend fun getSample(id: Int): SampleSoja? = sampleDao.getById(id)

    override suspend fun setSample(
        grain: String, group: Int, sampleWeight: Float, lotWeight: Float,
        foreignMattersAndImpurities: Float, humidity: Float,
        greenish: Float, brokenCrackedDamaged: Float, burnt: Float,
        sour: Float, moldy: Float, fermented: Float, germinated: Float,
        immature: Float, shriveled: Float, damaged: Float, cleanWeight: Float
    ): SampleSoja = SampleSoja(
        grain = grain, group = group, lotWeight = lotWeight, sampleWeight = sampleWeight,
        cleanWeight = cleanWeight, foreignMattersAndImpurities = foreignMattersAndImpurities,
        humidity = humidity, greenish = greenish, brokenCrackedDamaged = brokenCrackedDamaged,
        damaged = damaged, burnt = burnt, sour = sour, moldy = moldy, fermented = fermented,
        germinated = germinated, immature = immature, shriveled = shriveled
    )

    override suspend fun setSample(sample: SampleSoja): Long = sampleDao.insert(sample)
    override suspend fun getClassification(id: Int): ClassificationSoja? = classificationDao.getById(id)

    override suspend fun getLimitsForGrain(grain: String, group: Int, limitSource: Int): Map<String, List<LimitCategory>> {
        return mapOf(
            "impurities" to limitDao.getLimitsForImpurities(grain, group, limitSource),
            "broken" to limitDao.getLimitsForBrokenCrackedDamaged(grain, group, limitSource),
            "greenish" to limitDao.getLimitsForGreenish(grain, group, limitSource),
            "burnt" to limitDao.getLimitsForBurnt(grain, group, limitSource),
            "burntOrSour" to limitDao.getLimitsForBurntOrSour(grain, group, limitSource),
            "moldy" to limitDao.getLimitsForMoldy(grain, group, limitSource),
            "spoiled" to limitDao.getLimitsForSpoiledTotal(grain, group, limitSource)
        )
    }

    override suspend fun setClass(grain: String, classificationId: Int, totalWeight: Float, otherColors: Float): ColorClassificationSoja {
        val otherColorsPercentage = tools.calculatePercentage(otherColors, totalWeight)
        val framingClass = if (otherColorsPercentage > 10.0f) "Misturada" else "Amarela"
        val colorClassification = ColorClassificationSoja(
            grain = grain, classificationId = classificationId,
            yellowPercentage = tools.calculatePercentage(totalWeight - otherColors, totalWeight),
            otherColorPercentage = otherColorsPercentage, framingClass = framingClass
        )
        colorClassificationDao.insert(colorClassification)
        return colorClassification
    }

    override suspend fun setDisqualification(classificationId: Int?, badConservation: Int, graveDefectSum: Int, strangeSmell: Int, toxicGrains: Int, insects: Int): Long {
        return disqualificationDao.insert(DisqualificationSoja(classificationId = classificationId, badConservation = badConservation, graveDefectSum = graveDefectSum, strangeSmell = strangeSmell, toxicGrains = toxicGrains, insects = insects))
    }

    override suspend fun setLimit(
        grain: String, group: Int, type: Int,
        impurities: Float, moisture: Float, brokenCrackedDamaged: Float,
        greenish: Float, burnt: Float, burntOrSour: Float, moldy: Float, spoiled: Float
    ): Long {
        val lastSource = limitDao.getLastSource()
        val newSource = if (lastSource == 0) 1 else lastSource + 1 // Nunca salva como 0

        val tipos = listOf(1, 2, 3)
        var lastId = 0L
        tipos.forEach { t ->
            val limit = LimitSoja(
                source = newSource, grain = grain, group = group, type = t,
                impuritiesLowerLim = 0.0f, impuritiesUpLim = impurities,
                moistureLowerLim = 0.0f, moistureUpLim = moisture,
                brokenCrackedDamagedLowerLim = 0.0f, brokenCrackedDamagedUpLim = brokenCrackedDamaged,
                greenishLowerLim = 0.0f, greenishUpLim = greenish,
                burntLowerLim = 0.0f, burntUpLim = burnt,
                burntOrSourLowerLim = 0.0f, burntOrSourUpLim = burntOrSour,
                moldyLowerLim = 0.0f, moldyUpLim = moldy,
                spoiledTotalLowerLim = 0.0f, spoiledTotalUpLim = spoiled
            )
            lastId = limitDao.insertLimit(limit)
        }
        return lastId
    }

    override suspend fun getLastLimitSource(): Int = limitDao.getLastSource()
    override suspend fun getLastColorClass(): ColorClassificationSoja? = colorClassificationDao.getLastColorClass()
    override suspend fun getDisqualificationByClassificationId(idClassification: Int): DisqualificationSoja? = disqualificationDao.getByClassificationId(classificationId = idClassification)

    override suspend fun updateDisqualification(classificationId: Int, finalType: Int) {
        val disqualificationId = disqualificationDao.getLastDisqualificationId()
        val defectSum = if (finalType == 0) 1 else 0
        disqualificationDao.updateClassificationId(disqualificationId, classificationId)
        disqualificationDao.updateGraveDefectSum(disqualificationId, defectSum)
    }

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = try { limitDao.getLimitsByType(grain, group, 1, 0) } catch (e: Exception) { null }
        return if (limit != null) {
            mapOf("impuritiesUpLim" to limit.impuritiesUpLim, "moistureUpLim" to limit.moistureUpLim, "brokenUpLim" to limit.brokenCrackedDamagedUpLim, "greenishUpLim" to limit.greenishUpLim, "burntUpLim" to limit.burntUpLim, "burntOrSourUpLim" to limit.burntOrSourUpLim, "moldyUpLim" to limit.moldyUpLim, "spoiledTotalUpLim" to limit.spoiledTotalUpLim)
        } else emptyMap()
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitSoja? {
        return try { limitDao.getLimitsByType(grain, group, tipo, source) } catch (e: Exception) { null }
    }

    override suspend fun getObservations(idClassification: Int, colorClass: ColorClassificationSoja?): String {
        val classification = classificationDao.getById(idClassification)
        return if (classification?.finalType == 0) "Desclassificada: excesso de defeitos graves." else " "
    }

    override suspend fun deleteCustomLimits() {
        limitDao.deleteCustomLimits()
    }
}