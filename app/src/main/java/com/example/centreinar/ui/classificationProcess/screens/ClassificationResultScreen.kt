package com.example.centreinar.ui.classificationProcess.screens

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
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

@Composable
fun ClassificationResult(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val classification by viewModel.classification.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showLimitsDialog by remember { mutableStateOf(false) }

    val lastUsedLimit by viewModel.lastUsedLimit.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var colorClassificationResult by remember { mutableStateOf<ColorClassificationSoja?>(null) }
    var observation by remember { mutableStateOf<String?>(null) }

    if (viewModel.doesDefineColorClass == true) {
        LaunchedEffect(Unit) {
            colorClassificationResult = viewModel.getClassColor()
            observation = viewModel.getObservations(colorClassificationResult)
        }
    }

    LaunchedEffect(classification) {
        if (classification != null) {
            viewModel.loadLastUsedLimit()
        }
    }

    // Root column
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Área rolável principal: usamos weight(1f) + verticalScroll
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                when {
                    isLoading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }

                    error != null -> {
                        Text(
                            text = error!!,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .padding(top = 16.dp)
                                .fillMaxWidth()
                        )
                    }

                    classification != null -> {
                        val finalTypeLabel = viewModel.getFinalTypeLabel(classification!!.finalType)

                        if (viewModel.doesDefineColorClass == true && colorClassificationResult != null) {
                            SimplifiedResultsTable(
                                finalTypeLabel,
                                colorClassificationResult!!.framingClass,
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            SimplifiedResultsTable(
                                finalTypeLabel,
                                "Não Definida",
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(Modifier.height(16.dp))
                        ClassificationTable(classification!!, modifier = Modifier.fillMaxWidth())
                        Spacer(Modifier.height(10.dp))

                        Text(
                            text = "Ver limites utilizados",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { showLimitsDialog = true }
                                .padding(vertical = 8.dp)
                        )

                        if (viewModel.doesDefineColorClass == true && colorClassificationResult != null) {
                            ClassColorResult(colorClassificationResult!!, modifier = Modifier.fillMaxWidth())
                        }

                        observation?.let {
                            ObservationCard(it, modifier = Modifier.fillMaxWidth())
                        }

                        Log.d("Observations", "onScreen: $observation")

                        Spacer(Modifier.height(16.dp))

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

                        Spacer(Modifier.height(8.dp))

                        Button(
                            onClick = {
                                lastUsedLimit?.let {
                                    viewModel.exportClassification(context, classification!!, it)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Exportar PDF")
                        }
                    }
                }
            }
        }

        // Diálogo de limites: limitamos a altura do conteúdo rolável dentro do diálogo
        if (showLimitsDialog) {
            AlertDialog(
                onDismissRequest = { showLimitsDialog = false },
                title = { Text("Limites de Classificação", style = MaterialTheme.typography.titleLarge) },
                text = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        if (lastUsedLimit != null) {
                            UsedLimitTable(lastUsedLimit!!, modifier = Modifier.fillMaxWidth())
                        } else {
                            Text("Carregando limites...")
                            LaunchedEffect(Unit) { viewModel.loadLastUsedLimit() }
                        }
                    }
                },
                //oi
                confirmButton = {
                    TextButton(onClick = { showLimitsDialog = false }) { Text("Fechar") }
                }
            )
        }
    }
}
