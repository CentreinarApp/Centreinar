package com.example.centreinar.util

import android.util.Log // Adicionado para Log
import com.example.centreinar.data.local.entity.LimitMilho
import javax.inject.Inject
import javax.inject.Singleton
import java.math.RoundingMode

@Singleton
class Utilities @Inject constructor() {

    // Encontra a categoria de acordo com intervalos de limite (Lógica de Soja)
    fun findCategoryForValue(intervals: List<Pair<Float, Float>>, value: Float): Int {
        intervals.forEachIndexed { index, interval ->
            // Assume que os limites são inclusivos no intervalo
            if (value >= interval.first && value <= interval.second) {
                return index + 1
            }
        }
        // retorna "fora de tipo" como último nível
        return intervals.size + 1
    }

    // Calcula o percentual de defeito (% com arredondamento)
    fun calculateDefectPercentage(defect: Float, weight: Float): Float {
        // CORREÇÃO 1: Proteger contra divisão por zero e valores NaN
        if (weight <= 0f || weight.isNaN() || defect.isNaN()) return 0f

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
        // CORREÇÃO 1: Proteger contra divisão por zero e valores NaN
        if (totalValue <= 0f || totalValue.isNaN()) return 0f
        if (partialValue.isNaN()) return 0f

        return (partialValue / totalValue) * 100
    }

    // Determina o tipo final do milho com base no pior defeito (MAPA)
    fun defineFinalTypeMilho(
        impurities: Float,
        broken: Float,
        ardido: Float,
        mofado: Float, // Usado para Avariados Total no Milho (geralmente)
        carunchado: Float,
        limits: List<LimitMilho> // Lista deve conter os Tipos 1, 2 e 3 para o Source.
    ): Int {
        if (limits.isEmpty()) return 0 // Retorna 0 (Código de Erro/Não Classificado)

        // Buscamos os limites de Milho e garantimos que existem
        val limitType1 = limits.firstOrNull { it.type == 1 }
        val limitType2 = limits.firstOrNull { it.type == 2 }
        val limitType3 = limits.firstOrNull { it.type == 3 }

        if (limitType1 == null || limitType2 == null || limitType3 == null) {
            Log.e("MilhoClass", "Limites Tipo 1, 2 ou 3 não encontrados na lista.")
            return 0 // Retorna 0 se os dados de limite estiverem incompletos
        }

        // Função auxiliar para verificar se o percentual EXCEDEU o limite de um Tipo
        fun exceedsLimit(limit: LimitMilho): Boolean {
            return impurities > limit.impuritiesUpLim ||
                    broken > limit.brokenUpLim ||
                    ardido > limit.ardidoUpLim ||
                    mofado > limit.mofadoUpLim ||
                    carunchado > limit.carunchadoUpLim
        }

        // A lógica do Milho deve enquadrar no pior Tipo.
        // Começamos do Tipo mais rigoroso e regredimos, ou retornamos Fora de Tipo (4).

        // Se excedeu o limite mais flexível (Tipo 3), é Fora de Tipo (4)
        if (exceedsLimit(limitType3)) {
            return 4 // Usamos 4 como código para FORA DE TIPO
        }

        // Se não excedeu o Tipo 3, o Milho se enquadra, pelo menos, no Tipo 3.
        var finalType = 3

        // Tenta enquadrar no Tipo 2 (mais rigoroso que o 3)
        if (!exceedsLimit(limitType2)) {
            finalType = 2
        }

        // Tenta enquadrar no Tipo 1 (mais rigoroso de todos)
        if (!exceedsLimit(limitType1)) {
            finalType = 1
        }

        // O valor final será 1, 2 ou 3 (se não excedeu o limite mais flexível).
        return finalType
    }
}