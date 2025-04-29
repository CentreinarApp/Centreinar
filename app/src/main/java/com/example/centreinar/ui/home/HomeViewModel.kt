package com.example.centreinar.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.centreinar.Classification
import com.example.centreinar.repositories.ClassificationRepository
import com.example.centreinar.Sample
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ClassificationRepository
) : ViewModel() {

    private val _text = MutableStateFlow<String?>(null)
    val text: StateFlow<String?> = _text.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun classifySample(sample: Sample) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null

                val resultId = repository.classifySample(sample, 0)
                val resultClassification = repository.getClassification(resultId.toInt())
                _text.value = formatResult(resultClassification)
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SampleInput", "Classification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

fun formatResult(classification: Classification?): String {
    if (classification == null) return "Sem resultados Disponiveis"

    return buildString {
        appendLine("=== Resultado da Classificação ===")
        appendLine("Grão: ${classification.grain}")
        appendLine("Grupo: ${classification.group}")

        appendLine()

        appendLine("--- Porcentagens ---")
        appendLine("Matéria Estranha e Impurezas: ${classification.foreignMattersPercentage}%")
        appendLine("Partidos, Quebrados e Amassados: ${classification.brokenCrackedDamagedPercentage}%")
        appendLine("Esverdeados: ${classification.greenishPercentage}%")
        appendLine("Mofados: ${classification.moldyPercentage}%")
        appendLine("Queimados: ${classification.burntPercentage}%")
        appendLine("Ardidos e Queimados: ${classification.burntOrSourPercentage}%")
        appendLine("Total de Avariados: ${classification.spoiledPercentage}%")
        appendLine()

        appendLine("--- Tipos por Defeito ---")
        appendLine("Matéria Estranha e Impurezas: ${classification.foreignMatters}")
        appendLine("Partidos, Quebrados e Amassados: ${classification.brokenCrackedDamaged}")
        appendLine("Esverdeados: ${classification.greenish}")
        appendLine( "Mofados: ${classification.moldy}")
        appendLine("Queimados: ${classification.burnt}")
        appendLine("Ardidos e Queimados: ${classification.burntOrSour}")
        appendLine("Total de Avariados: ${classification.spoiled}")
        appendLine()

        val finalType = when (classification.finalType) {
            7 -> "FORA DE TIPO"
            else -> classification.finalType.toString()
        }
        appendLine("Tipo Final" +
                ": $finalType")
    }
}