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
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.centreinar.ui.CentreinarTheme
import com.example.centreinar.ui.home.ClassificationInputScreen
import com.example.centreinar.ui.HomeScreen
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

@Composable
fun CentreinarApp() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { SimpleAppBar() }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(padding)
        ) {
            composable("home") { HomeScreen(navController) }
            composable("classification") { ClassificationInputScreen() }
            composable("discount") { /* Add your DiscountScreen here */ }
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