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
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.dao.DisqualificationMilhoDao
import com.example.centreinar.data.local.dao.DisqualificationSojaDao
import com.example.centreinar.data.local.dao.ToxicSeedSojaDao
import com.example.centreinar.data.local.dao.LimitMilhoDao
import com.example.centreinar.data.local.dao.LimitSojaDao
import com.example.centreinar.data.local.dao.ToxicSeedMilhoDao
import com.example.centreinar.data.local.entities.ToxicSeedSoja
import com.example.centreinar.data.local.entity.ColorClassificationMilho
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.local.entity.ToxicSeedMilho
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
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class ClassificationViewModel @Inject constructor(
    private val repositorySoja: ClassificationRepository,
    private val pdfExporterSoja: PDFExporterSoja,
    private val repositoryMilho: ClassificationRepositoryMilhoImpl,
    private val pdfExporterMilho: PDFExporterMilho,
    private val limitSojaDao: LimitSojaDao,
    private val limitMilhoDao: LimitMilhoDao,
    private val disqualificationSojaDao: DisqualificationSojaDao,
    private val disqualificationMilhoDao: DisqualificationMilhoDao,
    private val toxicSeedSojaDao: ToxicSeedSojaDao,
    private val toxicSeedMilhoDao: ToxicSeedMilhoDao,

    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // =========================================================================
    // ESTADOS GERAIS
    // =========================================================================

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var selectedGrain by savedStateHandle.saveable { mutableStateOf<String?>(null) }
    var selectedGroup by savedStateHandle.saveable { mutableStateOf<Int?>(null) }
    var isOfficial by savedStateHandle.saveable { mutableStateOf<Boolean?>(null) }
    var observation by savedStateHandle.saveable { mutableStateOf<String?>(null) }
    var doesDefineColorClass by savedStateHandle.saveable { mutableStateOf<Boolean?>(null) }

    private val _allOfficialLimits = MutableStateFlow<List<Any>>(emptyList())
    val allOfficialLimits: StateFlow<List<Any>> = _allOfficialLimits.asStateFlow()

    // =========================================================================
    // ESTADOS SOJA
    // =========================================================================

    private val _classificationSoja = MutableStateFlow<ClassificationSoja?>(null)
    val classification: StateFlow<ClassificationSoja?> = _classificationSoja.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _lastUsedLimit = MutableStateFlow<LimitSoja?>(null)
    val lastUsedLimit: StateFlow<LimitSoja?> = _lastUsedLimit.asStateFlow()

    private val _disqualificationSoja = MutableStateFlow<DisqualificationSoja?>(null)
    val disqualificationSoja: StateFlow<DisqualificationSoja?> = _disqualificationSoja.asStateFlow()

    private val _toxicSeedsSoja = MutableStateFlow<List<ToxicSeedSoja>>(emptyList())
    val toxicSeedsSoja: StateFlow<List<ToxicSeedSoja>> = _toxicSeedsSoja.asStateFlow()

    private val _colorClassificationSoja = MutableStateFlow<ColorClassificationSoja?>(null)
    val colorClassificationSoja: StateFlow<ColorClassificationSoja?> = _colorClassificationSoja.asStateFlow()

    // =========================================================================
    // ESTADOS MILHO
    // =========================================================================

    private val _classificationMilho = MutableStateFlow<ClassificationMilho?>(null)
    val classificationMilho: StateFlow<ClassificationMilho?> = _classificationMilho.asStateFlow()

    private val _limitMilho = MutableStateFlow<LimitMilho?>(null)
    val limitMilho: StateFlow<LimitMilho?> = _limitMilho.asStateFlow()

    private val _disqualificationMilho = MutableStateFlow<DisqualificationMilho?>(null)
    val disqualificationMilho: StateFlow<DisqualificationMilho?> = _disqualificationMilho.asStateFlow()

    private val _toxicSeedsMilho = MutableStateFlow<List<ToxicSeedMilho>>(emptyList())
    val toxicSeedsMilho: StateFlow<List<ToxicSeedMilho>> = _toxicSeedsMilho.asStateFlow()

    private val _complementaryMilho = MutableStateFlow<ColorClassificationMilho?>(null)
    val complementaryMilho: StateFlow<ColorClassificationMilho?> = _complementaryMilho.asStateFlow()

    // =========================================================================
    // GESTÃO DE ESTADO E LIMPEZA
    // =========================================================================

    fun clearStates() {
        _classificationSoja.value = null
        _defaultLimits.value = null
        _lastUsedLimit.value = null
        _classificationMilho.value = null
        _allOfficialLimits.value = emptyList()
        _isLoading.value = false
        _error.value = null
        selectedGrain = null
        selectedGroup = null
        isOfficial = null
        observation = null
        doesDefineColorClass = null
        _classificationMilho.value = null
        _complementaryMilho.value = null
        _disqualificationMilho.value = null
        _colorClassificationSoja.value = null
        _toxicSeedsMilho.value = emptyList()
    }

    // =========================================================================
    // LÓGICA DE CARREGAMENTO DE LIMITES
    // =========================================================================

    fun resetLimits() {
        _defaultLimits.value = null
        _allOfficialLimits.value = emptyList()
        _lastUsedLimit.value = null
        _limitMilho.value = null
    }

    fun loadDefaultLimits() {
        val grain = selectedGrain?.toString() ?: return
        val group = selectedGroup ?: return
        val official = isOfficial ?: true

        _defaultLimits.value = null
        _allOfficialLimits.value = emptyList()

        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true

                // Carrega a lista para a Tabela Comparativa se for Oficial
                if (official) {
                    if (grain == "Soja") {
                        _allOfficialLimits.value = limitSojaDao.getLimitsByGroup(grain, group, 0)
                    } else {
                        _allOfficialLimits.value = limitMilhoDao.getLimitsBySource(grain, 0, group)
                    }
                }

                // Carrega os limites base (Tipo 1) para preencher a UI inicial
                if (grain == "Soja") {
                    _defaultLimits.value = repositorySoja.getLimitOfType1Official(group, grain)
                } else {
                    val limitMilho = repositoryMilho.getLimit(grain, group, 1, 0)
                    limitMilho?.let {
                        _defaultLimits.value = mapOf(
                            "impuritiesUpLim" to it.impuritiesUpLim,
                            "moistureUpLim" to it.moistureUpLim,
                            "brokenUpLim" to it.brokenUpLim,
                            "ardidosUpLim" to it.ardidoUpLim,
                            "mofadosUpLim" to it.mofadoUpLim,
                            "carunchadoUpLim" to it.carunchadoUpLim,
                            "moldyUpLim" to it.mofadoUpLim,
                            "spoiledTotalUpLim" to it.spoiledTotalUpLim
                        )
                    }
                }
            } catch (e: Exception) {
                _error.value = "Erro ao carregar limites: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    // =========================================================================
    // LÓGICA DE CLASSIFICAÇÃO (SOJA E MILHO)
    // =========================================================================

    fun classifySample(sample: SampleSoja, otherColorsWeight: Float, baseWeightCor: Float, isColorDefined: Boolean) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                sample.grain = selectedGrain ?: "Soja"
                sample.group = selectedGroup ?: 1

                // Busca o source correto (se manual, pega o último gravado)
                val source = if (isOfficial == false) repositorySoja.getLastLimitSource() else 0

                // Captura o limite que será usado e guarda no estado para a tela ver
                val limitUtilizado = repositorySoja.getLimit(sample.grain, sample.group, 1, source)
                _lastUsedLimit.value = limitUtilizado

                // Pega o id da desclassificação
                val lastDisqId = disqualificationSojaDao.getLastDisqualificationId()

                // Pega a desclassificação do lote
                val lastDisq = disqualificationSojaDao.getById(lastDisqId)

                // Gera o id da classificação
                val resultId = repositorySoja.classifySample(sample, source, lastDisq!!)

                // Atualiza para o id da classificação
                disqualificationSojaDao.updateClassificationId(lastDisqId, resultId.toInt())

                _classificationSoja.value = repositorySoja.getClassification(resultId.toInt())

                // Busca a desclassificação e as sementes da Soja recém-salvas
                val disqSoja = disqualificationSojaDao.getByClassificationId(resultId.toInt())
                _disqualificationSoja.value = disqSoja

                if (disqSoja != null) {
                    _toxicSeedsSoja.value = toxicSeedSojaDao.getSeedsByDisqualificationId(disqSoja.id)
                } else {
                    _toxicSeedsSoja.value = emptyList()
                }

                if (isColorDefined && baseWeightCor > 0f) {
                    val otherColorPct = (otherColorsWeight / baseWeightCor * 100f)
                        .toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

                    val yellowPct = (100f - otherColorPct)
                        .toBigDecimal().setScale(2, RoundingMode.HALF_UP).toFloat()

                    // Se arredondado for <= 10, é Amarela.
                    val framingClass = if (otherColorPct <= 10.00f) "Classe Amarela" else "Classe Misturada"

                    val colorEntity = ColorClassificationSoja(
                        grain = "Soja",
                        classificationId = resultId.toInt(),
                        yellowPercentage = yellowPct,
                        otherColorPercentage = otherColorPct,
                        framingClass = framingClass
                    )

                    repositorySoja.insertColorClassification(colorEntity)
                    _colorClassificationSoja.value = colorEntity
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun classifySample(sample: SampleMilho, shouldDefineClass: Boolean, shouldDefineGroup: Boolean, weightYellow: Float, weightWhite: Float, weightMixedColors: Float, weightHard: Float, weightDent: Float, weightSemiHard: Float) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            try {
                val limitSource = if (isOfficial == false) repositoryMilho.getLastLimitSource() else 0
                val limitUsed = repositoryMilho.getLimit(sample.grain, sample.group, 1, limitSource)
                _limitMilho.value = limitUsed

                // Pega o id da desclassificação
                val lastDisqId = disqualificationMilhoDao.getLastDisqualificationId()

                // Pega a desclassificação do lote
                val lastDisq = disqualificationMilhoDao.getLastDisqualification()

                // Gera o id da classificação
                val resultId = repositoryMilho.classifySample(sample, limitSource, lastDisq!!)

                // Usamos o let para só executar o update se lastDisqId NÃO for nulo
                lastDisqId?.let { disqId ->
                    disqualificationMilhoDao.updateClassificationId(disqId, resultId.toInt())
                }

                _classificationMilho.value = repositoryMilho.getClassification(resultId.toInt())

                val disqMilho = disqualificationMilhoDao.getByClassificationId(resultId.toInt())
                _disqualificationMilho.value = disqMilho

                if (disqMilho != null) {
                    _toxicSeedsMilho.value = toxicSeedMilhoDao.getToxicSeedsByDisqualificationId(disqMilho.id)
                } else {
                    _toxicSeedsMilho.value = emptyList()
                }

                // Processamento da Classe de Cor
                var calculatedYellowPct = 0f
                var finalClassResult = ""

                if (shouldDefineClass) {
                    val totalClassWeight = weightYellow + weightWhite + weightMixedColors
                    if (totalClassWeight > 0f) {
                        calculatedYellowPct = (weightYellow / totalClassWeight) * 100f
                        val whitePct = (weightWhite / totalClassWeight) * 100f

                        finalClassResult = when {
                            calculatedYellowPct >= 95f -> "AMARELA"
                            whitePct >= 95f -> "BRANCA"
                            else -> "CORES"
                        }
                    }
                }

                // Processamento do Grupo (Forma)
                var calculatedHardPct = 0f
                var calculatedDentPct = 0f
                var calculatedSemiDuroPct = 0f
                var finalGroupResult = ""

                if (shouldDefineGroup) {
                    val totalGroupWeight = weightHard + weightDent + weightSemiHard
                    if (totalGroupWeight > 0f) {
                        calculatedHardPct = (weightHard / totalGroupWeight) * 100f
                        calculatedDentPct = (weightDent / totalGroupWeight) * 100f
                        calculatedSemiDuroPct = (weightSemiHard / totalGroupWeight) * 100f

                        finalGroupResult = when {
                            calculatedHardPct >= 85f -> "DURO"
                            calculatedDentPct >= 85f -> "DENTADO"
                            else -> "SEMIDURO"
                        }
                    }
                }

                // Persistência dos dados complementares
                if (shouldDefineClass || shouldDefineGroup) {
                    val complementaryData = ColorClassificationMilho(
                        classificationId = resultId,
                        yellowPercentage = calculatedYellowPct,
                        otherColorPercentage = 100f - calculatedYellowPct,
                        framingClass = finalClassResult,
                        duroPercentage = calculatedHardPct,
                        dentadoPercentage = calculatedDentPct,
                        semiDuroPercentage = calculatedSemiDuroPct,
                        framingGroup = finalGroupResult
                    )
                    repositoryMilho.insertColorClassificationMilho(complementaryData)
                    _complementaryMilho.value = complementaryData
                }

            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setLimit(impurities: Float, moisture: Float, brokenCrackedDamaged: Float, greenish: Float, burnt: Float, burntOrSour: Float, moldy: Float, spoiled: Float, carunchado: Float) {
        val grain = selectedGrain?.toString() ?: return
        val group = selectedGroup ?: return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                _isLoading.value = true
                if (grain == "Soja") {
                    repositorySoja.setLimit(grain, group, 1, impurities, moisture, brokenCrackedDamaged, greenish, burnt, burntOrSour, moldy, spoiled)
                } else {
                    repositoryMilho.setLimit(grain, group, 1, impurities, moisture, brokenCrackedDamaged, burntOrSour, moldy, spoiled, carunchado)
                }
            } catch (e: Exception) {
                _error.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteCustomLimits() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Chama o delete para ambos os grãos
                repositoryMilho.deleteCustomLimits()
                repositorySoja.deleteCustomLimits()
                Log.d("Cleanup", "Limites customizados (Soja e Milho) apagados com sucesso.")
            } catch (e: Exception) {
                Log.e("Cleanup", "Erro ao apagar limites: ${e.message}")
            }
        }
    }

    // =========================================================================
    // DESCLASSIFICAÇÃO E COR
    // =========================================================================

    fun saveDisqualificationDataSoja(
        badConservation: Int,
        strangeSmell: Int,
        insects: Int,
        toxicGrains: Int,
        toxicSeeds: List<Pair<String, String>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {

            val disqualification = DisqualificationSoja(
                classificationId = null,
                badConservation = badConservation,
                graveDefectSum = 0,
                strangeSmell = strangeSmell,
                insects = insects,
                toxicGrains = toxicGrains
            )

            val newDisqualificationId = disqualificationSojaDao.insert(disqualification).toInt()

            if (toxicGrains == 1 && toxicSeeds.isNotEmpty()) {
                val seedsToInsert = toxicSeeds.map { pair ->
                    ToxicSeedSoja(
                        disqualificationId = newDisqualificationId,
                        name = pair.first,
                        quantity = pair.second.toIntOrNull() ?: 0
                    )
                }

                toxicSeedSojaDao.insertAll(seedsToInsert)
            }

            onSuccess()
        }
    }

    fun saveDisqualificationDataMilho(
        badConservation: Int,
        strangeSmell: Int,
        insects: Int,
        toxicGrains: Int,
        toxicSeeds: List<Pair<String, String>>,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {

            // Cria a entidade do MILHO
            val disqualification = DisqualificationMilho(
                classificationId = null,
                badConservation = badConservation,
                strangeSmell = strangeSmell,
                insects = insects,
                toxicGrains = toxicGrains
            )

            // Salva usando o DAO do MILHO e pega o ID gerado
            val newDisqualificationId = disqualificationMilhoDao.insert(disqualification).toInt()

            // Se houver sementes tóxicas, salva na tabela de sementes do MILHO
            if (toxicGrains == 1 && toxicSeeds.isNotEmpty()) {
                val seedsToInsert = toxicSeeds.map { pair ->
                    ToxicSeedMilho(
                        disqualificationId = newDisqualificationId,
                        name = pair.first,
                        quantity = pair.second.toIntOrNull() ?: 0
                    )
                }

                // Salva usando o DAO de sementes do MILHO
                toxicSeedMilhoDao.insertAll(seedsToInsert)
            }

            onSuccess()
        }
    }

    suspend fun getClassColor(): ColorClassificationSoja? = repositorySoja.getLastColorClass()

    fun setClassColor(totalWeight: Float, otherColorsWeight: Float) {
        val classificationId = classification.value?.id ?: return
        viewModelScope.launch(Dispatchers.IO) {
            repositorySoja.setClass(selectedGrain!!, classificationId, totalWeight, otherColorsWeight)
        }
    }

    // =========================================================================
    // EXPORTAÇÃO E OBSERVAÇÕES
    // =========================================================================

    suspend fun getObservations(colorClass: ColorClassificationSoja?): String {
        val id = _classificationSoja.value?.id ?: return ""
        return if (doesDefineColorClass == true) repositorySoja.getObservations(id, colorClass) else repositorySoja.getObservations(idClassification = id)
    }

    fun exportClassification(context: Context, classification: ClassificationSoja, limit: LimitSoja) {
        viewModelScope.launch(Dispatchers.IO) {
            val sample = repositorySoja.getSample(classification.sampleId) ?: return@launch
            val colorClass = repositorySoja.getLastColorClass()
            val obs = getObservations(colorClass)
            pdfExporterSoja.exportClassificationToPdf(context, classification, sample, colorClass, obs, limit)
        }
    }

    fun exportClassificationMilho(context: Context, classification: ClassificationMilho, limit: LimitMilho) {
        viewModelScope.launch(Dispatchers.IO) {
            val sample = repositoryMilho.getSample(classification.sampleId) ?: return@launch
            pdfExporterMilho.exportClassificationToPdf(context, classification, sample, limit)
        }
    }

    fun getFinalTypeLabel(finalType: Int): String {
        if (finalType == 0) return "Desclassificada"
        if (finalType == 7) return "Fora de Tipo"
        return if (selectedGrain == "Soja") {
            if (selectedGroup == 1) (if (finalType == 1) "Tipo 1" else "Tipo 2") else "Padrão Básico"
        } else "Tipo $finalType"
    }

    fun prepareForPdfExport(grain: String) {
        _defaultLimits.value = null
        selectedGrain = grain
    }
}