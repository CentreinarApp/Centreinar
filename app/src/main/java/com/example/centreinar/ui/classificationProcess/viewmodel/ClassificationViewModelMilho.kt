package com.example.centreinar.ui.classificationProcess.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.repository.ClassificationRepositoryMilhoImpl
import com.example.centreinar.util.PDFExporterMilho
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassificationViewModelMilho @Inject constructor(
    private val repository: ClassificationRepositoryMilhoImpl,
    private val pdfExporter: PDFExporterMilho,
    savedStateHandle: SavedStateHandle // <-- INJETADO
) : ViewModel() {
    private val _classification = MutableStateFlow<ClassificationMilho?>(null)
    val classification: StateFlow<ClassificationMilho?> = _classification.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // --- ESTADOS SALVÁVEIS (persistem entre recriações de tela)
    var selectedGrain by savedStateHandle.saveable {
        mutableStateOf<String?>("Milho") // valor padrão
    }

    var isOfficial by savedStateHandle.saveable {
        mutableStateOf<Boolean?>(null)
    }

    var selectedGroup by savedStateHandle.saveable {
        mutableStateOf<Int?>(null)
    }

    /**
     * Classifica a amostra de milho de forma segura.
     * Protege contra exceções de ausência de limites e falhas de lógica.
     */
    fun classifySample(sample: SampleMilho) {
        viewModelScope.launch(Dispatchers.IO) {
            _isLoading.value = true
            _error.value = null
            try {
                val limitSource = if (isOfficial == false) repository.getLastLimitSource() else 0

                // Executa a classificação
                val id = repository.classifySample(sample, limitSource)
                val classification = repository.getClassification(id.toInt())

                _classification.value = classification
                Log.i("ClassificationMilho", "Classificação concluída com sucesso para o milho.")

            } catch (e: IllegalStateException) {
                // ⚠️ Caso específico: ausência de limites no banco
                _error.value = e.message
                Log.e("ClassificationMilho", "Erro de lógica: ${e.message}", e)
            } catch (e: Exception) {
                // ⚠️ Demais erros inesperados
                _error.value = e.message ?: "Erro inesperado durante a classificação"
                Log.e("ClassificationMilho", "Erro ao classificar", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Exporta o resultado da classificação para PDF.
     * Inclui tratamento de erros para evitar travamentos.
     */
    fun exportClassificationToPdf(
        context: Context,
        classification: ClassificationMilho,
        sample: SampleMilho,
        limit: LimitMilho
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
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

                pdfExporter.exportDiscountToPdf(context, discount, inputDiscount, limit)
                Log.i("ClassificationMilho", "PDF exportado com sucesso para o milho.")

            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao exportar PDF"
                Log.e("ClassificationMilho", "Erro ao exportar PDF", e)
            }
        }
    }
}