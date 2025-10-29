package com.example.centreinar.ui.classificationProcess.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun LimitInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val defaultLimits by viewModel.defaultLimits.collectAsStateWithLifecycle()
    val currentGrain = viewModel.selectedGrain
    val currentGroup = viewModel.selectedGroup

    var impurities by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var greenish by remember { mutableStateOf("") }
    var burnt by remember { mutableStateOf("") }
    var burntOrSour by remember { mutableStateOf("") }
    var moldy by remember { mutableStateOf("") }
    var spoiled by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var defaultsSet by remember { mutableStateOf(false) }
    val isEditable = viewModel.isOfficial != true

    val impuritiesFocus = remember { FocusRequester() }
    val moistureFocus = remember { FocusRequester() }
    val brokenFocus = remember { FocusRequester() }
    val greenishFocus = remember { FocusRequester() }
    val burntFocus = remember { FocusRequester() }
    val burntOrSourFocus = remember { FocusRequester() }
    val moldyFocus = remember { FocusRequester() }
    val spoiledFocus = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(currentGrain, currentGroup) {
        if (currentGrain != null && currentGroup != null) {
            Log.d("LimiteDebug", "Carregando limites para: $currentGrain, Grupo $currentGroup")
            viewModel.loadDefaultLimits()
            impuritiesFocus.requestFocus()
        } else {
            Log.d("LimiteDebug", "Grão/Grupo não definidos ainda")
        }
    }

    LaunchedEffect(defaultLimits) {
        if (defaultLimits != null && !defaultsSet) {
            impurities = defaultLimits?.get("impuritiesUpLim")?.toString() ?: ""
            moisture = defaultLimits?.get("moistureUpLim")?.toString() ?: ""
            brokenCrackedDamaged = defaultLimits?.get("brokenUpLim")?.toString() ?: ""
            greenish = defaultLimits?.get("greenishUpLim")?.toString() ?: ""
            burnt = defaultLimits?.get("burntUpLim")?.toString() ?: ""
            burntOrSour = defaultLimits?.get("burntOrSourUpLim")?.toString() ?: ""
            moldy = defaultLimits?.get("moldyUpLim")?.toString() ?: ""
            spoiled = defaultLimits?.get("spoiledTotalUpLim")?.toString() ?: ""
            defaultsSet = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Box(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
            Column {
                Text(
                    if (isEditable) "Insira os limites de tolerância" else "Limites de tolerância",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(24.dp))

                NumberInputField(
                    label = "Matéria estranha e Impurezas (%)",
                    value = impurities,
                    onValueChange = { impurities = it },
                    focusRequester = impuritiesFocus,
                    nextFocus = moistureFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                NumberInputField(
                    label = "Umidade (%)",
                    value = moisture,
                    onValueChange = { moisture = it },
                    focusRequester = moistureFocus,
                    nextFocus = burntFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                NumberInputField(
                    label = "Queimados (%)",
                    value = burnt,
                    onValueChange = { burnt = it },
                    focusRequester = burntFocus,
                    nextFocus = burntOrSourFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                NumberInputField(
                    label = "Ardidos e Queimados (%)",
                    value = burntOrSour,
                    onValueChange = { burntOrSour = it },
                    focusRequester = burntOrSourFocus,
                    nextFocus = moldyFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                NumberInputField(
                    label = "Mofados (%)",
                    value = moldy,
                    onValueChange = { moldy = it },
                    focusRequester = moldyFocus,
                    nextFocus = spoiledFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                NumberInputField(
                    label = "Total de Avariados (%)",
                    value = spoiled,
                    onValueChange = { spoiled = it },
                    focusRequester = spoiledFocus,
                    nextFocus = greenishFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                NumberInputField(
                    label = "Esverdeados (%)",
                    value = greenish,
                    onValueChange = { greenish = it },
                    focusRequester = greenishFocus,
                    nextFocus = brokenFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                NumberInputField(
                    label = "Partidos, Quebrados e Amassados (%)",
                    value = brokenCrackedDamaged,
                    onValueChange = { if (isEditable) brokenCrackedDamaged = it },
                    focusRequester = brokenFocus,
                    nextFocus = null,
                    onDone = { keyboardController?.hide() },
                    enabled = isEditable
                )

                errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 8.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val allFields = listOf(impurities, brokenCrackedDamaged, greenish, burnt, burntOrSour, moldy, spoiled)
                if (allFields.any { it.isEmpty() || it == "." }) {
                    errorMessage = "Por favor, preencha todos os campos."
                    return@Button
                }
                if (isEditable) {
                    try {
                        viewModel.setLimit(
                            impurities = impurities.toFloatOrDefault(),
                            moisture = moisture.toFloatOrDefault(),
                            brokenCrackedDamaged = brokenCrackedDamaged.toFloatOrDefault(),
                            greenish = greenish.toFloatOrDefault(),
                            burnt = burnt.toFloatOrDefault(),
                            burntOrSour = burntOrSour.toFloatOrDefault(),
                            moldy = moldy.toFloatOrDefault(),
                            spoiled = spoiled.toFloatOrDefault()
                        )
                    } catch (e: NumberFormatException) {
                        errorMessage = "Valores numéricos inválidos."
                    }
                }
                navController.navigate("disqualification")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (isEditable) "Enviar Limites" else "Próximo")
        }
    }
}

@Composable
private fun NumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester?,
    enabled: Boolean,
    onDone: (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            // Permite apenas dígitos e ponto
            if (it.isEmpty() || it.matches(Regex("^(\\d*\\.?\\d*)$"))) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { nextFocus?.requestFocus() },
            onDone = { onDone?.invoke() }
        ),
        singleLine = true,
        enabled = enabled,
        readOnly = !enabled
    )
}
//oi

private fun String.toFloatOrDefault(): Float = this.toFloatOrNull()?.takeIf { it > 0f } ?: 100f
