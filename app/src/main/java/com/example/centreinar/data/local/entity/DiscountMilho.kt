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
    @ColumnInfo(name = "mofadoLoss") val mofadoLoss: Float,
    @ColumnInfo(name = "carunchadoLoss") val carunchadoLoss: Float, // ➕ adicionado
    @ColumnInfo(name = "fermentedLoss") val fermentedLoss: Float,
    @ColumnInfo(name = "germinatedLoss") val germinatedLoss: Float,
    @ColumnInfo(name = "gessadoLoss") val gessadoLoss: Float,

    @ColumnInfo(name = "finalDiscount") val finalDiscount: Float,
    @ColumnInfo(name = "finalWeight") val finalWeight: Float
)
