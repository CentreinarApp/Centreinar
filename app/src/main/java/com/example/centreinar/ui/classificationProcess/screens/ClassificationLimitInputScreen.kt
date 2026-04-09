package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.components.ClassificationTopBar
import com.example.centreinar.ui.classificationProcess.strategy.CustomLimitPayload
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.main.screens.LimitInputScreen
import com.example.centreinar.util.Routes
import kotlinx.coroutines.launch

@Composable
fun ClassificationLimitInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()
    val scope             = rememberCoroutineScope()  // ← scope correto para Composables

    LimitInputScreen(
        topBar = { ClassificationTopBar(onBackClick = { navController.popBackStack() }) },
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
                scope.launch {
                    viewModel.setLimit(
                        CustomLimitPayload(
                            group  = viewModel.selectedGroup ?: 1,
                            limits = limits
                        )
                    )
                    navController.navigate("${Routes.DISQUALIFICATION}?classificationId=-1")
                }
            } else {
                navController.navigate("${Routes.DISQUALIFICATION}?classificationId=-1")
            }
        }
    )
}