package com.example.centreinar.repositories

import com.example.centreinar.Classification
import com.example.centreinar.ClassificationDao
import com.example.centreinar.ColorClassification
import com.example.centreinar.ColorClassificationDao
import com.example.centreinar.Disqualification
import com.example.centreinar.DisqualificationDao
import com.example.centreinar.LimitCategory
import com.example.centreinar.LimitDao
import com.example.centreinar.Sample
import com.example.centreinar.SampleDao
import com.example.centreinar.Utilities

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassificationRepositoryImpl @Inject constructor(
    private val limitDao: LimitDao,
    private val classificationDao: ClassificationDao,
    private val sampleDao: SampleDao,
    private val tools : Utilities,
    private val colorClassificationDao: ColorClassificationDao,
    private val disqualificationDao: DisqualificationDao

) : ClassificationRepository {

    override suspend fun classifySample(sample: Sample,limitSource: Int): Long {

        val limitMap = getLimitsForGrain(sample.grain,sample.group,limitSource)


        val limitImpuritiesList = limitMap["impurities"]?: emptyList()
        val limitBrokenList = limitMap["broken"]?: emptyList()
        val limitGreenishList = limitMap["greenish"]?: emptyList()
        val limitMoldyList = limitMap["moldy"]?: emptyList()
        val limitBurntList = limitMap["burnt"]?: emptyList()
        val limitBurntOrSourList = limitMap["burntOrSour"]?: emptyList()
        val limitSpoiledList = limitMap["spoiled"]?: emptyList()

        val percentageImpurities =  tools.calculateDefectPercentage(sample.foreignMattersAndImpurities, sample.sampleWeight)
        val cleanWeight = sample.sampleWeight * (100-percentageImpurities)/100

        val percentageBroken = tools.calculateDefectPercentage(sample.brokenCrackedDamaged, cleanWeight)
        val percentageGreenish = tools.calculateDefectPercentage(sample.greenish, cleanWeight)
        val percentageMoldy = tools.calculateDefectPercentage(sample.moldy, cleanWeight)
        val percentageBurnt = tools.calculateDefectPercentage(sample.burnt, cleanWeight)
        val percentageBurntOrSour = tools.calculateDefectPercentage(sample.sour + sample.burnt,cleanWeight)
        val percentageSpoiled = tools.calculateDefectPercentage(
            sample.moldy + sample.fermented + sample.sour + sample.burnt  + sample.germinated + sample.immature + sample.shriveled + sample.damaged,
            cleanWeight
        )

        val impuritiesType = tools.findCategoryForValue(limitImpuritiesList, percentageImpurities)
        val brokenType = tools.findCategoryForValue(limitBrokenList, percentageBroken)
        val greenishType = tools.findCategoryForValue(limitGreenishList, percentageGreenish)
        val moldyType = tools.findCategoryForValue(limitMoldyList, percentageMoldy)
        val burntType = tools.findCategoryForValue(limitBurntList, percentageBurnt)
        val burntOrSourType = tools.findCategoryForValue(limitBurntOrSourList, percentageBurntOrSour)
        val spoiledType = tools.findCategoryForValue(limitSpoiledList, percentageSpoiled)

        var finalType = listOf(brokenType, greenishType, moldyType, burntType, burntOrSourType, spoiledType,impuritiesType).maxOrNull() ?: 0

        var isDisqualify = false

        if(sample.group == 1){
            if(percentageBurntOrSour+percentageMoldy > 12){
                isDisqualify = true
            }
        }
        if(sample.group == 2){
            if(percentageBurntOrSour+percentageMoldy > 40){
                isDisqualify = true
            }
        }
        if(isDisqualify){
            finalType = 0
        }

        val classification = Classification(
            grain = sample.grain,
            group = sample.group,
            sampleId = sample.id,
            foreignMattersPercentage = percentageImpurities,
            brokenCrackedDamagedPercentage = percentageBroken,
            greenishPercentage = percentageGreenish,
            moldyPercentage = percentageMoldy,
            burntPercentage = percentageBurnt,
            burntOrSourPercentage = percentageBurntOrSour,
            spoiledPercentage = percentageSpoiled,
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

    override suspend fun getSample(id: Int):Sample?{
        return sampleDao.getById(id)
    }

    override  suspend fun setSample(grain: String,group: Int, sampleWeight: Float, lotWeight: Float, foreignMattersAndImpurities: Float, humidity: Float,greenish: Float,brokenCrackedDamaged: Float,burnt: Float, sour: Float,moldy: Float, fermented: Float,germinated: Float,immature: Float): Sample{
        return Sample(grain = grain,group = group , sampleWeight = sampleWeight,lotWeight = lotWeight, foreignMattersAndImpurities = foreignMattersAndImpurities, humidity = humidity, greenish = greenish, brokenCrackedDamaged = brokenCrackedDamaged, burnt = burnt, sour = sour,moldy = moldy, fermented = fermented,germinated = germinated,immature = immature)
    }

    override suspend fun getClassification(id: Int): Classification? {
        return classificationDao.getById(id)
    }

    override suspend fun getLimitsForGrain(grain: String, group: Int, limitSource: Int): Map<String, List<LimitCategory>>{

            return mapOf(
                "impurities" to limitDao.getLimitsForImpurities(grain,group,limitSource),
                "broken" to limitDao.getLimitsForBrokenCrackedDamaged(grain, group,limitSource),
                "greenish" to limitDao.getLimitsForGreenish(grain, group, limitSource),
                "burnt" to limitDao.getLimitsForBurnt(grain, group,limitSource),
                "burntOrSour" to limitDao.getLimitsForBurntOrSour(grain, group,limitSource),
                "moldy" to limitDao.getLimitsForMoldy(grain, group,limitSource),
                "spoiled" to limitDao.getLimitsForSpoiledTotal(grain, group,limitSource)
            )

    }

    override suspend fun setObservations(classification: Classification): String {
        var observation = ""
        val disqualification = disqualificationDao.getByClassificationId(classification.id)
        var count = 1
        if(classification.finalType == 0 || classification.finalType == 7) {

            if (classification.foreignMatters == 7){
                observation += "$count - Matéria Estranhas e Impurezas fora de tipo \n"
                count++
            }
            if (classification.burnt == 7){
                observation += "$count - Queimados fora de tipo \n"
                count++
            }
            if (classification.burntOrSour == 7){
                observation += "$count - Ardidos e Queimados fora de tipo \n"
                count++
            }
            if (classification.moldy == 7){
                observation += "$count - Mofados fora de tipo \n"
                count++
            }
            if (classification.spoiled == 7){
                observation += "$count - Total de Avariados fora de tipo \n"
                count++
            }
            if (classification.greenish == 7){
                observation += "$count - Esverdeados fora de tipo \n"
                count++
            }
            if (classification.brokenCrackedDamaged == 7){
                observation += "$count - Partidos, Quebrados e Amassados fora de tipo \n"
                count++
            }

            if(classification.finalType == 0){
                observation += "$count - Desclassificado pois soma de defeitos graves ultrapassa o limite de "
                count++
                if(classification.group == 1){
                    observation+= "12%.\n"
                }
                else {
                    observation+= "40%.\n"
                }
            }
        }

        if (disqualification.badConservation){
            observation +="$count - Desclassificado devido ao mal estado de conservação.\n"
            count++
        }
        if(disqualification.strangeSmell){
            observation +="$count - Desclassificado devido a presença de odor estranho no produto.\n"
            count++
        }
        if(disqualification.toxicGrains){
            observation +="$count - Desclassificado devido a presença de sementes toxicas.\n"
            count++
        }
        if(disqualification.insects){
            observation +="$count - Desclassificado devido a presença de insetos vivos, mortos ou partes desses no produto.\n"
            count++
        }

        return observation
    }

    override suspend fun setClass(classification: Classification, yellow: Float, otherColors: Float):ColorClassification {

        val sample: Sample = getSample(classification.sampleId)!!
        val otherColorsPercentage = tools.calculateDefectPercentage(otherColors, sample.cleanWeight)
        var framingClass = " "
        if(otherColorsPercentage > 10.0f) {
            framingClass = "Misturada"
        }
        else {
            framingClass = "Amarela"
        }
        val colorClassification = ColorClassification(
            grain = sample.grain,
            classificationId = classification.id,
            yellowPercentage =  tools.calculateDefectPercentage(yellow, sample.cleanWeight),
           otherColorPercentage = otherColorsPercentage,
            framingClass = framingClass
        )
        colorClassificationDao.insert(colorClassification)
        return colorClassification
    }

    override suspend fun setDisqualification(classificationId: Int,badConservation: Boolean, graveDefectSum: Boolean, strangeSmell: Boolean, toxicGrains: Boolean, insects:Boolean): Long {

       return disqualificationDao.insert(
           Disqualification(
            classificationId = classificationId,
            badConservation = badConservation,
            graveDefectSum = graveDefectSum,
            strangeSmell = strangeSmell,
            toxicGrains = toxicGrains, insects = insects
           )
       )
    }

}