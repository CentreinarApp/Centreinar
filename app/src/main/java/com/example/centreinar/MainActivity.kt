    package com.example.centreinar

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import androidx.compose.foundation.layout.padding
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.TopAppBar
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.remember
    import androidx.compose.ui.Modifier
    import androidx.hilt.navigation.compose.hiltViewModel
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.navigation.compose.rememberNavController
    import androidx.navigation.navigation

    import com.example.centreinar.ui.CentreinarTheme
    import com.example.centreinar.ui.home.ClassificationInputScreen
    import com.example.centreinar.ui.HomeScreen
    import com.example.centreinar.ui.home.DisqualificationScreen
    import com.example.centreinar.ui.home.OfficialOrNotOfficialScreen

    import com.example.centreinar.ui.home.GrainScreen
    import com.example.centreinar.ui.home.GroupSelectionScreen
    import com.example.centreinar.ui.home.HomeViewModel
    import com.example.centreinar.ui.home.LimitInputScreen
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
                        hiltViewModel<HomeViewModel>(parentEntry)
                    )
                }

                composable("grainSelection") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_flow")
                    }
                    GrainScreen(
                        navController,
                        hiltViewModel<HomeViewModel>(parentEntry)
                    )
                }

                composable("groupSelection") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_flow")
                    }
                    GroupSelectionScreen(
                        navController,
                        hiltViewModel<HomeViewModel>(parentEntry)
                    )
                }

                composable("officialOrNot") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_flow")
                    }
                    OfficialOrNotOfficialScreen(
                        navController,
                        hiltViewModel<HomeViewModel>(parentEntry)
                    )
                }

                composable("limitInput") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_flow")
                    }
                    LimitInputScreen(
                        navController,
                        hiltViewModel<HomeViewModel>(parentEntry)
                    )
                }

                composable("disqualification") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_flow")
                    }
                    DisqualificationScreen(
                        navController,
                        hiltViewModel<HomeViewModel>(parentEntry)
                    )
                }

                composable("classification") { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry("main_flow")
                    }
                    ClassificationInputScreen(
                        navController,
                        hiltViewModel<HomeViewModel>(parentEntry)
                    )
                }

                composable("discount") { /* Similar structure for discount screen */ }
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