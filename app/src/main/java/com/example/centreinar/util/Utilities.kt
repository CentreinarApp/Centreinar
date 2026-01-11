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
        totalSpoiled: Float,
        limits: List<LimitMilho>
    ): Int {
        if (limits.isEmpty()) return 0

        // Separa os limites por tipo
        val limitType1 = limits.firstOrNull { it.type == 1 }
        val limitType2 = limits.firstOrNull { it.type == 2 }
        val limitType3 = limits.firstOrNull { it.type == 3 }

        // Se faltar algum tipo na tabela, não dá para classificar
        if (limitType1 == null || limitType2 == null || limitType3 == null) {
            Log.e("MilhoClass", "Limites Tipo 1, 2 ou 3 não encontrados no Banco.")
            return 0
        }

        // --- FUNÇÃO AUXILIAR: Define o tipo de UM defeito específico ---
        // Retorna: 1, 2, 3 (Tipos), 7 (Fora de Tipo) ou 0 (Desclassificado)
        fun getDefectType(
            value: Float,
            lim1: Float,
            lim2: Float,
            lim3: Float,
            absoluteMax: Float // Limite de desclassificação
        ): Int {
            if (value > absoluteMax) return 0 // Desclassificado
            if (value > lim3) return 7        // Fora de Tipo
            if (value > lim2) return 3        // Tipo 3
            if (value > lim1) return 2        // Tipo 2
            return 1                          // Tipo 1
        }

        // --- CÁLCULO DOS TIPOS INDIVIDUAIS ---
        val typeImpurities = getDefectType(impurities, limitType1.impuritiesUpLim, limitType2.impuritiesUpLim, limitType3.impuritiesUpLim, limitType3.impuritiesUpLim)
        val typeBroken = getDefectType(broken, limitType1.brokenUpLim, limitType2.brokenUpLim, limitType3.brokenUpLim, limitType3.brokenUpLim)
        val typeArdido = getDefectType(ardido, limitType1.ardidoUpLim, limitType2.ardidoUpLim, limitType3.ardidoUpLim, limitType3.ardidoUpLim)
        val typeMofado = getDefectType(mofado, limitType1.mofadoUpLim, limitType2.mofadoUpLim, limitType3.mofadoUpLim, limitType3.mofadoUpLim)
        val typeCarunchado = getDefectType(carunchado, limitType1.carunchadoUpLim, limitType2.carunchadoUpLim, limitType3.carunchadoUpLim, limitType3.carunchadoUpLim)

        // O TOTAL DE AVARIADOS
        val typeSpoiledTotal = getDefectType(totalSpoiled, limitType1.spoiledTotalUpLim, limitType2.spoiledTotalUpLim, limitType3.spoiledTotalUpLim, limitType3.spoiledTotalUpLim)

        // Lista com todos os tipos calculados
        val allTypes = listOf(typeImpurities, typeBroken, typeArdido, typeMofado, typeCarunchado, typeSpoiledTotal)

        // Se algum defeito é 0 => Desclassificado
        if (allTypes.contains(0)) {
            return 0
        }

        // Se algum defeito é 7 => Fora de Tipo
        if (allTypes.contains(7)) {
            return 7
        }

        // Retorna o tipo final do grão, o maior valor do tipo calculado.
        return allTypes.maxOrNull() ?: 3
    }
}