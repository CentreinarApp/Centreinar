package com.example.centreinar.domain.rules

import java.math.RoundingMode
import javax.inject.Inject

data class ColorSojaResult(
    val yellowPct: Float,
    val otherColorPct: Float,
    val framingClass: String
)

class SojaRules @Inject constructor() {

    fun calculateColor(
        otherColorsWeight: Float,
        baseWeightCor: Float
    ): ColorSojaResult? {
        if (baseWeightCor <= 0f) return null

        val otherColorPct = (otherColorsWeight / baseWeightCor * 100f)
            .toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

        val yellowPct = (100f - otherColorPct)
            .toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

        // Se arredondado for <= 10, é Amarela.
        val framingClass = if (otherColorPct <= 10.00f) "Classe Amarela" else "Classe Misturada"

        return ColorSojaResult(
            yellowPct = yellowPct,
            otherColorPct = otherColorPct,
            framingClass = framingClass
        )
    }
}