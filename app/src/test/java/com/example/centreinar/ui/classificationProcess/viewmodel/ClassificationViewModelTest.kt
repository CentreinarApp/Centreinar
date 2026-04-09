package com.example.centreinar.ui.classificationProcess.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationPayload
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationUIState
import com.example.centreinar.ui.classificationProcess.strategy.GrainStrategy
import com.example.centreinar.ui.classificationProcess.strategy.StrategyResultData
import com.example.centreinar.util.Utilities
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ClassificationViewModelTest {

    // Criamos um dispatcher de teste para controlar as coroutines do ViewModel
    private val testDispatcher = StandardTestDispatcher()

    private val mockStrategySoja: GrainStrategy = mockk(relaxed = true)
    private val strategiesMap: Map<String, GrainStrategy> = mapOf("Soja" to mockStrategySoja)

    private val savedStateHandle: SavedStateHandle = SavedStateHandle()
    private val tools: Utilities = mockk(relaxed = true)

    private lateinit var viewModel: ClassificationViewModel

    @Before
    fun setUp() {
        // Define o dispatcher principal para o nosso dispatcher de teste
        Dispatchers.setMain(testDispatcher)

        viewModel = ClassificationViewModel(strategiesMap, savedStateHandle, tools)
    }

    @After
    fun tearDown() {
        // Limpa o dispatcher após os testes
        Dispatchers.resetMain()
    }

    @Test
    fun `clearStates - repoe todos os valores para o estado inicial`() {
        // Simulamos que o utilizador já tinha feito coisas no ecrã
        viewModel.isOfficial = false
        savedStateHandle["selectedGrain"] = "Soja"
        savedStateHandle["selectedGroup"] = 2

        // Chamamos a função de limpeza
        viewModel.clearStates()

        // Verificamos se tudo voltou ao normal
        assertTrue(viewModel.isOfficial)
        assertNull(viewModel.selectedGrain)
        assertNull(viewModel.selectedGroup)
        assertEquals(ClassificationUIState(), viewModel.uiState.value)
        assertNull(viewModel.error.value)
    }

    @Test
    fun `classifySample - com erro - emite estado de erro e retira loading`() = runTest {
        // Preparação
        viewModel.selectedGrain = "Soja"
        viewModel.selectedGroup = 1

        val errorMessage = "Erro na base de dados"
        val dummyPayload = mockk<ClassificationPayload>() // Mock do payload esperado

        // Simulamos que a estratégia vai lançar um erro quando tentar classificar
        coEvery { mockStrategySoja.classify(any(), any()) } throws Exception(errorMessage)

        // Ação
        viewModel.classifySample(dummyPayload)

        // Avança o tempo virtual das coroutines até terminarem de executar
        advanceUntilIdle()

        // Verificações
        assertFalse("O loading deve ser falso no final", viewModel.isLoading.value)
        // O ViewModel concatena "Erro na classificação: " com a mensagem original
        assertEquals("Erro na classificação: $errorMessage", viewModel.error.value)
    }

    @Test
    fun `classifySample - com sucesso - atualiza uiState e retira loading`() = runTest {
        // Preparação
        viewModel.selectedGrain = "Soja"
        viewModel.selectedGroup = 1

        val dummyPayload = mockk<ClassificationPayload>()

        // Criamos o estado de sucesso (ClassificationUIState)
        val mockResult = ClassificationUIState(
            sampleLotWeight = 1000f
            // os outros parâmetros assumem o default (null/emptyList)
        )

        coEvery { mockStrategySoja.classify(any(), any()) } returns mockResult

        // Ação
        viewModel.classifySample(dummyPayload)

        advanceUntilIdle() // Processa todas as coroutines pendentes

        // Verificações
        assertFalse(viewModel.isLoading.value)
        assertNull("Não deve haver erro", viewModel.error.value)
        assertEquals(1000f, viewModel.uiState.value.sampleLotWeight, 0.0f)
    }
}