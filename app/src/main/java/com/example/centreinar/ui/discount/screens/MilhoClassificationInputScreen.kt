package com.example.centreinar.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import java.math.RoundingMode

@Composable
fun MilhoClassificationInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        var lotWeight by remember { mutableStateOf("") }
        var sampleWeight by remember { mutableStateOf("") }
        var impurities by remember { mutableStateOf("") }
        var broken by remember { mutableStateOf("") }
        var ardido by remember { mutableStateOf("") }
        var mofado by remember { mutableStateOf("") }
        var carunchado by remember { mutableStateOf("") }
        var fermented by remember { mutableStateOf("") }
        var germinated by remember { mutableStateOf("") }
        var immature by remember { mutableStateOf("") }
        var gessado by remember { mutableStateOf("") }

        val lotFocus = remember { FocusRequester() }
        val sampleFocus = remember { FocusRequester() }
        val impuritiesFocus = remember { FocusRequester() }
        val brokenFocus = remember { FocusRequester() }

        val keyboardController = LocalSoftwareKeyboardController.current

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Text(
                "Classificação — Milho",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(16.dp)
            )
            LazyColumn(modifier = Modifier.padding(16.dp)) {
                item {
                    NumberField(
                        "Peso do lote (kg)",
                        lotWeight,
                        { lotWeight = it },
                        lotFocus,
                        sampleFocus
                    )
                }
                item {
                    NumberField(
                        "Peso da amostra (g)",
                        sampleWeight,
                        { sampleWeight = it },
                        sampleFocus,
                        impuritiesFocus
                    )
                }
                item {
                    NumberField(
                        "Matéria estranha e Impurezas (g)",
                        impurities,
                        { impurities = it },
                        impuritiesFocus,
                        brokenFocus
                    )
                }
                item {
                    NumberField(
                        "Partidos/Quebrados (g)",
                        broken,
                        { broken = it },
                        brokenFocus,
                        null
                    )
                }
                item {
                    Spacer(Modifier.height(8.dp))
                    NumberField("Ardidos (g)", ardido, { ardido = it }, FocusRequester(), null)
                    NumberField("Mofados (g)", mofado, { mofado = it }, FocusRequester(), null)
                    NumberField(
                        "Carunchado (g)",
                        carunchado,
                        { carunchado = it },
                        FocusRequester(),
                        null
                    )
                }
                item {
                    NumberField(
                        "Fermentados (g)",
                        fermented,
                        { fermented = it },
                        FocusRequester(),
                        null
                    )
                    NumberField(
                        "Germinados (g)",
                        germinated,
                        { germinated = it },
                        FocusRequester(),
                        null
                    )
                    NumberField("Imaturos (g)", immature, { immature = it }, FocusRequester(), null)
                    NumberField("Gessado (g)", gessado, { gessado = it }, FocusRequester(), null)
                }
            }

            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                // build SampleMilho similar to your entity structure
                val sample = SampleMilho(
                    grain = "milho",
                    group = viewModel.selectedGroup ?: 1,
                    lotWeight = lotWeight.toFloatOrNull() ?: 0f,
                    sampleWeight = sampleWeight.toFloatOrNull() ?: 0f,
                    cleanWeight = sampleWeight.toFloatOrNull() ?: 0f,
                    impurities = impurities.toFloatOrNull() ?: 0f,
                    broken = broken.toFloatOrNull() ?: 0f,
                    carunchado = carunchado.toFloatOrNull() ?: 0f,
                    ardido = ardido.toFloatOrNull() ?: 0f,
                    mofado = mofado.toFloatOrNull() ?: 0f,
                    fermented = fermented.toFloatOrNull() ?: 0f,
                    germinated = germinated.toFloatOrNull() ?: 0f,
                    immature = immature.toFloatOrNull() ?: 0f,
                    gessado = gessado.toFloatOrNull() ?: 0f
                )

                // classify using repository via ViewModel
                viewModel.classifySample(sample)
                navController.navigate("milhoClassificationResult")
            }, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("Classificar")
            }
        }
    }
}

@Composable
private fun NumberField(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester?
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    OutlinedTextField(
        value = value,
        onValueChange = { onChange(sanitizeFloatInput(it)) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp).focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done),
        keyboardActions = KeyboardActions(onNext = { nextFocus?.requestFocus() }, onDone = { keyboardController?.hide() }),
        singleLine = true
    )
}

private fun sanitizeFloatInput(input: String): String {
    return input.filter { it.isDigit() || it == '.' }
}
