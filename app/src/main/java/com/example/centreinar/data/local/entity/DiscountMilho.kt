package com.example.centreinar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "discount_milho")
data class DiscountMilho(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "inputDiscountId") val inputDiscountId: Int,

    @ColumnInfo(name = "impuritiesLoss") val impuritiesLoss: Float,
    @ColumnInfo(name = "humidityLoss") val humidityLoss: Float,
    @ColumnInfo(name = "technicalLoss") val technicalLoss: Float,
    @ColumnInfo(name = "brokenLoss") val brokenLoss: Float,
    @ColumnInfo(name = "ardidoLoss") val ardidoLoss: Float,
    @ColumnInfo(name = "carunchadoLoss") val carunchadoLoss: Float,
    @ColumnInfo(name = "spoiledLoss") val spoiledLoss: Float,

    @ColumnInfo(name = "deduction") val deduction: Float = 0f,
    @ColumnInfo(name = "deductionPrice") val deductionPrice: Float = 0f,

    @ColumnInfo(name = "impuritiesLossPrice") val impuritiesLossPrice: Float,
    @ColumnInfo(name = "humidityLossPrice") val humidityLossPrice: Float,
    @ColumnInfo(name = "technicalLossPrice") val technicalLossPrice: Float,
    @ColumnInfo(name = "brokenLossPrice") val brokenLossPrice: Float,
    @ColumnInfo(name = "ardidoLossPrice") val ardidoLossPrice: Float,
    @ColumnInfo(name = "carunchadoLossPrice") val carunchadoLossPrice: Float,
    @ColumnInfo(name = "spoiledLossPrice") val spoiledLossPrice: Float,

    @ColumnInfo(name = "finalDiscount") val finalDiscount: Float,
    @ColumnInfo(name = "finalWeight") val finalWeight: Float,

    @ColumnInfo(name = "finalDiscountPrice") val finalDiscountPrice: Float,
    @ColumnInfo(name = "finalWeightPrice") val finalWeightPrice: Float
)