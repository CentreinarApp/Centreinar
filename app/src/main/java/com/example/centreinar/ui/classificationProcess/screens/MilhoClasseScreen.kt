package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun MilhoClasseScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
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
