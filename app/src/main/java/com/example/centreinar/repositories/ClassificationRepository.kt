package com.example.centreinar.repositories

import com.example.centreinar.Classification
import com.example.centreinar.Sample
import com.example.centreinar.LimitCategory

interface ClassificationRepository {
    suspend fun classifySample(sample: Sample,limitSource: Int):Long
    suspend fun getSample(id: Int):Sample?
    suspend fun setSample(grain: String,group: Int, sampleWeight: Float, lotWeight: Float, foreignMattersAndImpurities: Float, humidity: Float,greenish: Float,brokenCrackedDamaged: Float,burnt: Float, sour: Float,moldy: Float, fermented: Float,germinated: Float,immature: Float): Sample
    suspend fun getClassification(id: Int): Classification?
    suspend fun getLimitsForGrain(grain: String, group: Int, limitSource: Int): Map<String, List<LimitCategory>>
    suspend fun getObservations(classification: Classification):String
    suspend fun getClass(classification: Classification,yellow:Float,otherColors:Float):String
}