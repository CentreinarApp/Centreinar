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
        description = "Os descontos aplicados ao lote de grãos são realizados sobre o resultado da classificação sempre que o parâmetro quantificado (Impurezas, umidade e defeitos) for superior ao valor de referência adotado.\n" +
                "\n" + "Nosso aplicativo simplifica o cálculo de descontos conforme as recomendações que tratam de requisitos técnicos necessários para a certificação de unidades armazenadoras (IN MAPA Nº33/2007). \n\nEsqueça as contas manuais e transforme dados complexos em negociações rápidas e transparentes, garantindo o controle total da sua rentabilidade na palma da sua mão.",
        callToActionText = "Escolha o grão que deseja calcular os descontos:"
    )
}