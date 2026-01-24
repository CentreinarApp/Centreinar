package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    val mockLimits = LimitMilho(
        source = 0,
        grain = "Milho",
        group = 1,
        type = 1,
        moistureUpLim = 14.0f,
        impuritiesUpLim = 1.00f,
        brokenUpLim = 3.00f,
        ardidoUpLim = 1.00f,
        mofadoUpLim = 1.00f,
        carunchadoUpLim = 2.00f,
        spoiledTotalUpLim = 6.00f
    )

    // Coleta de estados (usando WithLifecycle para igualar a tela de Soja)
    val classification by viewModel.classificationMilho.collectAsStateWithLifecycle(initialValue = null)
    val limitState by viewModel.limitMilho.collectAsStateWithLifecycle(initialValue = null)
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

    // Verificação se é classificação oficial
    val isOfficial = viewModel.isOfficial == true

    val safeLimits = limitState ?: mockLimits
    val context = LocalContext.current

    // Carrega os limites padrão assim que a tela abre
    LaunchedEffect(Unit) {
        viewModel.loadDefaultLimits()
    }

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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Nenhuma classificação disponível.",
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Voltar")
                }
            }
            return
        }

        val safeClass = classification!!

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Dados da Amostra",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            // Tabela de Resultado da Amostra
            ClassificationTable(
                classification = safeClass,
                limits = safeLimits,
                typeTranslator = viewModel::getFinalTypeLabel,
                modifier = Modifier.fillMaxWidth()
            )

            // Se for Oficial, exibe a tabela de limites do MAPA
            if (isOfficial) {
                Spacer(Modifier.height(32.dp))

                Text(
                    text = "Limites de Referência MAPA",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Tabela de limites
                if (allOfficialLimits.isNotEmpty()) {
                    OfficialReferenceTable(
                        grain = "Milho",
                        data = allOfficialLimits
                    )
                } else {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Carregando limites oficiais...", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        Spacer(Modifier.height(8.dp))

        // BOTÕES DE AÇÃO
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Voltar")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("home") }
                ) {
                    Text("Calcular Descontos")
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.prepareForPdfExport("Milho")
                    viewModel.exportClassificationMilho(context, safeClass, safeLimits)
                }
            ) {
                Text("Exportar PDF")
            }
        }
    }
}