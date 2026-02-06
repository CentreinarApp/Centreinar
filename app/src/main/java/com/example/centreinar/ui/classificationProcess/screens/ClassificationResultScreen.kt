package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.components.ClassificationTable
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun ClassificationResult(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val currentGrain = viewModel.selectedGrain
    val currentGroup = viewModel.selectedGroup
    val isOfficial = viewModel.isOfficial == true

    val classification by viewModel.classification.collectAsStateWithLifecycle()
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()
    val lastUsedLimitState by viewModel.lastUsedLimit.collectAsStateWithLifecycle()

    // Se for o grupo 1 => utiliza os valores de limite do grupo 1
    // Caso contrário, é o grupo 2 => utiliza os valores de limite do grupo 2
    val mockLimits = if (currentGroup == 1) {
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

    val safeLimits = lastUsedLimitState ?: mockLimits

    LaunchedEffect(currentGrain, currentGroup) {
        if (currentGrain != null && currentGroup != null) {
            viewModel.loadDefaultLimits()
        }
    }

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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }


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

            // Tabela de Resultado
            ClassificationTable(
                classification = classification!!,
                typeTranslator = viewModel::getFinalTypeLabel,
                limits = safeLimits,
                modifier = Modifier.fillMaxWidth()
            )

            // Campo dos limites
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

                // Carrega a tabela
                if (allOfficialLimits.isNotEmpty()) {
                    OfficialReferenceTable(
                        grain = currentGrain ?: "Soja",
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

        // Botões de ação
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
                    onClick = {
                        val id = classification?.id ?: 0
                        navController.navigate("discountInputScreen?classificationId=$id")
                    }
                ) {
                    Text("Calcular Descontos")
                }
            }

            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = {
                    viewModel.prepareForPdfExport(currentGrain ?: "Soja")
                    viewModel.exportClassification(context, classification!!, safeLimits)
                }
            ) {
                Text("Exportar PDF")
            }
        }
    }
}

@Composable
fun OfficialReferenceTable(grain: String, data: List<Any>) {
    val labels = if (grain == "Soja") {
        listOf("Ardidos e Queimados", "Queimados", "Mofados", "Avariados Total", "Esverdeados", "Partidos/Quebrados e Amassados", "Matérias Estranhas e Impurezas")
    } else {
        listOf("Ardidos", "Avariados Total", "Quebrados", "Matérias Estranhas e Impurezas", "Carunchados",)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Defeito", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                data.forEachIndexed { index, _ ->
                    Text("Tipo ${index + 1}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                }
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            labels.forEachIndexed { rowIndex, label ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1.3f),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    data.forEach { item ->
                        val value = when (item) {
                            is LimitSoja -> listOf(item.burntOrSourUpLim, item.burntUpLim, item.moldyUpLim, item.spoiledTotalUpLim, item.greenishUpLim, item.brokenCrackedDamagedUpLim, item.impuritiesUpLim)
                            is LimitMilho -> listOf(item.ardidoUpLim, item.spoiledTotalUpLim, item.brokenUpLim, item.impuritiesUpLim, item.carunchadoUpLim)
                            else -> emptyList()
                        }.getOrNull(rowIndex) ?: 0f

                        Text(
                            text = "$value%",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (rowIndex < labels.lastIndex) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                }
            }
        }
    }
}