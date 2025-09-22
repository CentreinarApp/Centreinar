package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun MilhoDefinirLimitesScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Etapa 4 - Limites de Tolerância", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                viewModel.isOfficial = true
                navController.navigate("milhoResultado")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Usar Parâmetros Oficiais")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.isOfficial = false
                navController.navigate("milhoResultado")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Definir Manualmente")
        }
    }
}
