package com.example.centreinar.util

import org.junit.Assert.*
import org.junit.Test
import java.math.BigDecimal
import java.math.RoundingMode

class UtilitiesTest {

    private val utils = Utilities()

    @Test
    fun `findCategoryForValue returns correct category index or 7 when out of ranges`() {
        val intervals = listOf(0f to 1f, 1.1f to 2f, 2.1f to 3f)
        assertEquals(1, utils.findCategoryForValue(intervals, 0.5f))
        assertEquals(2, utils.findCategoryForValue(intervals, 1.5f))
        assertEquals(3, utils.findCategoryForValue(intervals, 2.5f))
        assertEquals(7, utils.findCategoryForValue(intervals, 10f))
    }

    @Test
    fun `calculateDefectPercentage handles zero and normal cases`() {
        // weight zero -> 0
        assertEquals(0f, utils.calculateDefectPercentage(5f, 0f), 0.0001f)
        // normal case: (defect * 100) / weight
        val result = utils.calculateDefectPercentage(2f, 50f)
        // (2 * 100) / 50 = 4.0
        assertEquals(4.0f, result, 0.0001f)
    }

    @Test
    fun `calculateDifference returns positive difference or zero`() {
        assertEquals(2f, utils.calculateDifference(7f, 5f), 0.0001f)
        assertEquals(0f, utils.calculateDifference(3f, 5f), 0.0001f)
    }

    @Test
    fun `calculatePercentage handles edge cases`() {
        assertEquals(0f, utils.calculatePercentage(1f, 0f), 0.0001f)
        assertEquals(50f, utils.calculatePercentage(1f, 2f), 0.0001f)
        assertEquals(0f, utils.calculatePercentage(Float.NaN, 10f), 0.0001f)
    }
}
