package com.example.centreinar.domain.usecase

import com.example.centreinar.ClassificationSoja
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.domain.rules.ColorSojaResult
import com.example.centreinar.domain.rules.SojaRules
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.Runs
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test

class ClassifySojaUseCaseTest {

    private val repositorySoja: ClassificationRepository = mockk()
    private val sojaRules: SojaRules = mockk()
    private lateinit var useCase: ClassifySojaUseCase

    @Before
    fun setUp() {
        useCase = ClassifySojaUseCase(repositorySoja, sojaRules)

        // Mocks default
        coEvery { repositorySoja.getLastLimitSource() } returns 1
        coEvery { repositorySoja.getLimit(any(), any(), any(), any()) } returns mockk<LimitSoja>()
        coEvery { repositorySoja.classifySample(any(), any(), any()) } returns 200L
        coEvery { repositorySoja.updateClassificationIdOnDisqualification(any(), any()) } just Runs
        coEvery { repositorySoja.getClassification(any()) } returns mockk<ClassificationSoja>()
        coEvery { repositorySoja.getDisqualificationByClassificationId(any()) } returns null
        coEvery { repositorySoja.insertColorClassification(any()) } just Runs // Correção aqui!
    }

    @Test
    fun `execute - lastDisq nulo - lanca IllegalArgumentException`() = runTest {
        val sample = mockk<SampleSoja>(relaxed = true)

        // Simulando que o repositorio não tem desclassificação salva
        coEvery { repositorySoja.getLastDisqualificationId() } returns null
        coEvery { repositorySoja.getLastDisqualification() } returns null

        try {
            // Tentamos executar o UseCase
            useCase.execute(
                sample = sample,
                otherColorsWeight = 0f,
                baseWeightCor = 0f,
                isColorDefined = false,
                isOfficial = true
            )
            // Se a linha acima não jogar exceção, o teste deve falhar
            org.junit.Assert.fail("A exceção IllegalArgumentException deveria ter sido lançada")
        } catch (e: IllegalArgumentException) {
            // Sucesso! A exceção foi capturada. Agora verificamos a mensagem.
            assertEquals("Nenhuma desclassificação de Soja encontrada para vincular à amostra.", e.message)
        }
    }

    @Test
    fun `execute - sucesso e isOfficial true - usa source 0 e nao chama calculo de cor`() = runTest {
        val sample = mockk<SampleSoja>(relaxed = true)
        val disqMock = mockk<DisqualificationSoja>(relaxed = true)

        coEvery { repositorySoja.getLastDisqualificationId() } returns 33 // Ajustado para Long
        coEvery { repositorySoja.getLastDisqualification() } returns disqMock

        val result = useCase.execute(
            sample = sample,
            otherColorsWeight = 0f,
            baseWeightCor = 0f,
            isColorDefined = false,
            isOfficial = true
        )

        assertEquals(200L, result.resultId)
        assertNull(result.colorClassification)

        coVerify(exactly = 0) { repositorySoja.getLastLimitSource() } // Como é oficial (true), source deve ser 0
        coVerify(exactly = 1) { repositorySoja.updateClassificationIdOnDisqualification(33, 200) }
    }

    @Test
    fun `execute - isColorDefined true - calcula cor e salva ColorClassification`() = runTest {
        val sample = mockk<SampleSoja>(relaxed = true)
        val disqMock = mockk<DisqualificationSoja>(relaxed = true)

        coEvery { repositorySoja.getLastDisqualificationId() } returns 44 // Ajustado para Long
        coEvery { repositorySoja.getLastDisqualification() } returns disqMock

        val mockColorResult = ColorSojaResult(90f, 10f, "Amarela") // Ajuste conforme seu data class real
        every { sojaRules.calculateColor(any(), any()) } returns mockColorResult

        val result = useCase.execute(
            sample = sample,
            otherColorsWeight = 10f,
            baseWeightCor = 100f,
            isColorDefined = true,
            isOfficial = false
        )

        assertNotNull(result.colorClassification)
        assertEquals(90f, result.colorClassification?.yellowPercentage)
        assertEquals("Amarela", result.colorClassification?.framingClass)

        coVerify(exactly = 1) { repositorySoja.insertColorClassification(any()) }
    }
}