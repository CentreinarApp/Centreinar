package com.example.centreinar.ui.discount.screens

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.components.DiscountTopBar
import com.example.centreinar.ui.discount.strategy.FinancialDiscountPayload
import com.example.centreinar.ui.discount.viewmodel.DiscountNavigationEvent
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.main.screens.InputScreen
import com.example.centreinar.ui.main.screens.InputScreenMode
import com.example.centreinar.util.Routes

@Composable
fun DiscountInputScreen(
    navController: NavController,
    classificationId: Int? = null,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val grain   = viewModel.selectedGrain ?: ""

    // One-shot navigation — remove DiscountInputScreen do back stack
    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                is DiscountNavigationEvent.NavigateToResults -> {
                    navController.navigate(Routes.DISCOUNT_RESULTS) {
                        popUpTo(Routes.DISCOUNT_INPUT) { inclusive = true }
                    }
                }
            }
        }
    }

    InputScreen(
        topBar = { DiscountTopBar(onBackClick = { navController.popBackStack() }) },
        mode   = InputScreenMode.Discount(
            defectFields  = viewModel.getStrategy(grain)?.getDefectInputFields() ?: emptyList(),
            selectedGroup = viewModel.selectedGroup ?: 1,
            errorMessage  = uiState.error,
            onCalculate   = { fullMap, priceBySack, lotWeight, daysOfStorage,
                              doesTechnicalLoss, deductionValue, doesDeduction ->

                val resolvedStrategy = viewModel.getStrategy(grain) ?: return@Discount

                val defectsPayload   = resolvedStrategy.createDefectsPayload(fullMap)
                val financialPayload = FinancialDiscountPayload(
                    priceBySack       = priceBySack,
                    lotWeight         = lotWeight,
                    group             = viewModel.selectedGroup ?: 1,
                    daysOfStorage     = daysOfStorage,
                    doesTechnicalLoss = doesTechnicalLoss,
                    deductionValue    = deductionValue,
                    doesDeduction     = doesDeduction
                )
                viewModel.calculateDiscount(defectsPayload, financialPayload)
            }
        )
    )
}