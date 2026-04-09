package com.example.centreinar.domain.usecase

import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.util.Utilities
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CalculateDiscountSojaUseCaseTest {

    private val discountRepo: DiscountRepository = mockk()
    private val tools: Utilities = mockk()
    private lateinit var useCase: CalculateDiscountSojaUseCase

    private val defaultLimits = mapOf(
        "impurities"  to 1.0f,
        "moisture"    to 14.0f,
        "burnt"       to 1.0f,
        "burntOrSour" to 3.0f,
        "moldy"       to 6.0f,
        "spoiled"     to 8.0f,
        "greenish"    to 8.0f,
        "broken"      to 30.0f
    )

    @Before
    fun setUp() {
        useCase = CalculateDiscountSojaUseCase(discountRepo, tools)

        // Mock padrão para a Utility tools
        every { tools.calculateDifference(any(), any()) } answers {
            val v1 = firstArg<Float>()
            val v2 = secondArg<Float>()
            if (v1 > v2) v1 - v2 else 0f
        }
    }

    private fun mockInputSoja(
        impurities: Float = 0.5f,
        moisture: Float = 13f,
        burnt: Float = 0f,
        burntOrSour: Float = 0f,
        moldy: Float = 0f,
        spoiled: Float = 0f,
        greenish: Float = 0f,
        broken: Float = 0f
    ): InputDiscountSoja {
        val sample: InputDiscountSoja = mockk(relaxed = true)
        every { sample.id } returns 1
        every { sample.lotWeight } returns 60_000f
        every { sample.lotPrice } returns 3_000f
        every { sample.daysOfStorage } returns 0
        every { sample.deductionValue } returns 0f
        every { sample.foreignMattersAndImpurities } returns impurities
        every { sample.moisture } returns moisture
        every { sample.burnt } returns burnt
        every { sample.burntOrSour } returns burntOrSour
        every { sample.moldy } returns moldy
        every { sample.spoiled } returns spoiled
        every { sample.greenish } returns greenish
        every { sample.brokenCrackedDamaged } returns broken
        return sample
    }

    private fun mockInsertAndCapture(): CapturingSlot<DiscountSoja> {
        val slot = slot<DiscountSoja>()
        coEvery { discountRepo.insertDiscount(capture(slot)) } returns 10L
        return slot
    }

    @Test
    fun `execute - amostra dentro dos limites - nao gera descontos`() = runTest {
        val sample = mockInputSoja()
        val slot = mockInsertAndCapture()

        val resultId = useCase.execute(sample, defaultLimits, false, false, false)

        assertEquals(10L, resultId)
        with(slot.captured) {
            assertEquals(0f, impuritiesLoss, 0.01f)
            assertEquals(0f, humidityLoss, 0.01f)
            assertEquals(0f, burntLoss, 0.01f)
            assertEquals(0f, spoiledLoss, 0.01f)
        }
    }

    @Test
    fun `execute - impurezas e umidade acima do limite - calcula perdas`() = runTest {
        val sample = mockInputSoja(impurities = 2.0f, moisture = 16.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, false, false)

        assertTrue(slot.captured.impuritiesLoss > 0f)
        assertTrue(slot.captured.humidityLoss > 0f)
    }

    @Test
    fun `execute - desconto em cascata (avariados) - subtrai corretamente`() = runTest {
        val sample = mockInputSoja(burnt = 2.0f, burntOrSour = 4.0f, spoiled = 10.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, doesClassificationLoss = true, false)

        with(slot.captured) {
            assertTrue("Deve ter perda de queimados", burntLoss > 0f)
            assertEquals(0f, burntOrSourLoss, 0.01f)
        }
    }

    @Test
    fun `execute - sem limites passados - busca no repositorio`() = runTest {
        val sample = mockInputSoja()
        coEvery { discountRepo.getLimitsByType(any(), any(), any(), any()) } returns defaultLimits
        coEvery { discountRepo.insertDiscount(any()) } returns 99L

        useCase.execute(sample, false, false, false)

        coVerify(exactly = 1) { discountRepo.getLimitsByType(any(), any(), 1, any()) }
    }
}