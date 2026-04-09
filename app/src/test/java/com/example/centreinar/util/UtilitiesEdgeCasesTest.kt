package com.example.centreinar.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class UtilitiesEdgeCasesTest {

    private val utils = Utilities()

    @Test
    fun `calculateDefectPercentage returns 0 for NaN inputs`() {
        assertEquals(0f, utils.calculateDefectPercentage(Float.NaN, 100f), 0.0001f)
        assertEquals(0f, utils.calculateDefectPercentage(5f, Float.NaN), 0.0001f)
    }

    @Test
    fun `sanitizeNumericInput handles multiple dots gracefully`() {
        val s = sanitizeNumericInput("1.2.3.4")
        // Deve conter apenas um ponto após a sanitização
        assertEquals(1, s.count { it == '.' })
        // E o resultado esperado é "1.234"
        assertEquals("1.234", s)
    }
}
