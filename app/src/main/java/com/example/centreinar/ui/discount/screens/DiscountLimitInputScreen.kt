package com.example.centreinar.ui.discount.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.components.DiscountTopBar
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.main.screens.LimitInputScreen
import com.example.centreinar.util.Routes

@Composable
fun DiscountLimitInputScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

    LimitInputScreen(
        topBar = { DiscountTopBar( onBackClick = { navController.popBackStack() }) },
        limitFields       = viewModel.getLimitFields(),
        descriptor        = viewModel.currentDescriptor,
        currentGrain      = viewModel.selectedGrain,
        currentGroup      = viewModel.selectedGroup,
        isOfficial        = viewModel.isOfficial,
        isLoading         = uiState.isLoading,
        defaultLimitsFlow = viewModel.defaultLimits,
        allOfficialLimits = allOfficialLimits,
        onLoadLimits      = { viewModel.loadDefaultLimits() },
        onSaveAndContinue = { limits ->
            if (!viewModel.isOfficial) {
                viewModel.saveCustomLimit(limits)
            }
            navController.navigate(Routes.DISCOUNT_INPUT)
        }
    )
}