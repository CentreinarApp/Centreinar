    package com.example.centreinar.ui

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.Text
    import androidx.compose.material3.TopAppBar
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.remember
    import androidx.hilt.navigation.compose.hiltViewModel
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
    import androidx.navigation.navigation
    import com.example.centreinar.ui.classificationProcess.screens.ClassificationInputScreen
    import com.example.centreinar.ui.classificationProcess.screens.ClassificationResult
    import com.example.centreinar.ui.classificationProcess.screens.ColorClassInput
    import com.example.centreinar.ui.classificationProcess.screens.DisqualificationScreen
    import com.example.centreinar.ui.classificationProcess.screens.GrainScreen
    import com.example.centreinar.ui.classificationProcess.screens.GroupSelectionScreen
    import com.example.centreinar.ui.classificationProcess.screens.HomeScreen
    import com.example.centreinar.ui.classificationProcess.screens.LimitInputScreen
    import com.example.centreinar.ui.classificationProcess.screens.OfficialOrNotOfficialScreen
    import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
    import com.example.centreinar.ui.discount.screens.DiscountGroupSelectionScreen
    import com.example.centreinar.ui.discount.screens.GrainSelectionDiscountScreen
    import com.example.centreinar.ui.discount.screens.OfficialOrNotOfficialDiscountScreen
    import com.example.centreinar.ui.discount.screens.discountInputScreen
    import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
    import com.example.centreinar.ui.theme.CentreinarTheme
    import dagger.hilt.android.AndroidEntryPoint

    @AndroidEntryPoint
    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                CentreinarTheme {
                    CentreinarApp()
                }
            }
        }
    }
//    modifier = Modifier.padding(padding)

    @Composable
    fun CentreinarApp() {
        val navController = rememberNavController()

        NavHost(
            navController = navController,
            startDestination = "main_flow"
        ) {
            navigation(
                startDestination = "home",
                route = "main_flow"
            ) {
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

                composable("discount") {backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_flow")
                    }
                    discountInputScreen(
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

            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    private fun SimpleAppBar() {
        TopAppBar(
            title = { Text("Centreinar") },
        )
    }