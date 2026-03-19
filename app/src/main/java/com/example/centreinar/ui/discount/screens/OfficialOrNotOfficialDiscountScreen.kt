package com.example.centreinar.ui.discount.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.main.screens.OfficialSelectionScreen
import com.example.centreinar.util.Routes

@Composable
fun OfficialOrNotOfficialDiscountScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    OfficialSelectionScreen(
        titleOfficial = "Referências Oficiais",
        titleCustom   = "Referências Não Oficiais",
        onOptionSelected = { isOfficial ->
            viewModel.isOfficial = isOfficial
            navController.navigate(Routes.DISCOUNT_LIMIT_INPUT)
        }
    )
}