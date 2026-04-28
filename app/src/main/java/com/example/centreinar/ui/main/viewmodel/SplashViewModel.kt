package com.example.centreinar.ui.main.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.centreinar.ui.classificationProcess.strategy.GrainStrategy
import com.example.centreinar.util.retryUntilNotNull
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val strategies: Map<String, @JvmSuppressWildcards GrainStrategy>
) : ViewModel() {

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    init {
        checkDatabase()
    }

    private fun checkDatabase() {
        viewModelScope.launch {
            // Tenta carregar os limites do primeiro grão disponível para confirmar
            // que o banco de dados foi criado e está acessível
            val firstStrategy = strategies.values.firstOrNull()
            if (firstStrategy != null) {
                retryUntilNotNull(times = 20, delayMs = 300) {
                    firstStrategy.getBaseLimits(group = 1)
                }
            }
            delay(2000)
            _isReady.value = true
        }
    }
}