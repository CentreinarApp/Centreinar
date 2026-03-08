package com.example.centreinar.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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

import com.example.centreinar.ui.classificationProcess.screens.*
import com.example.centreinar.ui.discount.screens.*
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.theme.CentreinarTheme
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
            startDestination = "home",
            route = "main_flow"
        ) {

            // -------------------------------------------------------------------------
            // Fluxo CLASSIFICAÇÃO
            // -------------------------------------------------------------------------

            composable("home") { backStackEntry ->
                HomeScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable("grainSelection") { backStackEntry ->
                GrainScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable("groupSelection") { backStackEntry ->
                GroupSelectionScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable("officialOrNot") { backStackEntry ->
                OfficialOrNotOfficialScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable("limitInput") { backStackEntry ->
                LimitInputScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable(
                route = "disqualification?classificationId={classificationId}",
                arguments = listOf(
                    navArgument("classificationId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("classificationId") ?: -1
                DisqualificationScreen(
                    navController = navController,
                    classificationId = id,
                    viewModel = backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable("classification") { backStackEntry ->
                ClassificationInputScreen(
                    navController,
                    backStackEntry.sharedClassificationViewModel(navController)
                )
            }

            composable("classificationResult") { backStackEntry ->
                ClassificationResultScreen(
                    navController     = navController,
                    classificationViewModel = backStackEntry.sharedClassificationViewModel(navController),
                    discountViewModel = backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            // -------------------------------------------------------------------------
            // Fluxo DESCONTOS
            // -------------------------------------------------------------------------

            composable("grainSelectionDiscount") { backStackEntry ->
                GrainSelectionDiscountScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable("groupSelectionDiscount") { backStackEntry ->
                DiscountGroupSelectionScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable("officialOrNotDiscount") { backStackEntry ->
                OfficialOrNotOfficialDiscountScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable("discountLimitInput") { backStackEntry ->
                DiscountLimitInputScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable(
                route = "discountInputScreen?classificationId={classificationId}",
                arguments = listOf(
                    navArgument("classificationId") {
                        type = NavType.IntType
                        defaultValue = -1
                    }
                )
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getInt("classificationId") ?: -1
                DiscountInputScreen(
                    navController = navController,
                    classificationId = if (id != -1) id else null,
                    viewModel = backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable("milhoDiscountInputScreen?isOfficial={isOfficial}",
                arguments = listOf(
                    navArgument("isOfficial") {
                        type = NavType.BoolType
                        defaultValue = true
                    }
                )
            ) { backStackEntry ->
                DiscountInputScreen(
                    navController = navController,
                    classificationId = null,
                    viewModel = backStackEntry.sharedDiscountViewModel(navController)
                )
            }

            composable("discountResultsScreen") { backStackEntry ->
                DiscountResultScreen(
                    navController,
                    backStackEntry.sharedDiscountViewModel(navController)
                )
            }
        }
    }
}

// =============================================================================
// Funções de Extensão
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SimpleAppBar() {
    TopAppBar(title = { Text("Centreinar") })
}