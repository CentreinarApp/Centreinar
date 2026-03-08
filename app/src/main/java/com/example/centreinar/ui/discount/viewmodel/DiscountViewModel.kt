package com.example.centreinar.ui.discount.viewmodel

import android.content.Context
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.data.repository.DiscountRepository
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.ui.classificationProcess.strategy.BaseClassification
import com.example.centreinar.ui.discount.strategy.DiscountDefectsPayload
import com.example.centreinar.ui.discount.strategy.DiscountResult
import com.example.centreinar.ui.discount.strategy.DiscountUIState
import com.example.centreinar.ui.discount.strategy.FinancialDiscountPayload
import com.example.centreinar.ui.discount.strategy.GrainDiscountStrategy
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val discountRepoSoja: DiscountRepository,
    private val discountRepoMilho: DiscountRepositoryMilho,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    var selectedGrain by savedStateHandle.saveable { mutableStateOf<String>("Soja") }
    var selectedGroup by savedStateHandle.saveable { mutableStateOf<Int>(1) }
    var isOfficial    by savedStateHandle.saveable { mutableStateOf<Boolean>(true) }
    var sourceClassificationId: Int? = null

    fun setClassificationId(id: Int) {
        sourceClassificationId = id
    }

    private val currentStrategy: GrainDiscountStrategy?
        get() = strategies[selectedGrain]

    private val _uiState = MutableStateFlow(DiscountUIState())
    val uiState: StateFlow<DiscountUIState> = _uiState.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _allOfficialLimits = MutableStateFlow<List<Any>>(emptyList())
    val allOfficialLimits: StateFlow<List<Any>> = _allOfficialLimits.asStateFlow()

    private val _discountResult = MutableStateFlow<DiscountResult?>(null)
    val discountResult: StateFlow<DiscountResult?> = _discountResult.asStateFlow()

    private val _classificationPrefill = MutableStateFlow<ClassificationPrefill?>(null)
    val classificationPrefill: StateFlow<ClassificationPrefill?> = _classificationPrefill.asStateFlow()

    var currentDefectsMap: Map<String, Float> = emptyMap()

    fun loadFromClassification(
        lotWeight: Float,
        classification: BaseClassification,
        grain: String,
        group: Int,
        isOfficial: Boolean,
        classificationId: Int
    ) {
        selectedGrain           = grain
        selectedGroup           = group
        this.isOfficial         = isOfficial
        sourceClassificationId  = classificationId  // ← armazena para uso no PDF

        val defects = when (classification) {
            is ClassificationSoja -> mapOf(
                "impureza"    to classification.impuritiesPercentage,
                "ardidos"     to classification.burntOrSourPercentage,
                "queimados"   to classification.burntPercentage,
                "mofados"     to classification.moldyPercentage,
                "avariados"   to classification.spoiledPercentage,
                "esverdeados" to classification.greenishPercentage,
                "quebrados"   to classification.brokenCrackedDamagedPercentage
            )
            is ClassificationMilho -> mapOf(
                "impureza"    to classification.impuritiesPercentage,
                "ardidos"     to classification.ardidoPercentage,
                "carunchados" to classification.carunchadoPercentage,
                "avariados"   to (classification.spoiledTotalPercentage ?: 0f),
                "quebrados"   to classification.brokenPercentage
            )
            else -> emptyMap()
        }

        _classificationPrefill.value = ClassificationPrefill(
            lotWeight = lotWeight.takeIf { it > 0f },
            moisture  = classification.moisturePercentage,
            defects   = defects
        )
    }

    fun loadDefaultLimits() {
        val group    = selectedGroup
        val strategy = currentStrategy ?: return

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _uiState.update { it.copy(isLoading = true) }

                var baseLimits: Map<String, Float>? = null
                repeat(10) { _ ->
                    baseLimits = strategy.getBaseLimits(group)
                    if (baseLimits != null) return@repeat
                    kotlinx.coroutines.delay(300)
                }

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

    fun saveCustomLimit(
        moisture: String,
        impurities: String,
        broken: String,
        burntOrSour: String,
        burnt: String,
        moldy: String,
        spoiled: String,
        greenish: String,
        carunchado: String
    ) {
        val strategy = currentStrategy ?: return
        val fieldsMap = mapOf(
            "moistureUpLim"     to (moisture.toFloatOrNull()    ?: 0f),
            "impuritiesUpLim"   to (impurities.toFloatOrNull()  ?: 0f),
            "brokenUpLim"       to (broken.toFloatOrNull()      ?: 0f),
            "ardidoUpLim"       to (burntOrSour.toFloatOrNull() ?: 0f),
            "burntUpLim"        to (burnt.toFloatOrNull()       ?: 0f),
            "moldyUpLim"        to (moldy.toFloatOrNull()       ?: 0f),
            "spoiledTotalUpLim" to (spoiled.toFloatOrNull()     ?: 0f),
            "greenishUpLim"     to (greenish.toFloatOrNull()    ?: 0f),
            "carunchadoUpLim"   to (carunchado.toFloatOrNull()  ?: 0f)
        )
        viewModelScope.launch(Dispatchers.IO) {
            try {
                strategy.saveCustomLimitData(selectedGroup, fieldsMap)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao salvar limites: ${e.message}") }
            }
        }
    }

    fun saveInputDiscount(lotWeight: Float, priceBySack: Float) {
        val lotPrice = (lotWeight * priceBySack) / 60f
        viewModelScope.launch(Dispatchers.IO) {
            try {
                when (selectedGrain) {
                    "Soja" -> discountRepoSoja.setInputDiscount(
                        InputDiscountSoja(
                            grain                       = selectedGrain,
                            group                       = selectedGroup,
                            limitSource                 = 0,
                            classificationId            = null,
                            lotWeight                   = lotWeight,
                            lotPrice                    = lotPrice,
                            burnt                       = 0f,
                            burntOrSour                 = 0f,
                            moldy                       = 0f,
                            spoiled                     = 0f,
                            greenish                    = 0f,
                            brokenCrackedDamaged        = 0f
                        )
                    )
                    "Milho" -> discountRepoMilho.setInputDiscount(
                        InputDiscountMilho(
                            grain            = selectedGrain,
                            group            = selectedGroup,
                            classificationId = null,
                            lotWeight        = lotWeight,
                            lotPrice         = lotPrice
                        )
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Erro ao salvar input: ${e.message}") }
            }
        }
    }

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
                        // Injeta o classificationId no payload financeiro para que a
                        // strategy o grave no InputDiscount e o recupere no exportPdf
                        sourceClassificationId = sourceClassificationId
                    ),
                    isOfficial       = isOfficial
                )
                _discountResult.value = result
                _uiState.update { it.copy(isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun exportPdf(context: Context) {
        val strategy = currentStrategy ?: return
        viewModelScope.launch {
            try { strategy.exportDiscountToPdf(context, sourceClassificationId) }
            catch (e: Exception) { _uiState.update { it.copy(error = "Erro na exportação: ${e.message}") } }
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