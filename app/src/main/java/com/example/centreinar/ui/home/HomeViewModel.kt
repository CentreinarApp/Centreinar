package com.example.centreinar.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.centreinar.Classification
import com.example.centreinar.repositories.ClassificationRepository
import com.example.centreinar.Sample
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ClassificationRepository
) : ViewModel() {

    private val _classification = MutableStateFlow<Classification?>(null)
    val classification: StateFlow<Classification?> = _classification.asStateFlow()

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
                _classification.value = resultClassification
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SampleInput", "Classification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}