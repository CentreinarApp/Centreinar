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
    // Dependências SOJA
    private val repositorySoja: DiscountRepository,
    private val pdfExporterSoja: PDFExporterSoja,

    // Dependências MILHO
    private val repositoryMilho: DiscountRepositoryMilho,
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

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    // --- ESTADOS SALVÁVEIS ---
    var selectedGrain by savedStateHandle.saveable { mutableStateOf<String?>(null) }
    var selectedGroup by savedStateHandle.saveable { mutableStateOf<Int?>(null) }
    var isOfficial by savedStateHandle.saveable { mutableStateOf<Boolean?>(null) }

    // =========================================================================
    // ESTADOS SOJA
    // =========================================================================

    private val _discountsSoja = MutableStateFlow<DiscountSoja?>(null)
    val discounts: StateFlow<DiscountSoja?> = _discountsSoja.asStateFlow()

    private val _lastUsedLimitSoja = MutableStateFlow<LimitSoja?>(null)
    val lastUsedLimit: StateFlow<LimitSoja?> = _lastUsedLimitSoja.asStateFlow()

    // =========================================================================
    // ESTADOS MILHO
    // =========================================================================

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

        // Limpa Soja
        _discountsSoja.value = null
        _lastUsedLimitSoja.value = null

        // Limpa Milho
        _discountsMilho.value = null
        _lastUsedLimitMilho.value = null

        selectedGrain = null
        selectedGroup = null
        isOfficial = null
    }

    // Limpa o StateFlow para que a UI saiba que deve recarregar os dados
    fun resetLimits() {
        _defaultLimits.value = null
    }

    // =========================================================================
    // LÓGICA GERAL (Limites)
    // =========================================================================

    fun loadDefaultLimits() {
        // Zera os limites carregados previamente...
        _defaultLimits.value = null

        val grain = selectedGrain?.toString() ?: run {
            Log.w("DiscountLimit", "ERRO: selectedGrain é nulo.")
            return
        }
        val group = selectedGroup ?: run {
            Log.w("DiscountLimit", "ERRO: selectedGroup é nulo.")
            return
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Lógica da SOJA...
                if (grain == "Soja") {
                    _defaultLimits.value = repositorySoja.getLimitOfType1Official(
                        grain = grain,
                        group = group
                    )
                } else {
                    // Lógica do MILHO...
                    val grainFixed = if (grain.equals("milho", ignoreCase = true)) "Milho" else grain

                    // Busca limite oficial (source 0)
                    val limitMilho = repositoryMilho.getLimit(grainFixed, group, 1, 0)

                    if (limitMilho != null) {
                        _defaultLimits.value = mapOf(
                            "impuritiesUpLim" to limitMilho.impuritiesUpLim,
                            "moistureUpLim" to limitMilho.moistureUpLim,
                            "brokenUpLim" to limitMilho.brokenUpLim,
                            // Mapeia Ardidos para o campo burntOrSour da UI
                            "burntOrSourUpLim" to limitMilho.ardidoUpLim,
                            "moldyUpLim" to limitMilho.mofadoUpLim,
                            "carunchadoUpLim" to limitMilho.carunchadoUpLim,
                            // Zera campos inexistentes
                            "greenishUpLim" to 0f,
                            "burntUpLim" to 0f,
                            "spoiledTotalUpLim" to 0f
                        )
                        Log.d("DiscountLimit", "Limites de Milho carregados.")
                    } else {
                        Log.w("DiscountLimit", "Nenhum limite oficial encontrado para Milho.")
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro desconhecido"
                Log.e("DiscountLimit", "Falha ao carregar limites", e)
            }
        }
    }

    fun loadLastUsedLimit() {
        val grain = selectedGrain?.toString() ?: return
        val group = selectedGroup ?: 1

        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Lógica Soja...
                if (grain == "Soja") {
                    if (isOfficial == true) {
                        _lastUsedLimitSoja.value = repositorySoja.getLimit(grain, group, 1, 0)
                    } else {
                        val source = repositorySoja.getLastLimitSource()
                        _lastUsedLimitSoja.value = repositorySoja.getLimit(grain, group, 1, source)
                    }
                } else {
                    // Lógica Milho...
                    val grainFixed = if (grain.equals("milho", ignoreCase = true)) "Milho" else grain
                    if (isOfficial == true) {
                        _lastUsedLimitMilho.value = repositoryMilho.getLimit(grainFixed, group, 1, 0)
                    } else {
                        val source = repositoryMilho.getLastLimitSource()
                        _lastUsedLimitMilho.value = repositoryMilho.getLimit(grainFixed, group, 1, source)
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("UsedLimit", "Limit hasn't been loaded", e)
            }
        }
    }

    // Função Unificada para Salvar Limites Personalizados
    fun setLimit(
        impurities: Float, moisture: Float, brokenCrackedDamaged: Float,
        greenish: Float, burnt: Float, burntOrSour: Float, moldy: Float, spoiled: Float
    ) {
        val grain = selectedGrain ?: return
        val group = selectedGroup ?: 0

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                _error.value = null

                if (grain == "Soja") {
                    repositorySoja.setLimit(
                        grain.toString(), group, 1, impurities, moisture,
                        brokenCrackedDamaged, greenish, burnt, burntOrSour, moldy, spoiled
                    )
                } else {
                    // Lógica Milho: Mapeia os parâmetros da UI para o Repositório do Milho
                    // Nota: Milho não tem 'greenish', 'burnt', 'spoiled'
                    // 'burntOrSour' vira 'ardido'
                    repositoryMilho.setLimit(
                        grain = "Milho",
                        group = group,
                        type = 1,
                        impurities = impurities,
                        moisture = moisture,
                        broken = brokenCrackedDamaged,
                        ardido = burntOrSour, // Mapeado
                        mofado = moldy,
                        carunchado = 0f, // UI de desconto não mandou carunchado neste método específico, assume 0 ou ajusta
                        spoiledTotal = spoiled
                    )
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SetLimit", "Set Limit failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================================
    // LÓGICA SOJA
    // =========================================================================

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
                    inputDiscount.grain,
                    inputDiscount.group,
                    1,
                    inputDiscount,
                    doesTechnicalLoss = doesTechnicalLoss,
                    doesClassificationLoss,
                    doesDeduction
                )
                getDiscountSoja(discountId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("InputDiscountSoja", "Discount input was not saved", e)
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
                _error.value = e.message ?: "Unknown error"
                Log.e("GetDiscountSoja", "Error fetching discount", e)
            }
        }
    }

    fun getDiscountForClassification(
        priceBySack: Float,
        daysOfStorage: Int,
        deductionValue: Float
    ) {
        viewModelScope.launch {
            try {
                _discountsSoja.value = repositorySoja.getDiscountForClassification(
                    priceBySack,
                    daysOfStorage,
                    deductionValue
                )
                val lastInputDiscount = repositorySoja.getLastInputDiscount()
                selectedGrain = lastInputDiscount.grain
                selectedGroup = lastInputDiscount.group
                Log.e("ClassificationToDiscount", "Classification to discount Worked")
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("ClassificationToDisc", "Calculation failed", e)
            } finally {
                _isLoading.value = false
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
                    classification = repositorySoja.getLastClassification()
                    if (classification != null) {
                        sampleClassification = repositorySoja.getSampleById(classification.sampleId)
                    }
                }

                if (sample == null) {
                    _error.value = "inputDiscount data not found"
                    return@launch
                }

                pdfExporterSoja.exportDiscountToPdf(
                    context, discount, sample, limit, classification, sampleClassification
                )
            } catch (e: Exception) {
                _error.value = "Export failed: ${e.message}"
                Log.e("ExportSoja", "Export failed", e)
            }
        }
    }

    // =========================================================================
    // LÓGICA MILHO
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
                    inputDiscount.grain,
                    inputDiscount.group,
                    1,
                    inputDiscount,
                    doesTechnicalLoss = doesTechnicalLoss,
                    doesClassificationLoss = doesClassificationLoss,
                    doesDeduction = doesDeduction
                )

                getDiscountMilho(discountId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Erro desconhecido"
                Log.e("DiscountMilho", "Erro ao calcular desconto", e)
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
                _error.value = e.message ?: "Unknown error"
                Log.e("GetDiscountMilho", "Erro ao obter o desconto", e)
            }
        }
    }

    fun exportDiscount(context: Context, discount: DiscountMilho, limit: LimitMilho) {
        viewModelScope.launch {
            try {
                val sample = repositoryMilho.getLastInputDiscount()
                pdfExporterMilho.exportDiscountToPdf(
                    context, discount, sample, limit
                )
            } catch (e: Exception) {
                _error.value = "Falha ao exportar PDF: ${e.message}"
                Log.e("ExportMilho", "Falha na exportação", e)
            }
        }
    }
}