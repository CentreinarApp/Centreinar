package com.example.centreinar.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.centreinar.ui.classificationProcess.strategy.BaseLimit

@Entity(tableName = "limits_milho")
data class LimitMilho(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "source") val source: Int,
    @ColumnInfo(name = "grain") val grain: String = "milho",
    @ColumnInfo(name = "group") override val group: Int,
    @ColumnInfo(name = "type") val type: Int,

    // Defeitos Básicos
    @ColumnInfo(name = "moistureUpLim") override val moistureUpLim: Float,
    @ColumnInfo(name = "impuritiesUpLim") val impuritiesUpLim: Float,
    @ColumnInfo(name = "brokenUpLim") val brokenUpLim: Float,
    @ColumnInfo(name = "ardidoUpLim") val ardidoUpLim: Float,
    @ColumnInfo(name = "mofadoUpLim") val mofadoUpLim: Float,
    @ColumnInfo(name = "carunchadoUpLim") val carunchadoUpLim: Float,
    @ColumnInfo(name = "spoiledTotalUpLim") val spoiledTotalUpLim: Float,
) : BaseLimit
