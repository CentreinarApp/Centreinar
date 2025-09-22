package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun MilhoResultadoScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val classification by viewModel.classification.collectAsState()
    var observations by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(classification) {
        if (classification != null) {
            observations = viewModel.getObservations(null)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Etapa 5 - Resultado", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(24.dp))

        classification?.let {
            Text("Classe Final: ${it.finalType}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Observações: ${observations ?: "Carregando..."}")
        } ?: run {
            Text("Ainda não foi possível calcular a classificação.")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = { navController.navigate("milhoDescontos") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Avançar para Descontos")
        }
    }
}
