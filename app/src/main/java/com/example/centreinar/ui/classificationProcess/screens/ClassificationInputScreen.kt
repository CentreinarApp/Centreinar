package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.navigation.NavController
import com.example.centreinar.Sample
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import java.math.RoundingMode

@Composable
fun ClassificationInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val tabTitles = listOf("Informação Básica", "Defeitos 1", "Defeitos 2", "Defeitos 3")
    var selectedTab by remember { mutableStateOf(0) }

    // State from ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val classification by viewModel.classification.collectAsState()
    val selectedGroup by viewModel.classification.collectAsState()
    val selectedGrain  by viewModel.classification.collectAsState()

    // Form state variables
    var showClassConfirmation by remember { mutableStateOf(false) }
    var lotWeight by remember { mutableStateOf("") }
    var sampleWeight by remember { mutableStateOf("") }
    var foreignMatters by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var greenish by remember { mutableStateOf("") }
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var damaged by remember { mutableStateOf("") }
    var damagedInput by remember { mutableStateOf("") }
    var piercingInput by remember { mutableStateOf("") }
    var piercingDamaged by remember { mutableStateOf("") }
    var burnt by remember { mutableStateOf("") }
    var sour by remember { mutableStateOf("") }
    var moldy by remember { mutableStateOf("") }
    var fermented by remember { mutableStateOf("") }
    var germinated by remember { mutableStateOf("") }
    var immature by remember { mutableStateOf("") }
    var shriveled by remember { mutableStateOf("") }

    // Focus requesters for each tab
    // BasicInfoTab
    val lotWeightFocus = remember { FocusRequester() }
    val sampleWeightFocus = remember { FocusRequester() }
    val humidityFocus = remember { FocusRequester() }
    val foreignMattersFocus = remember { FocusRequester() }

    // GraveDefectsTab
    val burntFocus = remember { FocusRequester() }
    val sourFocus = remember { FocusRequester() }
    val moldyFocus = remember { FocusRequester() }

    // OtherDefectsTab
    val fermentedFocus = remember { FocusRequester() }
    val germinatedFocus = remember { FocusRequester() }
    val immatureFocus = remember { FocusRequester() }
    val shriveledFocus = remember { FocusRequester() }
    val piercingInputFocus = remember { FocusRequester() }
    val damagedInputFocus = remember { FocusRequester() }

    // FinalDefectsTab
    val greenishFocus = remember { FocusRequester() }
    val brokenCrackedDamagedFocus = remember { FocusRequester() }

    // Keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    // Focus management based on selected tab
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> lotWeightFocus.requestFocus()
            1 -> burntFocus.requestFocus()
            2 -> fermentedFocus.requestFocus()
            3 -> greenishFocus.requestFocus()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Tab navigation
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title) }
                )
            }
        }

        // Tab content
        when (selectedTab) {
            0 -> BasicInfoTab(
                lotWeight = lotWeight,
                onLotWeightChange = { lotWeight = it },
                sampleWeight = sampleWeight,
                onSampleWeightChange = { sampleWeight = it },
                humidity = humidity,
                onHumidityChange = { humidity = it },
                foreignMatters = foreignMatters,
                onForeignMattersChange = { foreignMatters = it },
                lotWeightFocus = lotWeightFocus,
                sampleWeightFocus = sampleWeightFocus,
                humidityFocus = humidityFocus,
                foreignMattersFocus = foreignMattersFocus
            )

            1 -> GraveDefectsTab(
                burnt = burnt,
                onBurntChange = { burnt = it },
                sour = sour,
                onSourChange = { sour = it },
                moldy = moldy,
                onMoldyChange = { moldy = it },
                burntFocus = burntFocus,
                sourFocus = sourFocus,
                moldyFocus = moldyFocus
            )

            2 -> OtherDefectsTab(
                fermented = fermented,
                onFermentedChange = { fermented = it },
                germinated = germinated,
                onGerminatedChange = { germinated = it },
                immature = immature,
                onImmatureChange = { immature = it },
                shriveled = shriveled,
                onShriveledChange = { shriveled = it },
                piercingInput = piercingInput,
                onPiercingInputChange = {
                    piercingInput = it
                    piercingDamaged = (it.toFloatOrNull()?.div(4)?.toString() ?: "")
                    // Recalculate damaged when piercing changes
                    damaged = calculateDamagedSum(damagedInput, piercingDamaged)
                },
                damagedInput = damagedInput,
                onDamagedInputChange = {
                    damagedInput = it
                    damaged = calculateDamagedSum(it, piercingDamaged)
                },
                damaged = damaged, // Added missing parameter
                fermentedFocus = fermentedFocus,
                germinatedFocus = germinatedFocus,
                immatureFocus = immatureFocus,
                shriveledFocus = shriveledFocus,
                piercingInputFocus = piercingInputFocus,
                damagedInputFocus = damagedInputFocus
            )

            3 -> FinalDefectsTab(
                greenish = greenish,
                onGreenishChange = { greenish = it },
                brokenCrackedDamaged = brokenCrackedDamaged,
                onBrokenCrackedDamagedChange = { brokenCrackedDamaged = it },
                greenishFocus = greenishFocus,
                brokenCrackedDamagedFocus = brokenCrackedDamagedFocus
            )
        }

        if (showClassConfirmation) {
            val grain = viewModel.selectedGrain
            var group = viewModel.selectedGroup
            if (group == null) {
                group = 1
            } //test this
            val sample = Sample(
                grain = grain.toString(),
                group = group,
                lotWeight = lotWeight
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                sampleWeight = sampleWeight
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                foreignMattersAndImpurities = foreignMatters
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                humidity = humidity
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                greenish = greenish
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                brokenCrackedDamaged = brokenCrackedDamaged
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                damaged = damaged
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                burnt = burnt
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                sour = sour
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                moldy = moldy
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                fermented = fermented
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                germinated = germinated
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                immature = immature
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f,
                shriveled = shriveled
                    .toBigDecimalOrNull()
                    ?.setScale(2, RoundingMode.HALF_UP)
                    ?.toFloat() ?: 0f
            )
            viewModel.classifySample(sample)

            AlertDialog(
                onDismissRequest = { showClassConfirmation = false },
                title = { Text("Definir classe?") },
                text = { Text("Deseja definir classe?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showClassConfirmation = false
                            navController.navigate("colorClassInput")
                            viewModel.doesDefineColorClass = true
                        }
                    ) {
                        Text("Sim")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            showClassConfirmation = false
                            navController.navigate("classificationResult")
                        }
                    ) {
                        Text("Não")
                    }
                }
            )
        }

        // Navigation controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (selectedTab > 0) {
                Button(onClick = { selectedTab-- }) {
                    Text("Voltar")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            if (selectedTab < tabTitles.lastIndex) {
                Button(onClick = { selectedTab++ }) {
                    Text("Avançar")
                }
            } else {
                Button(onClick = { showClassConfirmation = true }) {
                    Text("Classificar")
                }
            }
        }
    }
}

