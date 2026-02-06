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
import androidx.compose.ui.unit.sp
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
        val currentGroup = viewModel.selectedGroup

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
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        "Nenhuma classificação disponível.",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) {
                        Text("Voltar")
                    }
                }
            } else {
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

                    // --- INSERÇÃO DO CARD DE UMIDADE ---
                    val moistureValue = safeClass.moisturePercentage
                    val moistureLimit = safeLimits.moistureUpLim

                    MoistureInfoCard(moisture = moistureValue, limit = moistureLimit)

                    Spacer(Modifier.height(8.dp))

                    // Tabela de Resultado da Amostra
                    ClassificationTable(
                        classification = safeClass,
                        limits = safeLimits,
                        typeTranslator = viewModel::getFinalTypeLabel,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Se for Oficial, exibe a tabela de limites do MAPA
                    // Caso contrário, exibe os limites do usuário
                    Spacer(Modifier.height(32.dp))

                    Text(
                        text = if (isOfficial) "Limites de Referência MAPA" else "Limites Utilizados no Cálculo",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        textAlign = TextAlign.Center
                    )

                    // Preparamos os dados: Se oficial, a lista do banco. Se manual, o limite usado.
                    val dadosParaTabela = if (isOfficial) {
                        allOfficialLimits
                    } else {
                        // Quando manual, passamos o safeLimits dentro de uma lista
                        listOf(safeLimits)
                    }

                    // Agora verificamos se há dados para mostrar (seja a lista oficial ou a manual)
                    if (dadosParaTabela.isNotEmpty()) {
                        OfficialReferenceTable(
                            grain = "Milho",
                            group = currentGroup ?: 1,
                            isOfficial = isOfficial,
                            data = dadosParaTabela
                        )
                    } else {
                        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text(
                                "Carregando limites...",
                                style = MaterialTheme.typography.bodySmall
                            )
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
                            onClick = {
                                val id = safeClass.id
                                val official =
                                    isOfficial // Pega o estado isOfficial que você já tem nesta tela

                                // Passa os dois parâmetros na URL da rota
                                navController.navigate("milhoDiscountInputScreen?classificationId=$id&isOfficial=$official")
                            }
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
    }
}