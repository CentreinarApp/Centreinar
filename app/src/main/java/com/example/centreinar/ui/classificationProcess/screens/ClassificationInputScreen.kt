package com.example.centreinar.ui.classificationProcess.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import java.math.RoundingMode

@Composable
fun ClassificationInputScreen( // Função Principal
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // Leitura das variáveis 'val' do ViewModel
    val currentGrain = viewModel.selectedGrain
    val currentGroup = viewModel.selectedGroup

    val isMilho = currentGrain == "Milho"
    val isSoja = !isMilho

    // Variáveis de Estado para os Inputs (mantidas)
    var lotWeight by remember { mutableStateOf("") }
    var sampleWeight by remember { mutableStateOf("") }
    var foreignMatters by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var greenish by remember { mutableStateOf("") }
    var burnt by remember { mutableStateOf("") }
    var sour by remember { mutableStateOf("") }
    var moldy by remember { mutableStateOf("") }
    var fermented by remember { mutableStateOf("") }
    var germinated by remember { mutableStateOf("") }
    var immature by remember { mutableStateOf("") }
    var shriveled by remember { mutableStateOf("") }
    var damaged by remember { mutableStateOf("") } // Total de Danificados (Soma final Soja)
    var piercingInput by remember { mutableStateOf("") }
    var damagedInput by remember { mutableStateOf("") } // Demais danificados (Soja)

    var doesDefineColorClass by remember { mutableStateOf(false) }
    var gessado by remember { mutableStateOf("") } // Milho
    var carunchado by remember { mutableStateOf("") } // Milho

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Requesters de Foco
    val lotWeightFocus = remember { FocusRequester() }
    val sampleWeightFocus = remember { FocusRequester() }
    val moistureFocus = remember { FocusRequester() }
    val impuritiesFocus = remember { FocusRequester() }
    val brokenFocus = remember { FocusRequester() }
    val greenishFocus = remember { FocusRequester() }
    val sourFocus = remember { FocusRequester() }
    val burntFocus = remember { FocusRequester() }
    val moldyFocus = remember { FocusRequester() }
    val carunchadoFocus = remember { FocusRequester() }
    val gessadoFocus = remember { FocusRequester() }
    val insectFocus = remember { FocusRequester() }
    val damagedFocus = remember { FocusRequester() }

    val tabTitles = listOf("Informação Básica", "Avariados", "Defeitos Finais")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> lotWeightFocus.requestFocus()
            1 -> sourFocus.requestFocus()
            2 -> insectFocus.requestFocus()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. TAB ROW (Altura Fixa)
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title) }
                )
            }
        }

        // 2. TÍTULO (Altura Fixa)
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                "Insira os dados de classificação",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(5.dp))
        }

        // 3. CONTEÚDO DA ABA (ÁREA DE ROLAGEM) - USANDO WEIGHT(1F) + Box verticalScroll
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(8.dp)
        ) {
            when (selectedTab) {
                0 -> BasicInfoTabClassification(
                    lotWeight = lotWeight,
                    onLotWeightChange = { lotWeight = it },
                    sampleWeight = sampleWeight,
                    onSampleWeightChange = { sampleWeight = it },
                    moisture = humidity,
                    onMoistureChange = { humidity = it },
                    impurities = foreignMatters,
                    onImpuritiesChange = { foreignMatters = it },
                    brokenCrackedDamaged = brokenCrackedDamaged,
                    onBrokenCrackedDamagedChange = { brokenCrackedDamaged = it },
                    greenish = greenish,
                    onGreenishChange = { greenish = it },
                    lotWeightFocus = lotWeightFocus,
                    sampleWeightFocus = sampleWeightFocus,
                    moistureFocus = moistureFocus,
                    impuritiesFocus = impuritiesFocus,
                    brokenCrackedDamagedFocus = brokenFocus,
                    greenishFocus = greenishFocus
                )

                1 -> DefectsTab1(
                    isMilho = isMilho,
                    burnt = burnt,
                    onBurntChange = { burnt = it },
                    sour = sour,
                    onSourChange = { sour = it },
                    moldy = moldy,
                    onMoldyChange = { moldy = it },
                    shriveled = shriveled,
                    onShriveledChange = { shriveled = it },
                    fermented = fermented,
                    onFermentedChange = { fermented = it },
                    germinated = germinated,
                    onGerminatedChange = { germinated = it },
                    immature = immature,
                    onImmatureChange = { immature = it },
                    gessado = gessado,
                    onGessadoChange = { gessado = it },
                    carunchado = carunchado,
                    onCarunchadoChange = { carunchado = it },
                    burntFocus = burntFocus,
                    sourFocus = sourFocus,
                    moldyFocus = moldyFocus,
                    shriveledFocus = remember { FocusRequester() },
                    fermentedFocus = remember { FocusRequester() },
                    germinatedFocus = remember { FocusRequester() },
                    immatureFocus = remember { FocusRequester() },
                    gessadoFocus = gessadoFocus,
                    carunchadoFocus = carunchadoFocus
                )

                2 -> DefectsTab2(
                    isSoja = isSoja,
                    piercingInput = piercingInput,
                    onPiercingInputChange = {
                        piercingInput = it
                        val calculatedPiercing = (it.toFloatOrZero() / 4f).toString()
                        val dValue = damagedInput.toFloatOrZero().toString()
                        damaged = calculateDamagedSum(dValue, calculatedPiercing)
                    },
                    damagedInput = damagedInput,
                    onDamagedInputChange = {
                        damagedInput = it
                        val calculatedPiercing = (piercingInput.toFloatOrZero() / 4f).toString()
                        damaged = calculateDamagedSum(it, calculatedPiercing)
                    },
                    damaged = damaged, // Total de Danificados (read-only)
                    onDamagedChange = { damaged = it },
                    doesDefineColorClass = doesDefineColorClass,
                    onDoesDefineColorClassChange = { doesDefineColorClass = it },
                    insectFocus = insectFocus,
                    damagedFocus = damagedFocus
                )
            }
        }

        // 4. BOTÕES DE NAVEGAÇÃO (Altura Fixa)
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
                Button(onClick = {
                    selectedTab++
                }) {
                    Text("Avançar")
                }
            } else {
                Button(
                    onClick = {
                        val group = currentGroup ?: 1

                        if (sampleWeight.toFloatOrZero() <= 0f) {
                            errorMessage = "O peso da amostra não pode ser zero."
                            return@Button
                        }

                        if (currentGrain == "Soja") {
                            val sample = SampleSoja(
                                grain = "Soja",
                                group = group,
                                lotWeight = lotWeight.toFloatOrZero(),
                                sampleWeight = sampleWeight.toFloatOrZero(),
                                foreignMattersAndImpurities = foreignMatters.toFloatOrZero(),
                                humidity = humidity.toFloatOrZero(),
                                greenish = greenish.toFloatOrZero(),
                                brokenCrackedDamaged = brokenCrackedDamaged.toFloatOrZero(),
                                damaged = damaged.toFloatOrZero(), // Total de Danificados (soma)
                                burnt = burnt.toFloatOrZero(),
                                sour = sour.toFloatOrZero(),
                                moldy = moldy.toFloatOrZero(),
                                fermented = fermented.toFloatOrZero(),
                                germinated = germinated.toFloatOrZero(),
                                immature = immature.toFloatOrZero(),
                                shriveled = shriveled.toFloatOrZero()
                            )
                            viewModel.classifySample(sample)
                        } else if (currentGrain == "Milho") {
                            val sample = SampleMilho(
                                grain = "Milho",
                                group = group,
                                lotWeight = lotWeight.toFloatOrZero(),
                                sampleWeight = sampleWeight.toFloatOrZero(),
                                impurities = foreignMatters.toFloatOrZero(),
                                broken = brokenCrackedDamaged.toFloatOrZero(),
                                carunchado = carunchado.toFloatOrZero(),
                                ardido = sour.toFloatOrZero(),
                                mofado = moldy.toFloatOrZero(),
                                fermented = fermented.toFloatOrZero(),
                                germinated = germinated.toFloatOrZero(),
                                immature = immature.toFloatOrZero(),
                                gessado = gessado.toFloatOrZero()
                            )
                            Log.e("ClassificationInput", "Milho deve ser classificado via ViewModelMilho.")
                        }

                        navController.navigate("classificationResult")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Classificar")
                }
            }
        }

        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

