package com.example.centreinar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.centreinar.ui.classificationProcess.screens.ClassificationGroupSelectionScreen
import com.example.centreinar.ui.classificationProcess.screens.ClassificationInputScreen
import com.example.centreinar.ui.classificationProcess.screens.ClassificationLimitInputScreen
import com.example.centreinar.ui.classificationProcess.screens.ClassificationResultScreen
import com.example.centreinar.ui.classificationProcess.screens.DisqualificationScreen
import com.example.centreinar.ui.classificationProcess.screens.ClassificationGrainSelectionScreen
import com.example.centreinar.ui.classificationProcess.screens.OfficialOrNotOfficialScreen
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.discount.screens.DiscountGroupSelectionScreen
import com.example.centreinar.ui.discount.screens.DiscountInputScreen
import com.example.centreinar.ui.discount.screens.DiscountLimitInputScreen
import com.example.centreinar.ui.discount.screens.DiscountResultScreen
import com.example.centreinar.ui.discount.screens.GrainSelectionDiscountScreen
import com.example.centreinar.ui.discount.screens.OfficialOrNotOfficialDiscountScreen
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.main.screens.HomeScreen
import com.example.centreinar.ui.theme.CentreinarTheme
import com.example.centreinar.util.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CentreinarTheme {
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
        modifier = Modifier.fillMaxSize()
    ) {
        navigation(
            startDestination = Routes.HOME,
            route = "main_flow"
        ) {

            // -------------------------------------------------------------------------
            // Fluxo PRINCIPAL / HOME
            // -------------------------------------------------------------------------
            composable(Routes.HOME) { backStackEntry ->
                HomeScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            // -------------------------------------------------------------------------
            // Fluxo CLASSIFICAÇÃO
            // -------------------------------------------------------------------------
            composable(Routes.GRAIN_SELECTION) { backStackEntry ->
                ClassificationGrainSelectionScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable(Routes.GROUP_SELECTION) { backStackEntry ->
                // Utiliza o Wrapper que limpa estados e define o grupo no ClassificationViewModel
                ClassificationGroupSelectionScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable(Routes.OFFICIAL_OR_NOT) { backStackEntry ->
                OfficialOrNotOfficialScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable(Routes.LIMIT_INPUT) { backStackEntry ->
                // Utiliza o Wrapper que lida com CustomLimitPayload para classificação
                ClassificationLimitInputScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable(
                route = "${Routes.DISQUALIFICATION}?classificationId={classificationId}",
                arguments = listOf(
                    navArgument("classificationId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("classificationId") ?: -1
                DisqualificationScreen(
                    navController    = navController,
                    classificationId = id,
                    viewModel        = backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable(Routes.CLASSIFICATION_INPUT) { backStackEntry ->
                ClassificationInputScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable(Routes.CLASSIFICATION_RESULT) { backStackEntry ->
                ClassificationResultScreen(
                    navController           = navController,
                    classificationViewModel = backStackEntry.sharedClassificationViewModel(navController),
                    discountViewModel       = backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            // -------------------------------------------------------------------------
            // Fluxo DESCONTOS
            // -------------------------------------------------------------------------
            composable(Routes.GRAIN_SELECTION_DISCOUNT) { backStackEntry ->
                GrainSelectionDiscountScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable(Routes.GROUP_SELECTION_DISCOUNT) { backStackEntry ->
                // Utiliza o Wrapper que limpa estados e define o grupo no DiscountViewModel
                DiscountGroupSelectionScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable(Routes.OFFICIAL_OR_NOT_DISCOUNT) { backStackEntry ->
                OfficialOrNotOfficialDiscountScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable(Routes.DISCOUNT_LIMIT_INPUT) { backStackEntry ->
                // Utiliza o Wrapper que lida com saveCustomLimit para descontos
                DiscountLimitInputScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable(
                route = "${Routes.DISCOUNT_INPUT}?classificationId={classificationId}",
                arguments = listOf(
                    navArgument("classificationId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("classificationId") ?: -1
                DiscountInputScreen(
                    navController    = navController,
                    classificationId = if (id != -1) id else null,
                    viewModel        = backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable(Routes.DISCOUNT_RESULTS) { backStackEntry ->
                DiscountResultScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }
        }
    }
}

// =============================================================================
// Funções de Extensão — compartilhamento de ViewModel no escopo do NavGraph
// =============================================================================

@Composable
private fun NavBackStackEntry.sharedClassificationViewModel(
    navController: NavController
): ClassificationViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry("main_flow")
    }
    return hiltViewModel(parentEntry)
}

@Composable
private fun NavBackStackEntry.sharedDiscountViewModel(
    navController: NavController
): DiscountViewModel {
    val parentEntry = remember(this) {
        navController.getBackStackEntry("main_flow")
    }
    return hiltViewModel(parentEntry)
}