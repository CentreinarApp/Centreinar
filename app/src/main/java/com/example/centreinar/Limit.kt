package com.example.centreinar
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ColumnInfo

@Entity(tableName = "limits")
data class Limit(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    @ColumnInfo(name = "source") val source: Int,
    @ColumnInfo(name = "grain") val grain: String,
    @ColumnInfo(name = "group") val group : Int,
    @ColumnInfo(name = "type") val type : Int,
    @ColumnInfo(name = "brokenCrackedDamagedLowerLim")val brokenCrackedDamagedLowerLim: Float,
    @ColumnInfo(name = "brokenCrackedDamagedUpLim")val brokenCrackedDamagedUpLim: Float,
    @ColumnInfo(name = "greenishLowerLim")val greenishLowerLim: Float,
    @ColumnInfo(name = "greenishUpLim")val greenishUpLim: Float,
    @ColumnInfo(name = "burntLowerLim")val burntLowerLim: Float,
    @ColumnInfo(name = "burntUpLim")val burntUpLim: Float,
    @ColumnInfo(name = "moldyLowerLim")val moldyLowerLim: Float,
    @ColumnInfo(name = "moldyUpLim")val moldyUpLim: Float,
    @ColumnInfo(name = "burntOrSourLowerLim")val burntOrSourLowerLim: Float,
    @ColumnInfo(name = "burntOrSourUpLim")val burntOrSourUpLim: Float,
    @ColumnInfo(name = "spoiledTotalLowerLim")val spoiledTotalLowerLim : Float,
    @ColumnInfo(name = "spoiledTotalUpLim")val spoiledTotalUpLim : Float,
)