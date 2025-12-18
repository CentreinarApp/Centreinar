package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.components.ClassificationTable
import com.example.centreinar.ui.classificationProcess.components.LimitSoja
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun ClassificationResult(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // Definindo o objeto mock/padrão para garantir que os limites nunca sejam nulos no UI.
    val mockLimits = LimitSoja()

    //  Leitura da Classificação
    val classification by viewModel.classification.collectAsState(initial = null)

    //  Carregando Limites
    // O 'collectAsState' já faz o 'remember' e o 'State' por nós.
    val lastUsedLimitState by viewModel.lastUsedLimit.collectAsState(initial = mockLimits)

    val safeLimits = (lastUsedLimitState ?: mockLimits) as LimitSoja


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

        // safeClass é não-nulo a partir daqui
        val safeClass = classification!!


        ClassificationTable(
            classification = safeClass,
            typeTranslator = viewModel::getFinalTypeLabel,
            limits = safeLimits,
            modifier = Modifier.fillMaxWidth()
        )
        // Fim da Tabela

        Spacer(Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { navController.popBackStack() }
        ) {
            Text("Voltar")
        }
    }
}