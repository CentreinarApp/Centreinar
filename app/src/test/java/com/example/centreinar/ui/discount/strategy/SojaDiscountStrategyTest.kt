package com.example.centreinar.ui.discount.strategy

import android.content.Context
import com.example.centreinar.*
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.domain.usecase.CalculateDiscountSojaUseCase
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.PDFExporter
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SojaDiscountStrategyTest {

    private lateinit var classificationRepo: ClassificationRepository
    private lateinit var discountRepo: DiscountRepository
    private lateinit var useCase: CalculateDiscountSojaUseCase
    private lateinit var pdfExporter: PDFExporter

    private lateinit var strategy: SojaDiscountStrategy

    @Before
    fun setup() {
        classificationRepo = mockk(relaxed = true)
        discountRepo = mockk(relaxed = true)
        useCase = mockk(relaxed = true)
        pdfExporter = mockk(relaxed = true)

        strategy = SojaDiscountStrategy(
            classificationRepo,
            discountRepo,
            useCase,
            pdfExporter
        )
    }

    // -------------------------
    // HELPERS
    // -------------------------

    private fun fakeFinancial() = FinancialDiscountPayload(
        lotWeight = 600f,
        priceBySack = 120f,
        daysOfStorage = 5,
        group = 1,
        sourceClassificationId = null,
        doesTechnicalLoss = false,
        doesDeduction = false,
        deductionValue = 0f
    )

    private fun fakePayload() = DiscountDefectsPayload.Soja(
        moisture = 10f,
        impurities = 1f,
        burntOrSour = 1f,
        burnt = 1f,
        moldy = 1f,
        greenish = 1f,
        spoiled = 1f,
        broken = 1f
    )

    // -------------------------
    // TESTES
    // -------------------------

    @Test
    fun `createDefectsPayload deve mapear corretamente`() {
        val map = mapOf(
            FieldKeys.MOISTURE to 10f,
            FieldKeys.IMPURITIES to 2f,
            FieldKeys.BURNT to 1f
        )

        val result = strategy.createDefectsPayload(map) as DiscountDefectsPayload.Soja

        assertEquals(10f, result.moisture)
        assertEquals(2f, result.impurities)
        assertEquals(1f, result.burnt)
        assertEquals(0f, result.moldy)
    }

    @Test
    fun `getDiscountInputRows deve formatar corretamente`() {
        val rows = strategy.getDiscountInputRows(fakePayload(), fakeFinancial())

        val umidade = rows.first { it.label == "Umidade" }
        assertTrue(umidade.value.startsWith("10"))

        val preco = rows.first { it.label == "Preço por Saca" }
        assertTrue(preco.value.contains("120"))
    }

    @Test
    fun `getBaseLimits retorna null quando vazio`() = runTest {
        coEvery { classificationRepo.getLimitsByGroup(any(), any(), any()) } returns emptyList()

        val result = strategy.getBaseLimits(1)

        assertNull(result)
    }

    @Test
    fun `calculateDiscount fluxo completo`() = runTest {
        val fakeInput = mockk<InputDiscountSoja> {
            every { id } returns 1
            every { group } returns 1
        }

        val fakeDiscount = mockk<DiscountSoja>(relaxed = true)

        coEvery { discountRepo.getLastInputDiscount() } returns fakeInput
        coEvery { useCase.execute(any(), any(), any(), any()) } returns 1L
        coEvery { discountRepo.getDiscountById(1L) } returns fakeDiscount
        coEvery { classificationRepo.getLimitsByGroup(any(), any(), any()) } returns emptyList()

        val result = strategy.calculateDiscount(fakePayload(), fakeFinancial(), true)

        assertTrue(result.summaryRows.isNotEmpty())
    }

    @Test
    fun `calculateDiscount retorna vazio quando payload nao eh soja`() = runTest {
        val payloadErrado = DiscountDefectsPayload.Milho(
            moisture = 0f,
            impurities = 0f,
            broken = 0f,
            ardido = 0f,
            carunchado = 0f,
            spoiled = 0f,
            mofados = 0f,
            fermented = 0f,
            germinated = 0f,
            gessado = 0f
        )

        val result = strategy.calculateDiscount(
            defectsPayload = payloadErrado,
            financialPayload = fakeFinancial(),
            isOfficial = true
        )

        assertTrue(result.summaryRows.isEmpty())
    }

    @Test
    fun `exportDiscountToPdf nao executa sem discount`() = runTest {
        val context = mockk<Context>()

        strategy.exportDiscountToPdf(context, null)

        coVerify(exactly = 0) {
            pdfExporter.exportDiscount(any(), any())
        }
    }
}