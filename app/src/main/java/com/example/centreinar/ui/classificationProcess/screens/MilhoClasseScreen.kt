package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun MilhoClasseScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Etapa 3 - Classe", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { navController.navigate("milhoDefinirLimites") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Determinar Classe")
            }

            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { navController.navigate("milhoDefinirLimites") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Pular")
            }
        }
    }
}
