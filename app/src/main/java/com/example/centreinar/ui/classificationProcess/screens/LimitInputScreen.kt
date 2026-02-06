package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun LimitInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        val defaultLimits by viewModel.defaultLimits.collectAsStateWithLifecycle()
        val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

        val currentGrain = viewModel.selectedGrain
        val currentGroup = viewModel.selectedGroup
        val isOfficial = viewModel.isOfficial == true
        val isSoja = currentGrain == "Soja"

        var moisture by remember(currentGrain, currentGroup) { mutableStateOf("") }
        var impurities by remember { mutableStateOf("") }
        var brokenCrackedDamaged by remember { mutableStateOf("") }
        var greenish by remember { mutableStateOf("") }
        var burnt by remember { mutableStateOf("") }
        var burntOrSour by remember { mutableStateOf("") }
        var moldy by remember { mutableStateOf("") }
        var spoiled by remember { mutableStateOf("") }

        var errorMessage by remember { mutableStateOf<String?>(null) }
        val moistureFocus = remember { FocusRequester() }
        val scrollState = rememberScrollState()

        LaunchedEffect(currentGrain, currentGroup) {
            if (currentGrain != null) {
                viewModel.loadDefaultLimits()
            }
        }

        LaunchedEffect(defaultLimits) {
            defaultLimits?.let { limits ->
                moisture = limits["moistureUpLim"]?.toString() ?: ""
                if (!isOfficial) {
                    impurities = limits["impuritiesUpLim"]?.toString() ?: ""
                    brokenCrackedDamaged = limits["brokenUpLim"]?.toString() ?: ""
                    moldy = limits["moldyUpLim"]?.toString() ?: ""
                    spoiled = limits["spoiledTotalUpLim"]?.toString() ?: ""
                    burnt = limits["burntUpLim"]?.toString() ?: ""
                    if (isSoja) {
                        greenish = limits["greenishUpLim"]?.toString() ?: ""
                        burntOrSour = limits["burntOrSourUpLim"]?.toString() ?: ""
                    } else {
                        burntOrSour = limits["ardidosUpLim"]?.toString() ?: ""
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = if (!isOfficial) "Insira os limites de tolerância" else "Limites de Referência MAPA",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (currentGrain == "Soja") "$currentGrain - Grupo $currentGroup" else "$currentGrain",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            NumberInputField(
                label = "Umidade da Amostra (%)",
                value = moisture,
                onValueChange = { moisture = it },
                focusRequester = moistureFocus,
                nextFocus = null,
                enabled = !isOfficial,
                readOnly = isOfficial
            )

            Spacer(modifier = Modifier.height(16.dp))


            Box(modifier = Modifier.weight(1f)) {
                if (isOfficial) {
                    if (allOfficialLimits.isNotEmpty()) {
                        OfficialLimitsTable(grain = currentGrain ?: "", data = allOfficialLimits)
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        EditableFields(
                            isSoja = isSoja,
                            impurities = impurities,
                            onImpuritiesChange = { impurities = it },
                            burnt = burnt,
                            onBurntChange = { burnt = it },
                            burntOrSour = burntOrSour,
                            onBurntOrSourChange = { burntOrSour = it },
                            moldy = moldy,
                            onMoldyChange = { moldy = it },
                            spoiled = spoiled,
                            onSpoiledChange = { spoiled = it },
                            greenish = greenish,
                            onGreenishChange = { greenish = it },
                            broken = brokenCrackedDamaged,
                            onBrokenChange = { brokenCrackedDamaged = it }
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))

            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (moisture.isEmpty() || moisture == ".") {
                        errorMessage = "Informe a umidade da amostra."
                        return@Button
                    }

                    if (!isOfficial) {
                        viewModel.setLimit(
                            impurities.toFloatOrDefault(), moisture.toFloatOrDefault(),
                            brokenCrackedDamaged.toFloatOrDefault(), greenish.toFloatOrDefault(),
                            burnt.toFloatOrDefault(), burntOrSour.toFloatOrDefault(),
                            moldy.toFloatOrDefault(), spoiled.toFloatOrDefault()
                        )
                    }
                    navController.navigate("disqualification")
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(if (!isOfficial) "Salvar e Continuar" else "Próximo")
            }
        }
    }
}

@Composable
fun OfficialLimitsTable(grain: String, data: List<Any>) {
    val labels = if (grain == "Soja") {
        listOf("Ardidos/Queim.", "Queimados", "Mofados", "Avariados Total", "Esverdeados", "Partidos/Quebr./Amassados", "Matérias Estranhas e Impurezas")
    } else {
        listOf("Ardidos", "Avariados Total", "Quebrados", "Matérias Estranhas e Impurezas", "Carunchados")
    }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            // CABEÇALHO
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text("Defeito", modifier = Modifier.weight(1.3f), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                data.forEachIndexed { index, _ ->
                    Text("Tipo ${index + 1}", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            labels.forEachIndexed { rowIndex, label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(1.3f),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    data.forEach { item ->
                        val value = when (item) {
                            is LimitSoja -> listOf(item.burntOrSourUpLim, item.burntUpLim, item.moldyUpLim, item.spoiledTotalUpLim, item.greenishUpLim, item.brokenCrackedDamagedUpLim, item.impuritiesUpLim)
                            is LimitMilho -> listOf(item.ardidoUpLim, item.spoiledTotalUpLim, item.brokenUpLim, item.impuritiesUpLim, item.carunchadoUpLim)
                            else -> emptyList()
                        }.getOrNull(rowIndex) ?: 0f

                        Text(
                            text = "$value%",
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (rowIndex < labels.lastIndex) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun EditableFields(
    isSoja: Boolean,
    impurities: String, onImpuritiesChange: (String) -> Unit,
    burnt: String, onBurntChange: (String) -> Unit,
    burntOrSour: String, onBurntOrSourChange: (String) -> Unit,
    moldy: String, onMoldyChange: (String) -> Unit,
    spoiled: String, onSpoiledChange: (String) -> Unit,
    greenish: String, onGreenishChange: (String) -> Unit,
    broken: String, onBrokenChange: (String) -> Unit
) {
    Column {
        NumberInputField("Impurezas (%)", impurities, onImpuritiesChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))
        if (isSoja) {
            NumberInputField("Queimados (%)", burnt, onBurntChange, FocusRequester(), null, true)
            Spacer(Modifier.height(8.dp))
        }
        NumberInputField(if (isSoja) "Ardidos e Queimados (%)" else "Ardidos (%)", burntOrSour, onBurntOrSourChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))
        NumberInputField("Mofados (%)", moldy, onMoldyChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))
        NumberInputField("Total Avariados (%)", spoiled, onSpoiledChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))
        if (isSoja) {
            NumberInputField("Esverdeados (%)", greenish, onGreenishChange, FocusRequester(), null, true)
            Spacer(Modifier.height(8.dp))
        }
        NumberInputField("Quebrados (%)", broken, onBrokenChange, FocusRequester(), null, true)
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
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.matches(Regex("^(\\d*\\.?\\d*)$"))) onValueChange(it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done),
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly
    )
}

private fun String.toFloatOrDefault(): Float = this.toFloatOrNull()?.takeIf { it >= 0f } ?: 0f