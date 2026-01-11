package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.ui.classificationProcess.components.ClassificationTable
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun MilhoResultadoScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // Definindo o objeto mock para garantir que os limites nunca sejam nulos na UI.
    // Escolhi o melhor tipo do milho para limite => tipo 1
    val mockLimits = LimitMilho(
        source = 0,
        grain = "Milho",
        group = 1,
        type = 1,
        moistureUpLim = 14.0f,
        impuritiesUpLim = 1.0f,
        brokenUpLim = 3.0f,
        ardidoUpLim = 1.0f,
        mofadoUpLim = 1.0f,
        carunchadoUpLim = 1.0f,
        spoiledTotalUpLim = 4.0f
    )

    // Leitura da Classificação
    val classification by viewModel.classificationMilho.collectAsState(initial = null)

    // Leitura dos Limites
    val limitState by viewModel.limitMilho.collectAsState(initial = null)

    val safeLimits = limitState ?: mockLimits

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Resultado da Classificação (Milho)",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        if (classification == null) {
            Text(
                "Nenhuma classificação disponível.",
                color = MaterialTheme.colorScheme.error
            )

            Spacer(Modifier.height(16.dp))

            Button(onClick = { navController.popBackStack() }) {
                Text("Voltar")
            }
            return
        }

        val safeClass = classification!!

        // TABELA DE CLASSIFICAÇÃO
        ClassificationTable(
            classification = safeClass,
            limits = safeLimits,
            typeTranslator = viewModel::getFinalTypeLabel,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(24.dp))

        // BOTÕES DE AÇÃO
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                modifier = Modifier.weight(1f),
                onClick = { navController.popBackStack() },
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Text("Voltar")
            }

            Button(
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate("milhoDescontos") }
            ) {
                Text("Descontos")
            }
        }
    }
}