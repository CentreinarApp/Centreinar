package com.example.centreinar.data.repository

import com.example.centreinar.ClassificationSoja
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entities.ToxicSeedSoja
import com.example.centreinar.data.local.dao.*
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.Utilities
import com.example.centreinar.domain.model.LimitCategory
import com.example.centreinar.util.roundToTwoDecimals
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationRepositoryImpl @Inject constructor(
    private val limitDao: LimitSojaDao,
    private val classificationDao: ClassificationSojaDao,
    private val sampleDao: SampleSojaDao,
    private val tools: Utilities,
    private val colorClassificationDao: ColorClassificationSojaDao,
    private val disqualificationDao: DisqualificationSojaDao,
    private val toxicSeedDao: ToxicSeedSojaDao,
    private val colorClassificationSojaDao: ColorClassificationSojaDao
) : ClassificationRepository {

    override suspend fun classifySample(sample: SampleSoja, limitSource: Int, lastDisq: DisqualificationSoja): Long {
        val limitMap = getLimitsForGrain(sample.grain, sample.group, limitSource)
        val sampleId = setSample(sample)

        // Cálculos de PESO (mantendo os valores crus, sem arredondar)
        val cleanWeightGrams = if (sample.cleanWeight > 0f) sample.cleanWeight
        else sample.sampleWeight - sample.foreignMattersAndImpurities

        val safeCleanWeight = if (cleanWeightGrams > 0f) cleanWeightGrams else sample.sampleWeight

        // Soma dos pesos puros (em gramas) para os totais de avariados e graves
        val sumDefectWeights = sample.moldy + sample.fermented + sample.sour + sample.burnt +
                sample.germinated + sample.immature + sample.shriveled + sample.damaged

        val sumBurntOrSourWeights = sample.burnt + sample.sour

        // Cálculo das PORCENTAGENS (Usa o peso cru para o cálculo exato, e ARREDONDA o resultado final)
        val percentageImpurities    = tools.calculateDefectPercentage(sample.foreignMattersAndImpurities, sample.sampleWeight).roundToTwoDecimals()
        val percentageBroken        = tools.calculateDefectPercentage(sample.brokenCrackedDamaged, safeCleanWeight).roundToTwoDecimals()
        val percentageGreenish      = tools.calculateDefectPercentage(sample.greenish, safeCleanWeight).roundToTwoDecimals()
        val percentageMoldy         = tools.calculateDefectPercentage(sample.moldy, safeCleanWeight).roundToTwoDecimals()
        val percentageBurnt         = tools.calculateDefectPercentage(sample.burnt, safeCleanWeight).roundToTwoDecimals()
        val percentageSour          = tools.calculateDefectPercentage(sample.sour, safeCleanWeight).roundToTwoDecimals()
        val percentageBurntOrSour   = tools.calculateDefectPercentage(sumBurntOrSourWeights, safeCleanWeight).roundToTwoDecimals()
        val percentageSpoiled       = tools.calculateDefectPercentage(sumDefectWeights, safeCleanWeight).roundToTwoDecimals()
        val percentageDamaged       = tools.calculateDefectPercentage(sample.damaged, safeCleanWeight).roundToTwoDecimals()
        val percentageFermented     = tools.calculateDefectPercentage(sample.fermented, safeCleanWeight).roundToTwoDecimals()
        val percentageGerminated    = tools.calculateDefectPercentage(sample.germinated, safeCleanWeight).roundToTwoDecimals()
        val percentageImmature      = tools.calculateDefectPercentage(sample.immature, safeCleanWeight).roundToTwoDecimals()
        val percentageShriveled     = tools.calculateDefectPercentage(sample.shriveled, safeCleanWeight).roundToTwoDecimals()

        // Cálculo das PORCENTAGENS (Usa o peso cru para o cálculo exato, e ARREDONDA o resultado final)
        val impuritiesType  = tools.findCategoryForValue(limitMap["impurities"]?.map  { it.lowerL to it.upperL } ?: emptyList(), percentageImpurities)
        val brokenType      = tools.findCategoryForValue(limitMap["broken"]?.map      { it.lowerL to it.upperL } ?: emptyList(), percentageBroken)
        val greenishType    = tools.findCategoryForValue(limitMap["greenish"]?.map    { it.lowerL to it.upperL } ?: emptyList(), percentageGreenish)
        val moldyType       = tools.findCategoryForValue(limitMap["moldy"]?.map       { it.lowerL to it.upperL } ?: emptyList(), percentageMoldy)
        val burntType       = tools.findCategoryForValue(limitMap["burnt"]?.map       { it.lowerL to it.upperL } ?: emptyList(), percentageBurnt)
        val burntOrSourType = tools.findCategoryForValue(limitMap["burntOrSour"]?.map { it.lowerL to it.upperL } ?: emptyList(), percentageBurntOrSour)
        val spoiledType     = tools.findCategoryForValue(limitMap["spoiled"]?.map     { it.lowerL to it.upperL } ?: emptyList(), percentageSpoiled)

        var finalType = listOf(brokenType, greenishType, moldyType, burntType, burntOrSourType, spoiledType, impuritiesType).maxOrNull() ?: 1

        // Regras de Desclassificação
        val graveDefectsSum = (percentageBurntOrSour + percentageMoldy).roundToTwoDecimals()

        var isDisqualify = false
        if (sample.group == 1 && graveDefectsSum > 12f) isDisqualify = true
        if (sample.group == 2 && graveDefectsSum > 40f) isDisqualify = true

        if (lastDisq.badConservation == 1 || lastDisq.strangeSmell == 1 ||
            lastDisq.insects == 1 || lastDisq.toxicGrains == 1) {
            isDisqualify = true
        }

        // Salva no Banco de Dados
        val classification = ClassificationSoja(
            grain = sample.grain, group = sample.group, sampleId = sampleId.toInt(),

            // Salvando os valos das porcentagens dos defeitos
            moisturePercentage               = sample.moisture,
            impuritiesPercentage             = percentageImpurities,
            brokenCrackedDamagedPercentage   = percentageBroken,
            greenishPercentage               = percentageGreenish,
            moldyPercentage                  = percentageMoldy,
            burntPercentage                  = percentageBurnt,
            burntOrSourPercentage            = percentageBurntOrSour,
            spoiledPercentage                = percentageSpoiled,
            damagedPercentage                = percentageDamaged,
            sourPercentage                   = percentageSour,
            fermentedPercentage              = percentageFermented,
            germinatedPercentage             = percentageGerminated,
            immaturePercentage               = percentageImmature,
            shriveledPercentage              = percentageShriveled,

            // Salvando os valores dos tipos de defeito
            impuritiesType                   = impuritiesType,
            brokenCrackedDamagedType         = brokenType,
            greenishType                     = greenishType,
            moldyType                        = moldyType,
            burntType                        = burntType,
            burntOrSourType                  = burntOrSourType,
            spoiledType                      = spoiledType,
            fermentedType                    = -1,
            germinatedType                   = -1,
            immatureType                     = -1,
            shriveledType                    = -1,
            sourType                         = -1,
            finalType                        = finalType,
            isDisqualified                   = isDisqualify
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
        moisture = humidity, greenish = greenish, brokenCrackedDamaged = brokenCrackedDamaged,
        damaged = damaged, burnt = burnt, sour = sour, moldy = moldy, fermented = fermented,
        germinated = germinated, immature = immature, shriveled = shriveled
    )

    override suspend fun setSample(sample: SampleSoja): Long = sampleDao.insert(sample)

    override suspend fun getClassification(id: Int): ClassificationSoja? = classificationDao.getById(id)

    override suspend fun getLastDisqualificationId(): Int? = disqualificationDao.getLastDisqualificationId()

    override suspend fun getLastDisqualification(): DisqualificationSoja? = disqualificationDao.getLastDisqualification()

    override suspend fun updateClassificationIdOnDisqualification(disqualificationId: Int, classificationId: Int) {
        disqualificationDao.updateClassificationId(disqualificationId, classificationId)
    }

    override suspend fun getLimitsForGrain(grain: String, group: Int, limitSource: Int): Map<String, List<LimitCategory>> {
        return mapOf(
            "impurities"  to limitDao.getLimitsForImpurities(grain, group, limitSource),
            "broken"      to limitDao.getLimitsForBrokenCrackedDamaged(grain, group, limitSource),
            "greenish"    to limitDao.getLimitsForGreenish(grain, group, limitSource),
            "burnt"       to limitDao.getLimitsForBurnt(grain, group, limitSource),
            "burntOrSour" to limitDao.getLimitsForBurntOrSour(grain, group, limitSource),
            "moldy"       to limitDao.getLimitsForMoldy(grain, group, limitSource),
            "spoiled"     to limitDao.getLimitsForSpoiledTotal(grain, group, limitSource)
        )
    }

    override suspend fun setClass(grain: String, classificationId: Int, totalWeight: Float, otherColors: Float): ColorClassificationSoja {
        val otherColorsPercentage = tools.calculatePercentage(otherColors, totalWeight)
        val framingClass = if (otherColorsPercentage > 10.0f) "Misturada" else "Amarela"
        val colorClassification = ColorClassificationSoja(
            grain = grain, classificationId = classificationId,
            yellowPercentage     = tools.calculatePercentage(totalWeight - otherColors, totalWeight),
            otherColorPercentage = otherColorsPercentage,
            framingClass         = framingClass
        )
        colorClassificationDao.insert(colorClassification)
        return colorClassification
    }

    override suspend fun setDisqualification(
        classificationId: Int?, badConservation: Int, graveDefectSum: Int,
        strangeSmell: Int, toxicGrains: Int, insects: Int
    ): Long {
        return disqualificationDao.insert(
            DisqualificationSoja(
                classificationId = classificationId,
                badConservation  = badConservation,
                graveDefectSum   = graveDefectSum,
                strangeSmell     = strangeSmell,
                toxicGrains      = toxicGrains,
                insects          = insects
            )
        )
    }

    override suspend fun getColorClassification(classificationId: Int): ColorClassificationSoja? {
        return colorClassificationSojaDao.getByClassificationId(classificationId)
    }

    override suspend fun setLimit(
        grain: String, group: Int, type: Int,
        impurities: Float, moisture: Float, brokenCrackedDamaged: Float,
        greenish: Float, burnt: Float, burntOrSour: Float, moldy: Float, spoiled: Float
    ): Long {
        val lastSource = limitDao.getLastSource()
        val newSource  = if (lastSource == 0) 1 else lastSource + 1

        var lastId = 0L
        listOf(1, 2, 3).forEach { t ->
            val limit = LimitSoja(
                source = newSource, grain = grain, group = group, type = t,
                impuritiesLowerLim = 0f, impuritiesUpLim = impurities,
                moistureLowerLim = 0f, moistureUpLim = moisture,
                brokenCrackedDamagedLowerLim = 0f, brokenCrackedDamagedUpLim = brokenCrackedDamaged,
                greenishLowerLim = 0f, greenishUpLim = greenish,
                burntLowerLim = 0f, burntUpLim = burnt,
                burntOrSourLowerLim = 0f, burntOrSourUpLim = burntOrSour,
                moldyLowerLim = 0f, moldyUpLim = moldy,
                spoiledTotalLowerLim = 0f, spoiledTotalUpLim = spoiled
            )
            lastId = limitDao.insertLimit(limit)
        }
        return lastId
    }

    override suspend fun getLastLimitSource(): Int = limitDao.getLastSource()

    override suspend fun getLastColorClass(): ColorClassificationSoja? = colorClassificationDao.getLastColorClass()

    override suspend fun insertColorClassification(colorEntity: ColorClassificationSoja) {
        colorClassificationDao.insert(colorEntity)
    }

    override suspend fun getColorClassificationBySample(classificationId: Int): ColorClassificationSoja? {
        return colorClassificationDao.getByClassificationId(classificationId)
    }

    override suspend fun getDisqualificationByClassificationId(idClassification: Int): DisqualificationSoja? =
        disqualificationDao.getByClassificationId(classificationId = idClassification)

    override suspend fun insertDisqualification(disqualification: DisqualificationSoja): Long {
        return disqualificationDao.insert(disqualification)
    }

    override suspend fun updateDisqualification(classificationId: Int, finalType: Int) {
        val disqualificationId = disqualificationDao.getLastDisqualificationId()
        val defectSum = if (finalType == 0) 1 else 0
        disqualificationDao.updateClassificationId(disqualificationId, classificationId)
        disqualificationDao.updateGraveDefectSum(disqualificationId, defectSum)
    }

    // -------------------------------------------------------------------------
    // Retorna os limites do Tipo 1 oficial usando FieldKeys.* como chaves.
    // A LimitInputScreen lê essas chaves para preencher os campos de prefill.
    // -------------------------------------------------------------------------
    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = try { limitDao.getLimitsByType(grain, group, 1, 0) } catch (e: Exception) { null }
            ?: return emptyMap()
        return mapOf(
            FieldKeys.MOISTURE      to limit.moistureUpLim,
            FieldKeys.IMPURITIES    to limit.impuritiesUpLim,
            FieldKeys.BROKEN        to limit.brokenCrackedDamagedUpLim,
            FieldKeys.GREENISH      to limit.greenishUpLim,
            FieldKeys.BURNT         to limit.burntUpLim,
            FieldKeys.BURNT_OR_SOUR to limit.burntOrSourUpLim,
            FieldKeys.MOLDY         to limit.moldyUpLim,
            FieldKeys.SPOILED       to limit.spoiledTotalUpLim
        )
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitSoja? {
        return try { limitDao.getLimitsByType(grain, group, tipo, source) } catch (e: Exception) { null }
    }

    override suspend fun getLimitsByGroup(grain: String, group: Int, source: Int): List<LimitSoja> {
        return limitDao.getLimitsByGroup(grain, group, source)
    }

    override suspend fun getObservations(idClassification: Int, colorClass: ColorClassificationSoja?): String {
        val classification = classificationDao.getById(idClassification)
        return if (classification?.finalType == 0) "Desclassificada: excesso de defeitos graves." else " "
    }

    override suspend fun deleteCustomLimits() {
        limitDao.deleteCustomLimits()
    }

    override suspend fun getToxicSeedsByDisqualificationId(disqualificationId: Int): List<ToxicSeedSoja> {
        return toxicSeedDao.getToxicSeedsByDisqualificationId(disqualificationId)
    }

    override suspend fun insertToxicSeeds(seeds: List<ToxicSeedSoja>) {
        toxicSeedDao.insertAll(seeds)
    }
}