package com.example.centreinar.ui.discount.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.ui.classificationProcess.strategy.BaseClassification
import com.example.centreinar.ui.discount.strategy.DiscountDefectsPayload
import com.example.centreinar.ui.discount.strategy.DiscountInputRow
import com.example.centreinar.ui.discount.strategy.DiscountResult
import com.example.centreinar.ui.discount.strategy.FinancialDiscountPayload
import com.example.centreinar.ui.discount.strategy.GrainDiscountStrategy
import com.example.centreinar.util.FieldKeys
import io.mockk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Testes unitários para [DiscountViewModel].
 *
 * Estratégia de dispatcher:
 *   - Um único [TestCoroutineScheduler] é compartilhado entre [testDispatcher]
 *     (instalado no Main) e [ioDispatcher] (substituindo Dispatchers.IO via
 *     [Dispatchers.setMain] não é suficiente para IO — por isso o ViewModel
 *     precisa ser construído *após* o swap, e os blocos IO são drenados com
 *     [advanceUntilIdle]).
 *
 *   - Como [Dispatchers.IO] é hardcoded no ViewModel, usamos a propriedade
 *     de que kotlinx-coroutines-test >= 1.6 faz o [StandardTestDispatcher]
 *     compartilhar o mesmo scheduler quando passado como parâmetro para
 *     [runTest]. Combinando isso com [advanceUntilIdle] dentro do bloco de
 *     teste drenamos as coroutines de IO corretamente.
 *
 * Dependências:
 *   testImplementation("io.mockk:mockk:1.13.10")
 *   testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
 *   testImplementation("junit:junit:4.13.2")
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DiscountViewModelTest {

    // -------------------------------------------------------------------------
    // Infraestrutura
    // -------------------------------------------------------------------------

    // Scheduler único — compartilhado pelo Main e pelo runTest,
    // garantindo que advanceUntilIdle() drene também as coroutines de IO.
    private val scheduler      = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(scheduler)

    private lateinit var sojaStrategy: GrainDiscountStrategy
    private lateinit var milhoStrategy: GrainDiscountStrategy
    private lateinit var strategies: Map<String, GrainDiscountStrategy>
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var viewModel: DiscountViewModel

    private val sojaDescriptor = GrainDescriptor(
        name            = "soja",
        limitRegulation = "Instrução Normativa 11/2007",
        displayName     = "Soja",
        colorScheme     = "primary",
        supportsGroups  = true
    )

    private val milhoDescriptor = GrainDescriptor(
        name            = "milho",
        limitRegulation = "Instrução Normativa 60/2011",
        displayName     = "Milho",
        colorScheme     = "secondary",
        supportsGroups  = false
    )

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        sojaStrategy  = mockk(relaxed = true) { every { descriptor } returns sojaDescriptor }
        milhoStrategy = mockk(relaxed = true) { every { descriptor } returns milhoDescriptor }

        strategies       = mapOf("soja" to sojaStrategy, "milho" to milhoStrategy)
        savedStateHandle = SavedStateHandle()
        viewModel        = DiscountViewModel(strategies, savedStateHandle)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // =========================================================================
    // ESTADO INICIAL
    // =========================================================================

    @Test
    fun `estado inicial - selectedGrain e selectedGroup devem ser nulos`() {
        assertNull(viewModel.selectedGrain)
        assertNull(viewModel.selectedGroup)
        assertTrue(viewModel.isOfficial)
        assertNull(viewModel.sourceClassificationId)
    }

    @Test
    fun `estado inicial - uiState deve ser o estado padrao`() = runTest(scheduler) {
        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
        assertTrue(state.discountInputRows.isEmpty())
    }

    @Test
    fun `estado inicial - discountResult deve ser nulo`() = runTest(scheduler) {
        assertNull(viewModel.discountResult.first())
    }

    @Test
    fun `estado inicial - defaultLimits deve ser nulo`() = runTest(scheduler) {
        assertNull(viewModel.defaultLimits.first())
    }

    @Test
    fun `estado inicial - allOfficialLimits deve ser lista vazia`() = runTest(scheduler) {
        assertTrue(viewModel.allOfficialLimits.first().isEmpty())
    }

    @Test
    fun `estado inicial - classificationPrefill deve ser nulo`() = runTest(scheduler) {
        assertNull(viewModel.classificationPrefill.first())
    }

    // =========================================================================
    // availableGrainDescriptors
    // =========================================================================

    @Test
    fun `availableGrainDescriptors deve retornar descritores ordenados por displayName`() {
        val descriptors = viewModel.availableGrainDescriptors
        assertEquals(2, descriptors.size)
        assertEquals("Milho", descriptors[0].displayName)
        assertEquals("Soja", descriptors[1].displayName)
    }

    // =========================================================================
    // currentDescriptor / grainDescriptor
    // =========================================================================

    @Test
    fun `currentDescriptor retorna null quando nenhum grao esta selecionado`() {
        assertNull(viewModel.currentDescriptor)
    }

    @Test
    fun `currentDescriptor retorna o descriptor correto apos selecionar grao`() {
        viewModel.selectedGrain = "soja"
        assertEquals(sojaDescriptor, viewModel.currentDescriptor)
    }

    @Test
    fun `grainDescriptor retorna algum descriptor quando nenhum grao selecionado`() {
        val descriptor = viewModel.grainDescriptor
        assertNotNull(descriptor)
        assertTrue(descriptor.displayName == "Soja" || descriptor.displayName == "Milho")
    }

    @Test
    fun `grainDescriptor retorna descriptor do grao selecionado`() {
        viewModel.selectedGrain = "milho"
        assertEquals(milhoDescriptor, viewModel.grainDescriptor)
    }

    // =========================================================================
    // getStrategy
    // =========================================================================

    @Test
    fun `getStrategy retorna estrategia correta para grao existente`() {
        assertEquals(sojaStrategy, viewModel.getStrategy("soja"))
    }

    @Test
    fun `getStrategy retorna null para grao inexistente`() {
        assertNull(viewModel.getStrategy("trigo"))
    }

    // =========================================================================
    // setClassificationId
    // =========================================================================

    @Test
    fun `setClassificationId atualiza sourceClassificationId corretamente`() {
        viewModel.setClassificationId(42)
        assertEquals(42, viewModel.sourceClassificationId)
    }

    // =========================================================================
    // loadFromClassification
    // =========================================================================

    @Test
    fun `loadFromClassification configura todos os campos corretamente`() = runTest(scheduler) {
        val classification = mockk<BaseClassification> {
            every { moisturePercentage } returns 13.5f
            every { toDefectsMap() } returns mapOf("avariados" to 2.0f)
        }

        viewModel.loadFromClassification(
            lotWeight        = 1000f,
            classification   = classification,
            grain            = "soja",
            group            = 2,
            isOfficial       = false,
            classificationId = 99
        )

        assertEquals("soja", viewModel.selectedGrain)
        assertEquals(2, viewModel.selectedGroup)
        assertFalse(viewModel.isOfficial)
        assertEquals(99, viewModel.sourceClassificationId)

        val prefill = viewModel.classificationPrefill.first()
        assertNotNull(prefill)
        assertEquals(1000f, prefill!!.lotWeight)
        assertEquals(13.5f, prefill.moisture)
        assertEquals(2.0f, prefill.defects["avariados"])
    }

    @Test
    fun `loadFromClassification seta lotWeight como null quando valor e zero`() = runTest(scheduler) {
        val classification = mockk<BaseClassification> {
            every { moisturePercentage } returns 12f
            every { toDefectsMap() } returns emptyMap()
        }

        viewModel.loadFromClassification(
            lotWeight        = 0f,
            classification   = classification,
            grain            = "soja",
            group            = 1,
            isOfficial       = true,
            classificationId = 1
        )

        assertNull(viewModel.classificationPrefill.first()?.lotWeight)
    }

    // =========================================================================
    // loadDefaultLimits
    // =========================================================================

    @Test
    fun `loadDefaultLimits nao faz nada quando selectedGroup e nulo`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"
        viewModel.loadDefaultLimits()
        advanceUntilIdle()

        coVerify(exactly = 0) { sojaStrategy.getBaseLimits(any()) }
    }

    @Test
    fun `loadDefaultLimits nao faz nada quando selectedGrain e nulo`() = runTest(scheduler) {
        viewModel.selectedGroup = 1
        viewModel.loadDefaultLimits()
        advanceUntilIdle()

        coVerify(exactly = 0) { sojaStrategy.getBaseLimits(any()) }
    }

    @Test
    fun `loadDefaultLimits carrega limites com sucesso`() = runTest(scheduler) {
        val fakeLimits  = mapOf(FieldKeys.MOISTURE to 14f)
        val fakeDisplay = listOf<Any>()

        viewModel.selectedGrain = "soja"
        viewModel.selectedGroup = 1

        coEvery { sojaStrategy.getBaseLimits(1) } returns fakeLimits
        coEvery { sojaStrategy.getLimitsForDisplay(1, true) } returns fakeDisplay

        viewModel.loadDefaultLimits()
        advanceUntilIdle()

        assertEquals(fakeLimits, viewModel.defaultLimits.first())
        assertEquals(fakeDisplay, viewModel.allOfficialLimits.first())
        assertFalse(viewModel.uiState.first().isLoading)
        assertNull(viewModel.uiState.first().error)
    }

    @Test
    fun `loadDefaultLimits seta erro quando getBaseLimits sempre retorna null`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"
        viewModel.selectedGroup = 1

        // Retorna null em todas as tentativas do retryUntilNotNull —
        // o delay interno (300 ms × 10 tentativas) é avançado pelo advanceUntilIdle.
        coEvery { sojaStrategy.getBaseLimits(1) } returns null

        viewModel.loadDefaultLimits()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.error)
        assertFalse(state.isLoading)
        assertNull(viewModel.defaultLimits.first())
    }

    @Test
    fun `loadDefaultLimits seta erro quando exception e lancada`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"
        viewModel.selectedGroup = 1

        coEvery { sojaStrategy.getBaseLimits(1) } throws RuntimeException("Falha de rede")

        viewModel.loadDefaultLimits()
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertNotNull(state.error)
        assertTrue(state.error!!.contains("Falha de rede"))
        assertFalse(state.isLoading)
    }

    // =========================================================================
    // saveCustomLimit
    // =========================================================================

    @Test
    fun `saveCustomLimit nao faz nada quando currentStrategy e null`() = runTest(scheduler) {
        viewModel.saveCustomLimit(mapOf(FieldKeys.MOISTURE to 12f))
        advanceUntilIdle()

        coVerify(exactly = 0) { sojaStrategy.saveCustomLimitData(any(), any()) }
    }

    @Test
    fun `saveCustomLimit delega para a estrategia com group correto`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"
        viewModel.selectedGroup = 2
        val limits = mapOf(FieldKeys.MOISTURE to 12f)

        coEvery { sojaStrategy.saveCustomLimitData(2, limits) } just Runs

        viewModel.saveCustomLimit(limits)
        advanceUntilIdle()

        coVerify { sojaStrategy.saveCustomLimitData(2, limits) }
    }

    @Test
    fun `saveCustomLimit usa group 1 como fallback quando selectedGroup e nulo`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"
        val limits = mapOf(FieldKeys.MOISTURE to 12f)

        coEvery { sojaStrategy.saveCustomLimitData(1, limits) } just Runs

        viewModel.saveCustomLimit(limits)
        advanceUntilIdle()

        coVerify { sojaStrategy.saveCustomLimitData(1, limits) }
    }

    @Test
    fun `saveCustomLimit seta erro quando exception e lancada`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"
        viewModel.selectedGroup = 2
        val limits = mapOf(FieldKeys.MOISTURE to 12f)

        coEvery { sojaStrategy.saveCustomLimitData(2, limits) } throws Exception("DB error")

        viewModel.saveCustomLimit(limits)
        advanceUntilIdle()

        val error = viewModel.uiState.first().error
        assertNotNull(error)
        assertTrue(error!!.contains("DB error"))
    }

    // =========================================================================
    // getLimitFields
    // =========================================================================

    @Test
    fun `getLimitFields retorna lista vazia quando sem estrategia`() {
        assertTrue(viewModel.getLimitFields().isEmpty())
    }

    @Test
    fun `getLimitFields delega para a estrategia corretamente`() {
        viewModel.selectedGrain = "soja"
        val fakeFields = emptyList<com.example.centreinar.domain.model.LimitField>()
        every { sojaStrategy.getLimitFields() } returns fakeFields

        assertEquals(fakeFields, viewModel.getLimitFields())
    }

    // =========================================================================
    // calculateDiscount
    // =========================================================================

    @Test
    fun `calculateDiscount nao faz nada quando currentStrategy e null`() = runTest(scheduler) {
        val defectsPayload   = mockk<DiscountDefectsPayload>()
        val financialPayload = mockk<FinancialDiscountPayload>(relaxed = true)

        viewModel.calculateDiscount(defectsPayload, financialPayload)
        advanceUntilIdle()

        assertNull(viewModel.discountResult.first())
    }

    @Test
    fun `calculateDiscount atualiza discountResult e emite NavigateToResults`() = runTest(scheduler) {
        viewModel.selectedGrain          = "soja"
        viewModel.sourceClassificationId = 5

        val defectsPayload   = mockk<DiscountDefectsPayload>()
        val financialPayload = FinancialDiscountPayload(
            priceBySack       = 80f,
            lotWeight         = 1000f,
            group             = 1,
            daysOfStorage     = 0,
            doesTechnicalLoss = false,
            deductionValue    = 0f,
            doesDeduction     = false
        )
        val fakeResult    = mockk<DiscountResult>()
        val fakeInputRows = emptyList<DiscountInputRow>()

        coEvery {
            sojaStrategy.calculateDiscount(
                defectsPayload   = defectsPayload,
                financialPayload = financialPayload.copy(sourceClassificationId = 5),
                isOfficial       = true
            )
        } returns fakeResult

        every {
            sojaStrategy.getDiscountInputRows(defectsPayload, financialPayload)
        } returns fakeInputRows

        val navEvents = mutableListOf<DiscountNavigationEvent>()
        val job = launch { viewModel.navigationEvent.toList(navEvents) }

        viewModel.calculateDiscount(defectsPayload, financialPayload)
        advanceUntilIdle()

        assertEquals(fakeResult, viewModel.discountResult.first())
        assertFalse(viewModel.uiState.first().isLoading)
        assertNull(viewModel.uiState.first().error)
        assertTrue(navEvents.any { it is DiscountNavigationEvent.NavigateToResults })

        job.cancel()
    }

    @Test
    fun `calculateDiscount seta erro quando exception e lancada`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"

        val defectsPayload   = mockk<DiscountDefectsPayload>()
        val financialPayload = mockk<FinancialDiscountPayload>(relaxed = true)

        coEvery { sojaStrategy.calculateDiscount(any(), any(), any()) } throws RuntimeException("Cálculo falhou")

        viewModel.calculateDiscount(defectsPayload, financialPayload)
        advanceUntilIdle()

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertEquals("Cálculo falhou", state.error)
        assertNull(viewModel.discountResult.first())
    }

    // =========================================================================
    // calculateDiscountFromClassification
    // =========================================================================

    @Test
    fun `calculateDiscountFromClassification nao faz nada quando strategy e null`() = runTest(scheduler) {
        viewModel.selectedGroup = 1

        viewModel.calculateDiscountFromClassification(lotWeight = 1000f, priceBySack = 80f)
        advanceUntilIdle()

        coVerify(exactly = 0) { sojaStrategy.calculateDiscount(any(), any(), any()) }
    }

    @Test
    fun `calculateDiscountFromClassification nao faz nada quando prefill e null`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"
        viewModel.selectedGroup = 1

        viewModel.calculateDiscountFromClassification(lotWeight = 1000f, priceBySack = 80f)
        advanceUntilIdle()

        coVerify(exactly = 0) { sojaStrategy.calculateDiscount(any(), any(), any()) }
    }

    @Test
    fun `calculateDiscountFromClassification mescla moisture nos defects e chama calculateDiscount`() = runTest(scheduler) {
        val classification = mockk<BaseClassification> {
            every { moisturePercentage } returns 13f
            every { toDefectsMap() } returns mapOf("avariados" to 1f)
        }

        viewModel.loadFromClassification(
            lotWeight        = 500f,
            classification   = classification,
            grain            = "soja",
            group            = 1,
            isOfficial       = true,
            classificationId = 10
        )

        val fakeDefectsPayload = mockk<DiscountDefectsPayload>()
        val fakeResult         = mockk<DiscountResult>()

        every {
            sojaStrategy.createDefectsPayload(
                match { it[FieldKeys.MOISTURE] == 13f && it["avariados"] == 1f }
            )
        } returns fakeDefectsPayload

        coEvery { sojaStrategy.calculateDiscount(fakeDefectsPayload, any(), true) } returns fakeResult
        every { sojaStrategy.getDiscountInputRows(any(), any()) } returns emptyList()

        viewModel.calculateDiscountFromClassification(lotWeight = 500f, priceBySack = 90f)
        advanceUntilIdle()

        assertEquals(fakeResult, viewModel.discountResult.first())
    }

    @Test
    fun `calculateDiscountFromClassification repassa parametros financeiros corretamente`() = runTest(scheduler) {
        val classification = mockk<BaseClassification> {
            every { moisturePercentage } returns 14f
            every { toDefectsMap() } returns emptyMap()
        }

        viewModel.loadFromClassification(
            lotWeight        = 300f,
            classification   = classification,
            grain            = "soja",
            group            = 2,
            isOfficial       = true,
            classificationId = 20
        )

        every { sojaStrategy.createDefectsPayload(any()) } returns mockk()
        coEvery { sojaStrategy.calculateDiscount(any(), any(), any()) } returns mockk()
        every { sojaStrategy.getDiscountInputRows(any(), any()) } returns emptyList()

        viewModel.calculateDiscountFromClassification(
            lotWeight         = 300f,
            priceBySack       = 95f,
            daysOfStorage     = 30,
            doesTechnicalLoss = true,
            deductionValue    = 5f,
            doesDeduction     = true
        )
        advanceUntilIdle()

        coVerify {
            sojaStrategy.calculateDiscount(
                defectsPayload   = any(),
                financialPayload = match {
                    it.priceBySack            == 95f  &&
                            it.lotWeight              == 300f &&
                            it.group                  == 2    &&
                            it.daysOfStorage          == 30   &&
                            it.doesTechnicalLoss      == true &&
                            it.deductionValue         == 5f   &&
                            it.doesDeduction          == true &&
                            it.sourceClassificationId == 20
                },
                isOfficial = true
            )
        }
    }

    // =========================================================================
    // exportPdf
    // =========================================================================

    @Test
    fun `exportPdf nao faz nada quando strategy e null`() = runTest(scheduler) {
        val context = mockk<android.content.Context>()
        viewModel.exportPdf(context)
        advanceUntilIdle()

        coVerify(exactly = 0) { sojaStrategy.exportDiscountToPdf(any(), any()) }
    }

    @Test
    fun `exportPdf delega para a estrategia com sourceClassificationId correto`() = runTest(scheduler) {
        val context = mockk<android.content.Context>()
        viewModel.selectedGrain          = "soja"
        viewModel.sourceClassificationId = 7

        coEvery { sojaStrategy.exportDiscountToPdf(context, 7) } just Runs

        viewModel.exportPdf(context)
        advanceUntilIdle()

        coVerify { sojaStrategy.exportDiscountToPdf(context, 7) }
    }

    @Test
    fun `exportPdf seta erro quando exception e lancada`() = runTest(scheduler) {
        val context = mockk<android.content.Context>()
        viewModel.selectedGrain = "soja"

        coEvery { sojaStrategy.exportDiscountToPdf(any(), any()) } throws RuntimeException("Erro PDF")

        viewModel.exportPdf(context)
        advanceUntilIdle()

        val error = viewModel.uiState.first().error
        assertNotNull(error)
        assertTrue(error!!.contains("Erro PDF"))
    }

    // =========================================================================
    // clearDiscountResult
    // =========================================================================

    @Test
    fun `clearDiscountResult limpa o resultado apos calculo bem-sucedido`() = runTest(scheduler) {
        viewModel.selectedGrain = "soja"

        coEvery { sojaStrategy.calculateDiscount(any(), any(), any()) } returns mockk()
        every { sojaStrategy.getDiscountInputRows(any(), any()) } returns emptyList()

        viewModel.calculateDiscount(mockk(), mockk(relaxed = true))
        advanceUntilIdle()

        assertNotNull(viewModel.discountResult.first())

        viewModel.clearDiscountResult()

        assertNull(viewModel.discountResult.first())
    }

    // =========================================================================
    // clearStates
    // =========================================================================

    @Test
    fun `clearStates reseta todos os campos para os valores iniciais`() = runTest(scheduler) {
        viewModel.selectedGrain          = "soja"
        viewModel.selectedGroup          = 2
        viewModel.isOfficial             = false
        viewModel.sourceClassificationId = 99

        viewModel.clearStates()

        assertNull(viewModel.selectedGrain)
        assertNull(viewModel.selectedGroup)
        assertTrue(viewModel.isOfficial)
        assertNull(viewModel.sourceClassificationId)
        assertNull(viewModel.discountResult.first())
        assertNull(viewModel.defaultLimits.first())
        assertTrue(viewModel.allOfficialLimits.first().isEmpty())
        assertNull(viewModel.classificationPrefill.first())

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // =========================================================================
    // resetUiState
    // =========================================================================

    @Test
    fun `resetUiState limpa ui sem perder selectedGrain e selectedGroup`() = runTest(scheduler) {
        viewModel.selectedGrain = "milho"
        viewModel.selectedGroup = 3

        viewModel.resetUiState()

        assertEquals("milho", viewModel.selectedGrain)
        assertEquals(3, viewModel.selectedGroup)

        assertNull(viewModel.defaultLimits.first())
        assertTrue(viewModel.allOfficialLimits.first().isEmpty())

        val state = viewModel.uiState.first()
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // =========================================================================
    // Derivações de selectedGrain
    // =========================================================================

    @Test
    fun `alteracoes em selectedGrain sao refletidas imediatamente em currentDescriptor`() {
        assertNull(viewModel.currentDescriptor)

        viewModel.selectedGrain = "milho"
        assertEquals(milhoDescriptor, viewModel.currentDescriptor)

        viewModel.selectedGrain = "soja"
        assertEquals(sojaDescriptor, viewModel.currentDescriptor)

        viewModel.selectedGrain = null
        assertNull(viewModel.currentDescriptor)
    }

    @Test
    fun `isOfficial restaurado para true apos clearStates`() {
        viewModel.isOfficial = false
        assertFalse(viewModel.isOfficial)

        viewModel.clearStates()
        assertTrue(viewModel.isOfficial)
    }
}