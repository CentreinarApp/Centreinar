package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


class DiscountSoja {
@Entity(tableName = "discount_soja")
data class DiscountSoja(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "inputDiscountId") val inputDiscountId: Int,

    @ColumnInfo(name = "impuritiesLoss") val impuritiesLoss: Float,
    @ColumnInfo(name = "humidityLoss") val humidityLoss: Float,
    @ColumnInfo(name = "technicalLoss") val technicalLoss: Float,
    @ColumnInfo(name = "burntLoss") val burntLoss: Float,
    @ColumnInfo(name = "burntOrSourLoss") val burntOrSourLoss: Float,
    @ColumnInfo(name = "moldyLoss") val moldyLoss: Float,
    @ColumnInfo(name = "spoiledLoss") val spoiledLoss: Float,
    @ColumnInfo(name = "greenishLoss") val greenishLoss: Float,
    @ColumnInfo(name = "brokenLoss") val brokenLoss: Float,

    @ColumnInfo(name = "classificationDiscount") val classificationDiscount: Float,
    @ColumnInfo(name = "humidityAndImpuritiesDiscount") val humidityAndImpuritiesDiscount: Float,

    @ColumnInfo(name = "impuritiesLossPrice") val impuritiesLossPrice: Float,
    @ColumnInfo(name = "humidityLossPrice") val humidityLossPrice: Float,
    @ColumnInfo(name = "technicalLossPrice") val technicalLossPrice: Float,
    @ColumnInfo(name = "burntLossPrice") val burntLossPrice: Float,
    @ColumnInfo(name = "burntOrSourLossPrice") val burntOrSourLossPrice: Float,
    @ColumnInfo(name = "moldyLossPrice") val moldyLossPrice: Float,
    @ColumnInfo(name = "spoiledLossPrice") val spoiledLossPrice: Float,
    @ColumnInfo(name = "greenishLossPrice") val greenishLossPrice: Float,
    @ColumnInfo(name = "brokenLossPrice") val brokenLossPrice: Float,
    @ColumnInfo(name = "classificationDiscountPrice") val classificationDiscountPrice: Float,
    @ColumnInfo(name = "humidityAndImpuritiesDiscountPrice") val humidityAndImpuritiesDiscountPrice: Float,

    @ColumnInfo(name = "deductionValue") val deductionValue: Float,
    @ColumnInfo(name = "deduction") val deduction: Float,

    @ColumnInfo(name = "finalDiscount") val finalDiscount: Float,
    @ColumnInfo(name = "finalWeight") val finalWeight: Float,

    @ColumnInfo(name = "finalDiscountPrice") val finalDiscountPrice: Float,
    @ColumnInfo(name = "finalWeightPrice") val finalWeightPrice: Float
)


}
