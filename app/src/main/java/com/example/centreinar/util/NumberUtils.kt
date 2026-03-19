package com.example.centreinar.util

import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale
import kotlin.math.roundToInt

// Formatador para float com ponto X.XX
private val universalFormat = DecimalFormat("0.00", DecimalFormatSymbols(Locale.US))

// Função para qualquer Float usar esse formatador
fun Float.toUniversalString(): String {
    return universalFormat.format(this)
}

// Função para arredondar para duas casas decimais
fun Float.roundToTwoDecimals(): Float {
    return this.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()
}

// Função para arredondar para uma casa decimal
fun Float.roundToOneDecimal(): Float {
    return (this * 10).roundToInt() / 10f
}