package com.example.centreinar.data.repository

import com.example.centreinar.Classification
import com.example.centreinar.ColorClassification
import com.example.centreinar.Sample
import com.example.centreinar.domain.model.LimitCategory

interface ClassificationRepository {
    suspend fun classifySample(sample: Sample, limitSource: Int): Long
    suspend fun getSample(id: Int): Sample?
    suspend fun setSample(
        grain: String,
        group: Int,
        sampleWeight: Float,
        lotWeight: Float,
        foreignMattersAndImpurities: Float,
        humidity: Float,
        greenish: Float,
        brokenCrackedDamaged: Float,
        burnt: Float,
        sour: Float,
        moldy: Float,
        fermented: Float,
        germinated: Float,
        immature: Float
    ): Sample
    suspend fun setSample(sample: Sample):Long
    suspend fun getClassification(id: Int): Classification?
    suspend fun getLimitsForGrain(
        grain: String,
        group: Int,
        limitSource: Int
    ): Map<String, List<LimitCategory>>

    suspend fun setObservations(classification: Classification): String
    suspend fun setClass(
        grain:String,
        classificationId: Int,
        totalWeight: Float,
        otherColors: Float
    ): ColorClassification

    suspend fun setDisqualification(
        classificationId: Int,
        badConservation: Int,
        graveDefectSum: Int,
        strangeSmell: Int,
        toxicGrains: Int,
        insects: Int
    ): Long

    suspend fun setLimit(
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
    ): Long

    suspend fun getLastLimitSource():Int
    suspend fun getLastColorClass():ColorClassification
    suspend fun updateDisqualification(classificationId:Int,finalType:Int)
    suspend fun getLimitOfType1Official(group:Int,grain:String):Map<String,Float>
    suspend fun getObservations(idClassification:Int):String


}