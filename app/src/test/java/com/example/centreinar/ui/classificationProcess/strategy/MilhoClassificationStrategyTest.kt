package com.example.centreinar.ui.classificationProcess.strategy

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.domain.usecase.ClassificationResult
import com.example.centreinar.domain.usecase.ClassifyMilhoUseCase
import com.example.centreinar.data.repository.ClassificationRepositoryMilho
import com.example.centreinar.util.PDFExporter
import io.mockk.*
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class MilhoClassificationStrategyTest {

    private lateinit var repository: ClassificationRepositoryMilho
    private lateinit var useCase: ClassifyMilhoUseCase
    private lateinit var pdfExporter: PDFExporter
    private lateinit var strategy: MilhoClassificationStrategy

    @Before
    fun setup() {
        repository = mockk(relaxed = true)
        useCase = mockk()
        pdfExporter = mockk(relaxed = true)

        strategy = MilhoClassificationStrategy(repository, useCase, pdfExporter)
    }

    @Test
    fun `buildPayload deve mapear corretamente`() {
        val state = ClassificationInputState(
            group = 1,
            lotWeight = 100f,
            sampleWeight = 50f,
            moisture = 12f,
            cleanSampleWeight = 40f,
            foreignMatters = 2f,
            brokenCrackedDamaged = 3f,
            carunchado = 1f,
            sour = 1f,
            moldy = 1f,
            fermented = 1f,
            germinated = 1f,
            immature = 1f,
            gessado = 1f
        )

        val payload = strategy.buildPayload(state) as ClassificationPayload.Milho

        assertEquals(100f, payload.sample.lotWeight)
        assertEquals(50f, payload.sample.sampleWeight)
        assertEquals(3f, payload.sample.broken)
    }

    @Test
    fun `getClassificationInputFields deve retornar campos`() {
        val fields = strategy.getClassificationInputFields()
        assertTrue(fields.isNotEmpty())
        assertTrue(fields.any { it.label.contains("Ardidos") })
    }

    @Test
    fun `getLimitFields deve retornar limites`() {
        val limits = strategy.getLimitFields()
        assertEquals(5, limits.size)
    }

    @Test
    fun `calculateDisplayCleanWeight deve calcular corretamente`() {
        val result = strategy.calculateDisplayCleanWeight(
            sampleWeight = 100f,
            foreignMatters = 10f,
            fieldValues = mapOf("broken" to 20f)
        )

        assertEquals(70f, result)
    }

    @Test
    fun `calculateDisplayCleanWeight nao pode ser negativo`() {
        val result = strategy.calculateDisplayCleanWeight(
            sampleWeight = 10f,
            foreignMatters = 20f,
            fieldValues = mapOf("broken" to 20f)
        )

        assertEquals(0f, result)
    }

    @Test
    fun `classify deve montar estado completo`() = runTest {
        val sample = SampleMilho(1, "Milho", 1, 50f, 12f)

        val classification = mockk<ClassificationMilho>(relaxed = true)
        val limit = mockk<LimitMilho>(relaxed = true)

        val result = ClassificationResult(
            resultId = 1L,
            limitUsed = limit,
            classification = classification,
            disqualification = null,
            toxicSeeds = emptyList(),
            complementaryData = null
        )

        coEvery {
            useCase.execute(
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any(),
                any()
            )
        } returns result

        val payload = ClassificationPayload.Milho(
            sample = sample,
            shouldDefineClass = false,
            weightYellow = 0f,
            weightWhite = 0f,
            weightMixedColors = 0f,
            shouldDefineGroup = false,
            weightHard = 0f,
            weightDent = 0f,
            weightSemiHard = 0f
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
        assertEquals("Tipo 2", strategy.getTypeLabel(2, 1))
    }
}