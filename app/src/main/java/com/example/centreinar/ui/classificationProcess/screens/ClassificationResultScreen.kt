package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.components.ClassificationTable
import com.example.centreinar.LimitSoja
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun ClassificationResult(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {

    // Se for o grupo 1 => utiliza os valores de limite do grupo 1
    // Caso contrário, é o grupo 2 => utiliza os valores de limite do grupo 2
    val mockLimits = if (viewModel.selectedGroup == 1) {
        LimitSoja(
            source = 0, grain = "Soja", group = 1, type = 1,
            impuritiesLowerLim = 0.0f, impuritiesUpLim = 1.0f,
            moistureLowerLim = 0.0f, moistureUpLim = 14.0f,
            brokenCrackedDamagedLowerLim = 0.0f, brokenCrackedDamagedUpLim = 8.0f,
            greenishLowerLim = 0.0f, greenishUpLim = 2.0f,
            burntLowerLim = 0.0f, burntUpLim = 0.3f,
            burntOrSourLowerLim = 0.0f, burntOrSourUpLim = 1.0f,
            moldyLowerLim = 0.0f, moldyUpLim = 0.5f,
            spoiledTotalLowerLim = 0.0f, spoiledTotalUpLim = 4.0f
        )
    } else {
        LimitSoja(
            source = 0, grain = "Soja", group = 2, type = 1,
            impuritiesLowerLim = 0.0f, impuritiesUpLim = 1.0f,
            moistureLowerLim = 0.0f, moistureUpLim = 14.0f,
            brokenCrackedDamagedLowerLim = 0.0f, brokenCrackedDamagedUpLim = 30.0f,
            greenishLowerLim = 0.0f, greenishUpLim = 8.0f,
            burntLowerLim = 0.0f, burntUpLim = 1.0f,
            burntOrSourLowerLim = 0.0f, burntOrSourUpLim = 4.0f,
            moldyLowerLim = 0.0f, moldyUpLim = 6.0f,
            spoiledTotalLowerLim = 0.0f, spoiledTotalUpLim = 8.0f
        )
    }

    val classification by viewModel.classification.collectAsState(initial = null)

    val lastUsedLimitState by viewModel.lastUsedLimit.collectAsState(initial = mockLimits)

    val safeLimits = lastUsedLimitState ?: mockLimits

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            "Resultado da Classificação",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(16.dp))

        if (classification == null) {
            Text("Nenhuma classificação disponível.", color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { navController.popBackStack() }) { Text("Voltar") }
            return
        }

        val safeClass = classification!!

        Column(modifier = Modifier.weight(1f)) {
            ClassificationTable(
                classification = safeClass,
                typeTranslator = viewModel::getFinalTypeLabel,
                limits = safeLimits,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(20.dp))

        // Botões de ação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier
                    .weight(1f),
                onClick = { navController.popBackStack() }
            ) {
                Text("Voltar")
            }

            Button(
                modifier = Modifier
                    .weight(1f),
                onClick = { navController.navigate("home") }
            ) {
                Text("Realizar Cálculo de Desconto")
            }
        }

        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            viewModel.prepareForPdfExport("Soja")
            viewModel.exportClassification(context, safeClass, safeLimits)
        }) {
            Text("Exportar PDF")
        }
    }
}