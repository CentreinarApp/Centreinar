package com.example.centreinar.ui.discount.viewmodel

import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.domain.repository.DiscountRepositoryMilho
import com.example.centreinar.util.PDFExporter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DiscountViewModelMilho @Inject constructor(
    private val repository: DiscountRepositoryMilho,
    private val pdfExporter: PDFExporter,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _discounts = MutableStateFlow<DiscountMilho?>(null)
    val discounts: StateFlow<DiscountMilho?> = _discounts.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _defaultLimits = MutableStateFlow<Map<String, Float>?>(null)
    val defaultLimits: StateFlow<Map<String, Float>?> = _defaultLimits.asStateFlow()

    private val _lastUsedLimit = MutableStateFlow<LimitMilho?>(null)
    val lastUsedLimit: StateFlow<LimitMilho?> = _lastUsedLimit.asStateFlow()

    var selectedGroup: Int? = null
    var isOfficial: Boolean? = null

    fun clearStates() {
        _isLoading.value = false
        _error.value = null
        _defaultLimits.value = null
        _lastUsedLimit.value = null
        selectedGroup = null
        isOfficial = null
    }

    fun setDiscount(
        inputDiscount: InputDiscountMilho,
        doesTechnicalLoss: Boolean,
        doesClassificationLoss: Boolean,
        doesDeduction: Boolean
    ): Long {
        var discountId = 0L
        viewModelScope.launch {
            try {
                if (isOfficial == false) {
                    inputDiscount.limitSource = repository.getLastLimitSource()
                }
                repository.setInputDiscount(inputDiscount)

                discountId = repository.calculateDiscount(
                    inputDiscount.grain,
                    inputDiscount.group,
                    1,
                    inputDiscount,
                    doesTechnicalLoss = doesTechnicalLoss,
                    doesClassificationLoss = doesClassificationLoss,
                    doesDeduction = doesDeduction
                )

                getDiscount(discountId)
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
                Log.e("DiscountMilho", "Erro ao calcular desconto", e)
            } finally {
                _isLoading.value = false
            }
        }
        return discountId
    }

    fun getDiscount(id: Long) {
        viewModelScope.launch {
            try {
                _discounts.value = repository.getDiscountById(id)
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadDefaultLimits() {
        viewModelScope.launch {
            val group = selectedGroup ?: 1
            try {
                _defaultLimits.value = repository.getLimitOfType1Official(
                    grain = "milho",
                    group = group
                )
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            }
        }
    }

    fun loadLastUsedLimit() {
        viewModelScope.launch {
            val group = selectedGroup ?: 1
            try {
                if (isOfficial == true) {
                    _lastUsedLimit.value = repository.getLimit("milho", group, 1, 0)
                } else {
                    val source = repository.getLastLimitSource()
                    _lastUsedLimit.value = repository.getLimit("milho", group, 1, source)
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Unknown error"
            }
        }
    }

    fun exportDiscount(context: Context, discount: DiscountMilho, limit: LimitMilho) {
        viewModelScope.launch {
            try {
                val sample = repository.getLastInputDiscount()
                pdfExporter.exportDiscountToPdf(
                    context,
                    discount,
                    sample,
                    limit,
                    null,
                    null
                )
            } catch (e: Exception) {
                _error.value = "Falha ao exportar PDF: ${e.message ?: "Erro desconhecido"}"
            }
        }
    }
}
