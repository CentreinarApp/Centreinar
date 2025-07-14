package com.example.centreinar.ui.discount.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.InputDiscount
import com.example.centreinar.Limit
import com.example.centreinar.data.repository.DiscountRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscountViewModel @Inject constructor(
    private val repository: DiscountRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _lastUsedLimit = MutableStateFlow<Limit?>(null)
    val lastUsedLimit: StateFlow<Limit?> = _lastUsedLimit.asStateFlow()

    var selectedGrain by savedStateHandle.saveable {
        mutableStateOf<String?>(null)
    }

    var selectedGroup by savedStateHandle.saveable {
        mutableStateOf<Int?>(null)
    }

    var isOfficial by savedStateHandle.saveable {
        mutableStateOf<Boolean?>(null)
    }

    fun clearStates() {

        _isLoading.value = false
        _error.value = null
        _defaultLimits.value = null
        _lastUsedLimit.value = null

        selectedGrain = null
        selectedGroup = null
        isOfficial = null
    }

    fun setDiscount(inputDiscount : InputDiscount,doesTechnicalLoss:Boolean,doesClassificationLoss: Boolean,
                    doesDeduction: Boolean):Long{
        var discountId = 0L
        viewModelScope.launch {
            try {
                //
                discountId = repository.calculateDiscount(inputDiscount.grain,inputDiscount.group,1,inputDiscount, doesTechnicalLoss = doesTechnicalLoss,doesClassificationLoss,doesDeduction)
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("InputDiscount", "Discount input was not saved", e)
            } finally {
                _isLoading.value = false
            }
        }
        return discountId
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
}