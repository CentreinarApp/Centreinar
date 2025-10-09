package com.example.centreinar.ui.classificationProcess.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ColorClassificationSoja
import com.example.centreinar.ui.classificationProcess.components.ClassColorResult
import com.example.centreinar.ui.classificationProcess.components.ClassificationTable
import com.example.centreinar.ui.classificationProcess.components.ObservationCard
import com.example.centreinar.ui.classificationProcess.components.SimplifiedResultsTable
import com.example.centreinar.ui.classificationProcess.components.UsedLimitTable
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun ClassificationResult(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
){
    val classification by viewModel.classification.collectAsState()
    val defaultLimit by viewModel.defaultLimits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showLimitsDialog by remember { mutableStateOf(false) }

    // --- Otimização para o Diálogo de Limites ---
    // Usamos o 'lastUsedLimit' para exibir os limites usados
    val lastUsedLimit by viewModel.lastUsedLimit.collectAsStateWithLifecycle()
    var isLimitLoading by remember { mutableStateOf(false) } // Novo estado para o diálogo

    val context = LocalContext.current

    var colorClassificationResult by remember { mutableStateOf<ColorClassificationSoja?>(null) }
    var observation by remember { mutableStateOf<String?>(null) }


    // Quando doesDefineColorClass for verdadeiro, busca a cor e observação
    if (viewModel.doesDefineColorClass == true) {
        LaunchedEffect(Unit) {
            // Envolvendo em Dispatchers.IO para segurança, embora ViewModelScope use Main por padrão,
            // as chamadas internas do repo já usam IO. Mantenho a estrutura LaunchedEffect(Unit).
            colorClassificationResult = viewModel.getClassColor()
            observation = viewModel.getObservations(colorClassificationResult)
        }
    }

    // Lógica para pré-carregar os limites do Diálogo quando o resultado da classificação estiver pronto
    LaunchedEffect(classification) {
        if (classification != null) {
            // Carrega os limites em background assim que o resultado da classificação estiver disponível
            viewModel.loadLastUsedLimit()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // COLUNA PRINCIPAL COM SCROLL
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isLoading -> {
                    // ... (Indicador de Progresso)
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                error != null -> {
                    // ... (Mensagem de Erro)
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    )
                }

                classification != null -> {
                    // ... (SimpliedResultsTable)
                    if (viewModel.doesDefineColorClass == true && colorClassificationResult != null) {
                        SimplifiedResultsTable(
                            classification!!.finalType.toString(),
                            colorClassificationResult!!.framingClass,
                            modifier = Modifier.fillMaxWidth()
                        )
                    } else {
                        SimplifiedResultsTable(
                            classification!!.finalType.toString(),
                            "Não Definida",
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    ClassificationTable(
                        classification = classification!!,
                        modifier = Modifier.fillMaxWidth()
                    )


                    Spacer(Modifier.height(10.dp))

                    // Botão para abrir o Diálogo de Limites
                    Text(
                        text = "Ver limites utilizados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                // O limite já deve estar carregado pelo LaunchedEffect(classification)
                                showLimitsDialog = true
                            }
                            .padding(vertical = 8.dp)
                    )

                    if (viewModel.doesDefineColorClass == true && colorClassificationResult != null) {
                        ClassColorResult(
                            colorClassificationResult!!,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if(observation != null){
                        ObservationCard(
                            observation!!,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }


                    Log.e("Observations", "observations in screen:${observation}")
                    Spacer(Modifier.height(16.dp))

                    // Botões de Navegação e Exportação
                    Button(
                        onClick = {
                            navController.navigate("home")
                            viewModel.clearStates()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nova Análise")

                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            navController.navigate("classificationToDiscount")
                            viewModel.clearStates()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calcular Desconto")

                    }
                    Button(onClick = {
                        // Não é mais necessário chamar loadLastUsedLimit() aqui, pois já foi feito no LaunchedEffect
                        lastUsedLimit?.let{
                            viewModel.exportClassification(context, classification!!, lastUsedLimit!!)
                        }
                    }) {
                        Text("Exportar PDF")
                    }
                }
            }
        }

        // --- DIÁLOGO DE LIMITES ---
        if (showLimitsDialog) {
            AlertDialog(
                onDismissRequest = { showLimitsDialog = false },
                title = {
                    Text(
                        "Limites de Classificação",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                text = {
                    // Coluna com Scroll para garantir que a tabela caiba no diálogo
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState()) // <-- SCROLL NO DIÁLOGO
                            .padding(vertical = 8.dp)
                    ) {
                        if (lastUsedLimit != null) {
                            UsedLimitTable(
                                lastUsedLimit!!,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            // Mensagem enquanto carrega ou se falhar
                            Text("Carregando limites...")
                            // Otimização: Força o carregamento novamente se estiver faltando, mas só no Main
                            LaunchedEffect(Unit) {
                                viewModel.loadLastUsedLimit()
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = { showLimitsDialog = false },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("Fechar")
                    }
                },
                // Removido o padding do modifier do AlertDialog para dar mais espaço ao conteúdo
            )
        }
    }
}