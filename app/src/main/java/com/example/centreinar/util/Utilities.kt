package com.example.centreinar.util

import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton
import java.math.RoundingMode

@Singleton
class Utilities @Inject constructor() {
    fun findCategoryForValue(intervals: List<Pair<Float, Float>>, value: Float): Int {
        intervals.forEachIndexed { index, interval ->
            if (value >= interval.first && value <= interval.second) {
                return index + 1
            }
        }
        return 7
    }

    fun calculateDefectPercentage(defect: Float, weight: Float): Float {
        if (weight <= 0f || weight.isNaN() || defect.isNaN()) return 0f
        return ((defect * 100) / weight).toBigDecimal()
            .setScale(2, RoundingMode.HALF_UP)
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
}

// =============================================================================
// Testes para ver se o banco de dados foi criado ou não no app
// Tenta [times] vezes chamar [block]. Se o resultado não for nulo, retorna
// imediatamente. Caso contrário, aguarda [delayMs] ms antes de tentar de novo.
// Retorna null se todas as tentativas falharem.
// =============================================================================
suspend fun <T> retryUntilNotNull(
    times: Int = 10,
    delayMs: Long = 300,
    block: suspend () -> T?
): T? {
    repeat(times) {
        val result = block()
        if (result != null) return result
        delay(delayMs)
    }
    return null
}

fun sanitizeNumericInput(raw: String): String {
    val cleaned = raw.replace(',', '.')
    val sb = StringBuilder()
    var hasDot = false

    for (ch in cleaned) {
        when {
            ch.isDigit() -> sb.append(ch)
            ch == '.' -> {
                // aceita ponto somente se já houver pelo menos um dígito antes e ainda não tiver ponto
                if (sb.isNotEmpty() && !hasDot) {
                    sb.append('.')
                    hasDot = true
                }
            }
        }
    }

    return sb.toString()
}


fun String.toFloatOrDefault(): Float = replace(",", ".").toFloatOrNull() ?: 0f