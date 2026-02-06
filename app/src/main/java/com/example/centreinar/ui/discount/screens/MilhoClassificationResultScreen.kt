package com.example.centreinar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.components.MilhoClassificationTable
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.milho.components.MilhoDiscountResultsTable

@Composable
fun MilhoClassificationResultScreen(
    navController: NavController,
    classificationViewModel: ClassificationViewModel = hiltViewModel(),
    discountViewModel: DiscountViewModel = hiltViewModel()
) {
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        val classificationState by classificationViewModel.classification.collectAsState()
        val discountState by discountViewModel.discountsMilho.collectAsState()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                text = "Resultados — Milho",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )

            Spacer(Modifier.height(8.dp))

            // Classificação
            if (classificationState is ClassificationMilho) {
                MilhoClassificationTable(classification = classificationState as ClassificationMilho)
            } else {
                Text("Carregando classificação...", modifier = Modifier.padding(8.dp))
            }

            Spacer(Modifier.height(16.dp))

            // Descontos
            if (discountState != null) {
                MilhoDiscountResultsTable(discounts = discountState!!)
            } else {
                // Se não tem desconto, mostramos um aviso visual ou deixamos vazio
                Text(
                    "Cálculo financeiro ainda não realizado.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }

            Spacer(Modifier.height(24.dp))

            // BOTÕES DE NAVEGAÇÃO
            if (discountState == null) {
                Button(
                    onClick = { navController.navigate("milhoDiscountInput") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calcular Descontos / Financeiro")
                }
            } else {
                Button(
                    onClick = {
                        classificationViewModel.clearStates()
                        discountViewModel.clearStates()

                        navController.navigate("home") {
                            popUpTo("home") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nova Análise")
                }
            }

            Spacer(Modifier.height(20.dp))
        }
    }
}