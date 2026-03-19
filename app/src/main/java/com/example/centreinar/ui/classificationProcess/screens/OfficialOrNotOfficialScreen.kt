package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.main.screens.OfficialSelectionScreen
import com.example.centreinar.util.Routes


@Composable
fun OfficialOrNotOfficialScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    OfficialSelectionScreen(
        titleOfficial = "Referências Oficiais",
        titleCustom   = "Referências Não Oficiais",
        onOptionSelected = { isOfficial ->
            viewModel.isOfficial = isOfficial
            navController.navigate(Routes.LIMIT_INPUT)
        }
    )
}