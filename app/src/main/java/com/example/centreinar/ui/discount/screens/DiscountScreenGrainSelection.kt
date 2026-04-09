package com.example.centreinar.ui.discount.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.components.DiscountTopBar
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.main.screens.GrainSelectionScreen
import com.example.centreinar.util.Routes

@Composable
fun DiscountScreenGrainSelection(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    GrainSelectionScreen(
        grainDescriptors = viewModel.availableGrainDescriptors,
        onGrainSelected  = { descriptor ->
            viewModel.clearStates()
            viewModel.selectedGrain = descriptor.name

            if (descriptor.supportsGroups) {
                navController.navigate(Routes.GROUP_SELECTION_DISCOUNT)
            } else {
                viewModel.selectedGroup = 1
                navController.navigate(Routes.OFFICIAL_OR_NOT_DISCOUNT)
            }
        },
        topBar = { DiscountTopBar(
            onBackClick = { navController.popBackStack() },
            modifier = Modifier
        ) },
        description = "Regulamentado pelo Ministério da Agricultura, Pecuária e Armazenagem (MAPA), o cálculo de descontos é o pilar que garante a conversão justa de cada avaria em valor real, protegendo a rentabilidade de cada lote.\n" +
                "\n" + "Nosso aplicativo simplifica a gestão de descontos. Esqueça as contas manuais e transforme dados complexos em negociações rápidas e transparentes, garantindo o controle total da sua rentabilidade na palma da sua mão.",
        callToActionText = "Escolha o grão que deseja calcular os descontos:"
    )
}