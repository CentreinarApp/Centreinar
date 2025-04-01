package com.example.centreinar.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.launch
import com.example.centreinar.LimitDao
import com.example.centreinar.LimitCategory
import com.example.centreinar.Sample
import com.example.centreinar.Classification
import com.example.centreinar.ClassificationDao

class HomeViewModel(private val daoLimits: LimitDao, private val classificationDao: ClassificationDao) : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    init {
        fetchLimits()
    }

    private fun fetchLimits() {
        viewModelScope.launch(Dispatchers.IO) {  // Run in background thread
            val sample = Sample(
                grain = "corn",
                group = 2,
                weight = 135.8f,
                foreignMattersAndImpurities = 0.5f,
                humidity = 10.1f,
                greenish = 0.0f,
                brokenCrackedDamaged = 16.1f,
                burnt = 3.0f,
                sour = 0.1f,
                moldy = 5.2f,
                fermented = 1.5f,
                germinated = 0.0f,
                immature = 0.2f
            )
            val resultId = classify(sample)

            val resultClassification = withContext(Dispatchers.IO) {
                classificationDao.getById(resultId.toInt())
            }

            withContext(Dispatchers.Main) {
                _text.value = resultClassification.toString()
            }
        }
    }

    private suspend fun classify(sample: Sample): Long = withContext(Dispatchers.IO) {
        val grain = "Corn"

        val limitBrokenList = daoLimits.getLimitsForBrokenCrackedDamaged(grain, 2)
        val limitGreenishList = daoLimits.getLimitsForGreenish(grain, 2)
        val limitMoldyList = daoLimits.getLimitsForMoldy(grain, 2)
        val limitBurntList = daoLimits.getLimitsForBurnt(grain, 2)
        val limitBurntOrSourList = daoLimits.getLimitsForBurntOrSour(grain, 2)
        val limitSpoiledList = daoLimits.getLimitsForSpoiledTotal(grain, 2)

        val percentageBroken = turnIntoPercentage(sample.brokenCrackedDamaged, sample.weight)
        val percentageGreenish = turnIntoPercentage(sample.greenish, sample.weight)
        val percentageMoldy = turnIntoPercentage(sample.moldy, sample.weight)
        val percentageBurnt = turnIntoPercentage(sample.burnt, sample.weight)
        val percentageBurntOrSour = turnIntoPercentage(sample.sour + sample.burnt, sample.weight)
        val percentageSpoiled = turnIntoPercentage(
            sample.moldy + sample.fermented + sample.sour + sample.germinated + sample.immature,
            sample.weight
        )


        val brokenType = betweenAInterval(limitBrokenList, percentageBroken)
        val greenishType = betweenAInterval(limitGreenishList, percentageGreenish)
        val moldyType = betweenAInterval(limitMoldyList, percentageMoldy)
        val burntType = betweenAInterval(limitBurntList, percentageBurnt)
        val burntOrSourType = betweenAInterval(limitBurntOrSourList, percentageBurntOrSour)
        val spoiledType = betweenAInterval(limitSpoiledList, percentageSpoiled)

        val finalType = listOf(brokenType, greenishType, moldyType, burntType, burntOrSourType, spoiledType).maxOrNull() ?: 0

        val impuritiesType = if (sample.foreignMattersAndImpurities > 1) 7 else finalType

        val classification = Classification(
            grain = sample.grain,
            group = sample.group,
            sampleId = sample.id,
            foreignMatters = impuritiesType,
            brokenCrackedDamaged = brokenType,
            greenish = greenishType,
            moldy = moldyType,
            burnt = burntType,
            burntOrSour = burntOrSourType,
            spoiled = spoiledType,
            finalType = finalType
        )

        return@withContext classificationDao.insert(classification)
    }

    private fun betweenAInterval(intervals: List<LimitCategory>, value: Float): Int {
        if(value == 0.0f) return 1
        for (interval in intervals) {
            if (interval.lowerL < value && interval.upperL >= value) {
                return interval.type
            }
        }
        return 7
    }

    private fun turnIntoPercentage(defect: Float, weight: Float): Float {
        return (defect * 100) / weight
    }
}