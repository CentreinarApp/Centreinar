package com.example.centreinar.ui.discount.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.DiscountSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.LimitSojaDao
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
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
class DiscountViewModel @Inject constructor(
    private val repositorySoja: DiscountRepository,
    private val pdfExporterSoja: PDFExporterSoja,
    private val limitSojaDao: LimitSojaDao,
    private val repositoryMilho: DiscountRepositoryMilho,
    private val pdfExporterMilho: PDFExporterMilho,
    private val limitMilhoDao: LimitMilhoDao,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // =========================================================================
    // ESTADOS GERAIS (Compartilhados)
    // =========================================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _allOfficialLimits = MutableStateFlow<List<Any>>(emptyList())
    val allOfficialLimits: StateFlow<List<Any>> = _allOfficialLimits.asStateFlow()

    var selectedGrain by savedStateHandle.saveable { mutableStateOf<String?>(null) }
    var selectedGroup by savedStateHandle.saveable { mutableStateOf<Int?>(null) }
    var isOfficial by savedStateHandle.saveable { mutableStateOf<Boolean?>(null) }

    // =========================================================================
    // ESTADOS PARA AUTO-PREENCHIMENTO (AUTO-FILL)
    // =========================================================================

    // Soja //
    private val _loadedClassification = MutableStateFlow<ClassificationSoja?>(null)
    val loadedClassification: StateFlow<ClassificationSoja?> = _loadedClassification.asStateFlow()

    private val _loadedSample = MutableStateFlow<SampleSoja?>(null)
    val loadedSample: StateFlow<SampleSoja?> = _loadedSample.asStateFlow()

    // Milho //
    private val _loadedClassificationMilho = MutableStateFlow<ClassificationMilho?>(null)
    val loadedClassificationMilho: StateFlow<ClassificationMilho?> = _loadedClassificationMilho.asStateFlow()

    private val _loadedSampleMilho = MutableStateFlow<SampleMilho?>(null)
    val loadedSampleMilho: StateFlow<SampleMilho?> = _loadedSampleMilho.asStateFlow()

    // =========================================================================
    // LÓGICA DE CARREGAMENTO AUTO-FILL
    // =========================================================================

    fun loadClassificationData(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                val classification = repositorySoja.getClassificationById(id)
                if (classification != null) {
                    _loadedClassification.value = classification
                    selectedGrain = classification.grain
                    selectedGroup = classification.group
                    _loadedSample.value = repositorySoja.getSampleById(classification.sampleId)
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados Soja: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadClassificationMilhoData(id: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                val classification = repositoryMilho.getClassificationById(id)
                if (classification != null) {
                    _loadedClassificationMilho.value = classification
                    selectedGrain = "Milho"
                    selectedGroup = classification.group
                    // Busca amostra usando o repositório de milho e a entidade SampleMilho
                    _loadedSampleMilho.value = repositoryMilho.getSampleById(classification.sampleId)
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados Milho: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================================
    // ESTADOS DE RESULTADO
    // =========================================================================

    private val _discountsSoja = MutableStateFlow<DiscountSoja?>(null)
    val discounts: StateFlow<DiscountSoja?> = _discountsSoja.asStateFlow()

    private val _lastUsedLimitSoja = MutableStateFlow<LimitSoja?>(null)
    val lastUsedLimit: StateFlow<LimitSoja?> = _lastUsedLimitSoja.asStateFlow()

    private val _discountsMilho = MutableStateFlow<DiscountMilho?>(null)
    val discountsMilho: StateFlow<DiscountMilho?> = _discountsMilho.asStateFlow()

    private val _lastUsedLimitMilho = MutableStateFlow<LimitMilho?>(null)
    val lastUsedLimitMilho: StateFlow<LimitMilho?> = _lastUsedLimitMilho.asStateFlow()

    // =========================================================================
    // GESTÃO DE ESTADO
    // =========================================================================

    fun clearStates() {
        _isLoading.value = false
        _error.value = null
        _defaultLimits.value = null
        _allOfficialLimits.value = emptyList()
        _discountsSoja.value = null
        _lastUsedLimitSoja.value = null
        _discountsMilho.value = null
        _lastUsedLimitMilho.value = null
        _loadedClassification.value = null
        _loadedSample.value = null
        _loadedClassificationMilho.value = null
        _loadedSampleMilho.value = null
        selectedGrain = null
        selectedGroup = null
        isOfficial = null
    }

    fun resetLimits() {
        _defaultLimits.value = null
        _allOfficialLimits.value = emptyList()
    }

    // =========================================================================
    // LÓGICA GERAL (Limites)
    // =========================================================================

    fun loadDefaultLimits() {
        val grainVal = selectedGrain ?: return
        val groupVal = selectedGroup ?: return
        val officialVal = isOfficial ?: true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (officialVal) {
                    if (grainVal == "Soja") {
                        _allOfficialLimits.value = limitSojaDao.getLimitsByGroup(grainVal, groupVal, 0)
                    } else {
                        _allOfficialLimits.value = limitMilhoDao.getLimitsBySource("Milho", 0, groupVal)
                    }
                }

                if (grainVal == "Soja") {
                    _defaultLimits.value = repositorySoja.getLimitOfType1Official(groupVal, grainVal)
                } else {
                    repositoryMilho.getLimit("Milho", groupVal, 1, 0)?.let { limit ->
                        _defaultLimits.value = mapOf(
                            "impuritiesUpLim" to limit.impuritiesUpLim,
                            "moistureUpLim" to limit.moistureUpLim,
                            "brokenUpLim" to limit.brokenUpLim,
                            "burntOrSourUpLim" to limit.ardidoUpLim,
                            "moldyUpLim" to limit.mofadoUpLim,
                            "carunchadoUpLim" to limit.carunchadoUpLim,
                            "spoiledTotalUpLim" to limit.spoiledTotalUpLim
                        )
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar limites: ${e.message}"
            }
        }
    }

    fun loadLastUsedLimit() {
        val grainVal = selectedGrain ?: return
        val groupVal = selectedGroup ?: 1
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (grainVal == "Soja") {
                    val source = if (isOfficial == true) 0 else repositorySoja.getLastLimitSource()
                    _lastUsedLimitSoja.value = repositorySoja.getLimit(grainVal, groupVal, 1, source)
                } else {
                    val source = if (isOfficial == true) 0 else repositoryMilho.getLastLimitSource()
                    _lastUsedLimitMilho.value = repositoryMilho.getLimit("Milho", groupVal, 1, source)
                }
            } catch (e: Exception) {
                Log.e("UsedLimit", "Error loading last limit", e)
            }
        }
    }

    fun setLimit(
        impurities: Float,
        moisture: Float,
        broken: Float,
        greenish: Float,
        burnt: Float,
        ardido: Float,
        moldy: Float,
        spoiled: Float,
        carunchado: Float = 0f
    ) {
        val grain = selectedGrain ?: return
        val group = selectedGroup ?: 0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                if (grain == "Soja") {
                    repositorySoja.setLimit(
                        grain = grain, group = group, type = 1, impurities = impurities, moisture = moisture,
                        brokenCrackedDamaged = broken, greenish = greenish, burnt = burnt, burntOrSour = ardido,
                        moldy = moldy, spoiled = spoiled
                    )
                } else {
                    repositoryMilho.setLimit(
                        grain = "Milho", group = group, type = 1, impurities = impurities, moisture = moisture,
                        broken = broken, ardido = ardido, mofado = moldy, carunchado = carunchado, spoiledTotal = spoiled
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================================
    // LÓGICA (Cálculo e Desconto)
    // =========================================================================

    // --- CÁLCULO SOJA ---
    fun setDiscount(input: InputDiscountSoja, tech: Boolean, classif: Boolean, deduct: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (isOfficial == false) input.limitSource = repositorySoja.getLastLimitSource()
                repositorySoja.setInputDiscount(input)
                val id = repositorySoja.calculateDiscount(input.grain, input.group, 1, input, tech, classif, deduct)

                // CRÍTICO: Atualiza o estado que a tela de resultado observa
                _discountsSoja.value = repositorySoja.getDiscountById(id)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // --- CÁLCULO MILHO ---
    fun setDiscount(input: InputDiscountMilho, tech: Boolean, classif: Boolean, deduct: Boolean) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (isOfficial == false) input.limitSource = repositoryMilho.getLastLimitSource()
                repositoryMilho.setInputDiscount(input)
                val id = repositoryMilho.calculateDiscount(input.grain, input.group, 1, input, tech, classif, deduct)

                // Atualiza o estado que a tela de resultado observa
                _discountsMilho.value = repositoryMilho.getDiscountById(id)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun getDiscountForClassification(price: Float, days: Int, deduction: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _discountsSoja.value = repositorySoja.getDiscountForClassification(price, days, deduction)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================================
    // EXPORTAÇÃO
    // =========================================================================

    fun exportDiscount(context: Context, discount: DiscountSoja, limit: LimitSoja) {
        viewModelScope.launch {
            try {
                val sample = repositorySoja.getLastInputDiscount()
                var classif: ClassificationSoja? = null
                var sampClass: SampleSoja? = null
                if (sample.classificationId != null) {
                    classif = repositorySoja.getClassificationById(sample.classificationId!!)
                    if (classif != null) sampClass = repositorySoja.getSampleById(classif.sampleId)
                }
                pdfExporterSoja.exportDiscountToPdf(context, discount, sample, limit, classif, sampClass)
            } catch (e: Exception) {
                _error.value = "Export falhou: ${e.message}"
            }
        }
    }

    fun exportDiscountMilho(context: Context, discount: DiscountMilho, limit: LimitMilho) {
        viewModelScope.launch {
            try {
                val sample = repositoryMilho.getLastInputDiscount()
                pdfExporterMilho.exportDiscountToPdf(context, discount, sample, limit)
            } catch (e: Exception) {
                _error.value = "Export falhou: ${e.message}"
            }
        }
    }
}