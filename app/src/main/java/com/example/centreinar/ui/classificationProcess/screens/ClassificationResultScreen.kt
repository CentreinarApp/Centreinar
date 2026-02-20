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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.components.ClassificationTable
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.ui.classificationProcess.components.ColorAndGroupCard
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.classificationProcess.components.DisqualificationInfoCard
import com.example.centreinar.ui.classificationProcess.components.OfficialReferenceTable
import com.example.centreinar.ui.classificationProcess.components.MoistureInfoCard
import com.example.centreinar.ui.classificationProcess.components.ColorAndGroupCard


@Composable
fun ClassificationResult(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val isOfficialState = viewModel.isOfficial == true
    DisposableEffect(isOfficialState) {
        onDispose {
            if (!isOfficialState) {
                viewModel.deleteCustomLimits()
            }
        }
    }

    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        val context = LocalContext.current
        val currentGrain = viewModel.selectedGrain
        val currentGroup = viewModel.selectedGroup
        val isOfficial = viewModel.isOfficial == true

        val classification by viewModel.classification.collectAsStateWithLifecycle()
        val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()
        val lastUsedLimitState by viewModel.lastUsedLimit.collectAsStateWithLifecycle()

        val disqualificationSoja by viewModel.disqualificationSoja.collectAsStateWithLifecycle()
        val toxicSeedsSoja by viewModel.toxicSeedsSoja.collectAsStateWithLifecycle()
        val colorClassificationSoja by viewModel.colorClassificationSoja.collectAsStateWithLifecycle()

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
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            Text(
                "Resultado da Classificação",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center
            )
            Text(
                if (isOfficial) "Referência Oficial" else "Referência Não Oficial",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                fontSize = 15.sp
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

                // --- NOVO BOX DE UMIDADE ---
                val moistureValue = when (val c = classification) {
                    is com.example.centreinar.ClassificationSoja -> c.moisturePercentage
                    is com.example.centreinar.ClassificationMilho -> c.moisturePercentage
                    else -> 0f
                }

                val moistureLimit = when (val l = safeLimits) {
                    is LimitSoja -> l.moistureUpLim
                    is LimitMilho -> l.moistureUpLim
                    else -> 14.0f
                }

                MoistureInfoCard(moisture = moistureValue, limit = moistureLimit)

                Spacer(Modifier.height(8.dp))

                // Tabela de Resultado
                ClassificationTable(
                    classification = classification!!,
                    disqualificationSoja = disqualificationSoja!!,
                    typeTranslator = viewModel::getFinalTypeLabel,
                    limits = safeLimits,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))
                colorClassificationSoja?.let { sojaData ->
                    ColorAndGroupCard(
                        title = sojaData.framingClass,
                        subtitle = "Amarela: %.2f%% | Outras Cores: %.2f%%".format(
                            sojaData.yellowPercentage, sojaData.otherColorPercentage
                        ),
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                }

                Spacer(Modifier.height(26.dp))

                // Card da Desclassificação
                disqualificationSoja?.let { disq ->
                    val toxicSeedsPairs = toxicSeedsSoja.map { it.name to it.quantity }

                    DisqualificationInfoCard(
                        badConservation = disq.badConservation == 1,
                        strangeSmell = disq.strangeSmell == 1,
                        insects = disq.insects == 1,
                        toxicGrains = disq.toxicGrains == 1,
                        toxicSeeds = toxicSeedsPairs
                    )
                }

                // Campo dos limites
                Spacer(Modifier.height(32.dp))

                Text(
                    text = if (isOfficialState) "Limites de Referência MAPA" else "Limites Utilizados no Cálculo",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    textAlign = TextAlign.Center
                )

                // Preparamos os dados para a tabela
                val dadosParaTabela = if (isOfficialState) {
                    allOfficialLimits
                } else {
                    listOf(safeLimits) // Se manual, passa apenas o limite usado
                }

                if (dadosParaTabela.isNotEmpty()) {
                    OfficialReferenceTable(
                        grain = "Soja",
                        group = currentGroup ?: 1,
                        isOfficial = isOfficial,
                        data = dadosParaTabela,
                    )
                } else {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Carregando limites...", style = MaterialTheme.typography.bodySmall)
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
}