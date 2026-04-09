package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.components.ClassificationTopBar
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
        },
        topBar = { ClassificationTopBar(
            onBackClick = { navController.popBackStack() },
            modifier = Modifier
        ) },
        description = "Regulamentada pelo Ministério da Agricultura, Pecuária e Armazenagem (MAPA), a classificação de grãos é o pilar que define a segurança e o valor real de cada lote.\n\nNosso aplicativo simplifica esse processo essencial, automatizando o cálculo de umidade, impurezas e avarias. Transforme dados complexos em negociações justas, rápidas e transparentes, garantindo precisão total na palma da sua mão.",
        callToActionText = "Escolha o grão que deseja clasificar:"
    )
}