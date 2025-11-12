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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import java.math.RoundingMode
import kotlin.math.roundToInt

// -------------------------------------------------------------------------------------------------
// --- TELA PRINCIPAL ---
// -------------------------------------------------------------------------------------------------

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

    val focusManager = LocalFocusManager.current

    // Variáveis de Estado para os Inputs
    var lotWeight by remember { mutableStateOf("") }
    var sampleWeight by remember { mutableStateOf("") }
    var foreignMatters by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }

    // CAMPOS DEFEITOS FINAIS
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var greenish by remember { mutableStateOf("") }

    var burnt by remember { mutableStateOf("") }
    var sour by remember { mutableStateOf("") }
    var moldy by remember { mutableStateOf("") }
    var fermented by remember { mutableStateOf("") }
    var germinated by remember { mutableStateOf("") }
    var immature by remember { mutableStateOf("") }
    var shriveled by remember { mutableStateOf("") }

    // CAMPOS DEFEITOS MOVIDOS PARA TAB 1
    var damaged by remember { mutableStateOf("") } // Total de Danificados (Soma final Soja)
    var piercingInput by remember { mutableStateOf("") }
    var damagedInput by remember { mutableStateOf("") } // Demais danificados (Soja)

    var doesDefineColorClass by remember { mutableStateOf(false) }
    var gessado by remember { mutableStateOf("") } // Milho
    var carunchado by remember { mutableStateOf("") } // Milho

    // CAMPOS CLASSE DE COR
    var yellowGrainsWeight by remember { mutableStateOf("") }
    var otherColorsGrainsWeight by remember { mutableStateOf("") }

    var colorClassResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // CÁLCULO DA AMOSTRA LIMPA (NOVO: Valor Flutuante Garantido)
    val cleanSampleWeightFloat: Float by remember(sampleWeight, foreignMatters) {
        val sWeight = sampleWeight.toFloatOrZero()
        val fm = foreignMatters.toFloatOrZero()
        mutableStateOf(sWeight - fm)
    }

    // CÁLCULO DA AMOSTRA LIMPA (Para Exibição - String formatada)
    val cleanSampleWeightDisplay: String = remember(cleanSampleWeightFloat) {
        if (cleanSampleWeightFloat > 0) "%.2f".format(cleanSampleWeightFloat) else "0.00"
    }


    // Requesters de Foco (Ajustados)
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
    val insectFocus = remember { FocusRequester() } // Agora na Tab 1
    val damagedFocus = remember { FocusRequester() } // Agora na Tab 1
    val yellowGrainsFocus = remember { FocusRequester() }
    val otherColorsFocus = remember { FocusRequester() }

    val tabTitles = listOf("Informação Básica", "Avariados", "Defeitos Finais")
    var selectedTab by remember { mutableStateOf(0) }

    // Lógica de Foco ao Mudar de Aba (Ajustado)
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> lotWeightFocus.requestFocus()
            1 -> sourFocus.requestFocus()
            2 -> brokenFocus.requestFocus() // Tab 2 agora começa em brokenCrackedDamaged
        }
    }

    // Cálculo e Definição da Classe de Cor
    LaunchedEffect(doesDefineColorClass, cleanSampleWeightFloat, yellowGrainsWeight, otherColorsGrainsWeight) {
        if (doesDefineColorClass && isSoja) {
            val totalSample = cleanSampleWeightFloat
            val otherColors = otherColorsGrainsWeight.toFloatOrZero()

            if (totalSample > 0.01f && otherColors >= 0.0f) {
                val otherColorPct = (otherColors / totalSample) * 100f

                colorClassResult = if (otherColorPct.roundToInt() <= 10) {
                    "Classe Amarela"
                } else {
                    "Classe Misturada"
                }
            } else if (doesDefineColorClass && isSoja) {
                colorClassResult = "Classe Amarela"
            }
        } else {
            colorClassResult = null
        }
    }


    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // 1. TAB ROW
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title) }
                )
            }
        }

        // 2. TÍTULO
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                "Insira os dados de classificação",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(modifier = Modifier.height(5.dp))
        }

        // 3. CONTEÚDO DA ABA (ÁREA DE ROLAGEM)
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
                    cleanSampleWeight = cleanSampleWeightDisplay, // USANDO DISPLAY STRING
                    lotWeightFocus = lotWeightFocus,
                    sampleWeightFocus = sampleWeightFocus,
                    moistureFocus = moistureFocus,
                    impuritiesFocus = impuritiesFocus
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
                    carunchadoFocus = carunchadoFocus,
                    // Novos parâmetros de Soja para Tab 1
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
                    damaged = damaged,
                    onDamagedChange = { damaged = it },
                    insectFocus = insectFocus,
                    damagedFocus = damagedFocus,
                )

                2 -> DefectsTab2(
                    isSoja = isSoja,
                    // Defeitos finais (Partidos e Esverdeados)
                    brokenCrackedDamaged = brokenCrackedDamaged,
                    onBrokenCrackedDamagedChange = { brokenCrackedDamaged = it },
                    greenish = greenish,
                    onGreenishChange = { greenish = it },
                    brokenCrackedDamagedFocus = brokenFocus,
                    greenishFocus = greenishFocus,

                    // Classe de Cor
                    doesDefineColorClass = doesDefineColorClass,
                    onDoesDefineColorClassChange = { doesDefineColorClass = it },
                    cleanSampleWeight = cleanSampleWeightDisplay, // USANDO DISPLAY STRING
                    yellowGrainsWeight = yellowGrainsWeight,
                    onYellowGrainsWeightChange = { yellowGrainsWeight = it },
                    otherColorsGrainsWeight = otherColorsGrainsWeight,
                    onOtherColorsGrainsWeightChange = { otherColorsGrainsWeight = it },
                    yellowGrainsFocus = yellowGrainsFocus,
                    otherColorsFocus = otherColorsFocus,
                )
            }
        }

        // 4. BOTÕES DE NAVEGAÇÃO
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
                        // CHAVE DA CORREÇÃO: Usa o Float calculado, que é robusto e não depende de conversão de String na hora.
                        focusManager.clearFocus()

                        val group = currentGroup ?: 1

                        if (cleanSampleWeightFloat <= 0f) { // <<< VALIDAÇÃO REFORÇADA AQUI
                            errorMessage = "O peso da amostra limpa não pode ser zero. Verifique o Peso da Amostra e as Impurezas."
                            return@Button
                        }

                        // Validação Adicional para Classe de Cor
                        if (doesDefineColorClass && isSoja) {
                            val totalColorWeight = yellowGrainsWeight.toFloatOrZero() + otherColorsGrainsWeight.toFloatOrZero()
                            val cleanWeight = cleanSampleWeightFloat
                            if (totalColorWeight > cleanWeight * 1.05f) { // Permite uma pequena margem de erro
                                errorMessage = "A soma dos pesos de cor (${"%.2f".format(totalColorWeight)}g) excede o Peso da Amostra Limpa (${cleanSampleWeightDisplay}g)."
                                return@Button
                            }
                        }

                        // ... Lógica de Classificação (mantida)
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
                                damaged = damaged.toFloatOrZero(),
                                burnt = burnt.toFloatOrZero(),
                                sour = sour.toFloatOrZero(),
                                moldy = moldy.toFloatOrZero(),
                                fermented = fermented.toFloatOrZero(),
                                germinated = germinated.toFloatOrZero(),
                                immature = immature.toFloatOrZero(),
                                shriveled = shriveled.toFloatOrZero()
                            )
                            // O ViewModel precisa ser atualizado com o colorClassResult antes de navegar.
                            // viewModel.setClassificacaoCor(colorClassResult) // Exemplo

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

