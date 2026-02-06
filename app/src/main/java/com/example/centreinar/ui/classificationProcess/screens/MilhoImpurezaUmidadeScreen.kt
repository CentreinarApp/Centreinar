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
fun MilhoImpurezaUmidadeScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        var pesoAmostra by remember { mutableStateOf("") }
        var pesoImpurezas by remember { mutableStateOf("") }
        var umidade by remember { mutableStateOf("") }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text("Etapa 1 - Umidade e Impurezas", style = MaterialTheme.typography.headlineSmall)

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pesoAmostra,
                onValueChange = { pesoAmostra = it },
                label = { Text("Peso da Amostra (g)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = pesoImpurezas,
                onValueChange = { pesoImpurezas = it },
                label = { Text("Peso das Impurezas (g)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = umidade,
                onValueChange = { umidade = it },
                label = { Text("Umidade (%)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = {
                    // Salvar valores no ViewModel se necessário
                    navController.navigate("milhoDefeitos")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Próximo")
            }
        }
    }
}
