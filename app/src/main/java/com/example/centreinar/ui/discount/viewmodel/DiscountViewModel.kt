package com.example.centreinar.ui.discount.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.ui.classificationProcess.strategy.BaseClassification
import com.example.centreinar.ui.discount.strategy.DiscountDefectsPayload
import com.example.centreinar.ui.discount.strategy.DiscountResult
import com.example.centreinar.ui.discount.strategy.DiscountUIState
import com.example.centreinar.ui.discount.strategy.FinancialDiscountPayload
import com.example.centreinar.ui.discount.strategy.GrainDiscountStrategy
import com.example.centreinar.util.retryUntilNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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

    var selectedGrain         by savedStateHandle.saveable { mutableStateOf<String>("Soja") }
    var selectedGroup         by savedStateHandle.saveable { mutableStateOf<Int>(1) }
    var isOfficial            by savedStateHandle.saveable { mutableStateOf<Boolean>(true) }
    var sourceClassificationId: Int? = null

    fun setClassificationId(id: Int) {
        sourceClassificationId = id
    }

    private val currentStrategy: GrainDiscountStrategy?
        get() = strategies[selectedGrain]

    // Descriptor do grão atualmente selecionado — consumido pelas telas de desconto.
    val currentDescriptor: GrainDescriptor?
        get() = currentStrategy?.descriptor

    val availableGrainDescriptors: List<GrainDescriptor> =
        strategies.values
            .map { it.descriptor }
            .sortedBy { it.displayName }

    // =========================================================================
    // ESTADOS
    // =========================================================================

    private val _uiState = MutableStateFlow(DiscountUIState())
    val uiState: StateFlow<DiscountUIState> = _uiState.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _allOfficialLimits = MutableStateFlow<List<Any>>(emptyList())
    val allOfficialLimits: StateFlow<List<Any>> = _allOfficialLimits.asStateFlow()

    private val _discountResult = MutableStateFlow<DiscountResult?>(null)
    val discountResult: StateFlow<DiscountResult?> = _discountResult.asStateFlow()

    // -------------------------------------------------------------------------
    // One-shot navigation event — Channel garante que o evento é consumido
    // exatamente uma vez, independente de recomposições ou back stack.
    // -------------------------------------------------------------------------
    private val _navigationEvent = Channel<DiscountNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _classificationPrefill = MutableStateFlow<ClassificationPrefill?>(null)

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

    // -------------------------------------------------------------------------
    // Calcula o desconto direto a partir dos dados de classificação.
    // Recebe apenas os dados financeiros — os defeitos vêm do classificationPrefill
    // que já foi populado por loadFromClassification().
    // A tela não precisa conhecer DiscountDefectsPayload, nem acessar o prefill.
    // -------------------------------------------------------------------------
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
            priceBySack   = priceBySack,
            lotWeight     = lotWeight,
            group         = selectedGroup,
            daysOfStorage = daysOfStorage,
            doesTechnicalLoss = doesTechnicalLoss,
            deductionValue    = deductionValue,
            doesDeduction     = doesDeduction
        )

        calculateDiscount(defectsPayload, financialPayload)
    }

    fun loadDefaultLimits() {
        val group    = selectedGroup
        val strategy = currentStrategy ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoading = true) }

                val baseLimits = retryUntilNotNull { strategy.getBaseLimits(group) }

                if (baseLimits == null) {
                    _uiState.update { it.copy(error = "Não foi possível carregar os limites.") }
                    return@launch
                }

                _defaultLimits.value = baseLimits

                if (isOfficial) {
                    _allOfficialLimits.value = strategy.getOfficialLimitsList(group)
                }

            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao carregar limites: ${e.message}") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    fun saveCustomLimit(limits: Map<String, Float>) {
        val strategy = currentStrategy ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                strategy.saveCustomLimitData(selectedGroup, limits)
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
                    isOfficial       = isOfficial
                )
                _discountResult.value = result

                // Delega para a strategy — que define a ordem e os campos do seu grão
                val prefill = _classificationPrefill.value
                val inputRows = strategy.getDiscountInputRows(prefill, financialPayload)
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

    fun getStrategy(grainName: String): GrainDiscountStrategy? = strategies[grainName]

    fun clearStates() {
        _uiState.value               = DiscountUIState()
        _defaultLimits.value         = null
        _allOfficialLimits.value     = emptyList()
        _discountResult.value        = null
        _classificationPrefill.value = null
        sourceClassificationId       = null
    }
}

sealed class DiscountNavigationEvent {
    object NavigateToResults : DiscountNavigationEvent()
}