@Composable
fun BasicInfoTab(
    lotWeight: String,
    onLotWeightChange: (String) -> Unit,
    sampleWeight: String,
    onSampleWeightChange: (String) -> Unit,
    humidity: String,
    onHumidityChange: (String) -> Unit,
    foreignMatters: String,
    onForeignMattersChange: (String) -> Unit,
    lotWeightFocus: FocusRequester,
    sampleWeightFocus: FocusRequester,
    humidityFocus: FocusRequester,
    foreignMattersFocus: FocusRequester
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            NumberInputField(
                value = lotWeight,
                onValueChange = onLotWeightChange,
                label = "Peso do lote (kg)",
                focusRequester = lotWeightFocus,
                nextFocus = sampleWeightFocus
            )
        }
        item {
            NumberInputField(
                value = sampleWeight,
                onValueChange = onSampleWeightChange,
                label = "Peso da amostra de trabalho (g)",
                focusRequester = sampleWeightFocus,
                nextFocus = humidityFocus
            )
        }
        item {
            NumberInputField(
                value = humidity,
                onValueChange = onHumidityChange,
                label = "Umidade (%) ",
                focusRequester = humidityFocus,
                nextFocus = foreignMattersFocus
            )
        }
        item {
            NumberInputField(
                value = foreignMatters,
                onValueChange = onForeignMattersChange,
                label = "Matéria Estranha e Impurezas (g)",
                focusRequester = foreignMattersFocus,
                nextFocus = null
            )
        }
    }
}

@Composable
fun GraveDefectsTab(
    burnt: String,
    onBurntChange: (String) -> Unit,
    sour: String,
    onSourChange: (String) -> Unit,
    moldy: String,
    onMoldyChange: (String) -> Unit,
    burntFocus: FocusRequester,
    sourFocus: FocusRequester,
    moldyFocus: FocusRequester
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            NumberInputField(
                value = burnt,
                onValueChange = onBurntChange,
                label = "Queimados (g)",
                focusRequester = burntFocus,
                nextFocus = sourFocus
            )
        }
        item {
            NumberInputField(
                value = sour,
                onValueChange = onSourChange,
                label = "Ardidos (g)",
                focusRequester = sourFocus,
                nextFocus = moldyFocus
            )
        }
        item {
            NumberInputField(
                value = moldy,
                onValueChange = onMoldyChange,
                label = "Mofados (g)",
                focusRequester = moldyFocus,
                nextFocus = null
            )
        }
    }
}

