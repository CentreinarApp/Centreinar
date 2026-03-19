package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.main.screens.GrainSelectionScreen
import com.example.centreinar.util.Routes

@Composable
fun ClassificationGrainSelectionScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    GrainSelectionScreen(
        grainDescriptors = viewModel.availableGrainDescriptors,
        onGrainSelected  = { descriptor ->
            viewModel.clearStates()
            viewModel.selectedGrain = descriptor.name

            if (descriptor.supportsGroups) {
                navController.navigate(Routes.GROUP_SELECTION)
            } else {
                viewModel.selectedGroup = 1
                navController.navigate(Routes.OFFICIAL_OR_NOT)
            }
        }
    )
}