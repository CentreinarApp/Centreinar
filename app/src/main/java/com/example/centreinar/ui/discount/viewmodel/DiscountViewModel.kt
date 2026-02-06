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

    // Estados para preenchimento automático (Soja)
    private val _loadedClassification = MutableStateFlow<ClassificationSoja?>(null)
    val loadedClassification: StateFlow<ClassificationSoja?> = _loadedClassification.asStateFlow()

    private val _loadedSample = MutableStateFlow<SampleSoja?>(null)
    val loadedSample: StateFlow<SampleSoja?> = _loadedSample.asStateFlow()

    // =========================================================================
    // LÓGICA DE CARREGAMENTO DE CLASSIFICAÇÃO (AUTO-FILL)
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

                    val sample = repositorySoja.getSampleById(classification.sampleId)
                    if (sample != null) {
                        _loadedSample.value = sample
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar dados: ${e.message}"
                Log.e("DiscountVM", "LoadClassificationData failed", e)
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
                        val grainFixed = if (grainVal.equals("milho", ignoreCase = true)) "Milho" else grainVal
                        _allOfficialLimits.value = limitMilhoDao.getLimitsBySource(grainFixed, 0, groupVal)
                    }
                }

                if (grainVal == "Soja") {
                    // Ordem correta: (group, grain) conforme sua interface
                    _defaultLimits.value = repositorySoja.getLimitOfType1Official(groupVal, grainVal)
                } else {
                    val grainFixed = if (grainVal.equals("milho", ignoreCase = true)) "Milho" else grainVal
                    val limitMilho = repositoryMilho.getLimit(grainFixed, groupVal, 1, 0)

                    if (limitMilho != null) {
                        _defaultLimits.value = mapOf(
                            "impuritiesUpLim" to limitMilho.impuritiesUpLim,
                            "moistureUpLim" to limitMilho.moistureUpLim,
                            "brokenUpLim" to limitMilho.brokenUpLim,
                            "burntOrSourUpLim" to limitMilho.ardidoUpLim,
                            "moldyUpLim" to limitMilho.mofadoUpLim,
                            "carunchadoUpLim" to limitMilho.carunchadoUpLim,
                            "spoiledTotalUpLim" to limitMilho.spoiledTotalUpLim,
                            "greenishUpLim" to 0f,
                            "burntUpLim" to 0f
                        )
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao carregar limites"
            }
        }
    }

    fun loadLastUsedLimit() {
        val grainVal = selectedGrain ?: return
        val groupVal = selectedGroup ?: 1

        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (grainVal == "Soja") {
                    if (isOfficial == true) {
                        _lastUsedLimitSoja.value = repositorySoja.getLimit(grainVal, groupVal, 1, 0)
                    } else {
                        val source = repositorySoja.getLastLimitSource()
                        _lastUsedLimitSoja.value = repositorySoja.getLimit(grainVal, groupVal, 1, source)
                    }
                } else {
                    val grainFixed = if (grainVal.equals("milho", ignoreCase = true)) "Milho" else grainVal
                    if (isOfficial == true) {
                        _lastUsedLimitMilho.value = repositoryMilho.getLimit(grainFixed, groupVal, 1, 0)
                    } else {
                        val source = repositoryMilho.getLastLimitSource()
                        _lastUsedLimitMilho.value = repositoryMilho.getLimit(grainFixed, groupVal, 1, source)
                    }
                }
            } catch (e: Exception) {
                Log.e("UsedLimit", "Error loading last limit", e)
            }
        }
    }

    fun setLimit(
        impurities: Float, moisture: Float, brokenCrackedDamaged: Float,
        greenish: Float, burnt: Float, burntOrSour: Float, moldy: Float,
        spoiled: Float, carunchado: Float = 0f
    ) {
        val grain = selectedGrain ?: return
        val group = selectedGroup ?: 0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                if (grain == "Soja") {
                    repositorySoja.setLimit(
                        grain, group, 1, impurities, moisture,
                        brokenCrackedDamaged, greenish, burnt, burntOrSour, moldy, spoiled
                    )
                } else {
                    repositoryMilho.setLimit(
                        "Milho", group, 1, impurities, moisture,
                        brokenCrackedDamaged, burntOrSour, moldy, carunchado, spoiled
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
    // LÓGICA SOJA (Cálculo e Desconto)
    // =========================================================================

    fun getDiscountForClassification(
        priceBySack: Float,
        daysOfStorage: Int,
        deductionValue: Float
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                val discount = repositorySoja.getDiscountForClassification(
                    priceBySack, daysOfStorage, deductionValue
                )
                _discountsSoja.value = discount

                val lastInput = repositorySoja.getLastInputDiscount()
                selectedGrain = lastInput.grain
                selectedGroup = lastInput.group

            } catch (e: Exception) {
                _error.value = "Erro ao calcular desconto: ${e.message}"
                Log.e("DiscountVM", "getDiscountForClassification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setDiscount(
        inputDiscount: InputDiscountSoja,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        var discountId = 0L
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (isOfficial == false) {
                    inputDiscount.limitSource = repositorySoja.getLastLimitSource()
                }
                repositorySoja.setInputDiscount(inputDiscount)

                discountId = repositorySoja.calculateDiscount(
                    inputDiscount.grain, inputDiscount.group, 1, inputDiscount,
                    doesTechnicalLoss, doesClassificationLoss, doesDeduction
                )
                getDiscountSoja(discountId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
        return discountId
    }

    fun getDiscountSoja(id: Long) {
        viewModelScope.launch {
            try {
                _discountsSoja.value = repositorySoja.getDiscountById(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun exportDiscount(context: Context, discount: DiscountSoja, limit: LimitSoja) {
        viewModelScope.launch {
            try {
                val sample = repositorySoja.getLastInputDiscount()
                var classification: ClassificationSoja? = null
                var sampleClassification: SampleSoja? = null

                if (sample.classificationId != null) {
                    classification = repositorySoja.getClassificationById(sample.classificationId!!)
                    if (classification != null) {
                        sampleClassification = repositorySoja.getSampleById(classification.sampleId)
                    }
                }

                pdfExporterSoja.exportDiscountToPdf(
                    context, discount, sample, limit, classification, sampleClassification
                )
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
            }
        }
    }

    // =========================================================================
    // LÓGICA MILHO (Cálculo e Desconto)
    // =========================================================================

    fun setDiscount(
        inputDiscount: InputDiscountMilho,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        var discountId = 0L
        viewModelScope.launch {
            try {
                _isLoading.value = true
                if (isOfficial == false) {
                    inputDiscount.limitSource = repositoryMilho.getLastLimitSource()
                }
                repositoryMilho.setInputDiscount(inputDiscount)

                discountId = repositoryMilho.calculateDiscount(
                    inputDiscount.grain, inputDiscount.group, 1, inputDiscount,
                    doesTechnicalLoss, doesClassificationLoss, doesDeduction
                )
                getDiscountMilho(discountId)
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
        return discountId
    }

    fun getDiscountMilho(id: Long) {
        viewModelScope.launch {
            try {
                _discountsMilho.value = repositoryMilho.getDiscountById(id)
            } catch (e: Exception) {
                _error.value = e.message
            }
        }
    }

    fun exportDiscountMilho(context: Context, discount: DiscountMilho, limit: LimitMilho) {
        viewModelScope.launch {
            try {
                val sample = repositoryMilho.getLastInputDiscount()
                pdfExporterMilho.exportDiscountToPdf(context, discount, sample, limit)
            } catch (e: Exception) {
                _error.value = "Falha ao exportar PDF: ${e.message}"
            }
        }
    }
}