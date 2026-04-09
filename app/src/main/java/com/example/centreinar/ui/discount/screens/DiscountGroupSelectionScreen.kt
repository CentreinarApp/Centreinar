package com.example.centreinar.ui.discount.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.components.DiscountTopBar
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.main.screens.GroupSelectionScreen
import com.example.centreinar.util.Routes

@Composable
fun DiscountGroupSelectionScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    GroupSelectionScreen(
        onGroupSelected = { group ->
            viewModel.resetUiState()
            viewModel.selectedGroup = group
            navController.navigate(Routes.OFFICIAL_OR_NOT_DISCOUNT)
        },
        topBar = {
            DiscountTopBar(
                onBackClick = { navController.popBackStack() }
            )
        }
    )
}