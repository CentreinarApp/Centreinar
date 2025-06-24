package com.example.centreinar.ui.classificationProcess.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.Classification
import com.example.centreinar.ColorClassification
import com.example.centreinar.Limit
import com.example.centreinar.Sample
import com.example.centreinar.data.repository.ClassificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ClassificationViewModel @Inject constructor(
    private val repository: ClassificationRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _classification = MutableStateFlow<Classification?>(null)
    val classification: StateFlow<Classification?> = _classification.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _lastUsedLimit = MutableStateFlow<Limit?>(null)
    val lastUsedLimit: StateFlow<Limit?> = _lastUsedLimit.asStateFlow()

//    private val _observation = MutableStateFlow<String>("")
//    val observation: StateFlow<String> = _observation

//
//    private val _selectedGrain = MutableStateFlow<String?>(null)
//    val selectedGrain: StateFlow<String?> = _selectedGrain.asStateFlow()
//
//    private val _selectedGroup = MutableStateFlow<Int?>(null)
//    val selectedGroup: StateFlow<Int?> = _selectedGroup.asStateFlow()
//
//    private val _selectedOfficial = MutableStateFlow<Boolean?>(null)
//    val selectedOfficial : StateFlow<Boolean?> = _selectedOfficial.asStateFlow()
//

    var selectedGrain by savedStateHandle.saveable {
        mutableStateOf<String?>(null)
    }

    var selectedGroup by savedStateHandle.saveable {
        mutableStateOf<Int?>(null)
    }

    var isOfficial by savedStateHandle.saveable {
        mutableStateOf<Boolean?>(null)
    }
    var observation by savedStateHandle.saveable {
        mutableStateOf<String?>(null)
    }
    var doesDefineColorClass by savedStateHandle.saveable {
        mutableStateOf<Boolean?>(null)
    }

    fun classifySample(sample: Sample) {
        val grain = selectedGrain?:' '
        val group = selectedGroup?:0
        val isOfficial = isOfficial

        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                sample.grain = grain.toString()
                sample.group = group

                var source = 0

                if(isOfficial == false){
                    source = repository.getLastLimitSource()
                }

                val resultId = repository.classifySample(sample, source)
                val resultClassification = repository.getClassification(resultId.toInt())
                if (resultClassification != null) {
                    repository.updateDisqualification(resultId.toInt(),resultClassification.finalType)
                }
                _classification.value = resultClassification
                fetchObservation()
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SampleInput", "Classification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setLimit(
                 impurities:Float,
                 brokenCrackedDamaged: Float,
                 greenish: Float,
                 burnt:Float,
                 burntOrSour:Float,
                 moldy:Float,
                 spoiled:Float) {

        val grain = selectedGrain?:' '
        val group = selectedGroup?:0

        if (grain == null || group == null) {
            Log.e("HomeViewModel", "Grain or group not selected")
            return
        }
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                repository.setLimit(grain.toString(),group,1,impurities,brokenCrackedDamaged, greenish, burnt, burntOrSour, moldy, spoiled)

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SampleInput", "Classification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setDisqualification(badConservation: Int, strangeSmell: Int , insects: Int, toxicGrains: Int){
        viewModelScope.launch {
            try{
                repository.setDisqualification(0,badConservation,0,strangeSmell,toxicGrains,insects)
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("SetDisqualification", "Disqualification failed", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchObservation() {
       val classification = _classification.value
       if (classification != null) {
            viewModelScope.launch {
                _isLoading.value = true
                try {
                    val result = repository.getObservations(classification.id)
                    observation = result
                    Log.e("Observations", "Observation was made: $result")
                } catch (e: Exception) {
                    _error.value = e.message ?: "Unknown error"
                    Log.e("Observation", "Observation failed", e)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    suspend fun getClassColor(): ColorClassification? { // Return a nullable type for safety
        return try {
            _isLoading.value = true // Show loading indicator while fetching
            repository.getLastColorClass() // This will now wait and return the real data
        } catch (e: Exception) {
            _error.value = e.message ?: "Falha ao buscar a classe de cor"
            Log.e("ClassColor", "Class Color failed", e)
            null // Return null if there's an error
        } finally {
            _isLoading.value = false // Hide loading indicator
        }
    }


    fun setClassColor(totalWeight:Float,otherColorsWeight:Float){
        val classification = classification.value
        val grain = selectedGrain
        if(classification != null ){
            viewModelScope.launch {
                try {
                    repository.setClass(grain!!,classification.id,totalWeight,otherColorsWeight)
                } catch (e: Exception) {
                    _error.value = e.message ?: "Unknown error"
                    Log.e("ClassColor", "Class Color failed", e)
                } finally {
                    _isLoading.value = false
                }
            }
        }
    }

    fun loadDefaultLimits(){
        viewModelScope.launch {
            val grain = selectedGrain?.toString() ?: ""
            val group = selectedGroup ?: 0
            try {
                _defaultLimits.value = repository.getLimitOfType1Official(
                    grain = grain,
                    group = group
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("ClassColor", "Class Color failed", e)
            }
        }
    }

    fun loadLastUsedLimit(){
        viewModelScope.launch {
            val grain = selectedGrain?.toString() ?: ""
            val group = selectedGroup ?: 0
            try {
                if(isOfficial == true){
                    _lastUsedLimit.value = repository.getLimit(grain,group,1,0)
                }

                else{
                    val source = repository.getLastLimitSource()
                    _lastUsedLimit.value = repository.getLimit(grain,group,1,source)
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("UsedLimit", "Limit hasn't been loaded", e)
            }
        }
    }

}

