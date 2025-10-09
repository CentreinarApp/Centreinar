package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import android.util.Log // Adicionado para Log.d/e

@Composable
fun LimitInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // Collect default limits from ViewModel
    val defaultLimits by viewModel.defaultLimits.collectAsStateWithLifecycle()

    // --- NOVO: Obtenha os valores do ViewModel para reatividade ---
    val currentGrain = viewModel.selectedGrain
    val currentGroup = viewModel.selectedGroup

    // State variables
    var impurities by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var greenish by remember { mutableStateOf("") }
    var burnt by remember { mutableStateOf("") }
    var burntOrSour by remember { mutableStateOf("") }
    var moldy by remember { mutableStateOf("") }
    var spoiled by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var defaultsSet by remember { mutableStateOf(false) } // Track if defaults are set
    val isEditable = viewModel.isOfficial != true

    // Focus requesters (mantidos)
    val impuritiesFocus = remember { FocusRequester() }
    val moistureFocus= remember { FocusRequester() }
    val brokenFocus = remember { FocusRequester() }
    val greenishFocus = remember { FocusRequester() }
    val burntFocus = remember { FocusRequester() }
    val burntOrSourFocus = remember { FocusRequester() }
    val moldyFocus = remember { FocusRequester() }
    val spoiledFocus = remember { FocusRequester() }

    // Get keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    // 1. Defina o estado da rolagem
    val scrollState = rememberScrollState()

    // CORREÇÃO: LaunchedEffect agora reage a mudanças no Grão e Grupo
    LaunchedEffect(currentGrain, currentGroup) {
        if (currentGrain != null && currentGroup != null) {
            Log.d("LimiteDebug", "Parâmetros prontos. Carregando limites para: $currentGrain, Grupo $currentGroup")
            viewModel.loadDefaultLimits() // Chamado apenas quando Grão e Grupo existem
            impuritiesFocus.requestFocus()
        } else {
            Log.w("LimiteDebug", "Aguardando seleção de Grão/Grupo antes de carregar limites.")
        }
    }

    LaunchedEffect(defaultLimits) {
        if (defaultLimits != null && !defaultsSet) {
            impurities = defaultLimits?.get("impuritiesUpLim")?.toString() ?: ""
            moisture = defaultLimits?.get("moistureUpLim")?.toString()?:""
            brokenCrackedDamaged = defaultLimits?.get("brokenUpLim")?.toString() ?: ""
            greenish = defaultLimits?.get("greenishUpLim")?.toString() ?: ""
            burnt = defaultLimits?.get("burntUpLim")?.toString() ?: ""
            burntOrSour = defaultLimits?.get("burntOrSourUpLim")?.toString() ?: ""
            moldy = defaultLimits?.get("moldyUpLim")?.toString() ?: ""
            spoiled = defaultLimits?.get("spoiledTotalUpLim")?.toString() ?: ""
            defaultsSet = true
        }
    }

    // 2. Aplique a rolagem ao Column principal
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
            .verticalScroll(scrollState) // <-- APLICAÇÃO DA ROLAGEM
    ) {
        if(isEditable) {
            Text(
                "Insira os limites de tolerância",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        else{
            Text(
                "Limites de tolerância",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        // Impurities
        NumberInputField(
            value = impurities,
            onValueChange = { impurities = it },
            label = "Matéria estranha e Impurezas (%)",
            focusRequester = impuritiesFocus,
            nextFocus = moistureFocus,
            enabled = isEditable
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Moisture
        NumberInputField(
            value = moisture,
            onValueChange = { moisture = it },
            label = "Umidade (%)",
            focusRequester = moistureFocus,
            nextFocus = burntFocus,
            enabled = isEditable
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Burnt
        NumberInputField(
            value = burnt,
            onValueChange = { burnt = it },
            label = "Queimados (%)",
            focusRequester = burntFocus,
            nextFocus = burntOrSourFocus,
            enabled = isEditable
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Burnt or Sour
        NumberInputField(
            value = burntOrSour,
            onValueChange = { burntOrSour = it },
            label = "Ardidos e Queimados (%)",
            focusRequester = burntOrSourFocus,
            nextFocus = moldyFocus,
            enabled = isEditable
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Moldy
        NumberInputField(
            value = moldy,
            onValueChange = { moldy = it },
            label = "Mofados (%)",
            focusRequester = moldyFocus,
            nextFocus = spoiledFocus,
            enabled = isEditable
        )

        Spacer(modifier = Modifier.height(16.dp))

        // spoiled
        NumberInputField(
            value = spoiled,
            onValueChange = { spoiled = it },
            label = "Total de Avariados (%)",
            focusRequester = spoiledFocus,
            nextFocus = greenishFocus,
            enabled = isEditable
        )

        Spacer(modifier = Modifier.height(16.dp))

        // greenish
        NumberInputField(
            value = greenish,
            onValueChange = { greenish = it },
            label = "Esverdeados (%)",
            focusRequester = greenishFocus,
            nextFocus = brokenFocus,
            enabled = isEditable
        )

        Spacer(modifier = Modifier.height(16.dp))

        // broken
        NumberInputField(
            value = brokenCrackedDamaged,
            onValueChange = { if (isEditable) brokenCrackedDamaged = it },
            label = "Partidos, Quebrados e Amassados (%)",
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
                val allFields = listOf(
                    impurities, brokenCrackedDamaged, greenish,
                    burnt, burntOrSour, moldy, spoiled
                )

                // Validate all fields
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
                            greenish = toFloat(greenish),
                            burnt = toFloat(burnt),
                            burntOrSour = toFloat(burntOrSour),
                            moldy = toFloat(moldy),
                            spoiled = toFloat(spoiled)
                        )
                    } catch (e: NumberFormatException) {
                        errorMessage = "Valores numéricos inválidos detectados"
                    }
                }
                navController.navigate("disqualification")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            if(isEditable){
                Text("Enviar Limites")
            }
            else{
                Text("Próximo")
            }
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
    var valueFloat = value.toFloat()
    if(valueFloat == 0.0f){
        valueFloat = 100.0f
    }
    return valueFloat
}