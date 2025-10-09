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

        // percentuais principais
        val percentageImpurities = tools.calculateDefectPercentage(sample.foreignMattersAndImpurities, sample.sampleWeight)
        val cleanWeight = sample.sampleWeight * (100 - percentageImpurities) / 100

        val percentageBroken = tools.calculateDefectPercentage(sample.brokenCrackedDamaged, cleanWeight)
        val percentageGreenish = tools.calculateDefectPercentage(sample.greenish, cleanWeight)
        val percentageMoldy = tools.calculateDefectPercentage(sample.moldy, cleanWeight)
        val percentageBurnt = tools.calculateDefectPercentage(sample.burnt, cleanWeight)
        val percentageSour = tools.calculateDefectPercentage(sample.sour, cleanWeight)
        val percentageBurntOrSour = percentageBurnt + percentageSour
        val percentageSpoiled = tools.calculateDefectPercentage(
            sample.moldy + sample.fermented + sample.sour + sample.burnt + sample.germinated + sample.immature + sample.shriveled + sample.damaged,
            cleanWeight
        )

        // outros defeitos
        val percentageDamaged = tools.calculateDefectPercentage(sample.damaged, cleanWeight)
        val percentageFermented = tools.calculateDefectPercentage(sample.fermented, cleanWeight)
        val percentageGerminated = tools.calculateDefectPercentage(sample.germinated, cleanWeight)
        val percentageImmature = tools.calculateDefectPercentage(sample.immature, cleanWeight)
        val percentageShriveled = tools.calculateDefectPercentage(sample.shriveled, cleanWeight)

        // categorias
        val impuritiesType = tools.findCategoryForValue(
            limitMap["impurities"]?.map { it.lowerL to it.upperL } ?: emptyList(),
            percentageImpurities
        )
        val brokenType = tools.findCategoryForValue(
            limitMap["broken"]?.map { it.lowerL to it.upperL } ?: emptyList(),
            percentageBroken
        )
        val greenishType = tools.findCategoryForValue(
            limitMap["greenish"]?.map { it.lowerL to it.upperL } ?: emptyList(),
            percentageGreenish
        )
        val moldyType = tools.findCategoryForValue(
            limitMap["moldy"]?.map { it.lowerL to it.upperL } ?: emptyList(),
            percentageMoldy
        )
        val burntType = tools.findCategoryForValue(
            limitMap["burnt"]?.map { it.lowerL to it.upperL } ?: emptyList(),
            percentageBurnt
        )
        val burntOrSourType = tools.findCategoryForValue(
            limitMap["burntOrSour"]?.map { it.lowerL to it.upperL } ?: emptyList(),
            percentageBurntOrSour
        )
        val spoiledType = tools.findCategoryForValue(
            limitMap["spoiled"]?.map { it.lowerL to it.upperL } ?: emptyList(),
            percentageSpoiled
        )

        var finalType = listOf(
            brokenType, greenishType, moldyType, burntType, burntOrSourType, spoiledType, impuritiesType
        ).maxOrNull() ?: 0

        var isDisqualify = false
        if (sample.group == 1 && percentageBurntOrSour + percentageMoldy > 12) isDisqualify = true
        if (sample.group == 2 && percentageBurntOrSour + percentageMoldy > 40) isDisqualify = true
        if (isDisqualify) finalType = 0

        val classification = ClassificationSoja(
            grain = sample.grain,
            group = sample.group,
            sampleId = sampleId.toInt(),
            foreignMattersPercentage = percentageImpurities,
            brokenCrackedDamagedPercentage = percentageBroken,
            greenishPercentage = percentageGreenish,
            moldyPercentage = percentageMoldy,
            burntPercentage = percentageBurnt,
            burntOrSourPercentage = percentageBurntOrSour,
            spoiledPercentage = percentageSpoiled,
            damagedPercentage = percentageDamaged,
            sourPercentage = percentageSour,
            fermentedPercentage = percentageFermented,
            germinatedPercentage = percentageGerminated,
            immaturePercentage = percentageImmature,
            shriveledPercentage = percentageShriveled,
            foreignMatters = impuritiesType,
            brokenCrackedDamaged = brokenType,
            greenish = greenishType,
            moldy = moldyType,
            burnt = burntType,
            burntOrSour = burntOrSourType,
            spoiled = spoiledType,
            finalType = finalType,
        )

        return classificationDao.insert(classification)
    }

    override suspend fun getSample(id: Int): SampleSoja? = sampleDao.getById(id)

    override suspend fun setSample(
        grain: String, group: Int, sampleWeight: Float, lotWeight: Float,
        foreignMattersAndImpurities: Float, humidity: Float,
        greenish: Float, brokenCrackedDamaged: Float, burnt: Float,
        sour: Float, moldy: Float, fermented: Float, germinated: Float, immature: Float
    ): SampleSoja {
        return SampleSoja(
            grain = grain, group = group, sampleWeight = sampleWeight, lotWeight = lotWeight,
            foreignMattersAndImpurities = foreignMattersAndImpurities, humidity = humidity,
            greenish = greenish, brokenCrackedDamaged = brokenCrackedDamaged,
            burnt = burnt, sour = sour, moldy = moldy, fermented = fermented,
            germinated = germinated, immature = immature
        )
    }

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
            grain = grain,
            classificationId = classificationId,
            yellowPercentage = tools.calculatePercentage(totalWeight - otherColors, totalWeight),
            otherColorPercentage = otherColorsPercentage,
            framingClass = framingClass
        )
        colorClassificationDao.insert(colorClassification)
        return colorClassification
    }

    override suspend fun setDisqualification(
        classificationId: Int, badConservation: Int, graveDefectSum: Int,
        strangeSmell: Int, toxicGrains: Int, insects: Int
    ): Long {
        return disqualificationDao.insert(
            DisqualificationSoja(
                classificationId = classificationId,
                badConservation = badConservation,
                graveDefectSum = graveDefectSum,
                strangeSmell = strangeSmell,
                toxicGrains = toxicGrains,
                insects = insects
            )
        )
    }

    override suspend fun setLimit(
        grain: String, group: Int, type: Int,
        impurities: Float, moisture: Float, brokenCrackedDamaged: Float,
        greenish: Float, burnt: Float, burntOrSour: Float, moldy: Float, spoiled: Float
    ): Long {
        val lastSource = limitDao.getLastSource()
        val source = lastSource + 1
        val limit = LimitSoja(
            source = source, grain = grain, group = group, type = type,
            impuritiesLowerLim = 0.0f, impuritiesUpLim = impurities,
            moistureLowerLim = 0.0f, moistureUpLim = moisture,
            brokenCrackedDamagedLowerLim = 0.0f, brokenCrackedDamagedUpLim = brokenCrackedDamaged,
            greenishLowerLim = 0.0f, greenishUpLim = greenish,
            burntLowerLim = 0.0f, burntUpLim = burnt,
            burntOrSourLowerLim = 0.0f, burntOrSourUpLim = burntOrSour,
            moldyLowerLim = 0.0f, moldyUpLim = moldy,
            spoiledTotalLowerLim = 0.0f, spoiledTotalUpLim = spoiled
        )
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLastLimitSource(): Int = limitDao.getLastSource()
    override suspend fun getLastColorClass(): ColorClassificationSoja? = colorClassificationDao.getLastColorClass()
    override suspend fun getDisqualificationByClassificationId(idClassification: Int): DisqualificationSoja? =
        disqualificationDao.getByClassificationId(classificationId = idClassification)

    override suspend fun updateDisqualification(classificationId: Int, finalType: Int) {
        val disqualificationId = disqualificationDao.getLastDisqualificationId()
        val defectSum = if (finalType == 0) 1 else 0
        disqualificationDao.updateClassificationId(disqualificationId, classificationId)
        disqualificationDao.updateGraveDefectSum(disqualificationId, defectSum)
    }

    // CORREÇÃO DE LÓGICA APLICADA AQUI
    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {

        // 1. Tenta buscar o limite oficial (source=0) e lida com o caso de ser nulo (Limites não encontrados)
        val limit: LimitSoja? = try {
            limitDao.getLimitsByType(grain, group, 1, 0)
        } catch (e: Exception) {
            Log.e("Repo", "Exceção ao buscar limite oficial: ${e.message}")
            null
        }

        // 2. Se o limite for encontrado, faz o mapeamento. Caso contrário, retorna um mapa vazio.
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
            Log.w("Repo", "Limites oficiais (Source 0) não encontrados para Grão: $grain, Grupo: $group. Retornando mapa vazio.")
            emptyMap()
        }
    }

    override suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): LimitSoja =
        limitDao.getLimitsByType(grain, group, tipo, source)!! // **ATENÇÃO**: Isso pode falhar se não for o limite oficial.

    override suspend fun getObservations(idClassification: Int, colorClass: ColorClassificationSoja?): String {
        val classification = classificationDao.getById(idClassification)
        var response = " "
        if (classification != null) {
            if (classification.finalType == 0) {
                val percentage = if (classification.group == 2) 40 else 12
                response += "Desclassificada pois soma de defeitos graves excede o limite de $percentage%.\n "
            }
            if (classification.finalType == 7) {
                if (classification.foreignMatters == 7) response += "Fora de tipo: Matéria Estranha/Impurezas acima do limite.\n"
                if (classification.burnt == 7) response += "Fora de tipo: grãos queimados acima do limite.\n"
                if (classification.burntOrSour == 7) response += "Fora de tipo: soma de queimados e ardidos acima do limite.\n"
                if (classification.moldy == 7) response += "Fora de tipo: grãos mofados acima do limite.\n"
                if (classification.spoiled == 7) response += "Fora de tipo: total de grãos avariados acima do limite.\n"
                if (classification.greenish == 7) response += "Fora de tipo: grãos esverdeados acima do limite.\n"
                if (classification.brokenCrackedDamaged == 7) response += "Fora de tipo: partidos/amassados acima do limite.\n"
            }
            val disqualification = disqualificationDao.getByClassificationId(idClassification)
            if (disqualification != null) {
                if (disqualification.insects == 1) response += "Desclassificado: presença de insetos.\n"
                if (disqualification.toxicGrains == 1) response += "Desclassificado: sementes tóxicas.\n"
                if (disqualification.strangeSmell == 1) response += "Desclassificado: odor estranho.\n"
                if (disqualification.badConservation == 1) response += "Desclassificado: mal estado de conservação.\n"
            }
            val colorClassification = getLastColorClass()
            if (colorClassification != null && colorClassification.otherColorPercentage > 10.0f) {
                response += "Amostra de Classe Misturada.\n"
            }
        }
        return response
    }
}