// --- FUNÇÕES DE ABA (mesmas da sua versão, com pequenas garantias de validação) ---

@Composable
fun BasicInfoTabClassification(
    lotWeight: String,
    onLotWeightChange: (String) -> Unit,
    sampleWeight: String,
    onSampleWeightChange: (String) -> Unit,
    moisture: String,
    onMoistureChange: (String) -> Unit,
    impurities: String,
    onImpuritiesChange: (String) -> Unit,
    brokenCrackedDamaged: String,
    onBrokenCrackedDamagedChange: (String) -> Unit,
    greenish: String,
    onGreenishChange: (String) -> Unit,
    lotWeightFocus: FocusRequester,
    sampleWeightFocus: FocusRequester,
    moistureFocus: FocusRequester,
    impuritiesFocus: FocusRequester,
    brokenCrackedDamagedFocus: FocusRequester,
    greenishFocus: FocusRequester
) {
    Column(modifier = Modifier.padding(16.dp)) {
        NumberInputField(
            value = lotWeight,
            onValueChange = onLotWeightChange,
            label = "Peso do Lote (kg)",
            focusRequester = lotWeightFocus,
            nextFocus = sampleWeightFocus,
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = sampleWeight,
            onValueChange = onSampleWeightChange,
            label = "Peso da Amostra (g)",
            focusRequester = sampleWeightFocus,
            nextFocus = moistureFocus,
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = moisture,
            onValueChange = onMoistureChange,
            label = "Umidade (%)",
            focusRequester = moistureFocus,
            nextFocus = impuritiesFocus,
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = impurities,
            onValueChange = onImpuritiesChange,
            label = "Matéria Estranha e Impurezas (%)",
            focusRequester = impuritiesFocus,
            nextFocus = brokenCrackedDamagedFocus,
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = brokenCrackedDamaged,
            onValueChange = onBrokenCrackedDamagedChange,
            label = "Partidos, Quebrados e Amassados (g)",
            focusRequester = brokenCrackedDamagedFocus,
            nextFocus = greenishFocus,
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = greenish,
            onValueChange = onGreenishChange,
            label = "Esverdeados (g)",
            focusRequester = greenishFocus,
            nextFocus = null
        )
    }
}

