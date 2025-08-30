package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey


class LimitSoja {
@Entity(tableName = "limits_soja")
data class LimitSoja(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "source")
    val source: Int,

    @ColumnInfo(name = "grain")
    val grain: String,

    @ColumnInfo(name = "group")
    val group: Int,

    @ColumnInfo(name = "type")
    val type: Int,

    @ColumnInfo(name = "moistureLowerLim")
    val moistureLowerLim: Float,

    @ColumnInfo(name = "moistureUpLim")
    val moistureUpLim: Float,

    @ColumnInfo(name = "impuritiesLowerLim")
    val impuritiesLowerLim: Float,

    @ColumnInfo(name = "impuritiesUpLim")
    val impuritiesUpLim: Float,

    @ColumnInfo(name = "brokenCrackedDamagedLowerLim")
    val brokenCrackedDamagedLowerLim: Float,

    @ColumnInfo(name = "brokenCrackedDamagedUpLim")
    val brokenCrackedDamagedUpLim: Float,

    @ColumnInfo(name = "greenishLowerLim")
    val greenishLowerLim: Float,

    @ColumnInfo(name = "greenishUpLim")
    val greenishUpLim: Float,

    @ColumnInfo(name = "burntLowerLim")
    val burntLowerLim: Float,

    @ColumnInfo(name = "burntUpLim")
    val burntUpLim: Float,

    @ColumnInfo(name = "moldyLowerLim")
    val moldyLowerLim: Float,

    @ColumnInfo(name = "moldyUpLim")
    val moldyUpLim: Float,

    @ColumnInfo(name = "burntOrSourLowerLim")
    val burntOrSourLowerLim: Float,

    @ColumnInfo(name = "burntOrSourUpLim")
    val burntOrSourUpLim: Float,

    @ColumnInfo(name = "spoiledTotalLowerLim")
    val spoiledTotalLowerLim: Float,

    @ColumnInfo(name = "spoiledTotalUpLim")
    val spoiledTotalUpLim: Float,
)


}
