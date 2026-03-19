package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.domain.model.ToxicSeedDetail
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.util.Routes

@Composable
fun DisqualificationScreen(
    navController: NavController,
    classificationId: Int,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

        var badConservation    by remember { mutableStateOf(false) }
        var strangeSmell       by remember { mutableStateOf(false) }
        var insects            by remember { mutableStateOf(false) }
        var toxicGrains        by remember { mutableStateOf(false) }
        var toxicSeedDetails   by remember { mutableStateOf(listOf<ToxicSeedDetail>()) }
        var toxicTypesQuantity by remember { mutableStateOf("0") }
        var toxicSeedError     by remember { mutableStateOf<String?>(null) }

        val typesQuantity  = toxicTypesQuantity.toIntOrNull() ?: 0
        val focusManager   = LocalFocusManager.current
        val typesCountFocus = remember { FocusRequester() }

        fun updateSeedDetail(index: Int, newName: String? = null, newQuantity: String? = null) {
            if (index < 0 || index >= toxicSeedDetails.size) return
            val list    = toxicSeedDetails.toMutableList()
            val current = list[index]
            list[index] = ToxicSeedDetail(newName ?: current.first, newQuantity ?: current.second)
            toxicSeedDetails = list.toList()
        }

        LaunchedEffect(typesQuantity) {
            val currentSize = toxicSeedDetails.size
            when {
                typesQuantity > currentSize -> {
                    toxicSeedDetails = toxicSeedDetails +
                            List(typesQuantity - currentSize) { ToxicSeedDetail("", "0") }
                }
                typesQuantity < currentSize -> {
                    toxicSeedDetails = toxicSeedDetails.take(typesQuantity)
                }
            }
        }

        // Cria pares de FocusRequester para cada tipo de semente: [nome, quantidade]
        // Recriado quando typesQuantity muda
        val seedFocusRequesters = remember(typesQuantity) {
            List(typesQuantity) { FocusRequester() to FocusRequester() }
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color    = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                    Text(
                        "O lote apresenta:",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Spacer(Modifier.height(24.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = badConservation, onCheckedChange = { badConservation = it })
                        Text("Mal estado de conservação?", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = strangeSmell, onCheckedChange = { strangeSmell = it })
                        Text("Cheiro estranho?", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = insects, onCheckedChange = { insects = it })
                        Text("Insetos vivos ou mortos?", color = MaterialTheme.colorScheme.onBackground)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked       = toxicGrains,
                            onCheckedChange = {
                                toxicGrains = it
                                if (!it) {
                                    toxicTypesQuantity = "0"
                                    toxicSeedDetails   = emptyList()
                                }
                            }
                        )
                        Text("Sementes tóxicas?", color = MaterialTheme.colorScheme.onBackground)
                    }

                    if (toxicGrains) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            "Detalhes dos Tipos de Sementes Tóxicas",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = MaterialTheme.colorScheme.primary
                            )
                        )
                        Spacer(Modifier.height(16.dp))

                        // Campo de quantidade de tipos — Next vai para o nome do tipo 1 (se existir)
                        DisqNumberField(
                            value          = toxicTypesQuantity,
                            onValueChange  = { if ((it.toIntOrNull() ?: 0) >= 0) toxicTypesQuantity = it },
                            label          = "Quantidade de TIPOS Encontrados",
                            focusRequester = typesCountFocus,
                            nextFocus      = seedFocusRequesters.firstOrNull()?.first,
                            imeAction      = if (typesQuantity > 0) ImeAction.Next else ImeAction.Done,
                            onDone         = { focusManager.clearFocus() }
                        )

                        Spacer(Modifier.height(24.dp))

                        if (typesQuantity > 0) {
                            Text("Detalhes por Tipo:", style = MaterialTheme.typography.titleSmall)
                            Spacer(Modifier.height(8.dp))

                            repeat(typesQuantity) { index ->
                                val currentDetail = toxicSeedDetails.getOrElse(index) {
                                    ToxicSeedDetail("", "0")
                                }
                                val (nameFocus, qtyFocus) = seedFocusRequesters[index]

                                // Próximo foco: quantidade deste tipo → nome do próximo tipo
                                // No último tipo, quantidade → Done
                                val nextNameFocus = seedFocusRequesters.getOrNull(index + 1)?.first
                                val isLastField   = index == typesQuantity - 1

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 16.dp)
                                ) {
                                    Text("Tipo ${index + 1}", style = MaterialTheme.typography.bodyLarge)

                                    // Campo nome — Next vai para quantidade deste tipo
                                    OutlinedTextField(
                                        value         = currentDetail.first,
                                        onValueChange = { updateSeedDetail(index, newName = it) },
                                        label         = { Text("Qual semente? (Ex: Mamona)") },
                                        modifier      = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp)
                                            .focusRequester(nameFocus),
                                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                        keyboardActions = KeyboardActions(
                                            onNext = { qtyFocus.requestFocus() }
                                        ),
                                        singleLine = true
                                    )

                                    // Campo quantidade — Next vai para nome do próximo tipo (ou Done)
                                    DisqNumberField(
                                        value          = currentDetail.second,
                                        onValueChange  = { updateSeedDetail(index, newQuantity = it) },
                                        label          = "Quantas unidades?",
                                        focusRequester = qtyFocus,
                                        nextFocus      = nextNameFocus,
                                        imeAction      = if (isLastField) ImeAction.Done else ImeAction.Next,
                                        onDone         = { focusManager.clearFocus() }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))
                }

                toxicSeedError?.let {
                    Text(
                        text  = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick  = {
                        if (toxicGrains && toxicSeedDetails.any { (it.second.toIntOrNull() ?: 0) <= 0 }) {
                            toxicSeedError = "Informe a quantidade de cada semente tóxica."
                            return@Button
                        }
                        toxicSeedError = null

                        viewModel.saveDisqualificationData(
                            classificationId = classificationId,
                            badConservation  = if (badConservation) 1 else 0,
                            strangeSmell     = if (strangeSmell)    1 else 0,
                            insects          = if (insects)         1 else 0,
                            toxicGrains      = if (toxicGrains)     1 else 0,
                            toxicSeeds       = toxicSeedDetails,
                            onSuccess        = { navController.navigate(Routes.CLASSIFICATION_INPUT) }
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar")
                }
            }
        }
    }
}

// =============================================================================
// CAMPO NUMÉRICO COM FOCO E IMEACTION CONFIGURÁVEIS
// O valor "0" funciona como placeholder: some ao focar, volta ao desfocar vazio.
// =============================================================================

@Composable
private fun DisqNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester?,
    imeAction: ImeAction,
    onDone: () -> Unit = {}
) {
    var isFocused    by remember { mutableStateOf(false) }
    val displayValue  = if (isFocused && value == "0") "" else value

    OutlinedTextField(
        value         = displayValue,
        onValueChange = { raw ->
            when {
                raw.isEmpty() -> onValueChange("0")
                raw.matches(Regex("^(\\d*)$")) -> {
                    // Remove zero à esquerda: "01" → "1"
                    onValueChange(raw.trimStart('0').ifEmpty { "0" })
                }
            }
        },
        label           = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction    = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { nextFocus?.requestFocus() },
            onDone = { onDone() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                isFocused = state.isFocused
                if (!state.isFocused && value.isEmpty()) onValueChange("0")
            },
        singleLine = true
    )
}