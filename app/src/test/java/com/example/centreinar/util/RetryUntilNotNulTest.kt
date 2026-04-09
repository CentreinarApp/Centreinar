package com.example.centreinar.util

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import kotlin.system.measureTimeMillis

@OptIn(ExperimentalCoroutinesApi::class)
class RetryUntilNotNullTest {

    @Test
    fun `retryUntilNotNull returns value when block eventually non-null`() = runTest {
        var attempts = 0
        val result = retryUntilNotNull(times = 5, delayMs = 10) {
            attempts++
            if (attempts < 3) null else "ok"
        }
        assertEquals("ok", result)
        assertTrue(attempts >= 3)
    }

    @Test
    fun `retryUntilNotNull returns null when all attempts fail`() = runTest {
        var attempts = 0
        val result = retryUntilNotNull(times = 3, delayMs = 5) {
            attempts++
            null
        }
        assertNull(result)
        assertEquals(3, attempts)
    }
}
