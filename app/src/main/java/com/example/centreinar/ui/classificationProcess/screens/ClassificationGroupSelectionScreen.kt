package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.main.screens.GroupSelectionScreen
import com.example.centreinar.util.Routes

@Composable
fun ClassificationGroupSelectionScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    GroupSelectionScreen(
        onGroupSelected = { group ->
            viewModel.resetLimits()
            viewModel.selectedGroup = group
            navController.navigate(Routes.OFFICIAL_OR_NOT)
        }
    )
}