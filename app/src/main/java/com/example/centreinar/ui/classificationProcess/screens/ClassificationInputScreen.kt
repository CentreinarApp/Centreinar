package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.components.ClassificationTopBar
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationNavigationEvent
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.main.screens.InputScreen
import com.example.centreinar.ui.main.screens.InputScreenMode
import com.example.centreinar.util.Routes

@Composable
fun ClassificationInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val descriptor   = viewModel.currentDescriptor ?: return
    val currentGroup = viewModel.selectedGroup ?: 1
    val inputFields  = viewModel.getClassificationInputFields()

    // Navega para o resultado assim que o ViewModel sinalizar que terminou
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is ClassificationNavigationEvent.NavigateToResult ->
                    navController.navigate(Routes.CLASSIFICATION_RESULT)
            }
        }
    }

    InputScreen(
        topBar = { ClassificationTopBar(onBackClick = { navController.popBackStack() }) },
        mode   = InputScreenMode.Classification(
            descriptor           = descriptor,
            inputFields          = inputFields,
            currentGroup         = currentGroup,
            calculateCleanWeight = viewModel.calculateCleanWeight,
            cleanWeightCalculationText = viewModel.cleanWeightLabel,
            onClassify           = { state -> viewModel.classify(state) }
        )
    )
}