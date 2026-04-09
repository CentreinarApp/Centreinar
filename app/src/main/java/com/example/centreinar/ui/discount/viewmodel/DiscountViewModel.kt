package com.example.centreinar.ui.discount.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.ui.classificationProcess.strategy.BaseClassification
import com.example.centreinar.ui.discount.strategy.DiscountDefectsPayload
import com.example.centreinar.ui.discount.strategy.DiscountResult
import com.example.centreinar.ui.discount.strategy.DiscountUIState
import com.example.centreinar.ui.discount.strategy.FinancialDiscountPayload
import com.example.centreinar.ui.discount.strategy.GrainDiscountStrategy
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.retryUntilNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ClassificationPrefill(
    val lotWeight: Float?,
    val moisture: Float,
    val defects: Map<String, Float>
)

@HiltViewModel
class DiscountViewModel @Inject constructor(
    private val strategies: Map<String, @JvmSuppressWildcards GrainDiscountStrategy>,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // =========================================================================
    // ESTADO DE NAVEGAÇÃO
    //
    // Começa null — atualizado pelos botões de seleção nas telas.
    // currentStrategy é sempre derivado de selectedGrain em tempo real.
    // =========================================================================

    var selectedGrain         by savedStateHandle.saveable { mutableStateOf<String?>(null) }
    var selectedGroup         by savedStateHandle.saveable { mutableStateOf<Int?>(null) }
    var isOfficial            by savedStateHandle.saveable { mutableStateOf<Boolean>(true) }
    var sourceClassificationId: Int? = null

    // Sempre derivado de selectedGrain — nunca fixado na construção
    private val currentStrategy: GrainDiscountStrategy?
        get() = strategies[selectedGrain]

    // Descriptor do grão atualmente selecionado
    val currentDescriptor: GrainDescriptor?
        get() = currentStrategy?.descriptor

    // grainDescriptor: usado pelas telas OfficialOrNot.
    // Retorna o descriptor do grão atual ou o primeiro disponível como fallback.
    val grainDescriptor: GrainDescriptor
        get() = currentStrategy?.descriptor
            ?: strategies.values.first().descriptor

    val availableGrainDescriptors: List<GrainDescriptor> =
        strategies.values
            .map { it.descriptor }
            .sortedBy { it.displayName }

    fun setClassificationId(id: Int) {
        sourceClassificationId = id
    }

    fun getStrategy(grainName: String): GrainDiscountStrategy? = strategies[grainName]

    // =========================================================================
    // ESTADOS UI
    // =========================================================================

    private val _uiState = MutableStateFlow(DiscountUIState())
    val uiState: StateFlow<DiscountUIState> = _uiState.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _allOfficialLimits = MutableStateFlow<List<Any>>(emptyList())
    val allOfficialLimits: StateFlow<List<Any>> = _allOfficialLimits.asStateFlow()

    private val _discountResult = MutableStateFlow<DiscountResult?>(null)
    val discountResult: StateFlow<DiscountResult?> = _discountResult.asStateFlow()

    // One-shot navigation event — consumido exatamente uma vez
    private val _navigationEvent = Channel<DiscountNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _classificationPrefill = MutableStateFlow<ClassificationPrefill?>(null)
    val classificationPrefill: StateFlow<ClassificationPrefill?> = _classificationPrefill.asStateFlow()

    // =========================================================================
    // LÓGICA DE NEGÓCIO
    // =========================================================================

    fun loadFromClassification(
        lotWeight: Float,
        classification: BaseClassification,
        grain: String,
        group: Int,
        isOfficial: Boolean,
        classificationId: Int
    ) {
        selectedGrain          = grain
        selectedGroup          = group
        this.isOfficial        = isOfficial
        sourceClassificationId = classificationId

        _classificationPrefill.value = ClassificationPrefill(
            lotWeight = lotWeight.takeIf { it > 0f },
            moisture  = classification.moisturePercentage,
            defects   = classification.toDefectsMap()
        )
    }

    fun calculateDiscountFromClassification(
        lotWeight: Float,
        priceBySack: Float,
        daysOfStorage: Int = 0,
        doesTechnicalLoss: Boolean = false,
        deductionValue: Float = 0f,
        doesDeduction: Boolean = false
    ) {
        val strategy = currentStrategy ?: return
        val prefill  = _classificationPrefill.value ?: return

        val defectsMap = prefill.defects +
                mapOf(FieldKeys.MOISTURE to prefill.moisture)

        val defectsPayload   = strategy.createDefectsPayload(defectsMap)
        val financialPayload = FinancialDiscountPayload(
            priceBySack       = priceBySack,
            lotWeight         = lotWeight,
            group             = selectedGroup ?: 1,
            daysOfStorage     = daysOfStorage,
            doesTechnicalLoss = doesTechnicalLoss,
            deductionValue    = deductionValue,
            doesDeduction     = doesDeduction
        )

        calculateDiscount(defectsPayload, financialPayload)
    }

    fun loadDefaultLimits() {
        val group    = selectedGroup ?: return
        val strategy = currentStrategy ?: return

        viewModelScope.launch {
            try {
                _uiState.update { it.copy(isLoading = true, error = null) }

                val baseLimits = retryUntilNotNull { strategy.getBaseLimits(group) }

                if (baseLimits == null) {
                    _uiState.update { it.copy(error = "Não foi possível carregar os limites.") }
                    return@launch
                }

                _defaultLimits.value = baseLimits
                _allOfficialLimits.value = strategy.getLimitsForDisplay(group, isOfficial)

                // sucesso: garante que não exista erro
                _uiState.update { it.copy(error = null) }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao carregar limites: ${e.message}") }
            } finally {
                // NÃO limpar error aqui
                _uiState.update { it.copy(isLoading = false) }
            }
        }

    }

    // Recebe Map<String, Float> com FieldKeys.* — alinhado com LimitInputScreen genérico
    fun saveCustomLimit(limits: Map<String, Float>) {
        val strategy = currentStrategy ?: return
        viewModelScope.launch {
            try {
                strategy.saveCustomLimitData(selectedGroup ?: 1, limits)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao salvar limites: ${e.message}") }
            }
        }
    }

    fun getLimitFields() = currentStrategy?.getLimitFields() ?: emptyList()

    fun calculateDiscount(
        defectsPayload: DiscountDefectsPayload,
        financialPayload: FinancialDiscountPayload
    ) {
        val strategy = currentStrategy ?: return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = strategy.calculateDiscount(
                    defectsPayload   = defectsPayload,
                    financialPayload = financialPayload.copy(
                        sourceClassificationId = sourceClassificationId
                    ),
                    isOfficial = isOfficial
                )
                _discountResult.value = result

                // Passa diretamente o defectsPayload que veio do formulário de inputs (seja manual ou preenchido)
                val inputRows = strategy.getDiscountInputRows(defectsPayload, financialPayload)
                _uiState.update { it.copy(isLoading = false, discountInputRows = inputRows) }
                _navigationEvent.send(DiscountNavigationEvent.NavigateToResults)
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun exportPdf(context: Context) {
        val strategy = currentStrategy ?: return
        viewModelScope.launch {
            try {
                strategy.exportDiscountToPdf(context, sourceClassificationId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro na exportação: ${e.message}") }
            }
        }
    }

    fun clearDiscountResult() {
        _discountResult.value = null
    }

    /** Reset completo — chamado ao iniciar novo fluxo na GrainSelection. */
    fun clearStates() {
        _uiState.value               = DiscountUIState()
        _defaultLimits.value         = null
        _allOfficialLimits.value     = emptyList()
        _discountResult.value        = null
        _classificationPrefill.value = null
        sourceClassificationId       = null
        selectedGrain = null
        selectedGroup = null
        isOfficial    = true
    }

    /** Reset apenas dos dados de UI — preserva grain/group já selecionados.
     *  Chamado pelo GroupSelectionScreen para limpar limites sem perder o grão. */
    fun resetUiState() {
        _uiState.value           = DiscountUIState()
        _defaultLimits.value     = null
        _allOfficialLimits.value = emptyList()
    }
}

sealed class DiscountNavigationEvent {
    object NavigateToResults : DiscountNavigationEvent()
}