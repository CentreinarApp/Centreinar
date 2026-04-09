package com.example.centreinar.ui.classificationProcess.strategy

import com.example.centreinar.ClassificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.domain.usecase.ClassificationSojaResult
import com.example.centreinar.domain.usecase.ClassifySojaUseCase
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.util.PDFExporter
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SojaClassificationStrategyTest {

    private lateinit var repository: ClassificationRepository
    private lateinit var useCase: ClassifySojaUseCase
    private lateinit var pdfExporter: PDFExporter
    private lateinit var strategy: SojaClassificationStrategy

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = mockk()
        pdfExporter = mockk(relaxed = true)

        strategy = SojaClassificationStrategy(repository, useCase, pdfExporter)
    }

    @Test
    fun `buildPayload deve mapear corretamente`() {
        val state = ClassificationInputState(
            group = 1,
            lotWeight = 100f,
            sampleWeight = 50f,
            moisture = 12f,
            foreignMatters = 2f,
            greenish = 3f,
            brokenCrackedDamaged = 4f,
            damaged = 2f
        )

        val payload = strategy.buildPayload(state) as ClassificationPayload.Soja

        assertEquals(100f, payload.sample.lotWeight)
        assertEquals(3f, payload.sample.greenish)
    }

    @Test
    fun `getClassificationInputFields deve retornar campos`() {
        val fields = strategy.getClassificationInputFields()
        assertTrue(fields.isNotEmpty())
    }

    @Test
    fun `getLimitFields deve retornar limites`() {
        val limits = strategy.getLimitFields()
        assertTrue(limits.isNotEmpty())
    }

    @Test
    fun `classify deve montar estado completo`() = runTest {
        val sample = SampleSoja(1, "Soja", 1, 50f, 12f)

        val classification = mockk<ClassificationSoja>(relaxed = true)
        val limit = mockk<LimitSoja>(relaxed = true)

        val result = ClassificationSojaResult(
            resultId = 1L,
            limitUsed = limit,
            classification = classification,
            disqualification = null,
            toxicSeeds = emptyList(),
            colorClassification = null
        )

        coEvery {
            useCase.execute(
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns result

        val payload = ClassificationPayload.Soja(
            sample = sample,
            otherColorsWeight = 0f,
            baseWeightCor = 0f,
            isColorDefined = false
        )

        val state = strategy.classify(payload, false)

        assertNotNull(state)
        assertTrue(state.sampleInputRows.isNotEmpty())
        assertTrue(state.tableRows.isNotEmpty())
    }

    @Test
    fun `getTypeLabel deve retornar valores corretos`() {
        assertEquals("-", strategy.getTypeLabel(-1, 1))
        assertEquals("Desclassificada", strategy.getTypeLabel(0, 1))
        assertEquals("Fora de Tipo", strategy.getTypeLabel(7, 1))
    }
}