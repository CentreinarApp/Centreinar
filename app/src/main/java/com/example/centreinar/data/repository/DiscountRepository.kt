package com.example.centreinar.data.repository

import com.example.centreinar.Discount
import com.example.centreinar.InputDiscount

interface DiscountRepository{
    suspend fun calculateDiscount(grain:String, group:Int, tipo:Int, sample: InputDiscount,doesTechnicalLoss:Boolean,doesClassificationLoss:Boolean, doesDeduction:Boolean): Long
    suspend fun calculateDiscount(grain:String, group:Int, tipo:Int, sample: InputDiscount, limit: Map<String,Float>, doesTechnicalLoss:Boolean,doesClassificationLoss:Boolean, doesDeduction:Boolean): Long
    suspend fun calculateTechnicalLoss(storageDays: Int, humidityAndImpuritiesLoss:Float, lotWeight:Float):Float
    suspend fun calculateDeduction(deductionValue:Float,classificationLoss:Float):Float
    suspend fun getDiscountById(id: Long): Discount?
    suspend fun getLimitsByType(grain:String, group:Int, tipo:Int,limitSource: Int):Map<String,Float>
}

//implement a way of creating custom limits