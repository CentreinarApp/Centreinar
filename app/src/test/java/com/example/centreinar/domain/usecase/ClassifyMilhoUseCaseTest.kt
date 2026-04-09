package com.example.centreinar.domain.usecase

import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.repository.ClassificationRepositoryMilho
import com.example.centreinar.domain.rules.ClassMilhoResult
import com.example.centreinar.domain.rules.MilhoRules
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
import org.junit.Before
import org.junit.Test

class ClassifyMilhoUseCaseTest {

    private val repositoryMilho: ClassificationRepositoryMilho = mockk()
    private val milhoRules: MilhoRules = mockk()
    private lateinit var useCase: ClassifyMilhoUseCase

    @Before
    fun setUp() {
        useCase = ClassifyMilhoUseCase(repositoryMilho, milhoRules)

        // Mocks básicos de retorno para evitar NullPointers
        coEvery { repositoryMilho.getLastLimitSource() } returns 1
        coEvery { repositoryMilho.getLimit(any(), any(), any(), any()) } returns mockk<LimitMilho>()
        coEvery { repositoryMilho.classifySample(any(), any(), any()) } returns 100L
        coEvery { repositoryMilho.updateClassificationIdOnDisqualification(any(), any()) } just Runs
        coEvery { repositoryMilho.getClassification(any()) } returns mockk<ClassificationMilho>()
        coEvery { repositoryMilho.getDisqualificationByClassificationId(any()) } returns null
        coEvery { repositoryMilho.insertColorClassificationMilho(any()) } just Runs // Correção do Unit
    }

    @Test
    fun `execute - isOfficial falso - busca fonte de limite e salva classificacao`() = runTest {
        val sample = mockk<SampleMilho>(relaxed = true)

        // Correção do toxicGrains usando parâmetros nomeados
        val lastDisq = DisqualificationMilho(
            classificationId = 0,
            badConservation = 0,
            strangeSmell = 0,
            insects = 0,
            toxicGrains = 0
        )

        coEvery { repositoryMilho.getLastDisqualificationId() } returns 50
        coEvery { repositoryMilho.getLastDisqualification() } returns lastDisq

        val result = useCase.execute(
            sample = sample,
            shouldDefineClass = false,
            shouldDefineGroup = false,
            weightYellow = 0f, weightWhite = 0f, weightMixedColors = 0f,
            weightHard = 0f, weightDent = 0f, weightSemiHard = 0f,
            isOfficial = false
        )

        assertEquals(100L, result.resultId)
        assertNull(result.complementaryData) // Pois shouldDefineClass e Group são falsos

        coVerify { repositoryMilho.getLastLimitSource() } // Chamou pq isOfficial = false
        coVerify { repositoryMilho.updateClassificationIdOnDisqualification(50, 100) }
    }

    @Test
    fun `execute - getLastDisqualification retorna nulo - cria Disqualification segura default`() = runTest {
        val sample = mockk<SampleMilho>(relaxed = true)

        coEvery { repositoryMilho.getLastDisqualificationId() } returns null
        coEvery { repositoryMilho.getLastDisqualification() } returns null

        useCase.execute(
            sample = sample, shouldDefineClass = false, shouldDefineGroup = false,
            weightYellow = 0f, weightWhite = 0f, weightMixedColors = 0f,
            weightHard = 0f, weightDent = 0f, weightSemiHard = 0f, isOfficial = true
        )

        coVerify(exactly = 0) { repositoryMilho.updateClassificationIdOnDisqualification(any(), any()) }
        // Como passou pelo safeDisq, o classifySample não deve ter quebrado com NullPointer
        coVerify(exactly = 1) { repositoryMilho.classifySample(sample, 0, any()) }
    }

    @Test
    fun `execute - shouldDefineClass true - gera dados complementares`() = runTest {
        val sample = mockk<SampleMilho>(relaxed = true)
        coEvery { repositoryMilho.getLastDisqualificationId() } returns null
        coEvery { repositoryMilho.getLastDisqualification() } returns null

        val mockClassResult = mockk<ClassMilhoResult>(relaxed = true)
        every { milhoRules.calculateClass(any(), any(), any()) } returns mockClassResult

        val result = useCase.execute(
            sample = sample,
            shouldDefineClass = true,
            shouldDefineGroup = false, // Só Classe
            weightYellow = 50f, weightWhite = 0f, weightMixedColors = 0f,
            weightHard = 0f, weightDent = 0f, weightSemiHard = 0f,
            isOfficial = true
        )

        assertNotNull(result.complementaryData)
        coVerify(exactly = 1) { repositoryMilho.insertColorClassificationMilho(any()) }
    }
}