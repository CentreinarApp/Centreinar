package com.example.centreinar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.components.MilhoClassificationTable

@Composable
fun MilhoClassificationResultScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val classification by viewModel.classification.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Resultado — Milho", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(8.dp))
        Spacer(Modifier.height(8.dp))

        val lastMilhoClassification = remember { mutableStateOf<ClassificationMilho?>(null) }
            if (lastMilhoClassification.value != null) {
            MilhoClassificationTable(lastMilhoClassification.value!!)
        } else {
            Text("Nenhuma classificação de milho encontrada neste estado. Garanta que o repositório devolve a classificação e que o ViewModel a expõe.", modifier = Modifier.padding(16.dp))
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { navController.navigate("home") }, modifier = Modifier.fillMaxWidth()) {
            Text("Nova Análise")
        }
    }
}
