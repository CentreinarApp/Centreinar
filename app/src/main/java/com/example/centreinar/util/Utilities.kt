package com.example.centreinar.util

import com.example.centreinar.data.local.entity.LimitMilho
import javax.inject.Inject
import javax.inject.Singleton
import java.math.RoundingMode

@Singleton
class Utilities @Inject constructor() {

    // Encontra a categoria de acordo com intervalos de limite
    fun findCategoryForValue(intervals: List<Pair<Float, Float>>, value: Float): Int {
        if (value == 0.0f) return 1
        intervals.forEachIndexed { index, interval ->
            if (value >= interval.first && value <= interval.second) {
                return index + 1
            }
        }
        return 7
    }

    // Calcula o percentual de defeito
    fun calculateDefectPercentage(defect: Float, weight: Float): Float {
        if (weight <= 0f) return 0f
        return ((defect * 100) / weight).toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toFloat()
    }

    // Calcula diferença para limite máximo
    fun calculateDifference(defect: Float, defectTolerance: Float): Float {
        if (defect <= defectTolerance) return 0f
        return ((defect - defectTolerance) / (100 - defectTolerance)) * 100
    }

    // Calcula percentual parcial
    fun calculatePercentage(partialValue: Float, totalValue: Float): Float {
        if (totalValue <= 0f || totalValue.isNaN() || partialValue.isNaN()) return 0f
        return (partialValue / totalValue) * 100
    }

    // Determina o tipo final do milho com base nos limites
    fun defineFinalTypeMilho(
        impurities: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float,
        limits: List<LimitMilho>
    ): Int {
        if (limits.isEmpty()) return 0

        val limit = limits.first() // pega o primeiro limite disponível

        // lógica simplificada: se algum defeito ultrapassa o limite máximo, aumenta o tipo
        var type = 1

        if (impurities > limit.impuritiesUpLim) type += 1
        if (broken > limit.brokenUpLim) type += 1
        if (ardido > limit.ardidoUpLim) type += 1
        if (mofado > limit.mofadoUpLim) type += 1
        if (carunchado > limit.carunchadoUpLim) type += 1

        // tipo máximo permitido
        if (type > 5) type = 5

        return type
    }
}


