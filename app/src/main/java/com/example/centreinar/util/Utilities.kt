package com.example.centreinar.util

import android.util.Log
import com.example.centreinar.data.local.entity.LimitMilho
import javax.inject.Inject
import javax.inject.Singleton
import java.math.RoundingMode

@Singleton
class Utilities @Inject constructor() {

    // ✅ Encontra a categoria correta com base nos intervalos
    // ✅ RETORNA 7 SE ULTRAPASSAR O LIMITE (FORA DE TIPO)
    fun findCategoryForValue(intervals: List<Pair<Float, Float>>, value: Float): Int {
        if (intervals.isEmpty()) return 7

        intervals.forEachIndexed { index, interval ->
            if (value >= interval.first && value <= interval.second) {
                return index + 1 // Tipo 1, 2, 3...
            }
        }

        return 7 // 🚨 FORA DE TIPO PADRONIZADO
    }

    // ✅ Cálculo seguro de percentual de defeito (com arredondamento)
    fun calculateDefectPercentage(defect: Float, weight: Float): Float {
        if (weight <= 0f || weight.isNaN() || defect.isNaN()) return 0f

        return ((defect * 100) / weight)
            .toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toFloat()
    }

    // ✅ Calcula excesso acima do limite
    fun calculateDifference(defect: Float, defectTolerance: Float): Float {
        return if (defect > defectTolerance) defect - defectTolerance else 0f
    }

    // ✅ Cálculo de percentual parcial (ex: cor)
    fun calculatePercentage(partialValue: Float, totalValue: Float): Float {
        if (totalValue <= 0f || totalValue.isNaN()) return 0f
        if (partialValue.isNaN()) return 0f

        return ((partialValue / totalValue) * 100)
            .toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
            .toFloat()
    }

    // ✅ CLASSIFICAÇÃO FINAL DO MILHO (com FORA DE TIPO = 7)
    fun defineFinalTypeMilho(
        impurities: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float,
        limits: List<LimitMilho>
    ): Int {

        if (limits.isEmpty()) {
            Log.e("MilhoClass", "Lista de limites vazia.")
            return 0
        }

        val limitType1 = limits.firstOrNull { it.type == 1 }
        val limitType2 = limits.firstOrNull { it.type == 2 }
        val limitType3 = limits.firstOrNull { it.type == 3 }

        if (limitType1 == null || limitType2 == null || limitType3 == null) {
            Log.e("MilhoClass", "Limites Tipo 1, 2 ou 3 não encontrados.")
            return 0
        }

        fun exceedsLimit(limit: LimitMilho): Boolean {
            return impurities > limit.impuritiesUpLim ||
                    broken > limit.brokenUpLim ||
                    ardido > limit.ardidoUpLim ||
                    mofado > limit.mofadoUpLim ||
                    carunchado > limit.carunchadoUpLim
        }

        // 🚨 FORA DE TIPO
        if (exceedsLimit(limitType3)) {
            return 7
        }

        // ✅ Melhor tipo possível
        var finalType = 3

        if (!exceedsLimit(limitType2)) {
            finalType = 2
        }

        if (!exceedsLimit(limitType1)) {
            finalType = 1
        }

        return finalType
    }
}
