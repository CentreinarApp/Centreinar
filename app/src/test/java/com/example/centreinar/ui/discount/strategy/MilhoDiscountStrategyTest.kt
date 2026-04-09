package com.example.centreinar.ui.discount.strategy

import android.content.Context
import com.example.centreinar.data.local.entity.*
import com.example.centreinar.data.repository.ClassificationRepositoryMilho
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import com.example.centreinar.domain.usecase.CalculateDiscountMilhoUseCase
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.PDFExporter
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MilhoDiscountStrategyTest {

    private lateinit var classificationRepo: ClassificationRepositoryMilho
    private lateinit var discountRepo: DiscountRepositoryMilho
    private lateinit var useCase: CalculateDiscountMilhoUseCase
    private lateinit var pdfExporter: PDFExporter

    private lateinit var strategy: MilhoDiscountStrategy

    @Before
    fun setup() {
        classificationRepo = mockk(relaxed = true)
        discountRepo = mockk(relaxed = true)
        useCase = mockk(relaxed = true)
        pdfExporter = mockk(relaxed = true)

        strategy = MilhoDiscountStrategy(
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

    private fun fakePayload() = DiscountDefectsPayload.Milho(
        moisture = 10f,
        impurities = 1f,
        broken = 1f,
        ardido = 1f,
        carunchado = 1f,
        spoiled = 1f,
        mofados = 0f,
        fermented = 0f,
        germinated = 0f,
        gessado = 0f
    )

    // -------------------------
    // TESTES
    // -------------------------

    @Test
    fun `createDefectsPayload milho mapeia corretamente`() {
        val map = mapOf(
            FieldKeys.MOISTURE to 12f,
            FieldKeys.ARDIDO to 2f
        )

        val result = strategy.createDefectsPayload(map) as DiscountDefectsPayload.Milho

        assertEquals(12f, result.moisture)
        assertEquals(2f, result.ardido)
        assertEquals(0f, result.carunchado)
    }

    @Test
    fun `getDiscountInputRows milho retorna dados`() {
        val rows = strategy.getDiscountInputRows(fakePayload(), fakeFinancial())

        assertTrue(rows.isNotEmpty())
        assertTrue(rows.any { it.label == "Umidade" })
    }

    @Test
    fun `getOfficialTableData vazio`() = runTest {
        coEvery { classificationRepo.getLimitsByGroup(any(), any(), any()) } returns emptyList()

        val result = strategy.getOfficialTableData(1)

        assertEquals(0, result.first)
        assertTrue(result.second.isEmpty())
    }

    @Test
    fun `calculateDiscount milho fluxo completo`() = runTest {
        val fakeInput = mockk<InputDiscountMilho> {
            every { group } returns 1
        }

        val fakeDiscount = mockk<DiscountMilho>(relaxed = true)

        coEvery { discountRepo.getLastInputDiscount() } returns fakeInput
        coEvery { useCase.execute(any(), any(), any(), any()) } returns 1L
        coEvery { discountRepo.getDiscountById(any()) } returns fakeDiscount
        coEvery { classificationRepo.getLimitsByGroup(any(), any(), any()) } returns emptyList()

        val result = strategy.calculateDiscount(fakePayload(), fakeFinancial(), true)

        assertNotNull(result)
    }

    @Test
    fun `calculateDiscount retorna vazio quando payload nao eh milho`() = runTest {
        val payloadErrado = DiscountDefectsPayload.Soja(
            moisture = 0f,
            impurities = 0f,
            burntOrSour = 0f,
            burnt = 0f,
            moldy = 0f,
            greenish = 0f,
            spoiled = 0f,
            broken = 0f
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