package com.example.centreinar.ui.classificationProcess.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.util.PDFExporterSoja
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ClassificationViewModel @Inject constructor(
    private val repository: ClassificationRepository,
    private val pdfExporter: PDFExporterSoja,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _classification = MutableStateFlow<ClassificationSoja?>(null)
    val classification: StateFlow<ClassificationSoja?> = _classification.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _lastUsedLimit = MutableStateFlow<LimitSoja?>(null)
    val lastUsedLimit: StateFlow<LimitSoja?> = _lastUsedLimit.asStateFlow()

    // --- ESTADO SALVÁVEL ---
    var selectedGrain by savedStateHandle.saveable {
        mutableStateOf<String?>(null)
    }

    var selectedGroup by savedStateHandle.saveable {
        mutableStateOf<Int?>(null)
    }

    var isOfficial by savedStateHandle.saveable {
        mutableStateOf<Boolean?>(null)
    }

    var observation by savedStateHandle.saveable {
        mutableStateOf<String?>(null)
    }
    var doesDefineColorClass by savedStateHandle.saveable {
        mutableStateOf<Boolean?>(null)
    }
    // ------------------------------------------

    fun clearStates() {
        _classification.value = null
        _isLoading.value = false
        _error.value = null
        _defaultLimits.value = null
        _lastUsedLimit.value = null

        selectedGrain = null
        selectedGroup = null
        isOfficial = null
        observation = null
        doesDefineColorClass = null
    }

    fun classifySample(sample: SampleSoja) {
        val grain = selectedGrain ?: run { _error.value = "Grão não selecionado"; return }
        val group = selectedGroup ?: run { _error.value = "Grupo não selecionado"; return }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null
                sample.grain = grain.toString()
                sample.group = group

                var source = 0

                if (isOfficial == false) {
                    source = repository.getLastLimitSource()
                }

                val resultId = repository.classifySample(sample, source)
                val resultClassification = repository.getClassification(resultId.toInt())

                // 🚨 ATUALIZAÇÃO DA DESCLASSIFICAÇÃO
                // Atualiza o registro de desclassificação que foi inserido previamente com ID=null/0
                if (resultClassification != null) {
                    repository.updateDisqualification(resultId.toInt(), resultClassification.finalType)
                }

                _classification.value = resultClassification
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SampleInput", "Classification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setLimit(
        impurities: Float,
        moisture: Float,
        brokenCrackedDamaged: Float,
        greenish: Float,
        burnt: Float,
        burntOrSour: Float,
        moldy: Float,
        spoiled: Float
    ) {

        val grain = selectedGrain?.toString() ?: run {
            Log.e("LimiteDebug", "ERRO: selectedGrain é nulo ao tentar salvar. Cancelando setLimit.")
            return
        }
        val group = selectedGroup ?: run {
            Log.e("LimiteDebug", "ERRO: selectedGroup é nulo ao tentar salvar. Cancelando setLimit.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null
                repository.setLimit(
                    grain,
                    group,
                    1,
                    impurities,
                    moisture,
                    brokenCrackedDamaged,
                    greenish,
                    burnt,
                    burntOrSour,
                    moldy,
                    spoiled
                )

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SampleInput", "Classification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // 🚨 CORREÇÃO CRÍTICA DO CRASH: Passa null para o classificationId na inserção inicial
    fun setDisqualification(badConservation: Int, strangeSmell: Int, insects: Int, toxicGrains: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // classificationId é passado como null para evitar o erro de Chave Estrangeira
                repository.setDisqualification(
                    classificationId = null, // <-- CORREÇÃO
                    badConservation = badConservation,
                    graveDefectSum = 0, // Inserido como 0 (será atualizado em classifySample)
                    strangeSmell = strangeSmell,
                    toxicGrains = toxicGrains,
                    insects = insects
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SetDisqualification", "Disqualification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    suspend fun getClassColor(): ColorClassificationSoja? {
        return try {
            _isLoading.value = true
            repository.getLastColorClass()
        } catch (e: Exception) {
            _error.value = e.message ?: "Falha ao buscar a classe de cor"
            Log.e("ClassColor", "Class Color failed", e)
            null
        } finally {
            _isLoading.value = false
        }
    }


    fun setClassColor(totalWeight: Float, otherColorsWeight: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val grain = selectedGrain ?: run {
                _error.value = "Grain not selected"
                return@launch
            }
            val classificationId = classification.value?.id ?: run {
                _error.value = "Classification not complete"
                return@launch
            }

            repository.setClass(grain, classificationId, totalWeight, otherColorsWeight)
        }
    }

    fun loadDefaultLimits() {
        val grain = selectedGrain?.toString() ?: run {
            Log.w("LimiteDebug", "ERRO: selectedGrain é nulo. Cancelando loadDefaultLimits.")
            return
        }
        val group = selectedGroup ?: run {
            Log.w("LimiteDebug", "ERRO: selectedGroup é nulo. Cancelando loadDefaultLimits.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("LimiteDebug", "Buscando limites para: Grão=$grain, Grupo=$group")
            try {
                _defaultLimits.value = repository.getLimitOfType1Official(
                    grain = grain,
                    group = group
                )
                Log.d("LimiteDebug", "Busca concluída. Itens carregados: ${_defaultLimits.value?.size}")

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("ClassColor", "Falha ao carregar limites", e)
            }
        }
    }

    fun loadLastUsedLimit() {
        val grain = selectedGrain?.toString() ?: run { return }
        val group = selectedGroup ?: run { return }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (isOfficial == true) {
                    _lastUsedLimit.value = repository.getLimit(grain, group, 1, 0)
                } else {
                    val source = repository.getLastLimitSource()
                    _lastUsedLimit.value = repository.getLimit(grain, group, 1, source)
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("UsedLimit", "Limit hasn't been loaded", e)
            }
        }
    }

    suspend fun getObservations(colorClass: ColorClassificationSoja?): String {
        val classification = _classification.value ?: return "Erro na Classificação"
        return if (doesDefineColorClass == true) {
            repository.getObservations(classification.id, colorClass)
        } else {
            repository.getObservations(idClassification = classification.id)
        }
    }

    // --- NOVO MÉTODO PÚBLICO: Mapeia o código numérico para o rótulo de texto (Correção de Rótulos) ---
    fun getFinalTypeLabel(finalType: Int): String {
        val group = selectedGroup
        val grain = selectedGrain

        // 0 é o código de Desclassificação (Universal)
        if (finalType == 0) return "Desclassificada"

        // 7 é o código que você usa para 'Fora de Tipo' (ou código > 2 no Grupo 1)
        if (finalType == 7) return "Fora de Tipo"

        // Lógica de mapeamento para SOJA
        if (grain == "Soja") {
            return when (group) {
                1 -> when (finalType) {
                    1 -> "Tipo 1"
                    2 -> "Tipo 2"
                    else -> "Fora de Tipo" // Qualquer outro código > 2
                }
                2 -> when (finalType) {
                    // CORREÇÃO: Grupo 2 só tem Padrão Básico
                    1, 2, 3 -> "Padrão Básico"
                    else -> "Fora de Tipo"
                }
                else -> "Erro de Grupo"
            }
        }

        // Lógica de mapeamento para MILHO
        if (grain == "Milho") {
            return when (finalType) {
                1 -> "Tipo 1"
                2 -> "Tipo 2"
                3 -> "Tipo 3"
                // No Milho, o Fora de Tipo é o Tipo 4, que na sua lógica de limite é o código 4.
                4 -> "Fora de Tipo"
                else -> "Erro de Tipo"
            }
        }

        return "Erro de Classificação"
    }
    // ---------------------------------------------------------------------------------------------


    fun exportClassification(context: Context, classification: ClassificationSoja, limit: LimitSoja) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Fetch data sequentially - each call will wait for completion
                val sample = repository.getSample(classification.sampleId)
                val colorClassification = repository.getLastColorClass()
                val observation = repository.getObservations(classification.id, colorClassification)


                // Check if we have all required data
                if (sample == null) {
                    _error.value = "Sample data not found"
                    Log.e("Export", "Sample not found for ID: ${classification.sampleId}")
                    return@launch
                }


                pdfExporter.exportClassificationToPdf(
                    context,
                    classification,
                    sample,
                    colorClassification,
                    observation,
                    limit
                )
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message ?: "Unknown error"}"
                Log.e("Export", "Export failed", e)
            }
        }
    }
}