package com.example.centreinar.data.repository

import android.util.Log
import com.example.centreinar.Classification
import com.example.centreinar.ColorClassification
import com.example.centreinar.Disqualification
import com.example.centreinar.Limit
import com.example.centreinar.Sample
import com.example.centreinar.data.local.dao.ClassificationDao
import com.example.centreinar.data.local.dao.ColorClassificationDao
import com.example.centreinar.data.local.dao.DisqualificationDao
import com.example.centreinar.data.local.dao.LimitDao
import com.example.centreinar.data.local.dao.SampleDao
import com.example.centreinar.domain.model.LimitCategory
import com.example.centreinar.util.Utilities
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

        val sampleId = setSample(sample)

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
            sampleId = sampleId.toInt(),
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

    override suspend fun setSample(sample: Sample): Long {
        return sampleDao.insert(sample)
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

        if (disqualification.badConservation == 0){
            observation +="$count - Desclassificado devido ao mal estado de conservação.\n"
            count++
        }
        if(disqualification.strangeSmell == 0){
            observation +="$count - Desclassificado devido a presença de odor estranho no produto.\n"
            count++
        }
        if(disqualification.toxicGrains == 0){
            observation +="$count - Desclassificado devido a presença de sementes toxicas.\n"
            count++
        }
        if(disqualification.insects == 0){
            observation +="$count - Desclassificado devido a presença de insetos vivos, mortos ou partes desses no produto.\n"
            count++
        }

        return observation
    }

    override suspend fun setClass(grain:String, classificationId: Int, totalWeight: Float, otherColors: Float):ColorClassification {

        val otherColorsPercentage = tools.calculatePercentage(otherColors, totalWeight)
        var framingClass = " "
        if(otherColorsPercentage > 10.0f) {
            framingClass = "Misturada"
        }
        else {
            framingClass = "Amarela"
        }

        val colorClassification = ColorClassification(
            grain = grain,
            classificationId = classificationId,
            yellowPercentage =  tools.calculatePercentage(totalWeight-otherColors,totalWeight),
           otherColorPercentage = otherColorsPercentage,
            framingClass = framingClass
        )
        colorClassificationDao.insert(colorClassification)
        return colorClassification
    }

    override suspend fun setDisqualification(classificationId: Int,badConservation: Int, graveDefectSum: Int, strangeSmell: Int, toxicGrains: Int, insects:Int): Long {

       return disqualificationDao.insert(
           Disqualification(
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
        grain:String,
        group:Int,
        type:Int,
        impurities:Float,
        brokenCrackedDamaged: Float,
        greenish: Float,
        burnt:Float,
        burntOrSour:Float,
        moldy:Float,
        spoiled:Float
    ):Long {
        val lastSource = limitDao.getLastSource()
        val source = lastSource + 1
        val limit = Limit(
            source = source,
            grain = grain,
            group = group,
            type = type,
            impuritiesLowerLim = 0.0f,
            impuritiesUpLim = impurities,
            brokenCrackedDamagedLowerLim = 0.0f,
            brokenCrackedDamagedUpLim = brokenCrackedDamaged,
            greenishLowerLim = 0.0f,
            greenishUpLim = greenish,
            burntLowerLim = 0.0f,
            burntUpLim = burnt,
            burntOrSourLowerLim = 0.0f,
            burntOrSourUpLim = burntOrSour,
            moldyLowerLim = 0.0f,
            moldyUpLim = moldy,
            spoiledTotalLowerLim = 0.0f,
            spoiledTotalUpLim = spoiled
        )
        return limitDao.insertLimit(limit)
    }

    override suspend fun getLastLimitSource():Int {
        return limitDao.getLastSource()
    }

    override suspend fun updateDisqualification(classificationId: Int, finalType: Int) {
        val disqualificationId = disqualificationDao.getLastDisqualificationId()
        var defectSum = 0
        if(finalType == 0){
            defectSum = 1
        }
        disqualificationDao.updateClassificationId(disqualificationId,classificationId)
        disqualificationDao.updateGraveDefectSum(disqualificationId, defectSum)
    }

    override suspend fun getObservations(idClassification: Int):String{
//        val classification = getClassification(idClassification)
        val classification = classificationDao.getById(idClassification)
        var response = " "
        if (classification != null) {
            Log.e("Observations","The classification exists")
            if(classification.finalType == 0){
                var percentage = 12
                if(classification.group == 2) {percentage = 40}
                response +="Desclassificada pois soma de defeitos graves excede o limite de $percentage%.\n "
                Log.e("Observations","Observations: ${response}")
            }
            if(classification.finalType == 7){
                if(classification.foreignMatters == 7){
                    response+="Fora de tipo pois a porcentagem de Matéria Estranha e Impurezas excedeu o limite.\n"
                    Log.e("Observations","Observations: ${response}")

                }
                if(classification.burnt == 7){
                    response+="Fora de tipo pois a porcentagem de grãos queimados excedeu o limite.\n"
                    Log.e("Observations","Observations: ${response}")
                }
                if(classification.burntOrSour == 7){
                    response+="Fora de tipo pois a soma de grãos queimados e ardidos excedeu o limite.\n"
                    Log.e("Observations","Observations: ${response}")
                }

                if(classification.moldy == 7){
                    response+="Fora de tipo pois a porcentagem de grãos mofados excedeu o limite.\n"
                    Log.e("Observations","Observations: ${response}")
                }
                if(classification.spoiled == 7){
                    response+="Fora de tipo pois o total de grãos avariados excedeu o limite.\n"
                    Log.e("Observations","Observations: ${response}")
                }
                if(classification.greenish == 7){
                    response+="Fora de tipo pois a porcentagem de grãos esverdeados excedeu o limite.\n"
                    Log.e("Observations","Observations: ${response}")

                }
                if(classification.brokenCrackedDamaged == 7){
                    response+="Fora de tipo pois a porcentagem de grãos Partidos,Queimados e Amassados excedeu o limite.\n"
                    Log.e("Observations","Observations: ${response}")

                }
            }
            val disqualification  = disqualificationDao.getByClassificationId(classificationId = idClassification)
            if(disqualification != null){
                if(disqualification.insects == 1){
                    //make two different messages in case of which group the sample is being classify in
                    response += "Desclassificado devido a presença de insetos.\n"
                    Log.e("Observations","Observations: ${response}")

                }
                if(disqualification.toxicGrains == 1){
                    response += "Desclassificado devido a presença de sementes tóxicas.\n"
                    Log.e("Observations","Observations: ${response}")

                }
                if(disqualification.strangeSmell == 1){
                    response += "Desclassificado devido a presença de odor estranho.\n"
                    Log.e("Observations","Observations: ${response}")

                }
                if(disqualification.badConservation == 1){
                    response += "Desclassificado devido ao mal estado de conservação do lote.\n"
                    Log.e("Observations","Observations: ${response}")
                }
            }
            val colorClass = colorClassificationDao.getByClassificationId(idClassification)
            if(colorClass != null){
                if(colorClass.otherColorPercentage > 10.0f){
                    response += "Amostra de Classe Misturada.\n"
                    Log.e("Observations","Observations: ${response}")
                }
            }

        }
        Log.e("Observations","Observations: ${response}")
        return response
    }

    override suspend fun getLastColorClass(): ColorClassification {
       return colorClassificationDao.getLastColorClass()
    }

    override suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float> {
        val limit = limitDao.getLimitsByType(grain,group,1,0)
        return mapOf(
            "impuritiesLowerLim" to limit.impuritiesLowerLim,
            "impuritiesUpLim" to limit.impuritiesUpLim,
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
}