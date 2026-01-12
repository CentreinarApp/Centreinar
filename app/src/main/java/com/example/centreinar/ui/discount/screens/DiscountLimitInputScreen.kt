package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel

@Composable
fun DiscountLimitInputScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val defaultLimits by viewModel.defaultLimits.collectAsStateWithLifecycle()

    // Pega o grão atual para decidir o layout e a navegação
    val currentGrain = viewModel.selectedGrain
    val isSoja = currentGrain == "Soja"

    // RESETAR ESTADOS
    var impurities by remember(currentGrain) { mutableStateOf("") }
    var moisture by remember(currentGrain) { mutableStateOf("") }
    var brokenCrackedDamaged by remember(currentGrain) { mutableStateOf("") }
    var greenish by remember(currentGrain) { mutableStateOf("") }
    var burnt by remember(currentGrain) { mutableStateOf("") }
    var burntOrSour by remember(currentGrain) { mutableStateOf("") }
    var moldy by remember(currentGrain) { mutableStateOf("") }
    var spoiled by remember(currentGrain) { mutableStateOf("") }
    // Campo extra para Milho
    var carunchado by remember(currentGrain) { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var defaultsSet by remember(currentGrain) { mutableStateOf(false) }

    val isEditable = viewModel.isOfficial != true

    // Focus requesters
    val impuritiesFocus = remember { FocusRequester() }
    val moistureFocus = remember { FocusRequester() }
    val brokenFocus = remember { FocusRequester() }
    val greenishFocus = remember { FocusRequester() }
    val burntFocus = remember { FocusRequester() }
    val burntOrSourFocus = remember { FocusRequester() }
    val moldyFocus = remember { FocusRequester() }
    val spoiledFocus = remember { FocusRequester() }
    val carunchadoFocus = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // Carrega limites ao entrar
    LaunchedEffect(Unit) {
        viewModel.loadDefaultLimits()
        impuritiesFocus.requestFocus()
    }

    // Preenche os campos quando os limites chegam
    LaunchedEffect(defaultLimits) {
        if (defaultLimits != null && !defaultsSet) {
            impurities = defaultLimits?.get("impuritiesUpLim")?.toString() ?: ""
            moisture = defaultLimits?.get("moistureUpLim")?.toString() ?: ""
            brokenCrackedDamaged = defaultLimits?.get("brokenUpLim")?.toString() ?: ""
            moldy = defaultLimits?.get("moldyUpLim")?.toString() ?: ""

            // Mapeamento Milho: Ardido usa o campo burntOrSour
            burntOrSour = defaultLimits?.get("burntOrSourUpLim")?.toString() ?: ""

            // Mapeamento Milho: Carunchado
            carunchado = defaultLimits?.get("carunchadoUpLim")?.toString() ?: ""

            if (isSoja) {
                greenish = defaultLimits?.get("greenishUpLim")?.toString() ?: ""
                burnt = defaultLimits?.get("burntUpLim")?.toString() ?: ""
                spoiled = defaultLimits?.get("spoiledTotalUpLim")?.toString() ?: ""
            } else {
                greenish = "0"
                burnt = "0"
                // Se Milho tiver limite de Avariados Total, usa spoiled, se não 0
                spoiled = defaultLimits?.get("spoiledTotalUpLim")?.toString() ?: "0"
            }
            defaultsSet = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(scrollState)
    ) {
        val title = if (isEditable) "Insira os limites ($currentGrain)" else "Limites de tolerância ($currentGrain)"
        Text(
            title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(24.dp))

        // --- CAMPOS COMUNS ---
        NumberInputField(
            value = impurities,
            onValueChange = { impurities = it },
            label = "Matéria estranha e Impurezas (%)",
            focusRequester = impuritiesFocus,
            nextFocus = moistureFocus,
            enabled = isEditable
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = moisture,
            onValueChange = { moisture = it },
            label = "Umidade (%)",
            focusRequester = moistureFocus,
            nextFocus = if (isSoja) burntFocus else burntOrSourFocus,
            enabled = isEditable
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- CAMPOS ESPECÍFICOS SOJA ---
        if (isSoja) {
            NumberInputField(
                value = burnt,
                onValueChange = { burnt = it },
                label = "Queimados (%)",
                focusRequester = burntFocus,
                nextFocus = burntOrSourFocus,
                enabled = isEditable
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- ARDIDOS (Milho) / QUEIMADOS E ARDIDOS (Soja) ---
        NumberInputField(
            value = burntOrSour,
            onValueChange = { burntOrSour = it },
            label = if (isSoja) "Ardidos e Queimados (%)" else "Ardidos (%)",
            focusRequester = burntOrSourFocus,
            nextFocus = moldyFocus,
            enabled = isEditable
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = moldy,
            onValueChange = { moldy = it },
            label = "Mofados (%)",
            focusRequester = moldyFocus,
            nextFocus = if (!isSoja) carunchadoFocus else spoiledFocus,
            enabled = isEditable
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- CAMPO ESPECÍFICO MILHO: CARUNCHADO ---
        if (!isSoja) {
            NumberInputField(
                value = carunchado,
                onValueChange = { carunchado = it },
                label = "Carunchados (%)",
                focusRequester = carunchadoFocus,
                nextFocus = spoiledFocus,
                enabled = isEditable
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // --- TOTAL AVARIADOS ---
        // Exibe para Soja e para Milho
        NumberInputField(
            value = spoiled,
            onValueChange = { spoiled = it },
            label = "Total de Avariados (%)",
            focusRequester = spoiledFocus,
            nextFocus = if (isSoja) greenishFocus else brokenFocus,
            enabled = isEditable
        )
        Spacer(modifier = Modifier.height(16.dp))

        // --- ESVERDEADOS (Só Soja) ---
        if (isSoja) {
            NumberInputField(
                value = greenish,
                onValueChange = { greenish = it },
                label = "Esverdeados (%)",
                focusRequester = greenishFocus,
                nextFocus = brokenFocus,
                enabled = isEditable
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        NumberInputField(
            value = brokenCrackedDamaged,
            onValueChange = { if (isEditable) brokenCrackedDamaged = it },
            label = if(isSoja) "Partidos, Quebrados e Amassados (%)" else "Quebrados (%)",
            focusRequester = brokenFocus,
            nextFocus = null,
            onDone = { keyboardController?.hide() },
            enabled = isEditable
        )

        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                // Validação
                val allFields = mutableListOf(impurities, moisture, brokenCrackedDamaged, burntOrSour, moldy)
                if (isSoja) {
                    allFields.add(greenish)
                    allFields.add(burnt)
                    allFields.add(spoiled)
                } else {
                    // Valida campos do Milho
                    allFields.add(carunchado)
                    allFields.add(spoiled)
                }

                if (allFields.any { it.isEmpty() || it == "." }) {
                    errorMessage = "Por favor preencha todos os campos com valores válidos"
                    return@Button
                }

                if(isEditable){
                    try {
                        viewModel.setLimit(
                            impurities = toFloat(impurities),
                            moisture = toFloat(moisture),
                            brokenCrackedDamaged = toFloat(brokenCrackedDamaged),
                            greenish = if(isSoja) toFloat(greenish) else 0f,
                            burnt = if(isSoja) toFloat(burnt) else 0f,
                            burntOrSour = toFloat(burntOrSour), // Ardido
                            moldy = toFloat(moldy),
                            spoiled = toFloat(spoiled)
                        )
                    } catch (e: NumberFormatException) {
                        errorMessage = "Valores numéricos inválidos detectados"
                        return@Button
                    }
                }

                if (isSoja) {
                    navController.navigate("discount")
                } else {
                    navController.navigate("milhoDiscountInput")
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if(isEditable) "Salvar Limites" else "Próximo")
        }
    }
}

@Composable
private fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester? = null,
    onDone: (() -> Unit)? = null,
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
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

private fun toFloat(value : String): Float {
    return value.toFloatOrNull() ?: 0f
}