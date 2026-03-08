package com.example.centreinar.ui.classificationProcess.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationPayload
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationUIState
import com.example.centreinar.ui.classificationProcess.strategy.CustomLimitPayload
import com.example.centreinar.ui.classificationProcess.strategy.GrainStrategy
import com.example.centreinar.util.Utilities
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ClassificationViewModel @Inject constructor(
    private val strategies: Map<String, @JvmSuppressWildcards GrainStrategy>,
    private val savedStateHandle: SavedStateHandle,
    val utilities: Utilities
) : ViewModel() {
    private val currentStrategy: GrainStrategy?
        get() = strategies[selectedGrain]

    // =========================================================================
    // ESTADOS
    // =========================================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var selectedGrain by savedStateHandle.saveable { mutableStateOf<String?>(null) }
    var selectedGroup by savedStateHandle.saveable { mutableStateOf<Int?>(null) }
    var isOfficial by savedStateHandle.saveable { mutableStateOf<Boolean>(false) }
    var observation by savedStateHandle.saveable { mutableStateOf<String?>(null) }

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
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                val result = strategy.classify(payload, isOfficial)
                _uiState.value = result
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
        val strategy = currentStrategy ?: return // Garante que a estratégia foi carregada

        viewModelScope.launch {
            try {
                strategy.saveDisqualificationData(
                    classificationId = classificationId,
                    badConservation = badConservation,
                    strangeSmell = strangeSmell,
                    insects = insects,
                    toxicGrains = toxicGrains,
                    toxicSeeds = toxicSeeds
                )
                onSuccess()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun loadDefaultLimits() {
        val group = selectedGroup ?: return
        val strategy = currentStrategy ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true

                // Retry até o banco estar populado
                var baseLimits: Map<String, Float>? = null
                repeat(10) {
                    baseLimits = strategy.getBaseLimits(group)
                    if (baseLimits != null) return@repeat
                    kotlinx.coroutines.delay(300)
                }

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
        // Pegamos a estratégia atual
        val strategy = currentStrategy ?: return

        viewModelScope.launch {
            try {
                // A Strategy sabe em qual tabela do banco ela precisa dar o "delete"
                strategy.deleteCustomLimits()

                // Limpamos os limites do Estado da UI também, por segurança
                _uiState.update { it.copy(limitUsed = null) }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

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

    fun exportPdf(context: Context) {
        val strategy = currentStrategy ?: return
        val state = _uiState.value

        viewModelScope.launch(Dispatchers.IO) {
            try {
                strategy.exportClassificationToPdf(
                    context = context,
                    state = state,
                    limits = _allOfficialLimits.value,
                    observation = observation,
                    isOfficial = isOfficial
                )
            } catch (e: Exception) {
                _error.value = "Erro ao exportar PDF: ${e.message}"
            }
        }
    }

    // =========================================================================
    // UTILITÁRIOS
    // =========================================================================

    fun getFinalTypeLabel(finalType: Int): String {
        return currentStrategy?.getTypeLabel(finalType, selectedGroup ?: 1)
            ?: "Tipo $finalType"
    }

    fun clearStates() {
        _uiState.value = ClassificationUIState()
        _defaultLimits.value = null
        _allOfficialLimits.value = emptyList()
        _error.value = null
        observation = null
    }
}