@Composable
fun OtherDefectsTab(
    fermented: String,
    onFermentedChange: (String) -> Unit,
    germinated: String,
    onGerminatedChange: (String) -> Unit,
    immature: String,
    onImmatureChange: (String) -> Unit,
    shriveled: String,
    onShriveledChange: (String) -> Unit,
    piercingInput: String,
    onPiercingInputChange: (String) -> Unit,
    damagedInput: String,
    onDamagedInputChange: (String) -> Unit,
    damaged: String, // Added missing parameter
    fermentedFocus: FocusRequester,
    germinatedFocus: FocusRequester,
    immatureFocus: FocusRequester,
    shriveledFocus: FocusRequester,
    piercingInputFocus: FocusRequester,
    damagedInputFocus: FocusRequester
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            NumberInputField(
                value = fermented,
                onValueChange = onFermentedChange,
                label = "Fermentados (g)",
                focusRequester = fermentedFocus,
                nextFocus = germinatedFocus
            )
        }
        item {
            NumberInputField(
                value = germinated,
                onValueChange = onGerminatedChange,
                label = "Germinados (g)",
                focusRequester = germinatedFocus,
                nextFocus = immatureFocus
            )
        }
        item {
            NumberInputField(
                value = immature,
                onValueChange = onImmatureChange,
                label = "Imaturos (g)",
                focusRequester = immatureFocus,
                nextFocus = shriveledFocus
            )
        }
        item {
            NumberInputField(
                value = shriveled,
                onValueChange = onShriveledChange,
                label = "Chochos (g)",
                focusRequester = shriveledFocus,
                nextFocus = piercingInputFocus
            )
        }
        item {
            Column(Modifier.fillMaxWidth()) {
                NumberInputField(
                    value = piercingInput,
                    onValueChange = onPiercingInputChange,
                    label = "Picados (g)",
                    focusRequester = piercingInputFocus,
                    nextFocus = damagedInputFocus
                )
            }
        }
        item{
            Text(
                text = if (piercingInput.isNotEmpty()) {
                    val value = piercingInput.toFloatOrNull() ?: 0f
                    val result = value / 4
                    "$value / 4 = ${"%.2f".format(result)}"
                } else "",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        item {
            Column(Modifier.fillMaxWidth()) {
                NumberInputField(
                    value = damagedInput,
                    onValueChange = onDamagedInputChange,
                    label = "Danificados por outras pragas (g)",
                    focusRequester = damagedInputFocus,
                    nextFocus = null
                )
                Text(
                    text = if (damagedInput.isNotEmpty() || piercingInput.isNotEmpty()) {
                        val dValue = damagedInput.toFloatOrNull() ?: 0f
                        val pValue = piercingInput.toFloatOrNull()?.div(4) ?: 0f
                        val sum = dValue + pValue
                        "$dValue + $pValue = ${"%.2f".format(sum)}"
                    } else "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp) )
            }
        }
        item { // Wrapped in item
            OutlinedTextField(
                value = damaged,
                onValueChange = {},
                label = { Text("Soma de Grãos Danificados (g)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = false
            )
        }
    }
}

@Composable
fun FinalDefectsTab(
    greenish: String,
    onGreenishChange: (String) -> Unit,
    brokenCrackedDamaged: String,
    onBrokenCrackedDamagedChange: (String) -> Unit,
    greenishFocus: FocusRequester,
    brokenCrackedDamagedFocus: FocusRequester
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            NumberInputField(
                value = greenish,
                onValueChange = onGreenishChange,
                label = "Esverdeados (g)",
                focusRequester = greenishFocus,
                nextFocus = brokenCrackedDamagedFocus
            )
        }
        item {
            NumberInputField(
                value = brokenCrackedDamaged,
                onValueChange = onBrokenCrackedDamagedChange,
                label = "Partidos, Quebrados e Amassados (g)",
                focusRequester = brokenCrackedDamagedFocus,
                nextFocus = null
            )
        }
    }
}

@Composable
private fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester?
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = { onValueChange(sanitizeFloatInput(it)) },
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
            onDone = { keyboardController?.hide() }
        ),
        singleLine = true
    )
}

private fun sanitizeFloatInput(input: String): String {
    if (input.isEmpty()) return input

    // Filter non-digit/non-decimal characters
    var filtered = input.filter { it.isDigit() || it == '.' }

    // Handle multiple decimals
    val decimalCount = filtered.count { it == '.' }
    if (decimalCount > 1) {
        val firstDecimalIndex = filtered.indexOfFirst { it == '.' }
        // Get the part after the first decimal point
        val afterDecimal = filtered.substring(firstDecimalIndex + 1)
        // Remove any additional decimals from the part after the first decimal
        filtered = filtered.replaceRange(
            firstDecimalIndex + 1,
            filtered.length,
            afterDecimal.replace(".", "")
        )
    }

    // Prevent leading zero issues
    if (filtered.startsWith("00") && !filtered.startsWith("0.")) {
        filtered = "0" + filtered.drop(2)
    }

    return filtered
}

private fun calculateDamagedSum(damagedInput: String, piercingDamaged: String): String {
    val dInput = damagedInput.toFloatOrNull() ?: 0f
    val pDamaged = piercingDamaged.toFloatOrNull() ?: 0f
    val sum = dInput + pDamaged
    return "%.2f".format(sum)
}