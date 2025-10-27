package com.example.centreinar.ui.classificationProcess.screens

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.SampleSoja // Importe a entidade de Soja
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel // Importe o ViewModel
import java.math.RoundingMode
import com.example.centreinar.data.local.entity.SampleMilho // Importe a entidade de Milho


@Composable
fun ClassificationInputScreen( // Função Principal, nome corrigido
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    // CORREÇÃO: Leitura direta das variáveis 'var' do ViewModel
    val currentGrain = viewModel.selectedGrain
    val currentGroup = viewModel.selectedGroup

    // Variável para determinar se é Milho (para lógica de interface)
    val isMilho = currentGrain == "Milho"
    val isSoja = !isMilho

    // Variáveis de Estado para os Inputs
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
            1 -> sourFocus.requestFocus() // Foco na nova ordem: Ardidos
            2 -> insectFocus.requestFocus() // Foco nos picados
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
    ) {
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title) }
                )
            }
        }
        Text(
            "Insira os dados de classificação",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

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
                    // Atualiza o valor para a soma de Danificados
                    val calculatedPiercing = (it.toFloatOrZero() / 4f).toString()
                    val dValue = damagedInput.toFloatOrZero().toString()
                    damaged = calculateDamagedSum(dValue, calculatedPiercing)
                },
                damagedInput = damagedInput,
                onDamagedInputChange = {
                    damagedInput = it
                    // Atualiza o valor para a soma de Danificados
                    val calculatedPiercing = (piercingInput.toFloatOrZero() / 4f).toString()
                    damaged = calculateDamagedSum(it, calculatedPiercing)
                },
                damaged = damaged, // Total de Danificados (read-only)
                onDamagedChange = { damaged = it }, // Recebe a soma do cálculo
                doesDefineColorClass = doesDefineColorClass,
                onDoesDefineColorClassChange = { doesDefineColorClass = it },
                insectFocus = insectFocus,
                damagedFocus = damagedFocus
            )
        }

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

                        // Validação de campos essenciais
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
                            // CÓDIGO MILHO: Cria SampleMilho
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
                            // A navegação para Milho requer o ViewModelMilho.
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

// --- FUNÇÕES DE ABA CORRIGIDAS ---

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
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
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

        Spacer(modifier = Modifier.height(16.dp))
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
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // 1. Ardidos (Primeiro, ordem corrigida)
        NumberInputField(
            value = sour,
            onValueChange = onSourChange,
            label = "Ardidos (g)",
            focusRequester = sourFocus,
            nextFocus = if (isMilho) moldyFocus else burntFocus
        )

        // 2. Queimados (Apenas Soja)
        if (!isMilho) {
            NumberInputField(
                value = burnt,
                onValueChange = onBurntChange,
                label = "Queimados (g)",
                focusRequester = burntFocus,
                nextFocus = moldyFocus
            )
        }

        // 3. Mofados
        NumberInputField(
            value = moldy,
            onValueChange = onMoldyChange,
            label = "Mofados (g)",
            focusRequester = moldyFocus,
            nextFocus = if (isMilho) carunchadoFocus else fermentedFocus
        )

        // 4. Carunchados (Apenas Milho)
        if (isMilho) {
            NumberInputField(
                value = carunchado,
                onValueChange = onCarunchadoChange,
                label = "Carunchados (g)",
                focusRequester = carunchadoFocus,
                nextFocus = fermentedFocus
            )
        }

        // 5. Outros Defeitos (Comuns, Milho e Soja)
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

        // 6. Chochos (Soja) / Gessados (Milho)
        if (!isMilho) { // Soja
            NumberInputField(
                value = shriveled,
                onValueChange = onShriveledChange,
                label = "Chochos (g)",
                focusRequester = shriveledFocus,
                nextFocus = null
            )
        } else { // Milho
            NumberInputField(
                value = gessado,
                onValueChange = onGessadoChange, // CORREÇÃO: onValueChange é o nome do parâmetro
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
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        if (isSoja) {
            // Campos de Danificados/Picados são EXCLUSIVOS da Soja
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

            // Total de Danificados (ReadOnly)
            OutlinedTextField(
                value = damaged,
                onValueChange = onDamagedChange,
                label = { Text("Total de Danificados [(a)+(b)]") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true
            )
        } else {
            // Mensagem para Milho (se não houver campos de defeito adicionais)
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
            // Permite apenas números e pontos/vírgulas
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
            onDone = { keyboardController?.hide() }
        ),
        singleLine = true,
        enabled = enabled,
        readOnly = !enabled
    )
}

// Funções auxiliares de conversão (essenciais para evitar o bug de quebra)
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