package com.example.centreinar.ui.classificationProcess.screens

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
@Composable
fun ResumoFinalMilhoScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
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
        Text("Resumo Final - Milho", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))

        classification?.let {
            Text("Classe: ${it.finalType}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Observações: ${observations ?: "Carregando..."}")
        } ?: run {
            Text("Classificação não encontrada")
        }

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = {
                Toast.makeText(context, "Exportando PDF...", Toast.LENGTH_SHORT).show()
                // viewModel.exportClassification(context, classification!!, limit!!)
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Exportar PDF")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { navController.navigate("home") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Voltar ao Início")
        }
    }
}
