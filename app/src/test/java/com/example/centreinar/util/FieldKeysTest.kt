package com.example.centreinar.util

import org.junit.Assert.assertEquals
import org.junit.Test

class FieldKeysTest {

    @Test
    fun `field keys constants should match expected values`() {
        assertEquals("moisture", FieldKeys.MOISTURE)
        assertEquals("impurities", FieldKeys.IMPURITIES)
        assertEquals("broken", FieldKeys.BROKEN)

        // some specific keys
        assertEquals("burnt", FieldKeys.BURNT)
        assertEquals("ardido", FieldKeys.ARDIDO)
        assertEquals("lotWeight", FieldKeys.LOT_WEIGHT)
        assertEquals("lotPrice", FieldKeys.LOT_PRICE)
    }
}
