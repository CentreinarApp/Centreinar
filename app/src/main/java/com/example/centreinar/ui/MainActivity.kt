package com.example.centreinar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation

// Imports SOJA
import com.example.centreinar.ui.classificationProcess.screens.*
import com.example.centreinar.ui.discount.screens.*

// Imports MILHO (Certifique-se que os pacotes estão corretos)
import com.example.centreinar.ui.screens.MilhoClassificationInputScreen
import com.example.centreinar.ui.screens.MilhoClassificationResultScreen
import com.example.centreinar.ui.discount.screens.MilhoDiscountInputScreen

// ViewModels
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.theme.CentreinarTheme
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CentreinarTheme {
                // Surface é necessário para garantir que o NavHost preencha o espaço
                Surface(modifier = Modifier.fillMaxSize()) {
                    CentreinarApp()
                }
            }
        }
    }
}

@Composable
fun CentreinarApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "main_flow",
        modifier = Modifier.fillMaxSize() // 🚨 CORREÇÃO CRÍTICA AQUI: NavHost preenche o espaço
    ) {
        navigation(
            startDestination = "home",
            route = "main_flow"
        ) {
            // -----------------------------
            // Fluxo CLASSIFICAÇÃO (SOJA)
            // -----------------------------
            composable("home") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                HomeScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("grainSelection") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                GrainScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("groupSelection") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                GroupSelectionScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("officialOrNot") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                OfficialOrNotOfficialScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("limitInput") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                LimitInputScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("disqualification") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                DisqualificationScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("classification") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                ClassificationInputScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("classificationResult") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                ClassificationResult(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("colorClassInput") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                ColorClassInput(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            // -----------------------------
            // Fluxo DESCONTOS (Soja)
            // -----------------------------
            composable("discount") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                DiscountInputScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }

            composable("grainSelectionDiscount") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                GrainSelectionDiscountScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }

            composable("groupSelectionDiscount") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                DiscountGroupSelectionScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }

            composable("officialOrNotDiscount") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                OfficialOrNotOfficialDiscountScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }

            composable("discountLimitInput") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                DiscountLimitInputScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }

            composable("discountResultsScreen") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                DiscountResultScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }

            composable("classificationToDiscount") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                ClassificationToDiscountInputScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }

            // -----------------------------
            // Fluxo CLASSIFICAÇÃO (MILHO)
            // -----------------------------
            composable("milhoGrainSelection") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                MilhoClasseScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("milhoGroupSelection") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                MilhoDefeitosScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("milhoOfficialOrNot") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                MilhoDefinirLimitesScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("milhoLimitInput") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                MilhoImpurezaUmidadeScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("milhoDisqualification") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                MilhoDescontosScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("milhoClassificationInput") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                ClassificationInputScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("milhoResultado") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                MilhoResultadoScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }

            composable("milhoResumoFinal") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                ResumoFinalMilhoScreen(
                    navController,
                    hiltViewModel<ClassificationViewModel>(parentEntry)
                )
            }
            // =================================================
            // FLUXO MILHO (DESCONTOS)
            // =================================================

            composable("milhoDiscountInput") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                MilhoDiscountInputScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }

            composable("milhoDiscountResult") { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry("main_flow")
                }
                MilhoDiscountResultScreen(
                    navController,
                    hiltViewModel<DiscountViewModel>(parentEntry)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleAppBar() {
    TopAppBar(title = { Text("Centreinar") })
}