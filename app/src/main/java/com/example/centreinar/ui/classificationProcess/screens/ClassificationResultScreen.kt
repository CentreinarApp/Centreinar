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
import com.example.centreinar.ColorClassification
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
){
    val classification by viewModel.classification.collectAsState()
    val defaultLimit by viewModel.defaultLimits.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    var showLimitsDialog by remember { mutableStateOf(false) }
    val lastUsedLimit by viewModel.lastUsedLimit.collectAsStateWithLifecycle()
    val context = LocalContext.current

    //val doesDefineColorClass = viewModel.doesDefineColorClass
    var colorClassificationResult by remember { mutableStateOf<ColorClassification?>(null) }
    var observation by remember { mutableStateOf<String?>(null) }


    // When doesDefineColorClass becomes true, call the suspend function
    if (viewModel.doesDefineColorClass == true) {
        LaunchedEffect(Unit) {
            colorClassificationResult = viewModel.getClassColor()
            observation = viewModel.getObservations(colorClassificationResult)
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when {
                isLoading -> {
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
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth()
                    )
                }

                classification != null -> {

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

                    Text(
                        text = "Ver limites utilizados",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable { showLimitsDialog = true }
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
                        viewModel.loadLastUsedLimit()
                        lastUsedLimit?.let{
                            viewModel.exportClassification(context, classification!!, lastUsedLimit!!)
                        }
                    }) {
                        Text("Exportar PDF")
                    }
                }
            }
        }
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
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                    ) {
                        viewModel.loadLastUsedLimit()
                        lastUsedLimit?.let {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            ) {
                                UsedLimitTable(
                                    it,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = { showLimitsDialog = false },
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        Text("Fechar")
                    }
                },
                modifier = Modifier
                    .padding(24.dp)  // Reduced dialog padding
            )
        }
    }
}