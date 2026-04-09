package com.example.centreinar.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

class SojaRulesTest {

    private lateinit var sojaRules: SojaRules

    @Before
    fun setUp() {
        sojaRules = SojaRules()
    }

    @Test
    fun `calculateColor - baseWeight menor ou igual a zero - retorna nulo`() {
        val resultZero = sojaRules.calculateColor(otherColorsWeight = 10f, baseWeightCor = 0f)
        val resultNegative = sojaRules.calculateColor(otherColorsWeight = 10f, baseWeightCor = -5f)

        assertNull(resultZero)
        assertNull(resultNegative)
    }

    @Test
    fun `calculateColor - outras cores menor ou igual a 10pct - retorna Classe Amarela`() {
        // 5g de outras cores em uma base de 100g (5%)
        val result = sojaRules.calculateColor(otherColorsWeight = 5f, baseWeightCor = 100f)

        assertNotNull(result)
        assertEquals(5f, result!!.otherColorPct, 0.001f)
        assertEquals(95f, result.yellowPct, 0.001f)
        assertEquals("Classe Amarela", result.framingClass)
    }

    @Test
    fun `calculateColor - exatos 10pct de outras cores - retorna Classe Amarela`() {
        // Exatamente 10%
        val result = sojaRules.calculateColor(otherColorsWeight = 10f, baseWeightCor = 100f)

        assertNotNull(result)
        assertEquals(10f, result!!.otherColorPct, 0.001f)
        assertEquals("Classe Amarela", result.framingClass)
    }

    @Test
    fun `calculateColor - outras cores maior que 10pct - retorna Classe Misturada`() {
        // 15g de outras cores em uma base de 100g (15%)
        val result = sojaRules.calculateColor(otherColorsWeight = 15f, baseWeightCor = 100f)

        assertNotNull(result)
        assertEquals(15f, result!!.otherColorPct, 0.001f)
        assertEquals(85f, result.yellowPct, 0.001f)
        assertEquals("Classe Misturada", result.framingClass)
    }

    @Test
    fun `calculateColor - arredondamento para baixo mantem na Classe Amarela`() {
        // 10.004 de 100 (deve arredondar para 10.00 -> Amarela)
        val result = sojaRules.calculateColor(otherColorsWeight = 10.004f, baseWeightCor = 100f)

        assertNotNull(result)
        assertEquals(10.00f, result!!.otherColorPct, 0.001f)
        assertEquals("Classe Amarela", result.framingClass)
    }

    @Test
    fun `calculateColor - arredondamento para cima joga para Classe Misturada`() {
        // 10.006 de 100 (deve arredondar para 10.01 -> Misturada)
        val result = sojaRules.calculateColor(otherColorsWeight = 10.006f, baseWeightCor = 100f)

        assertNotNull(result)
        assertEquals(10.01f, result!!.otherColorPct, 0.001f)
        assertEquals("Classe Misturada", result.framingClass)
    }
}