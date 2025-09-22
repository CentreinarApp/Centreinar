package com.example.centreinar.util

import com.example.centreinar.data.local.entity.LimitMilho
import javax.inject.Inject
import javax.inject.Singleton
import java.math.RoundingMode

@Singleton
class Utilities @Inject constructor() {

    // Encontra a categoria de acordo com intervalos de limite
    fun findCategoryForValue(intervals: List<Pair<Float, Float>>, value: Float): Int {
        intervals.forEachIndexed { index, interval ->
            if (value >= interval.first && value <= interval.second) {
                return index + 1
            }
        }
        // retorna "fora de tipo" como último nível
        return intervals.size + 1
    }

    // Calcula o percentual de defeito (% com arredondamento)
    fun calculateDefectPercentage(defect: Float, weight: Float): Float {
        if (weight <= 0f) return 0f
        return ((defect * 100) / weight).toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toFloat()
    }

    // Calcula excesso em relação ao limite máximo (valor absoluto em %)
    fun calculateDifference(defect: Float, defectTolerance: Float): Float {
        return if (defect > defectTolerance) defect - defectTolerance else 0f
    }

    // Calcula percentual parcial
    fun calculatePercentage(partialValue: Float, totalValue: Float): Float {
        if (totalValue <= 0f || totalValue.isNaN() || partialValue.isNaN()) return 0f
        return (partialValue / totalValue) * 100
    }

    // Determina o tipo final do milho com base no pior defeito (MAPA)
    fun defineFinalTypeMilho(
        impurities: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float,
        limits: List<LimitMilho>
    ): Int {
        if (limits.isEmpty()) return 0
        val limit = limits.first()

        var type = 1

        if (impurities > limit.impuritiesUpLim) type = maxOf(type, 2)
        if (broken > limit.brokenUpLim) type = maxOf(type, 3)
        if (ardido > limit.ardidoUpLim) type = maxOf(type, 3)
        if (mofado > limit.mofadoUpLim) type = maxOf(type, 4)
        if (carunchado > limit.carunchadoUpLim) type = maxOf(type, 5)

        return type
    }
}
