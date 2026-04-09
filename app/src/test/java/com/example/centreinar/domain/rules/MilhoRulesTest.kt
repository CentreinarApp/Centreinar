package com.example.centreinar.domain.rules

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MilhoRulesTest {

    private lateinit var milhoRules: MilhoRules

    @Before
    fun setUp() {
        milhoRules = MilhoRules()
    }

    // =========================================================================
    // Testes para calculateClass (Classe do Milho)
    // =========================================================================

    @Test
    fun `calculateClass - peso total zero - retorna NAO_DEFINIDA`() {
        val result = milhoRules.calculateClass(0f, 0f, 0f)

        assertEquals(0f, result.yellowPct, 0.01f)
        assertEquals(ClassMilho.NAO_DEFINIDA, result.finalClass)
    }

    @Test
    fun `calculateClass - amarelo maior ou igual a 95pct - retorna AMARELA`() {
        // 95g amarelo, 5g branco = Total 100g -> 95% amarelo
        val result = milhoRules.calculateClass(95f, 5f, 0f)

        assertEquals(95f, result.yellowPct, 0.01f)
        assertEquals(ClassMilho.AMARELA, result.finalClass)
    }

    @Test
    fun `calculateClass - branco maior ou igual a 95pct - retorna BRANCA`() {
        // 5g amarelo, 95g branco = Total 100g -> 5% amarelo, 95% branco
        val result = milhoRules.calculateClass(5f, 95f, 0f)

        assertEquals(5f, result.yellowPct, 0.01f)
        assertEquals(ClassMilho.BRANCA, result.finalClass)
    }

    @Test
    fun `calculateClass - sem predominancia de 95pct - retorna CORES`() {
        // 50g amarelo, 50g branco = Total 100g -> 50% de cada
        val result = milhoRules.calculateClass(50f, 50f, 0f)

        assertEquals(50f, result.yellowPct, 0.01f)
        assertEquals(ClassMilho.CORES, result.finalClass)
    }

    // =========================================================================
    // Testes para calculateGroup (Grupo do Milho)
    // =========================================================================

    @Test
    fun `calculateGroup - peso total zero - retorna NAO_DEFINIDO`() {
        val result = milhoRules.calculateGroup(0f, 0f, 0f)

        assertEquals(0f, result.hardPct, 0.01f)
        assertEquals(0f, result.dentPct, 0.01f)
        assertEquals(0f, result.semiDuroPct, 0.01f)
        assertEquals(GroupMilho.NAO_DEFINIDO, result.finalGroup)
    }

    @Test
    fun `calculateGroup - duro maior ou igual a 85pct - retorna DURO`() {
        // 85g duro, 15g dentado
        val result = milhoRules.calculateGroup(85f, 15f, 0f)

        assertEquals(85f, result.hardPct, 0.01f)
        assertEquals(GroupMilho.DURO, result.finalGroup)
    }

    @Test
    fun `calculateGroup - dentado maior ou igual a 85pct - retorna DENTADO`() {
        // 10g duro, 90g dentado
        val result = milhoRules.calculateGroup(10f, 90f, 0f)

        assertEquals(90f, result.dentPct, 0.01f)
        assertEquals(GroupMilho.DENTADO, result.finalGroup)
    }

    @Test
    fun `calculateGroup - sem predominancia de 85pct - retorna SEMIDURO`() {
        // 50g duro, 50g dentado (Nenhum atinge 85%)
        val result = milhoRules.calculateGroup(50f, 50f, 0f)

        assertEquals(50f, result.hardPct, 0.01f)
        assertEquals(50f, result.dentPct, 0.01f)
        assertEquals(GroupMilho.SEMIDURO, result.finalGroup)
    }
}