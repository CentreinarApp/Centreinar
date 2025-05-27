package com.example.centreinar.ui.home

import ClassificationTable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.Sample
import java.math.RoundingMode

@Composable
fun ClassificationInputScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val tabTitles = listOf("Informação Básica", "Defeitos 1", "Defeitos 2", "Defeitos 3", "Resultado")
    var selectedTab by remember { mutableStateOf(0) }

    // State from ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val classification by viewModel.classification.collectAsState()

    // Form state variables
    //var grain by remember { mutableStateOf("") }
    //var group by remember { mutableStateOf("") }
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

    fun clearForm() {
        //grain = ""
        //group = ""
        lotWeight = ""
        sampleWeight = ""
        foreignMatters = ""
        humidity = ""
        greenish = ""
        brokenCrackedDamaged = ""
        damaged = ""
        damagedInput = ""
        piercingInput = ""
        piercingDamaged = ""
        burnt = ""
        sour = ""
        moldy = ""
        fermented = ""
        germinated = ""
        immature = ""
        shriveled = ""
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
//                grain = grain,
//                onGrainChange = { grain = it },
//                group = group,
//                onGroupChange = { group = it },
                humidity = humidity,
                onHumidityChange = { humidity = it },
                foreignMatters = foreignMatters,
                onForeignMattersChange = { foreignMatters = it },
                lotWeight = lotWeight,
                onLotWeightChange = { lotWeight = it },
                sampleWeight = sampleWeight,
                onSampleWeightChange = { sampleWeight = it },
            )

            1 -> GraveDefectsTab(
                burnt = burnt,
                onBurntChange = { burnt = it },
                sour = sour,
                onSourChange = { sour = it },
                moldy = moldy,
                onMoldyChange = { moldy = it }
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
                },
                damagedInput = damagedInput,
                onDamagedInputChange = {
                    damagedInput = it
                    damaged = ((it.toFloat() ?: (0f + piercingDamaged.toFloat()) ?: 0f)).toString()
                }
            )

            3 -> FinalDefectsTab(
                greenish = greenish,
                onGreenishChange = { greenish = it },
                brokenCrackedDamaged = brokenCrackedDamaged,
                onBrokenCrackedDamagedChange = { brokenCrackedDamaged = it }
            )

            4 -> {
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
                            ClassificationTable(
                                classification = classification!!,
                                modifier = Modifier.fillMaxWidth()
                            )
                            Spacer(Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    clearForm()
                                    selectedTab = 0
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Nova Análise")
                            }
                        }

                        else -> {
                            Text(
                                text = "Submeta os dados para ver os resultados da classificação",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .padding(top = 32.dp)
                                    .fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }



        // Navigation controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Previous button (unchanged)
            if (selectedTab > 0) {
                Button(onClick = { selectedTab-- }) {
                    Text("Previous")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }

            // Next or Classify (merged into Next logic)
            if (selectedTab < tabTitles.lastIndex) {
                Button(onClick = {
                    // If we're on the last input tab (index = tabs-1), submit first
                    if (selectedTab == tabTitles.lastIndex - 1) {
                        val sample = Sample(
                            //CHANGE
                            //are now input before
                            grain = "Outro",
                            group = 0,
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
                    }
                    // Move to the next tab (or Result tab, if we just submitted)
                    selectedTab++
                }) {
                    Text("Next")
                }
            } else {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
fun BasicInfoTab(
//    grain: String,
//    onGrainChange: (String) -> Unit,
//    group: String,
//    onGroupChange: (String) -> Unit,
    onLotWeightChange: (String) -> Unit,
    sampleWeight: String,
    onSampleWeightChange: (String) -> Unit,
    humidity: String,
    onHumidityChange: (String) -> Unit,
    foreignMatters: String,
    onForeignMattersChange: (String) -> Unit,
    lotWeight: String,

) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
//        item {
//            OutlinedTextField(
//                value = grain,
//                onValueChange = onGrainChange,
//                label = { Text("Grão") },
//                modifier = Modifier.fillMaxWidth())
//        }
//        item {
//            OutlinedTextField(
//                value = group,
//                onValueChange = onGroupChange,
//                label = { Text("Grupo") },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
//                modifier = Modifier.fillMaxWidth())
//        }
        item {
            OutlinedTextField(
                value = lotWeight,
                onValueChange = onLotWeightChange,
                label = { Text("Peso do lote (kg)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = sampleWeight,
                onValueChange = onSampleWeightChange,
                label = { Text("Peso da amostra de trabalho (g)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = humidity,
                onValueChange = onHumidityChange,
                label = { Text("Umidade") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = foreignMatters,
                onValueChange = onForeignMattersChange,
                label = { Text("Matéria Estranha e Impurezas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
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
    onMoldyChange: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            OutlinedTextField(
                value = burnt,
                onValueChange = onBurntChange,
                label = { Text("Queimados") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = sour,
                onValueChange = onSourChange,
                label = { Text("Ardidos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = moldy,
                onValueChange = onMoldyChange,
                label = { Text("Mofados") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
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
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            OutlinedTextField(
                value = fermented,
                onValueChange = onFermentedChange,
                label = { Text("Fermentados") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = germinated,
                onValueChange = onGerminatedChange,
                label = { Text("Germinados") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = immature,
                onValueChange = onImmatureChange,
                label = { Text("Imaturos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = shriveled,
                onValueChange = onShriveledChange,
                label = { Text("Chochos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            Column(Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = piercingInput,
                    onValueChange = onPiercingInputChange,
                    label = { Text("Picados") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth())
                Text(
                    text = if (piercingInput.isNotEmpty()) "$piercingInput / 4 = ${(piercingInput.toFloatOrNull()?.div(4) ?: 0)}"
                    else "",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp))
            }
        }
        item {
            OutlinedTextField(
                value = damagedInput,
                onValueChange = onDamagedInputChange,
                label = { Text("Danificados por outras pragas") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
            Text(
                text = if (damagedInput.isNotEmpty()){
                    "$damagedInput + ${(piercingInput.toFloatOrNull()?.div(4) ?: 0)} = ${(damagedInput.toFloatOrNull())?.plus(
                        (piercingInput.toFloatOrNull()?.div(4)!!)
                    )}"
                }
                else "",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp))
        }
    }
}

@Composable
fun FinalDefectsTab(
    greenish: String,
    onGreenishChange: (String) -> Unit,
    brokenCrackedDamaged: String,
    onBrokenCrackedDamagedChange: (String) -> Unit
) {
    LazyColumn(modifier = Modifier.padding(16.dp)) {
        item {
            OutlinedTextField(
                value = greenish,
                onValueChange = onGreenishChange,
                label = { Text("Esverdeados") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
        item {
            OutlinedTextField(
                value = brokenCrackedDamaged,
                onValueChange = onBrokenCrackedDamagedChange,
                label = { Text("Partidos, Quebrados e Amassados") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth())
        }
    }
}