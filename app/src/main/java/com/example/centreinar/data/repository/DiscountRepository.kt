package com.example.centreinar.data.repository

import com.example.centreinar.Discount
import com.example.centreinar.InputDiscount
import com.example.centreinar.Limit

interface DiscountRepository{
    suspend fun calculateDiscount(grain:String, group:Int, tipo:Int, sample: InputDiscount,doesTechnicalLoss:Boolean,doesClassificationLoss:Boolean, doesDeduction:Boolean): Long
    suspend fun calculateDiscount(grain:String, group:Int, tipo:Int, sample: InputDiscount, limit: Map<String,Float>, doesTechnicalLoss:Boolean,doesClassificationLoss:Boolean, doesDeduction:Boolean): Long
    suspend fun calculateTechnicalLoss(storageDays: Int, humidityAndImpuritiesLoss:Float, lotWeight:Float):Float
    suspend fun calculateDeduction(deductionValue:Float,classificationLoss:Float):Float
    suspend fun getDiscountById(id: Long): Discount?
    suspend fun getLimitsByType(grain:String, group:Int, tipo:Int,limitSource: Int):Map<String,Float>
    suspend fun setLimit(grain:String,
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
    suspend fun getLimit(grain: String, group: Int, tipo: Int, source: Int): Limit
    suspend fun getLimitOfType1Official(group: Int, grain: String): Map<String, Float>
}

//implement a way of creating custom limits