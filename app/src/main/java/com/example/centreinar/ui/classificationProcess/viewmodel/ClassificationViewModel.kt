package com.example.centreinar.ui.classificationProcess.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationInputState
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationPayload
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationUIState
import com.example.centreinar.ui.classificationProcess.strategy.CustomLimitPayload
import com.example.centreinar.ui.classificationProcess.strategy.GrainStrategy
import com.example.centreinar.util.Utilities
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

// =============================================================================
// NAVIGATION EVENT
// =============================================================================

sealed class ClassificationNavigationEvent {
    data object NavigateToResult : ClassificationNavigationEvent()
}

@HiltViewModel
class ClassificationViewModel @Inject constructor(
    private val strategies: Map<String, @JvmSuppressWildcards GrainStrategy>,
    private val savedStateHandle: SavedStateHandle,
    val utilities: Utilities
) : ViewModel() {

    // =========================================================================
    // ESTADO DE NAVEGAÇÃO
    //
    // Começa null — atualizado pelos botões de seleção nas telas.
    // currentStrategy é sempre derivado de selectedGrain em tempo real,
    // nunca fixado na construção do ViewModel.
    // =========================================================================

    var selectedGrain by savedStateHandle.saveable { mutableStateOf<String?>(null) }
    var selectedGroup by savedStateHandle.saveable { mutableStateOf<Int?>(null) }
    var isOfficial    by savedStateHandle.saveable { mutableStateOf<Boolean>(true) }
    var observation   by savedStateHandle.saveable { mutableStateOf<String?>(null) }

    // Sempre derivado de selectedGrain — atualiza automaticamente ao mudar o grão
    private val currentStrategy: GrainStrategy?
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

    val calculateCleanWeight: (Float, Float, Map<String, Float>) -> Float
        get() = { sw, fm, fields ->
            currentStrategy?.calculateDisplayCleanWeight(sw, fm, fields)
                ?: (sw - fm).coerceAtLeast(0f)
        }


    val cleanWeightLabel: String
        get() = currentStrategy?.cleanWeightLabel ?: "Peso da Amostra Limpa (Peso da Amostra de Trabalho - Peso da MEI):"


    fun buildPayload(state: ClassificationInputState): ClassificationPayload? =
        currentStrategy?.buildPayload(state)

    /**
     * Ponto de entrada conveniente para as telas — recebe o [ClassificationInputState]
     * diretamente, constrói o payload internamente e dispara a classificação.
     */
    fun classify(state: ClassificationInputState) {
        val payload = buildPayload(state) ?: return
        classifySample(payload)
    }

    // =========================================================================
    // ESTADOS UI
    // =========================================================================

    private val _navigationEvent = Channel<ClassificationNavigationEvent>(Channel.BUFFERED)
    val navigationEvent = _navigationEvent.receiveAsFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _allOfficialLimits = MutableStateFlow<List<Any>>(emptyList())
    val allOfficialLimits: StateFlow<List<Any>> = _allOfficialLimits.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _uiState = MutableStateFlow(ClassificationUIState())
    val uiState: StateFlow<ClassificationUIState> = _uiState.asStateFlow()

    // =========================================================================
    // LÓGICA DE NEGÓCIO
    // =========================================================================

    fun classifySample(payload: ClassificationPayload) {
        val strategy = currentStrategy ?: return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val result = strategy.classify(payload, isOfficial)
                _uiState.value = result
                _navigationEvent.send(ClassificationNavigationEvent.NavigateToResult)
            } catch (e: Exception) {
                _error.value = "Erro na classificação: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun saveDisqualificationData(
        classificationId: Int,
        badConservation: Int,
        strangeSmell: Int,
        insects: Int,
        toxicGrains: Int,
        toxicSeeds: List<Pair<String, String>>,
        onSuccess: () -> Unit
    ) {
        val strategy = currentStrategy ?: return
        viewModelScope.launch {
            try {
                strategy.saveDisqualificationData(
                    classificationId = classificationId,
                    badConservation  = badConservation,
                    strangeSmell     = strangeSmell,
                    insects          = insects,
                    toxicGrains      = toxicGrains,
                    toxicSeeds       = toxicSeeds
                )
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadDefaultLimits() {
        val group    = selectedGroup ?: return
        val strategy = currentStrategy ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true

                val baseLimits = retryUntilNotNull { strategy.getBaseLimits(group) }

                if (baseLimits == null) {
                    _error.value = "Não foi possível carregar os limites."
                    return@launch
                }

                _defaultLimits.value = baseLimits

                if (isOfficial) {
                    _allOfficialLimits.value = strategy.getOfficialLimits(group)
                }

            } catch (e: Exception) {
                _error.value = "Erro ao carregar limites: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun resetLimits() {
        val strategy = currentStrategy ?: return
        viewModelScope.launch {
            try {
                strategy.deleteCustomLimits()
                _uiState.update { it.copy(limitUsed = null) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getLimitFields() = currentStrategy?.getLimitFields() ?: emptyList()

    fun getClassificationInputFields() = currentStrategy?.getClassificationInputFields() ?: emptyList()

    fun deleteCustomLimits() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                currentStrategy?.deleteCustomLimits()
            } catch (e: Exception) {
                Log.e("ClassificationVM", "Erro ao deletar limites: ${e.message}")
            }
        }
    }

    fun setLimit(payload: CustomLimitPayload) {
        val strategy = currentStrategy ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                strategy.setCustomLimit(payload)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun setLimitAndAwait(payload: CustomLimitPayload) {
        val strategy = currentStrategy ?: return
        try {
            _isLoading.value = true
            strategy.setCustomLimit(payload)
        } catch (e: Exception) {
            _error.value = e.message
        } finally {
            _isLoading.value = false
        }
    }

    fun exportPdf(context: Context) {
        val strategy = currentStrategy ?: return
        val state    = _uiState.value
        viewModelScope.launch(Dispatchers.IO) {
            try {
                strategy.exportClassificationToPdf(
                    context     = context,
                    state       = state,
                    limits      = _allOfficialLimits.value,
                    observation = observation,
                    isOfficial  = isOfficial
                )
            } catch (e: Exception) {
                _error.value = "Erro ao exportar PDF: ${e.message}"
            }
        }
    }

    fun getFinalTypeLabel(finalType: Int): String =
        currentStrategy?.getTypeLabel(finalType, selectedGroup ?: 1) ?: "Tipo $finalType"

    /** Reset completo — chamado ao iniciar novo fluxo na GrainSelection. */
    fun clearStates() {
        _uiState.value           = ClassificationUIState()
        _defaultLimits.value     = null
        _allOfficialLimits.value = emptyList()
        _error.value             = null
        observation              = null
        selectedGrain = null
        selectedGroup = null
        isOfficial    = true
    }
}