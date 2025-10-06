package com.example.centreinar.ui.classificationProcess.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.data.repository.ClassificationRepositoryMilhoImpl
import com.example.centreinar.util.PDFExporterMilho
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ClassificationViewModelMilho @Inject constructor(
    private val repository: ClassificationRepositoryMilhoImpl,
    private val pdfExporter: PDFExporterMilho,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _classification = MutableStateFlow<ClassificationMilho?>(null)
    val classification: StateFlow<ClassificationMilho?> = _classification.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    var isOfficial: Boolean? = null

    /**
     * Classifica uma amostra de milho.
     * Corrige o erro de tipo: repository.classifySample retorna Long (id),
     * e depois buscamos a classificação completa.
     */
    fun classifySample(sample: SampleMilho) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val limitSource = if (isOfficial == false) repository.getLastLimitSource() else 0

                val id = repository.classifySample(sample, limitSource)
                val classification = repository.getClassification(id.toInt())

                _classification.value = classification

            } catch (e: Exception) {
                _error.value = e.message ?: "Erro na classificação"
                Log.e("ClassificationMilho", "Erro ao classificar", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Exporta a classificação para PDF.
     * Compatível com a assinatura:
     * pdfExporter.exportDiscountToPdf(context, discount, sampleInput, limit)
     */
    fun exportClassificationToPdf(
        context: Context,
        classification: ClassificationMilho,
        sample: SampleMilho,
        limit: LimitMilho
    ) {
        viewModelScope.launch {
            try {
                // Monta o InputDiscountMilho com base na amostra
                val inputDiscount = InputDiscountMilho(
                    classificationId = classification.id,
                    grain = sample.grain,
                    group = sample.group,
                    limitSource = 0, // ou outro valor se você quiser usar o da classificação
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

                // Monta um DiscountMilho com base nos dados de classificação
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

                // Chama o exportador de PDF
                pdfExporter.exportDiscountToPdf(
                    context,
                    discount,
                    inputDiscount,
                    limit
                )

            } catch (e: Exception) {
                _error.value = e.message ?: "Erro ao exportar PDF"
                Log.e("ClassificationMilho", "Erro ao exportar PDF", e)
            }
        }
    }
}
