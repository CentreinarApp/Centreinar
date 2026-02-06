package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel


@Composable
public fun GroupSelectionScreen(
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center // <-- Centralização mantida
        ) {
            Button(
                onClick = {
                    // Limpa os limites
                    viewModel.resetLimits()

                    // Seleciona o grupo
                    viewModel.selectedGroup = 1

                    // Navega para a rota da seleção da referência de limite
                    navController.navigate("officialOrNot")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Text("Grupo 1", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    // Limpa os limites
                    viewModel.resetLimits()

                    // Seleciona o grupo
                    viewModel.selectedGroup = 2

                    // Navega para a rota da seleção da referência de limite
                    navController.navigate("officialOrNot")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            ) {
                Text("Grupo 2", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}