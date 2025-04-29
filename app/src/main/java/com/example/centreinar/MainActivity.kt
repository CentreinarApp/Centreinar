package com.example.centreinar

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.centreinar.ui.CentreinarTheme
import com.example.centreinar.ui.home.ClassificationInputScreen
import com.example.centreinar.ui.HomeScreen
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(navController, drawerState, scope)
        }
    ) {
        Scaffold(
            topBar = { AppBar(scope, drawerState) },
            floatingActionButton = { AddFab() }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "home",
                modifier = Modifier.padding(padding)
            ) {
                composable("home") { HomeScreen() }
                composable("input") { ClassificationInputScreen() }
                // Add other destinations
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    scope: CoroutineScope,
    drawerState: DrawerState
) {
    TopAppBar(
        title = { Text("Centreinar") },
        navigationIcon = {
            IconButton(
                onClick = { scope.launch { drawerState.open() } }
            ) {
                Icon(Icons.Filled.Menu, contentDescription = "Menu")
            }
        }
    )
}

@Composable
private fun AddFab() {
    FloatingActionButton(onClick = { /* Handle FAB click */ }) {
        Icon(Icons.Filled.Add, contentDescription = "Add")
    }
}

@Composable
private fun NavigationDrawerContent(
    navController: NavController,
    drawerState: DrawerState,
    scope: CoroutineScope
) {
    Column(Modifier.fillMaxSize()) {
        // Drawer header
        Text("Menu", modifier = Modifier.padding(16.dp))

        // Navigation items
        NavigationDrawerItem(
            label = { Text("Home") },
            selected = false,
            onClick = {
                scope.launch { drawerState.close() }
                navController.navigate("home")
            }
        )
        NavigationDrawerItem(
            label = { Text("New Sample") },
            selected = false,
            onClick = { navController.navigate("input") }
        )
        // Add other navigation items
    }
}