package com.example.centreinar.ui.discount.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.saveable
import com.example.centreinar.InputDiscount
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

    var selectedGrain by savedStateHandle.saveable {
        mutableStateOf<String?>(null)
    }

    var selectedGroup by savedStateHandle.saveable {
        mutableStateOf<Int?>(null)
    }

    var isOfficial by savedStateHandle.saveable {
        mutableStateOf<Boolean?>(null)
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
}