package com.example.centreinar.util

import org.junit.Assert.*
import org.junit.Test
import java.util.Locale

class UtilExtensionsTest {

    @Test
    fun `toFloatOrZero accepts dot and comma and returns 0 on invalid`() {
        assertEquals(12.34f, "12.34".toFloatOrZero(), 0.0001f)
        assertEquals(12.34f, "12,34".toFloatOrZero(), 0.0001f)
        assertEquals(0f, "abc".toFloatOrZero(), 0.0f)
        assertEquals(0f, "".toFloatOrZero(), 0.0f)
    }

    @Test
    fun `toIntOrZero returns parsed int or zero`() {
        assertEquals(42, "42".toIntOrZero())
        assertEquals(0, "notanint".toIntOrZero())
        assertEquals(0, "".toIntOrZero())
    }

    @Test
    fun `toFloatOrDefault behaves like toFloatOrZero`() {
        assertEquals(3.5f, "3.5".toFloatOrDefault(), 0.0001f)
        assertEquals(3.5f, "3,5".toFloatOrDefault(), 0.0001f)
        assertEquals(0f, "x".toFloatOrDefault(), 0.0f)
    }

    @Test
    fun `sanitizeNumericInput removes invalid chars and keeps only first dot`() {
        val s1 = sanitizeNumericInput("12,34")
        assertEquals("12.34", s1)

        val s2 = sanitizeNumericInput("1a2b3.4.5")
        // Espera manter apenas o primeiro ponto: "123.45"
        assertEquals("123.45", s2)

        val s3 = sanitizeNumericInput("...1..2,3a4")
        // substitui vírgula por ponto e mantém apenas o primeiro ponto: "1.234"
        assertEquals("1.234", s3)
    }

    @Test
    fun `toUniversalString and rounding helpers produce expected formats`() {
        Locale.setDefault(Locale.US)
        val v = 3.14159f
        val s = v.toUniversalString()
        assertTrue(s.matches(Regex("\\d+\\.\\d{2}"))) // "3.14" style

        val rounded2 = 2.34567f.roundToTwoDecimals()
        assertEquals(2.35f, rounded2, 0.0001f)

        val rounded1 = 2.34f.roundToOneDecimal()
        assertEquals(2.3f, rounded1, 0.0001f)
    }
}
