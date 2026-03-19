package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.strategy.CustomLimitPayload
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.main.screens.LimitInputScreen
import com.example.centreinar.util.Routes

@Composable
fun ClassificationLimitInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

    LimitInputScreen(
        limitFields       = viewModel.getLimitFields(),
        descriptor        = viewModel.currentDescriptor,
        currentGrain      = viewModel.selectedGrain,
        currentGroup      = viewModel.selectedGroup,
        isOfficial        = viewModel.isOfficial == true,
        isLoading         = uiState.isLoading,
        defaultLimitsFlow = viewModel.defaultLimits,
        allOfficialLimits = allOfficialLimits,
        onLoadLimits      = { viewModel.loadDefaultLimits() },
        onSaveAndContinue = { limits ->
            if (viewModel.isOfficial != true) {
                viewModel.setLimit(
                    CustomLimitPayload(
                        group  = viewModel.selectedGroup ?: 1,
                        limits = limits
                    )
                )
            }
            navController.navigate("${Routes.DISQUALIFICATION}?classificationId=-1")
        }
    )
}