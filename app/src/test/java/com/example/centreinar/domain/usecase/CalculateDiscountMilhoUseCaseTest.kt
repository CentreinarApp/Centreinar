package com.example.centreinar.domain.usecase

import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import io.mockk.CapturingSlot
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class CalculateDiscountMilhoUseCaseTest {

    private val discountRepo: DiscountRepositoryMilho = mockk()
    private lateinit var useCase: CalculateDiscountMilhoUseCase

    private val defaultLimits = mapOf(
        "impurities"   to 1.0f,
        "moisture"     to 14.0f,
        "broken"       to 3.0f,
        "ardido"       to 1.0f,
        "carunchado"   to 2.0f,
        "spoiledTotal" to 6.0f
    )

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private fun sampleWithinLimits(
        lotWeight: Float = 60_000f,
        lotPrice: Float = 3_000f,
        daysOfStorage: Int = 0,
        deductionValue: Float = 0f,
        impurities: Float = 0.5f,
        moisture: Float = 13.0f,
        broken: Float = 2.0f,
        ardidos: Float = 0.5f,
        carunchado: Float = 1.0f,
        spoiled: Float = 4.0f
    ) = InputDiscountMilho(
        id = 1,
        grain = "Milho",
        group = 1,
        limitSource = 0,
        classificationId = 1,
        daysOfStorage = daysOfStorage,
        deductionValue = deductionValue,
        lotWeight = lotWeight,
        lotPrice = lotPrice,
        impurities = impurities,
        moisture = moisture,
        broken = broken,
        ardidos = ardidos,
        mofados = 0f,
        carunchado = carunchado,
        spoiled = spoiled
    )

    // CapturingSlot<T> é o tipo correto — slot<T>() é a função que cria, não um tipo
    private fun mockInsertAndCapture(): CapturingSlot<DiscountMilho> {
        val capturedSlot = slot<DiscountMilho>()
        coEvery { discountRepo.insertDiscount(capture(capturedSlot)) } returns 99L
        return capturedSlot
    }

    @Before
    fun setUp() {
        useCase = CalculateDiscountMilhoUseCase(discountRepo)
    }

    // =========================================================================
    // 1. Caminho feliz — sem descontos
    // =========================================================================

    @Test
    fun `execute - amostra dentro dos limites - nao gera perdas`() = runTest {
        val sample = sampleWithinLimits()
        val slot = mockInsertAndCapture()

        val resultId = useCase.execute(
            sample = sample,
            limit = defaultLimits,
            doesTechnicalLoss = false,
            doesClassificationLoss = false,
            doesDeduction = false
        )

        Assert.assertEquals(99L, resultId)
        with(slot.captured) {
            Assert.assertEquals(0f, impuritiesLoss, 0.01f)
            Assert.assertEquals(0f, humidityLoss, 0.01f)
            Assert.assertEquals(0f, technicalLoss, 0.01f)
            Assert.assertEquals(0f, brokenLoss, 0.01f)
            Assert.assertEquals(0f, ardidoLoss, 0.01f)
            Assert.assertEquals(0f, carunchadoLoss, 0.01f)
            Assert.assertEquals(0f, spoiledLoss, 0.01f)
            Assert.assertEquals(sample.lotWeight, finalWeight, 0.01f)
        }
    }

    // =========================================================================
    // 2. Impurezas acima do limite
    // =========================================================================

    @Test
    fun `execute - impurezas acima do limite - gera perda de impurezas`() = runTest {
        val sample = sampleWithinLimits(impurities = 2.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, false, false)

        Assert.assertTrue("Perda de impurezas deve ser > 0", slot.captured.impuritiesLoss > 0f)
    }

    @Test
    fun `execute - impurezas 2pct com limite 1pct - perda calculada corretamente`() = runTest {
        val sample = sampleWithinLimits(lotWeight = 10_000f, lotPrice = 500f, impurities = 2.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, false, false)

        val expectedLossPerc = (1.0f / (100f - 1.0f)) * 100f
        val expectedLossKg = (expectedLossPerc / 100f) * 10_000f
        Assert.assertEquals(expectedLossKg, slot.captured.impuritiesLoss, 0.1f)
    }

    // =========================================================================
    // 3. Umidade acima do limite
    // =========================================================================

    @Test
    fun `execute - umidade acima do limite - gera perda de umidade`() = runTest {
        val sample = sampleWithinLimits(moisture = 16.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, false, false)

        Assert.assertTrue("Perda de umidade deve ser > 0", slot.captured.humidityLoss > 0f)
    }

    // =========================================================================
    // 4. Quebra técnica
    // =========================================================================

    @Test
    fun `execute - doesTechnicalLoss true com dias positivos - gera quebra tecnica`() = runTest {
        val sample = sampleWithinLimits(daysOfStorage = 30, moisture = 16.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(
            sample,
            defaultLimits,
            doesTechnicalLoss = true,
            doesClassificationLoss = false,
            doesDeduction = false
        )

        Assert.assertTrue("Quebra técnica deve ser > 0", slot.captured.technicalLoss > 0f)
    }

    @Test
    fun `execute - doesTechnicalLoss true mas dias zero - nao gera quebra tecnica`() = runTest {
        val sample = sampleWithinLimits(daysOfStorage = 0, moisture = 16.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(
            sample,
            defaultLimits,
            doesTechnicalLoss = true,
            doesClassificationLoss = false,
            doesDeduction = false
        )

        Assert.assertEquals(0f, slot.captured.technicalLoss, 0.001f)
    }

    @Test
    fun `execute - doesTechnicalLoss false - sem quebra tecnica independente dos dias`() = runTest {
        val sample = sampleWithinLimits(daysOfStorage = 100, moisture = 16.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(
            sample,
            defaultLimits,
            doesTechnicalLoss = false,
            doesClassificationLoss = false,
            doesDeduction = false
        )

        Assert.assertEquals(0f, slot.captured.technicalLoss, 0.001f)
    }

    // =========================================================================
    // 5. Desconto de classificação
    // =========================================================================

    @Test
    fun `execute - doesClassificationLoss false - sem desconto de classificacao`() = runTest {
        val sample = sampleWithinLimits(broken = 5.0f, ardidos = 3.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(
            sample,
            defaultLimits,
            doesTechnicalLoss = false,
            doesClassificationLoss = false,
            doesDeduction = false
        )

        Assert.assertEquals(0f, slot.captured.classificationDiscount, 0.001f)
    }

    @Test
    fun `execute - doesClassificationLoss true - gera desconto de classificacao`() = runTest {
        val sample = sampleWithinLimits(broken = 5.0f, ardidos = 3.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(
            sample,
            defaultLimits,
            doesTechnicalLoss = false,
            doesClassificationLoss = true,
            doesDeduction = false
        )

        Assert.assertTrue(
            "Desconto de classificação deve ser > 0",
            slot.captured.classificationDiscount > 0f
        )
    }

    // =========================================================================
    // 6. Dedução
    // =========================================================================

    @Test
    fun `execute - doesDeduction true com valor positivo - aplica deducao`() = runTest {
        val sample = sampleWithinLimits(broken = 5.0f, ardidos = 3.0f, deductionValue = 50f)
        val slot = mockInsertAndCapture()

        useCase.execute(
            sample,
            defaultLimits,
            doesTechnicalLoss = false,
            doesClassificationLoss = true,
            doesDeduction = true
        )

        Assert.assertTrue("Dedução deve ser > 0", slot.captured.deduction > 0f)
    }

    @Test
    fun `execute - doesDeduction false - sem deducao mesmo com valor positivo`() = runTest {
        val sample = sampleWithinLimits(broken = 5.0f, deductionValue = 50f)
        val slot = mockInsertAndCapture()

        useCase.execute(
            sample,
            defaultLimits,
            doesTechnicalLoss = false,
            doesClassificationLoss = true,
            doesDeduction = false
        )

        Assert.assertEquals(0f, slot.captured.deduction, 0.001f)
    }

    // =========================================================================
    // 7. Peso final
    // =========================================================================

    @Test
    fun `execute - amostra com perdas - peso final menor que o lote`() = runTest {
        val sample = sampleWithinLimits(impurities = 3.0f, moisture = 16.0f, broken = 5.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(
            sample,
            defaultLimits,
            doesTechnicalLoss = true,
            doesClassificationLoss = true,
            doesDeduction = false
        )

        Assert.assertTrue(
            "Peso final deve ser menor que o peso do lote",
            slot.captured.finalWeight < sample.lotWeight
        )
    }

    @Test
    fun `execute - sem perdas - peso final igual ao lote`() = runTest {
        val sample = sampleWithinLimits()
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, false, false)

        Assert.assertEquals(sample.lotWeight, slot.captured.finalWeight, 0.01f)
    }

    // =========================================================================
    // 8. Busca de limites no repositório
    // =========================================================================

    @Test
    fun `execute - sem limite passado - busca limites no repositorio`() = runTest {
        val sample = sampleWithinLimits()
        coEvery {
            discountRepo.getLimitsByType(
                grain = sample.grain,
                group = sample.group,
                tipo = 1,
                limitSource = sample.limitSource
            )
        } returns defaultLimits
        coEvery { discountRepo.insertDiscount(any()) } returns 1L

        useCase.execute(
            sample = sample,
            doesTechnicalLoss = false,
            doesClassificationLoss = false,
            doesDeduction = false
        )

        coVerify(exactly = 1) {
            discountRepo.getLimitsByType(sample.grain, sample.group, 1, sample.limitSource)
        }
    }

    // =========================================================================
    // 9. Retorno do ID correto
    // =========================================================================

    @Test
    fun `execute - retorna o id do discount inserido`() = runTest {
        val sample = sampleWithinLimits()
        coEvery { discountRepo.insertDiscount(any()) } returns 42L

        val resultId = useCase.execute(sample, defaultLimits, false, false, false)

        Assert.assertEquals(42L, resultId)
    }

    // =========================================================================
    // 10. Ardido / Carunchado / Avariados
    // =========================================================================

    @Test
    fun `execute - ardido acima do limite - gera perda de ardido`() = runTest {
        val sample = sampleWithinLimits(ardidos = 3.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, doesClassificationLoss = true, false)

        Assert.assertTrue("Perda de ardido deve ser > 0", slot.captured.ardidoLoss > 0f)
    }

    @Test
    fun `execute - carunchado acima do limite - gera perda de carunchado`() = runTest {
        val sample = sampleWithinLimits(carunchado = 4.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, doesClassificationLoss = true, false)

        Assert.assertTrue("Perda de carunchado deve ser > 0", slot.captured.carunchadoLoss > 0f)
    }

    @Test
    fun `execute - avariados acima do limite - gera perda de avariados`() = runTest {
        val sample = sampleWithinLimits(ardidos = 1.0f, spoiled = 8.0f)
        val slot = mockInsertAndCapture()

        useCase.execute(sample, defaultLimits, false, doesClassificationLoss = true, false)

        Assert.assertTrue("Perda de avariados deve ser > 0", slot.captured.spoiledLoss > 0f)
    }
}