@Composable
fun DefectsTab1(
    isMilho: Boolean,
    burnt: String,
    onBurntChange: (String) -> Unit,
    sour: String,
    onSourChange: (String) -> Unit,
    moldy: String,
    onMoldyChange: (String) -> Unit,
    shriveled: String,
    onShriveledChange: (String) -> Unit,
    fermented: String,
    onFermentedChange: (String) -> Unit,
    germinated: String,
    onGerminatedChange: (String) -> Unit,
    immature: String,
    onImmatureChange: (String) -> Unit,
    gessado: String,
    onGessadoChange: (String) -> Unit,
    carunchado: String,
    onCarunchadoChange: (String) -> Unit,
    burntFocus: FocusRequester,
    sourFocus: FocusRequester,
    moldyFocus: FocusRequester,
    shriveledFocus: FocusRequester,
    fermentedFocus: FocusRequester,
    germinatedFocus: FocusRequester,
    immatureFocus: FocusRequester,
    gessadoFocus: FocusRequester,
    carunchadoFocus: FocusRequester
) {
    Column(modifier = Modifier.padding(16.dp)) {
        NumberInputField(
            value = sour,
            onValueChange = onSourChange,
            label = "Ardidos (g)",
            focusRequester = sourFocus,
            nextFocus = if (isMilho) moldyFocus else burntFocus
        )

        if (!isMilho) {
            NumberInputField(
                value = burnt,
                onValueChange = onBurntChange,
                label = "Queimados (g)",
                focusRequester = burntFocus,
                nextFocus = moldyFocus
            )
        }

        NumberInputField(
            value = moldy,
            onValueChange = onMoldyChange,
            label = "Mofados (g)",
            focusRequester = moldyFocus,
            nextFocus = if (isMilho) carunchadoFocus else fermentedFocus
        )

        if (isMilho) {
            NumberInputField(
                value = carunchado,
                onValueChange = onCarunchadoChange,
                label = "Carunchados (g)",
                focusRequester = carunchadoFocus,
                nextFocus = fermentedFocus
            )
        }

        NumberInputField(
            value = fermented,
            onValueChange = onFermentedChange,
            label = "Fermentados (g)",
            focusRequester = fermentedFocus,
            nextFocus = germinatedFocus
        )
        NumberInputField(
            value = germinated,
            onValueChange = onGerminatedChange,
            label = "Germinados (g)",
            focusRequester = germinatedFocus,
            nextFocus = immatureFocus
        )
        NumberInputField(
            value = immature,
            onValueChange = onImmatureChange,
            label = "Imaturos (g)",
            focusRequester = immatureFocus,
            nextFocus = if (isMilho) gessadoFocus else shriveledFocus
        )

        if (!isMilho) {
            NumberInputField(
                value = shriveled,
                onValueChange = onShriveledChange,
                label = "Chochos (g)",
                focusRequester = shriveledFocus,
                nextFocus = null
            )
        } else {
            NumberInputField(
                value = gessado,
                onValueChange = onGessadoChange,
                label = "Gessados (g)",
                focusRequester = gessadoFocus,
                nextFocus = null
            )
        }
    }
}

