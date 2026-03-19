package com.example.centreinar

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.centreinar.ui.classificationProcess.strategy.BaseLimit

@Entity(tableName = "limits_soja")
data class LimitSoja(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "source") val source: Int,
    @ColumnInfo(name = "grain") val grain: String,
    @ColumnInfo(name = "group") override val group: Int,
    @ColumnInfo(name = "type") val type: Int,

    // Defeitos Básicos
    @ColumnInfo(name = "moistureLowerLim") val moistureLowerLim: Float,
    @ColumnInfo(name = "moistureUpLim") override val moistureUpLim: Float,
    @ColumnInfo(name = "impuritiesLowerLim") val impuritiesLowerLim: Float,
    @ColumnInfo(name = "impuritiesUpLim") val impuritiesUpLim: Float,
    @ColumnInfo(name = "brokenCrackedDamagedLowerLim") val brokenCrackedDamagedLowerLim: Float,
    @ColumnInfo(name = "brokenCrackedDamagedUpLim") val brokenCrackedDamagedUpLim: Float,
    @ColumnInfo(name = "greenishLowerLim") val greenishLowerLim: Float,
    @ColumnInfo(name = "greenishUpLim") val greenishUpLim: Float,
    @ColumnInfo(name = "burntLowerLim") val burntLowerLim: Float,
    @ColumnInfo(name = "burntUpLim") val burntUpLim: Float,
    @ColumnInfo(name = "moldyLowerLim") val moldyLowerLim: Float,
    @ColumnInfo(name = "moldyUpLim") val moldyUpLim: Float,
    @ColumnInfo(name = "burntOrSourLowerLim") val burntOrSourLowerLim: Float,
    @ColumnInfo(name = "burntOrSourUpLim") val burntOrSourUpLim: Float,
    @ColumnInfo(name = "spoiledTotalLowerLim") val spoiledTotalLowerLim: Float,
    @ColumnInfo(name = "spoiledTotalUpLim") val spoiledTotalUpLim: Float,
) : BaseLimit {

    // Retorna as linhas de limites da soja para exibição nas tabelas
    override fun toDisplayRows(): List<Pair<String, Float>> = listOf(
        "Ardidos e Queimados"                  to burntOrSourUpLim,
        "Queimados"                            to burntUpLim,
        "Mofados"                              to moldyUpLim,
        "Avariados Total"                      to spoiledTotalUpLim,
        "Esverdeados"                          to greenishUpLim,
        "Partidos, Quebrados e Amassados"      to brokenCrackedDamagedUpLim,
        "Matérias Estranhas e Impurezas"       to impuritiesUpLim
    )
}