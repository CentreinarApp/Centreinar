package com.example.centreinar.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.centreinar.repositories.ClassificationRepository
import com.example.centreinar.Sample
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: ClassificationRepository
) : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> = _text

    init {
        fetchLimits()
    }

    private fun fetchLimits() {
        viewModelScope.launch {
            try {
                val sample = Sample(
                    grain = "corn",
                    group = 2,
                    lotWeight = 80000.0f,
                    sampleWeight = 135.8f,
                    foreignMattersAndImpurities = 0.5f,
                    humidity = 10.1f,
                    greenish = 0.0f,
                    brokenCrackedDamaged = 16.1f,
                    burnt = 3.0f,
                    sour = 0.1f,
                    moldy = 5.2f,
                    fermented = 1.5f,
                    germinated = 0.0f,
                    immature = 0.2f
                )

                // Proper coroutine context for Room operations
                val resultId = withContext(Dispatchers.IO) {
                    repository.classifySample(sample, 1)
                }

                val resultClassification = withContext(Dispatchers.IO) {
                    repository.getClassification(resultId.toInt())
                }

                _text.value = resultClassification?.toString() ?: "No results"
            } catch (e: Exception) {
                _text.postValue("Error")
                Log.w("myApp", e.message.toString())
            }
        }
    }
}