@Composable
fun DefectsTab2(
    isSoja: Boolean,
    piercingInput: String,
    onPiercingInputChange: (String) -> Unit,
    damagedInput: String,
    onDamagedInputChange: (String) -> Unit,
    damaged: String,
    onDamagedChange: (String) -> Unit,
    doesDefineColorClass: Boolean,
    onDoesDefineColorClassChange: (Boolean) -> Unit,
    insectFocus: FocusRequester,
    damagedFocus: FocusRequester
) {
    Column(modifier = Modifier.padding(16.dp)) {
        if (isSoja) {
            NumberInputField(
                value = piercingInput,
                onValueChange = onPiercingInputChange,
                label = "Grãos picados (a)",
                focusRequester = insectFocus,
                nextFocus = damagedFocus
            )

            Spacer(modifier = Modifier.height(16.dp))

            NumberInputField(
                value = damagedInput,
                onValueChange = onDamagedInputChange,
                label = "Demais grãos danificados (b)",
                focusRequester = damagedFocus,
                nextFocus = null
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = damaged,
                onValueChange = onDamagedChange,
                label = { Text("Total de Danificados [(a)+(b)]") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true
            )
        } else {
            Text("Nenhum defeito adicional para Milho nesta aba.")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesDefineColorClass, onCheckedChange = onDoesDefineColorClassChange)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Definir Classe de Cor?")
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
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.isEmpty() || it.matches(Regex("^(\\d*\\.?\\d*)$"))) {
                onValueChange(it)
            }
        },
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

// --- Funções auxiliares de conversão ---oi
private fun String.toFloatOrZero(): Float {
    return this.toBigDecimalOrNull()
        ?.setScale(2, RoundingMode.HALF_UP)
        ?.toFloat() ?: 0f
}

private fun calculateDamagedSum(damagedInput: String, piercingDamaged: String): String {
    val dInput = damagedInput.toFloatOrZero()
    val pDamaged = piercingDamaged.toFloatOrZero()
    val sum = dInput + pDamaged
    return "%.2f".format(sum)
}
