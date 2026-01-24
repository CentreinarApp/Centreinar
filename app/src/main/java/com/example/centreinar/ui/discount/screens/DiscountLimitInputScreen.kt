package com.example.centreinar.ui.discount.screens

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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
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
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel

@Composable
fun DiscountLimitInputScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val defaultLimits by viewModel.defaultLimits.collectAsStateWithLifecycle()
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

    val currentGrain = viewModel.selectedGrain
    val isSoja = currentGrain == "Soja"
    val isOfficial = viewModel.isOfficial == true

    // Estados dos campos
    var moisture by remember(currentGrain) { mutableStateOf("") }
    var impurities by remember(currentGrain) { mutableStateOf("") }
    var brokenCrackedDamaged by remember(currentGrain) { mutableStateOf("") }
    var greenish by remember(currentGrain) { mutableStateOf("") }
    var burnt by remember(currentGrain) { mutableStateOf("") }
    var burntOrSour by remember(currentGrain) { mutableStateOf("") }
    var moldy by remember(currentGrain) { mutableStateOf("") }
    var spoiled by remember(currentGrain) { mutableStateOf("") }
    var carunchado by remember(currentGrain) { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    val moistureFocus = remember { FocusRequester() }
    val scrollState = rememberScrollState()

    // Carrega limites
    LaunchedEffect(currentGrain) {
        viewModel.loadDefaultLimits()
    }

    // Preenche os campos quando os limites chegam
    LaunchedEffect(defaultLimits) {
        defaultLimits?.let { limits ->
            moisture = limits["moistureUpLim"]?.toString() ?: ""
            impurities = limits["impuritiesUpLim"]?.toString() ?: ""
            brokenCrackedDamaged = limits["brokenUpLim"]?.toString() ?: ""
            moldy = limits["moldyUpLim"]?.toString() ?: ""

            // Mapeamento Milho: Ardido usa o campo burntOrSour
            burntOrSour = limits["burntOrSourUpLim"]?.toString() ?: ""

            // Mapeamento Milho: Carunchado
            carunchado = limits["carunchadoUpLim"]?.toString() ?: ""

            if (isSoja) {
                greenish = limits["greenishUpLim"]?.toString() ?: ""
                burnt = limits["burntUpLim"]?.toString() ?: ""
                spoiled = limits["spoiledTotalUpLim"]?.toString() ?: ""
            } else {
                greenish = "0"
                burnt = "0"
                spoiled = limits["spoiledTotalUpLim"]?.toString() ?: "0"
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(24.dp)) {

        Text(
            text = if (!isOfficial) "Insira os limites de tolerância" else "Limites de Referência MAPA",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "$currentGrain",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(16.dp))

        // UMIDADE FIXA NO TOPO
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

        // BOX QUE OCUPA O CENTRO (TABELA OU CAMPOS)
        Box(modifier = Modifier.weight(1f)) {
            if (isOfficial) {
                if (allOfficialLimits.isNotEmpty()) {
                    OfficialLimitsTable(grain = currentGrain ?: "", data = allOfficialLimits)
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Carregando tabela oficial...")
                    }
                }
            } else {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    DiscountEditableFields(
                        isSoja = isSoja,
                        impurities = impurities, onImpuritiesChange = { impurities = it },
                        burnt = burnt, onBurntChange = { burnt = it },
                        burntOrSour = burntOrSour, onBurntOrSourChange = { burntOrSour = it },
                        moldy = moldy, onMoldyChange = { moldy = it },
                        spoiled = spoiled, onSpoiledChange = { spoiled = it },
                        greenish = greenish, onGreenishChange = { greenish = it },
                        broken = brokenCrackedDamaged, onBrokenChange = { brokenCrackedDamaged = it },
                        carunchado = carunchado, onCarunchadoChange = { carunchado = it }
                    )
                }
            }
        }

        Spacer(Modifier.height(32.dp))

        errorMessage?.let {
            Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(vertical = 4.dp))
        }

        Button(
            onClick = {
                if (moisture.isEmpty() || moisture == ".") {
                    errorMessage = "Informe a umidade base."
                    return@Button
                }

                if (!isOfficial) {
                    try {
                        viewModel.setLimit(
                            impurities = impurities.toFloatOrDefault(),
                            moisture = moisture.toFloatOrDefault(),
                            brokenCrackedDamaged = brokenCrackedDamaged.toFloatOrDefault(),
                            greenish = if(isSoja) greenish.toFloatOrDefault() else 0f,
                            burnt = if(isSoja) burnt.toFloatOrDefault() else 0f,
                            burntOrSour = burntOrSour.toFloatOrDefault(), // Ardido
                            moldy = moldy.toFloatOrDefault(),
                            spoiled = spoiled.toFloatOrDefault(),
                            carunchado = carunchado.toFloatOrDefault()
                        )
                    } catch (e: Exception) {
                        errorMessage = "Erro ao salvar valores."
                        return@Button
                    }
                }

                if (isSoja) {
                    navController.navigate("discount")
                } else {
                    // Se for milho, vá para a tela de input de desconto do milho ou resultado
                    navController.navigate("milhoDiscountInput")
                }
            },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text(if (!isOfficial) "Salvar e Continuar" else "Próximo")
        }
    }
}

@Composable
fun OfficialLimitsTable(grain: String, data: List<Any>) {
    val labels = if (grain == "Soja") {
        listOf("Ardidos/Queim.", "Queimados", "Mofados", "Avariados Total", "Esverdeados", "Partidos/Quebr.", "Impurezas")
    } else {
        listOf("Ardidos", "Mofados", "Avariados Total", "Quebrados", "Carunchados", "Impurezas")
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

            // LINHAS
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
                            is LimitMilho -> listOf(item.ardidoUpLim, item.mofadoUpLim, item.spoiledTotalUpLim, item.brokenUpLim, item.carunchadoUpLim, item.impuritiesUpLim)
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
fun DiscountEditableFields(
    isSoja: Boolean,
    impurities: String, onImpuritiesChange: (String) -> Unit,
    burnt: String, onBurntChange: (String) -> Unit,
    burntOrSour: String, onBurntOrSourChange: (String) -> Unit,
    moldy: String, onMoldyChange: (String) -> Unit,
    spoiled: String, onSpoiledChange: (String) -> Unit,
    greenish: String, onGreenishChange: (String) -> Unit,
    broken: String, onBrokenChange: (String) -> Unit,
    carunchado: String, onCarunchadoChange: (String) -> Unit
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

        // Mofados: Geralmente entra no cálculo de avariados, mas se precisar editar isoladamente:
        NumberInputField("Mofados (%)", moldy, onMoldyChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))

        if (!isSoja) {
            NumberInputField("Carunchados (%)", carunchado, onCarunchadoChange, FocusRequester(), null, true)
            Spacer(Modifier.height(8.dp))
        }

        NumberInputField("Total Avariados (%)", spoiled, onSpoiledChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))

        if (isSoja) {
            NumberInputField("Esverdeados (%)", greenish, onGreenishChange, FocusRequester(), null, true)
            Spacer(Modifier.height(8.dp))
        }

        NumberInputField(if (isSoja) "Quebrados/Amassados (%)" else "Quebrados (%)", broken, onBrokenChange, FocusRequester(), null, true)
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