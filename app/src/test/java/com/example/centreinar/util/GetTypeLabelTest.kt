package com.example.centreinar.util

import org.junit.Assert.assertEquals
import org.junit.Test

class GetTypeLabelTest {

    @Test
    fun `getTypeLabel special cases`() {
        assertEquals("---", getTypeLabel(-1, 1))
        assertEquals("Desclassificada", getTypeLabel(0, 1))
        assertEquals("Fora de Tipo", getTypeLabel(7, 1))
    }

    @Test
    fun `getTypeLabel group 2 returns Padrão Básico`() {
        assertEquals("Padrão Básico", getTypeLabel(1, 2))
        assertEquals("Padrão Básico", getTypeLabel(99, 2))
    }

    @Test
    fun `getTypeLabel maps numeric types for group not 2`() {
        assertEquals("Tipo 1", getTypeLabel(1, 1))
        assertEquals("Tipo 2", getTypeLabel(2, 1))
        assertEquals("Tipo 3", getTypeLabel(3, 1))
        assertEquals("Tipo 42", getTypeLabel(42, 1))
    }
}
