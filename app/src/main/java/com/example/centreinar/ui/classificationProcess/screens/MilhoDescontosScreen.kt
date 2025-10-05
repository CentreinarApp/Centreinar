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
fun MilhoDescontosScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    var descontoTotal by remember { mutableStateOf("0.0") }
    var pesoFinal by remember { mutableStateOf("0.0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Etapa 6 - Cálculo de Descontos", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Desconto Total: $descontoTotal %")
        Spacer(modifier = Modifier.height(8.dp))
        Text("Peso Final: $pesoFinal kg")

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("resumoFinalMilho") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Finalizar Classificação")
        }
    }
}