// -------------------------------------------------------------------------------------------------
// --- TAB 0: INFORMAÇÃO BÁSICA (MANTIDA) ---
// -------------------------------------------------------------------------------------------------

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
    cleanSampleWeight: String, // String de exibição
    lotWeightFocus: FocusRequester,
    sampleWeightFocus: FocusRequester,
    moistureFocus: FocusRequester,
    impuritiesFocus: FocusRequester,
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
            label = "Peso da amostra de trabalho (g)",
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
            label = "Matéria Estranha e Impurezas (g)",
            focusRequester = impuritiesFocus,
            nextFocus = null, // Último campo da aba
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Exibição do Peso da Amostra Limpa
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Peso da Amostra Limpa (Peso da amostra de Trabalho - Impurezas):",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "$cleanSampleWeight g",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// --- TAB 1: AVARIADOS (Defeitos de Soja MOVIDOS para cá) ---
// -------------------------------------------------------------------------------------------------

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
    carunchadoFocus: FocusRequester,
    // Novos parâmetros de Soja
    piercingInput: String,
    onPiercingInputChange: (String) -> Unit,
    damagedInput: String,
    onDamagedInputChange: (String) -> Unit,
    damaged: String,
    onDamagedChange: (String) -> Unit,
    insectFocus: FocusRequester,
    damagedFocus: FocusRequester,
) {
    // Adicionado verticalScroll para acomodar o conteúdo extra
    Column(modifier = Modifier.padding(16.dp)) {
        NumberInputField(
            value = sour,
            onValueChange = onSourChange,
            label = "Ardidos (g)",
            focusRequester = sourFocus,
            nextFocus = if (!isMilho) burntFocus else moldyFocus
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!isMilho) {
            NumberInputField(
                value = burnt,
                onValueChange = onBurntChange,
                label = "Queimados (g)",
                focusRequester = burntFocus,
                nextFocus = moldyFocus
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        NumberInputField(
            value = moldy,
            onValueChange = onMoldyChange,
            label = "Mofados (g)",
            focusRequester = moldyFocus,
            nextFocus = if (isMilho) carunchadoFocus else fermentedFocus
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (isMilho) {
            NumberInputField(
                value = carunchado,
                onValueChange = onCarunchadoChange,
                label = "Carunchados (g)",
                focusRequester = carunchadoFocus,
                nextFocus = fermentedFocus
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        NumberInputField(
            value = fermented,
            onValueChange = onFermentedChange,
            label = "Fermentados (g)",
            focusRequester = fermentedFocus,
            nextFocus = germinatedFocus
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = germinated,
            onValueChange = onGerminatedChange,
            label = "Germinados (g)",
            focusRequester = germinatedFocus,
            nextFocus = immatureFocus
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = immature,
            onValueChange = onImmatureChange,
            label = "Imaturos (g)",
            focusRequester = immatureFocus,
            // Próximo foco: se for milho, vai para gessado (último). Se for soja, vai para chochos/shriveled.
            nextFocus = if (isMilho) gessadoFocus else shriveledFocus
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (!isMilho) { // Soja
            NumberInputField(
                value = shriveled,
                onValueChange = onShriveledChange,
                label = "Chochos (g)",
                focusRequester = shriveledFocus,
                nextFocus = insectFocus // Próximo é Picados
            )
            Spacer(modifier = Modifier.height(24.dp))

            // --- DEFEITOS DANIFICADOS (MOVIDOS PARA CÁ) ---
            Text("", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

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
                nextFocus = null // Último campo da Tab 1
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = damaged,
                onValueChange = onDamagedChange,
                label = { Text("Total de Danificados [(a)+(b)]") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                enabled = false
            )
            // FIM DOS DEFEITOS DANIFICADOS

        } else { // Milho
            NumberInputField(
                value = gessado,
                onValueChange = onGessadoChange,
                label = "Gessados (g)",
                focusRequester = gessadoFocus,
                nextFocus = null // Último campo da Tab 1
            )
        }
    }
}

// -------------------------------------------------------------------------------------------------
// --- TAB 2: DEFEITOS FINAIS (Limpa) ---
// -------------------------------------------------------------------------------------------------

@Composable
fun DefectsTab2(
    isSoja: Boolean,
    brokenCrackedDamaged: String,
    onBrokenCrackedDamagedChange: (String) -> Unit,
    greenish: String,
    onGreenishChange: (String) -> Unit,
    brokenCrackedDamagedFocus: FocusRequester,
    greenishFocus: FocusRequester,
    // Classe de Cor
    doesDefineColorClass: Boolean,
    onDoesDefineColorClassChange: (Boolean) -> Unit,
    cleanSampleWeight: String, // String de exibição
    yellowGrainsWeight: String,
    onYellowGrainsWeightChange: (String) -> Unit,
    otherColorsGrainsWeight: String,
    onOtherColorsGrainsWeightChange: (String) -> Unit,
    yellowGrainsFocus: FocusRequester,
    otherColorsFocus: FocusRequester,
) {
    Column(modifier = Modifier.padding(16.dp)) {

        // --- 1. DEFEITOS FINAIS (Partidos e Esverdeados) ---
        Text("Defeitos Finais", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        NumberInputField(
            value = brokenCrackedDamaged,
            onValueChange = onBrokenCrackedDamagedChange,
            label = "Partidos, Quebrados e Amassados (g)",
            focusRequester = brokenCrackedDamagedFocus,
            nextFocus = greenishFocus
        )
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = greenish,
            onValueChange = onGreenishChange,
            label = "Esverdeados (g)",
            focusRequester = greenishFocus,
            nextFocus = if (isSoja) null else null // Vai para Classe de Cor se for Soja
        )

        Spacer(modifier = Modifier.height(24.dp))

        // --- 2. CLASSIFICAÇÃO DE COR ---
        if (isSoja) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = doesDefineColorClass, onCheckedChange = onDoesDefineColorClassChange)
                Spacer(modifier = Modifier.width(8.dp))
                // Renomeado para refletir a classe
                Text("Definir Classe de Cor?")
            }
        }


        if (doesDefineColorClass && isSoja) {
            Spacer(modifier = Modifier.height(16.dp))

            // Peso da Amostra de Grãos Sadios (read-only)
            OutlinedTextField(
                value = cleanSampleWeight, // String de exibição
                onValueChange = {}, // Read-only
                label = { Text("Peso da Amostra de Grãos Sadios (g)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                readOnly = true,
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Determinação de Classe de Cor", style = MaterialTheme.typography.titleSmall)

            NumberInputField(
                value = yellowGrainsWeight,
                onValueChange = onYellowGrainsWeightChange,
                label = "Peso de grãos amarelos (g)",
                focusRequester = yellowGrainsFocus,
                nextFocus = otherColorsFocus
            )
            Spacer(modifier = Modifier.height(16.dp))

            NumberInputField(
                value = otherColorsGrainsWeight,
                onValueChange = onOtherColorsGrainsWeightChange,
                label = "Peso de grãos de outras cores (g)",
                focusRequester = otherColorsFocus,
                nextFocus = null // Último campo da tela
            )
        } else if (!isSoja) {
            Text("Nenhum critério adicional nesta aba para Milho.", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// --- FUNÇÕES AUXILIARES (MANTIDAS) ---
// -------------------------------------------------------------------------------------------------

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
            onDone = {
                onDone?.invoke()
                keyboardController?.hide()
            }
        ),
        singleLine = true,
        enabled = enabled,
        readOnly = !enabled
    )
}

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