package com.example.centreinar.ui.classificationProcess.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.repository.ClassificationRepository
import com.example.centreinar.data.repository.ClassificationRepositoryMilhoImpl
import com.example.centreinar.util.PDFExporterMilho
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
    // Dependências SOJA
    private val repositorySoja: ClassificationRepository,
    private val pdfExporterSoja: PDFExporterSoja,

    // Dependências MILHO
    private val repositoryMilho: ClassificationRepositoryMilhoImpl,
    private val pdfExporterMilho: PDFExporterMilho,

    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // =========================================================================
    // ESTADOS GERAIS (Compartilhados)
    // =========================================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- ESTADOS SALVÁVEIS (SavedStateHandle) ---
    var selectedGrain by savedStateHandle.saveable { mutableStateOf<String?>(null) }
    var selectedGroup by savedStateHandle.saveable { mutableStateOf<Int?>(null) }
    var isOfficial by savedStateHandle.saveable { mutableStateOf<Boolean?>(null) }
    var observation by savedStateHandle.saveable { mutableStateOf<String?>(null) }

    // Específico Soja (mas mantido no SavedStateHandle geral)
    var doesDefineColorClass by savedStateHandle.saveable { mutableStateOf<Boolean?>(null) }

    // =========================================================================
    // ESTADOS SOJA
    // =========================================================================

    private val _classificationSoja = MutableStateFlow<ClassificationSoja?>(null)
    // Mantido o nome 'classification' para compatibilidade com sua UI de Soja atual
    val classification: StateFlow<ClassificationSoja?> = _classificationSoja.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _lastUsedLimit = MutableStateFlow<LimitSoja?>(null)
    val lastUsedLimit: StateFlow<LimitSoja?> = _lastUsedLimit.asStateFlow()

    // =========================================================================
    // ESTADOS MILHO
    // =========================================================================

    private val _classificationMilho = MutableStateFlow<ClassificationMilho?>(null)
    val classificationMilho: StateFlow<ClassificationMilho?> = _classificationMilho.asStateFlow()


    // =========================================================================
    // GESTÃO DE ESTADO
    // =========================================================================

    fun clearStates() {
        // Limpa Soja
        _classificationSoja.value = null
        _defaultLimits.value = null
        _lastUsedLimit.value = null

        // Limpa Milho
        _classificationMilho.value = null

        // Limpa Gerais
        _isLoading.value = false
        _error.value = null
        selectedGrain = null
        selectedGroup = null
        isOfficial = null
        observation = null
        doesDefineColorClass = null
    }

    // =========================================================================
    // LÓGICA SOJA
    // =========================================================================

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
                    source = repositorySoja.getLastLimitSource()
                }

                val resultId = repositorySoja.classifySample(sample, source)
                val resultClassification = repositorySoja.getClassification(resultId.toInt())

                // ATUALIZAÇÃO DA DESCLASSIFICAÇÃO
                if (resultClassification != null) {
                    repositorySoja.updateDisqualification(resultId.toInt(), resultClassification.finalType)
                }

                _classificationSoja.value = resultClassification
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro desconhecido"
                Log.e("SampleInputSoja", "Falha na classificação", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setLimit(
        impurities: Float, moisture: Float, brokenCrackedDamaged: Float,
        greenish: Float, burnt: Float, burntOrSour: Float, moldy: Float, spoiled: Float
    ) {
        val grain = selectedGrain?.toString() ?: return
        val group = selectedGroup ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null
                repositorySoja.setLimit(
                    grain, group, 1, impurities, moisture, brokenCrackedDamaged,
                    greenish, burnt, burntOrSour, moldy, spoiled
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SampleInput", "Classification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Garante que a UI recarregue
    fun resetLimits() {
        _defaultLimits.value = null
    }

    fun setDisqualification(badConservation: Int, strangeSmell: Int, insects: Int, toxicGrains: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repositorySoja.setDisqualification(
                    classificationId = null,
                    badConservation = badConservation,
                    graveDefectSum = 0,
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
            repositorySoja.getLastColorClass()
        } catch (e: Exception) {
            _error.value = e.message ?: "Falha ao buscar a classe de cor"
            null
        } finally {
            _isLoading.value = false
        }
    }

    fun setClassColor(totalWeight: Float, otherColorsWeight: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            val grain = selectedGrain ?: return@launch
            val classificationId = classification.value?.id ?: return@launch
            repositorySoja.setClass(grain, classificationId, totalWeight, otherColorsWeight)
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

        // Zera os limites carregados previamente...
        _defaultLimits.value = null

        viewModelScope.launch(Dispatchers.IO) {
            Log.d("LimiteDebug", "Buscando limites para: Grão=$grain, Grupo=$group")
            try {
                // Lógica para SOJA...
                if (grain == "Soja") {
                    // Lógica existente para SOJA (já retorna um Map)
                    _defaultLimits.value = repositorySoja.getLimitOfType1Official(
                        grain = grain,
                        group = group
                    )
                } else {
                    // Lógica para MILHO...

                    val limitMilho = repositoryMilho.getLimit(grain, group, 1, 0) // 0 = Oficial

                    if (limitMilho != null) {
                        // Mapeia as propriedades do objeto LimitMilho para as chaves que a UI espera
                        _defaultLimits.value = mapOf(
                            "impuritiesUpLim" to limitMilho.impuritiesUpLim,
                            "moistureUpLim" to limitMilho.moistureUpLim, // Milho usa umidade
                            "brokenUpLim" to limitMilho.brokenUpLim,
                            "ardidosUpLim" to limitMilho.ardidoUpLim,   // Chave específica para Milho
                            "mofadosUpLim" to limitMilho.mofadoUpLim,    // Chave específica para Milho
                            "carunchadoUpLim" to limitMilho.carunchadoUpLim,
                            "moldyUpLim" to limitMilho.mofadoUpLim
                        )
                        Log.d("LimiteDebug", "Limites de Milho carregados e mapeados.")
                    } else {
                        Log.w("LimiteDebug", "Nenhum limite oficial encontrado para Milho.")
                    }
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("LimitDebug", "Falha ao carregar limites", e)
            }
        }
    }

    fun loadLastUsedLimit() {
        val grain = selectedGrain?.toString() ?: return
        val group = selectedGroup ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (isOfficial == true) {
                    _lastUsedLimit.value = repositorySoja.getLimit(grain, group, 1, 0)
                } else {
                    val source = repositorySoja.getLastLimitSource()
                    _lastUsedLimit.value = repositorySoja.getLimit(grain, group, 1, source)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro desconhecido"
            }
        }
    }

    suspend fun getObservations(colorClass: ColorClassificationSoja?): String {
        val classification = _classificationSoja.value ?: return "Erro na Classificação"
        return if (doesDefineColorClass == true) {
            repositorySoja.getObservations(classification.id, colorClass)
        } else {
            repositorySoja.getObservations(idClassification = classification.id)
        }
    }

    fun exportClassification(context: Context, classification: ClassificationSoja, limit: LimitSoja) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val sample = repositorySoja.getSample(classification.sampleId)
                val colorClassification = repositorySoja.getLastColorClass()
                val observation = repositorySoja.getObservations(classification.id, colorClassification)

                if (sample == null) {
                    _error.value = "Dados de amostra não encontrados"
                    return@launch
                }

                pdfExporterSoja.exportClassificationToPdf(
                    context, classification, sample, colorClassification, observation, limit
                )
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            }
        }
    }

    // =========================================================================
    // LÓGICA MILHO
    // =========================================================================

    /**
     * Classifica a amostra de MILHO.
     */
    fun classifySample(sample: SampleMilho) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            try {
                val limitSource = if (isOfficial == false) repositoryMilho.getLastLimitSource() else 0

                val id = repositoryMilho.classifySample(sample, limitSource)
                val classification = repositoryMilho.getClassification(id.toInt())

                _classificationMilho.value = classification
                Log.i("ClassificationMilho", "Classificação concluída com sucesso para o milho.")

            } catch (e: IllegalStateException) {
                _error.value = e.message
                Log.e("ClassificationMilho", "Erro de lógica: ${e.message}", e)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro inesperado durante a classificação"
                Log.e("ClassificationMilho", "Erro ao classificar", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun exportClassificationToPdf(
        context: Context,
        classification: ClassificationMilho,
        sample: SampleMilho,
        limit: LimitMilho
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Criação das entidades de desconto para o PDF do Milho
                val inputDiscount = InputDiscountMilho(
                    classificationId = classification.id,
                    grain = sample.grain,
                    group = sample.group,
                    limitSource = 0,
                    daysOfStorage = 0,
                    lotWeight = sample.lotWeight,
                    lotPrice = 0f,
                    impurities = sample.impurities,
                    humidity = 0f,
                    broken = sample.broken,
                    ardidos = sample.ardido,
                    mofados = sample.mofado,
                    carunchado = sample.carunchado,
                    deductionValue = 0f
                )

                val discount = DiscountMilho(
                    id = 0,
                    inputDiscountId = inputDiscount.id,
                    impuritiesLoss = classification.impuritiesPercentage,
                    humidityLoss = 0f,
                    technicalLoss = 0f,
                    brokenLoss = classification.brokenPercentage,
                    ardidoLoss = classification.ardidoPercentage,
                    mofadoLoss = classification.mofadoPercentage,
                    carunchadoLoss = classification.carunchadoPercentage,
                    fermentedLoss = classification.fermentedPercentage,
                    germinatedLoss = classification.germinatedPercentage,
                    gessadoLoss = classification.gessadoPercentage,
                    finalDiscount = 0f,
                    finalWeight = 0f
                )

                pdfExporterMilho.exportDiscountToPdf(context, discount, inputDiscount, limit)
                Log.i("ClassificationMilho", "PDF exportado com sucesso para o milho.")

            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao exportar PDF"
                Log.e("ClassificationMilho", "Erro ao exportar PDF", e)
            }
        }
    }

    // =========================================================================
    // UTILITÁRIOS GERAIS
    // =========================================================================

    fun getFinalTypeLabel(finalType: Int): String {
        val group = selectedGroup
        val grain = selectedGrain

        // 0 é o código de Desclassificação (Universal)
        if (finalType == 0) return "Desclassificada"
        // 7 é o código padronizado para FORA DE TIPO
        if (finalType == 7) return "Fora de Tipo"

        // Lógica de mapeamento para SOJA
        if (grain == "Soja") {
            return when (group) {
                1 -> when (finalType) {
                    1 -> "Tipo 1"
                    2 -> "Tipo 2"
                    else -> "Fora de Tipo"
                }
                2 -> when (finalType) {
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
                4 -> "Fora de Tipo"
                else -> "Erro de Tipo"
            }
        }

        return "Erro de Classificação"
    }
}