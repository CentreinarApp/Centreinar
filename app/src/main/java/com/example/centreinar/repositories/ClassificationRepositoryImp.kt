package com.example.centreinar.repositories

import com.example.centreinar.Classification
import com.example.centreinar.ClassificationDao
import com.example.centreinar.LimitCategory
import com.example.centreinar.LimitDao
import com.example.centreinar.Sample
import com.example.centreinar.SampleDao
import com.example.centreinar.Utilities

import javax.inject.Inject

class ClassificationRepositoryImpl @Inject constructor(
    private val limitDao: LimitDao,
    private val classificationDao: ClassificationDao,
    private val sampleDao: SampleDao,
    private val tools : Utilities

) : ClassificationRepository {

    override suspend fun classifySample(sample: Sample,limitSource: Int): Long {

        val limitMap = getLimitsForGrain(sample.grain,sample.group,limitSource)

        val grain = sample.grain

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
            sample.moldy + sample.fermented + sample.sour + sample.germinated + sample.immature,
            cleanWeight
        )

        val brokenType = tools.findCategoryForValue(limitBrokenList, percentageBroken)
        val greenishType = tools.findCategoryForValue(limitGreenishList, percentageGreenish)
        val moldyType = tools.findCategoryForValue(limitMoldyList, percentageMoldy)
        val burntType = tools.findCategoryForValue(limitBurntList, percentageBurnt)
        val burntOrSourType = tools.findCategoryForValue(limitBurntOrSourList, percentageBurntOrSour)
        val spoiledType = tools.findCategoryForValue(limitSpoiledList, percentageSpoiled)

        val finalType = listOf(brokenType, greenishType, moldyType, burntType, burntOrSourType, spoiledType).maxOrNull() ?: 0

        val impuritiesType = if (percentageImpurities > 1) 7 else finalType

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
            finalType = finalType
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
                "broken" to limitDao.getLimitsForBrokenCrackedDamaged(grain, group,limitSource),
                "greenish" to limitDao.getLimitsForGreenish(grain, group, limitSource),
                "burnt" to limitDao.getLimitsForBurnt(grain, group,limitSource),
                "burntOrSour" to limitDao.getLimitsForBurntOrSour(grain, group,limitSource),
                "moldy" to limitDao.getLimitsForMoldy(grain, group,limitSource),
                "spoiled" to limitDao.getLimitsForSpoiledTotal(grain, group,limitSource)
            )

    }
}