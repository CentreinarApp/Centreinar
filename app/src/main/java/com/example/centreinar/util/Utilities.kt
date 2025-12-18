package com.example.centreinar.util

import android.util.Log
import com.example.centreinar.data.local.entity.LimitMilho
import javax.inject.Inject
import javax.inject.Singleton
import java.math.RoundingMode

@Singleton
class Utilities @Inject constructor() {


    fun findCategoryForValue(intervals: List<Pair<Float, Float>>, value: Float): Int {
        intervals.forEachIndexed { index, interval ->
            // Verifica se o valor está dentro do intervalo (ex: 0.0 a 1.0)
            if (value >= interval.first && value <= interval.second) {
                return index + 1 // Retorna o Tipo correspondente
            }
        }
        return 7 // Valor excedeu o limite do Tipo 3 (Fora de Tipo)
    }


    fun calculateDefectPercentage(defect: Float, weight: Float): Float {
        if (weight <= 0f || weight.isNaN() || defect.isNaN()) return 0f

        return ((defect * 100) / weight).toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP) // Arredondamento
            .toFloat()
    }


    fun calculateDifference(defect: Float, defectTolerance: Float): Float {
        return if (defect > defectTolerance) defect - defectTolerance else 0f
    }

    fun calculatePercentage(partialValue: Float, totalValue: Float): Float {
        if (totalValue <= 0f || totalValue.isNaN()) return 0f
        if (partialValue.isNaN()) return 0f

        return (partialValue / totalValue) * 100
    }


    fun defineFinalTypeMilho(
        impurities: Float,
        broken: Float,
        ardido: Float,
        mofado: Float,
        carunchado: Float,
        limits: List<LimitMilho>
    ): Int {
        if (limits.isEmpty()) return 0

        val limitType1 = limits.firstOrNull { it.type == 1 }
        val limitType2 = limits.firstOrNull { it.type == 2 }
        val limitType3 = limits.firstOrNull { it.type == 3 }

        if (limitType1 == null || limitType2 == null || limitType3 == null) {
            Log.e("MilhoClass", "Limites Tipo 1, 2 ou 3 não encontrados.")
            return 0
        }

        // Função interna para verificar se excede um limite específico
        fun exceedsLimit(limit: LimitMilho): Boolean {
            return impurities > limit.impuritiesUpLim ||
                    broken > limit.brokenUpLim ||
                    ardido > limit.ardidoUpLim ||
                    mofado > limit.mofadoUpLim ||
                    carunchado > limit.carunchadoUpLim
        }

        //  Verifica se é Fora de Tipo (excede o limite mais flexível, o Tipo 3)
        if (exceedsLimit(limitType3)) {
            return 7
        }

        //  Enquadra no melhor tipo possível (Hierarquia: 1 > 2 > 3)
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