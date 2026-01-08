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

    // Identifica se é Soja para mostrar/ocultar campos específicos
    val isSoja = currentGrain == "Soja"

    var impurities by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var moisture by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var brokenCrackedDamaged by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var greenish by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var burnt by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var burntOrSour by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var moldy by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var spoiled by remember(currentGrain, currentGroup) { mutableStateOf("") } // Avariados Total

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var defaultsSet by remember(currentGrain, currentGroup) { mutableStateOf(false) }
    val isEditable = viewModel.isOfficial != true

    // Focus Requesters
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

    // Carrega os limites ao entrar na tela
    LaunchedEffect(currentGrain, currentGroup) {
        if (currentGrain != null) {
            Log.d("LimiteDebug", "Carregando limites para: $currentGrain, Grupo $currentGroup")
            viewModel.loadDefaultLimits()
        }
    }

    // Preenche os campos quando os limites padrão chegam do Banco de Dados
    LaunchedEffect(defaultLimits) {
        if (defaultLimits != null && !defaultsSet) {
            impurities = defaultLimits?.get("impuritiesUpLim")?.toString() ?: ""
            moisture = defaultLimits?.get("moistureUpLim")?.toString() ?: ""
            brokenCrackedDamaged = defaultLimits?.get("brokenUpLim")?.toString() ?: ""

            // Campos comuns
            moldy = defaultLimits?.get("moldyUpLim")?.toString() ?: ""
            spoiled = defaultLimits?.get("spoiledTotalUpLim")?.toString() ?: ""

            // Campos específicos ou mapeados diferente
            burnt = defaultLimits?.get("burntUpLim")?.toString() ?: ""

            if (isSoja) {
                greenish = defaultLimits?.get("greenishUpLim")?.toString() ?: ""
                burntOrSour = defaultLimits?.get("burntOrSourUpLim")?.toString() ?: ""
            } else {
                // Para Milho, se não houver mapeamento direto, deixar vazio ou 0
                greenish = "0"
                burntOrSour = defaultLimits?.get("ardidosUpLim")?.toString() ?: "" // Exemplo: mapear ardidos aqui se necessário
            }

            defaultsSet = true
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(32.dp)) {
        Box(modifier = Modifier.weight(1f).verticalScroll(scrollState)) {
            Column {
                Text(
                    if (isEditable) "Insira os limites de tolerância ($currentGrain)" else "Limites de tolerância ($currentGrain)",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(24.dp))

                // --- CAMPOS COMUNS (SOJA E MILHO) ---

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
                    nextFocus = if(isSoja) burntFocus else burntOrSourFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                // --- CAMPOS ESPECÍFICOS DE SOJA ---
                if (isSoja) {
                    NumberInputField(
                        label = "Queimados (%)",
                        value = burnt,
                        onValueChange = { burnt = it },
                        focusRequester = burntFocus,
                        nextFocus = burntOrSourFocus,
                        enabled = isEditable
                    )
                    Spacer(Modifier.height(16.dp))
                }

                // Para Milho, usamos este campo como "Ardidos" ou mantemos a lógica de Ardidos/Queimados
                NumberInputField(
                    label = if(isSoja) "Ardidos e Queimados (%)" else "Ardidos (%)",
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
                    nextFocus = if (isSoja) greenishFocus else brokenFocus,
                    enabled = isEditable
                )
                Spacer(Modifier.height(16.dp))

                // "Esverdeados" geralmente só se aplica a Soja
                if (isSoja) {
                    NumberInputField(
                        label = "Esverdeados (%)",
                        value = greenish,
                        onValueChange = { greenish = it },
                        focusRequester = greenishFocus,
                        nextFocus = brokenFocus,
                        enabled = isEditable
                    )
                    Spacer(Modifier.height(16.dp))
                }

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
                // Validação dinâmica: Só valida "Esverdeados" e "Queimados" se for Soja
                val fieldsToCheck = mutableListOf(impurities, moisture, brokenCrackedDamaged, burntOrSour, moldy, spoiled)
                if (isSoja) {
                    fieldsToCheck.add(greenish)
                    fieldsToCheck.add(burnt)
                }

                if (fieldsToCheck.any { it.isEmpty() || it == "." }) {
                    errorMessage = "Por favor, preencha todos os campos obrigatórios."
                    return@Button
                }

                if (isEditable) {
                    try {
                        viewModel.setLimit(
                            impurities = impurities.toFloatOrDefault(),
                            moisture = moisture.toFloatOrDefault(),
                            brokenCrackedDamaged = brokenCrackedDamaged.toFloatOrDefault(),
                            // Se não for Soja, passa 0 para os campos ocultos
                            greenish = if (isSoja) greenish.toFloatOrDefault() else 0f,
                            burnt = if (isSoja) burnt.toFloatOrDefault() else 0f,
                            burntOrSour = burntOrSour.toFloatOrDefault(),
                            moldy = moldy.toFloatOrDefault(),
                            spoiled = spoiled.toFloatOrDefault()
                        )
                    } catch (e: NumberFormatException) {
                        errorMessage = "Valores numéricos inválidos."
                    }
                }

                // Navegação pode ser diferente dependendo do grão se necessário,
                // mas se ambos vão para 'disqualification', mantém assim.
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

private fun String.toFloatOrDefault(): Float = this.toFloatOrNull()?.takeIf { it >= 0f } ?: 0f