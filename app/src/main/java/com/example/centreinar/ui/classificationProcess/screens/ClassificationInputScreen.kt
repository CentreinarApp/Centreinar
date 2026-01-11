package com.example.centreinar.ui.classificationProcess.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.SampleSoja
import com.example.centreinar.data.local.entity.SampleMilho
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import java.math.RoundingMode
import kotlin.math.roundToInt

// -------------------------------------------------------------------------------------------------
// --- TELA PRINCIPAL ----
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

    // INPUT CONDICIONAL: Peso da Amostra Limpa (usado APENAS se definir classe de cor)
    var cleanSampleWeightInput by remember { mutableStateOf("") }

    // CAMPOS DEFEITOS GERAIS
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var greenish by remember { mutableStateOf("") }

    var burnt by remember { mutableStateOf("") }
    var sour by remember { mutableStateOf("") }
    var moldy by remember { mutableStateOf("") }
    var fermented by remember { mutableStateOf("") }
    var germinated by remember { mutableStateOf("") }
    var immature by remember { mutableStateOf("") }
    var shriveled by remember { mutableStateOf("") }

    // CAMPOS DEFEITOS DE SOMA
    var damaged by remember { mutableStateOf("") } // Total de Danificados (Soma final Soja)
    var piercingInput by remember { mutableStateOf("") }
    var damagedInput by remember { mutableStateOf("") } // Demais danificados (Soja)

    var doesDefineColorClass by remember { mutableStateOf(false) }
    var gessado by remember { mutableStateOf("") } // Milho
    var carunchado by remember { mutableStateOf("") } // Milho

    // CAMPOS CLASSE DE COR (SOJA)
    var otherColorsGrainsWeight by remember { mutableStateOf("") }
    var colorClassResult by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- NOVOS ESTADOS PARA CLASSE E GRUPO DO MILHO ---
    var defineClasseMilho by remember { mutableStateOf(false) }
    var weightAmarela by remember { mutableStateOf("") }
    var weightBranca by remember { mutableStateOf("") }
    var weightCores by remember { mutableStateOf("") }

    var defineGrupoMilho by remember { mutableStateOf(false) }
    var weightDuro by remember { mutableStateOf("") }
    var weightDentado by remember { mutableStateOf("") }
    var weightSemiduro by remember { mutableStateOf("") }

    // CÁLCULO DA AMOSTRA LIMPA (FLUXO NORMAL - AUTOMÁTICO NA TAB 0)
    val calculatedCleanSampleWeightFloat: Float by remember(sampleWeight, foreignMatters) {
        val sWeight = sampleWeight.toFloatOrZero()
        val fm = foreignMatters.toFloatOrZero()
        mutableStateOf(sWeight - fm)
    }

    // CÁLCULO DA AMOSTRA LIMPA (Para Exibição TAB 0 - String formatada)
    val calculatedCleanSampleWeightDisplay: String = remember(calculatedCleanSampleWeightFloat) {
        if (calculatedCleanSampleWeightFloat > 0) "%.2f".format(calculatedCleanSampleWeightFloat) else "0.00"
    }

    // VARIÁVEL BASE PARA CÁLCULOS (USA INPUT MANUAL SE DEFINIR CLASSE DE COR)
    val baseCleanWeightForColor: Float = remember(doesDefineColorClass, cleanSampleWeightInput, calculatedCleanSampleWeightFloat) {
        if (isSoja && doesDefineColorClass) {
            cleanSampleWeightInput.toFloatOrZero()
        } else {
            calculatedCleanSampleWeightFloat
        }
    }

    // NOVO CÁLCULO: Peso de Grãos Amarelos (Soja)
    val yellowGrainsDisplay: String by remember(baseCleanWeightForColor, otherColorsGrainsWeight) {
        val cleanWeight = baseCleanWeightForColor
        val otherColors = otherColorsGrainsWeight.toFloatOrZero()
        val yellowGrains = cleanWeight - otherColors
        mutableStateOf(if (yellowGrains > 0) "%.2f".format(yellowGrains) else "0.00")
    }
    val yellowGrainsFloat: Float = yellowGrainsDisplay.toFloatOrZero()


    // Requesters de Foco
    val lotWeightFocus = remember { FocusRequester() }
    val sampleWeightFocus = remember { FocusRequester() }
    val moistureFocus = remember { FocusRequester() }
    val impuritiesFocus = remember { FocusRequester() }
    val cleanSampleWeightInputFocus = remember { FocusRequester() }
    val brokenFocus = remember { FocusRequester() }
    val greenishFocus = remember { FocusRequester() }
    val sourFocus = remember { FocusRequester() }
    val burntFocus = remember { FocusRequester() }
    val moldyFocus = remember { FocusRequester() }
    val carunchadoFocus = remember { FocusRequester() }
    val insectFocus = remember { FocusRequester() }
    val damagedFocus = remember { FocusRequester() }
    val otherColorsFocus = remember { FocusRequester() }
    val gessadoFocus = remember { FocusRequester() }


    val tabTitles = listOf("Informação Básica", "Avariados", "Defeitos Finais")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> lotWeightFocus.requestFocus()
            1 -> sourFocus.requestFocus()
            2 -> brokenFocus.requestFocus()
        }
    }

    // Cálculo e Definição da Classe de Cor (Soja)
    LaunchedEffect(doesDefineColorClass, baseCleanWeightForColor, otherColorsGrainsWeight) {
        if (doesDefineColorClass && isSoja) {
            val totalSample = baseCleanWeightForColor
            val otherColors = otherColorsGrainsWeight.toFloatOrZero()
            if (totalSample > 0.01f && otherColors >= 0.0f) {
                val otherColorPct = (otherColors / totalSample) * 100f
                colorClassResult = if (otherColorPct.roundToInt() <= 10) "Classe Amarela" else "Classe Misturada"
            } else {
                colorClassResult = null
            }
        } else {
            colorClassResult = null
        }
    }


    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(selected = selectedTab == index, onClick = { selectedTab = index }, text = { Text(text = title) })
            }
        }

        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text("Insira os dados de classificação", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(5.dp))
        }

        Box(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(8.dp)) {
            when (selectedTab) {
                0 -> BasicInfoTabClassification(
                    lotWeight = lotWeight, onLotWeightChange = { lotWeight = it },
                    sampleWeight = sampleWeight, onSampleWeightChange = { sampleWeight = it },
                    moisture = humidity, onMoistureChange = { humidity = it },
                    impurities = foreignMatters, onImpuritiesChange = { foreignMatters = it },
                    cleanSampleWeight = calculatedCleanSampleWeightDisplay,
                    lotWeightFocus = lotWeightFocus, sampleWeightFocus = sampleWeightFocus,
                    moistureFocus = moistureFocus, impuritiesFocus = impuritiesFocus
                )

                1 -> DefectsTab1(
                    isMilho = isMilho,
                    burnt = burnt, onBurntChange = { burnt = it },
                    sour = sour, onSourChange = { sour = it },
                    moldy = moldy, onMoldyChange = { moldy = it },
                    shriveled = shriveled, onShriveledChange = { shriveled = it },
                    fermented = fermented, onFermentedChange = { fermented = it },
                    germinated = germinated, onGerminatedChange = { germinated = it },
                    immature = immature, onImmatureChange = { immature = it },
                    gessado = gessado, onGessadoChange = { gessado = it },
                    carunchado = carunchado, onCarunchadoChange = { carunchado = it },
                    burntFocus = burntFocus, sourFocus = sourFocus, moldyFocus = moldyFocus,
                    shriveledFocus = remember { FocusRequester() },
                    fermentedFocus = remember { FocusRequester() },
                    germinatedFocus = remember { FocusRequester() },
                    immatureFocus = remember { FocusRequester() },
                    gessadoFocus = gessadoFocus, carunchadoFocus = carunchadoFocus,
                    piercingInput = piercingInput,
                    onPiercingInputChange = {
                        piercingInput = it
                        val calculatedPiercing = (it.toFloatOrZero() / 4f).toString()
                        damaged = calculateDamagedSum(damagedInput, calculatedPiercing)
                    },
                    damagedInput = damagedInput,
                    onDamagedInputChange = {
                        damagedInput = it
                        val calculatedPiercing = (piercingInput.toFloatOrZero() / 4f).toString()
                        damaged = calculateDamagedSum(it, calculatedPiercing)
                    },
                    damaged = damaged, onDamagedChange = { damaged = it },
                    insectFocus = insectFocus, damagedFocus = damagedFocus
                )

                2 -> DefectsTab2(
                    isSoja = isSoja, isMilho = isMilho,
                    brokenCrackedDamaged = brokenCrackedDamaged, onBrokenCrackedDamagedChange = { brokenCrackedDamaged = it },
                    greenish = greenish, onGreenishChange = { greenish = it },
                    brokenCrackedDamagedFocus = brokenFocus, greenishFocus = greenishFocus,
                    doesDefineColorClass = doesDefineColorClass,
                    onDoesDefineColorClassChange = {
                        doesDefineColorClass = it
                        if (!it) cleanSampleWeightInput = ""
                    },
                    cleanSampleWeightInput = cleanSampleWeightInput, onCleanSampleWeightChange = { cleanSampleWeightInput = it },
                    cleanSampleWeightInputFocus = cleanSampleWeightInputFocus,
                    yellowGrainsDisplay = yellowGrainsDisplay,
                    otherColorsGrainsWeight = otherColorsGrainsWeight, onOtherColorsGrainsWeightChange = { otherColorsGrainsWeight = it },
                    otherColorsFocus = otherColorsFocus,
                    // Parâmetros Milho
                    milhoClasse = defineClasseMilho, onMilhoClasseToggle = { defineClasseMilho = it },
                    mAmarela = weightAmarela, onMAmarela = { weightAmarela = it },
                    mBranca = weightBranca, onMBranca = { weightBranca = it },
                    mCores = weightCores, onMCores = { weightCores = it },
                    milhoGrupo = defineGrupoMilho, onMilhoGrupoToggle = { defineGrupoMilho = it },
                    mDuro = weightDuro, onMDuro = { weightDuro = it },
                    mDentado = weightDentado, onMDentado = { weightDentado = it },
                    mSemi = weightSemiduro, onMSemi = { weightSemiduro = it }
                )
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            if (selectedTab > 0) Button(onClick = { selectedTab-- }) { Text("Voltar") } else Spacer(modifier = Modifier.weight(1f))

            if (selectedTab < tabTitles.lastIndex) {
                Button(onClick = { selectedTab++ }) { Text("Avançar") }
            } else {
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        val group = currentGroup ?: 1

                        // Validação do peso
                        if (baseCleanWeightForColor <= 0f) {
                            errorMessage = "O Peso da Amostra Limpa não pode ser zero ou negativo."
                            return@Button
                        }

                        if (isSoja) {
                            // --- LÓGICA SOJA ---
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
                            viewModel.classifySample(sample)

                            // Navega para resultado de SOJA
                            navController.navigate("classificationResult")

                        } else {
                            // --- LÓGICA MILHO ---
                            val sample = SampleMilho(
                                grain = "Milho",
                                group = group,
                                lotWeight = lotWeight.toFloatOrZero(),
                                sampleWeight = sampleWeight.toFloatOrZero(),
                                humidity = humidity.toFloatOrZero(),
                                cleanWeight = baseCleanWeightForColor,
                                impurities = foreignMatters.toFloatOrZero(), // Matéria estranha
                                broken = brokenCrackedDamaged.toFloatOrZero(), // Quebrados
                                carunchado = carunchado.toFloatOrZero(),
                                ardido = sour.toFloatOrZero(), // Ardidos
                                mofado = moldy.toFloatOrZero(),
                                fermented = fermented.toFloatOrZero(),
                                germinated = germinated.toFloatOrZero(),
                                immature = immature.toFloatOrZero(), // Chochos e Imaturos
                                gessado = gessado.toFloatOrZero()
                            )

                            // Chama a função de calcular no ViewModel
                            viewModel.classifySample(sample)

                            Log.d("ClassificationInput", "Milho Enviado para cálculo.")

                            // Navega para a tela de resultado de MILHO
                            navController.navigate("milhoResultado")
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Classificar")
                }
            }
        }

        if (errorMessage != null) {
            Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(16.dp))
        }
    }
}

// -------------------------------------------------------------------------------------------------
// --- TAB 0: INFORMAÇÃO BÁSICA ---
// -------------------------------------------------------------------------------------------------

@Composable
fun BasicInfoTabClassification(
    lotWeight: String, onLotWeightChange: (String) -> Unit,
    sampleWeight: String, onSampleWeightChange: (String) -> Unit,
    moisture: String, onMoistureChange: (String) -> Unit,
    impurities: String, onImpuritiesChange: (String) -> Unit,
    cleanSampleWeight: String,
    lotWeightFocus: FocusRequester, sampleWeightFocus: FocusRequester,
    moistureFocus: FocusRequester, impuritiesFocus: FocusRequester,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        NumberInputField(lotWeight, onLotWeightChange, "Peso do Lote (kg)", lotWeightFocus, sampleWeightFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(sampleWeight, onSampleWeightChange, "Peso da amostra de trabalho (g)", sampleWeightFocus, moistureFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(moisture, onMoistureChange, "Umidade (%)", moistureFocus, impuritiesFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(impurities, onImpuritiesChange, "Matéria Estranha e Impurezas (g)", impuritiesFocus, null)
        Spacer(modifier = Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Peso da Amostra Limpa (Trabalho - Impurezas):", style = MaterialTheme.typography.bodyMedium)
            Text("$cleanSampleWeight g", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = MaterialTheme.colorScheme.primary)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// --- TAB 1: AVARIADOS ---
// -------------------------------------------------------------------------------------------------

@Composable
fun DefectsTab1(
    isMilho: Boolean,
    burnt: String, onBurntChange: (String) -> Unit,
    sour: String, onSourChange: (String) -> Unit,
    moldy: String, onMoldyChange: (String) -> Unit,
    shriveled: String, onShriveledChange: (String) -> Unit,
    fermented: String, onFermentedChange: (String) -> Unit,
    germinated: String, onGerminatedChange: (String) -> Unit,
    immature: String, onImmatureChange: (String) -> Unit,
    gessado: String, onGessadoChange: (String) -> Unit,
    carunchado: String, onCarunchadoChange: (String) -> Unit,
    burntFocus: FocusRequester, sourFocus: FocusRequester, moldyFocus: FocusRequester,
    shriveledFocus: FocusRequester, fermentedFocus: FocusRequester, germinatedFocus: FocusRequester,
    immatureFocus: FocusRequester, gessadoFocus: FocusRequester, carunchadoFocus: FocusRequester,
    piercingInput: String, onPiercingInputChange: (String) -> Unit,
    damagedInput: String, onDamagedInputChange: (String) -> Unit,
    damaged: String, onDamagedChange: (String) -> Unit,
    insectFocus: FocusRequester, damagedFocus: FocusRequester,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Ordem do Milho ajustada conforme laudo: Ardidos -> Mofados -> Fermentados -> Germinados -> Chochos/Imaturos -> Gessados
        NumberInputField(sour, onSourChange, "Ardidos (g)", sourFocus, if (!isMilho) burntFocus else moldyFocus)
        Spacer(modifier = Modifier.height(16.dp))

        if (!isMilho) {
            NumberInputField(burnt, onBurntChange, "Queimados (g)", burntFocus, moldyFocus)
            Spacer(modifier = Modifier.height(16.dp))
        }

        NumberInputField(moldy, onMoldyChange, "Mofados (g)", moldyFocus, fermentedFocus)
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(fermented, onFermentedChange, "Fermentados (g)", fermentedFocus, germinatedFocus)
        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(germinated, onGerminatedChange, "Germinados (g)", germinatedFocus, immatureFocus)
        Spacer(modifier = Modifier.height(16.dp))

        // Nome unificado para Milho: Chochos e Imaturos
        NumberInputField(immature, onImmatureChange, if (isMilho) "Chochos e Imaturos (g)" else "Imaturos (g)", immatureFocus, if (isMilho) gessadoFocus else shriveledFocus)
        Spacer(modifier = Modifier.height(16.dp))

        if (isMilho) {
            NumberInputField(gessado, onGessadoChange, "Gessados (g)", gessadoFocus, carunchadoFocus)
            Spacer(modifier = Modifier.height(16.dp))
            NumberInputField(carunchado, onCarunchadoChange, "Grãos Carunchados (g)", carunchadoFocus, null)
        } else {
            NumberInputField(shriveled, onShriveledChange, "Chochos (g)", shriveledFocus, insectFocus)
            Spacer(modifier = Modifier.height(24.dp))
            Text("Danificados", style = MaterialTheme.typography.titleMedium)
            NumberInputField(piercingInput, onPiercingInputChange, "Grãos picados (a)", insectFocus, damagedFocus)
            Spacer(modifier = Modifier.height(16.dp))
            NumberInputField(damagedInput, onDamagedInputChange, "Demais grãos danificados (b)", damagedFocus, null)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = damaged, onValueChange = onDamagedChange, label = { Text("Total de Danificados [(a)+(b)]") }, modifier = Modifier.fillMaxWidth(), readOnly = true, enabled = false)
        }
    }
}

// -------------------------------------------------------------------------------------------------
// --- TAB 2: DEFEITOS FINAIS ---
// -------------------------------------------------------------------------------------------------

@Composable
fun DefectsTab2(
    isSoja: Boolean, isMilho: Boolean,
    brokenCrackedDamaged: String, onBrokenCrackedDamagedChange: (String) -> Unit,
    greenish: String, onGreenishChange: (String) -> Unit,
    brokenCrackedDamagedFocus: FocusRequester, greenishFocus: FocusRequester,
    doesDefineColorClass: Boolean, onDoesDefineColorClassChange: (Boolean) -> Unit,
    cleanSampleWeightInput: String, onCleanSampleWeightChange: (String) -> Unit,
    cleanSampleWeightInputFocus: FocusRequester,
    yellowGrainsDisplay: String,
    otherColorsGrainsWeight: String, onOtherColorsGrainsWeightChange: (String) -> Unit,
    otherColorsFocus: FocusRequester,
    // Novos parâmetros Milho
    milhoClasse: Boolean, onMilhoClasseToggle: (Boolean) -> Unit,
    mAmarela: String, onMAmarela: (String) -> Unit,
    mBranca: String, onMBranca: (String) -> Unit,
    mCores: String, onMCores: (String) -> Unit,
    milhoGrupo: Boolean, onMilhoGrupoToggle: (Boolean) -> Unit,
    mDuro: String, onMDuro: (String) -> Unit,
    mDentado: String, onMDentado: (String) -> Unit,
    mSemi: String, onMSemi: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Defeitos Finais", style = MaterialTheme.typography.titleMedium)
        NumberInputField(brokenCrackedDamaged, onBrokenCrackedDamagedChange, if (isMilho) "Grãos Quebrados (g)" else "Partidos, Quebrados e Amassados (g)", brokenCrackedDamagedFocus, if (isSoja) greenishFocus else null)

        if (isSoja) {
            Spacer(modifier = Modifier.height(16.dp))
            NumberInputField(greenish, onGreenishChange, "Esverdeados (g)", greenishFocus, null)
            Spacer(modifier = Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = doesDefineColorClass, onCheckedChange = onDoesDefineColorClassChange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Definir Classe?")
            }
            if (doesDefineColorClass) {
                Spacer(modifier = Modifier.height(16.dp))
                NumberInputField(cleanSampleWeightInput, onCleanSampleWeightChange, "Peso da Amostra Limpa (Base Cor) (g)", cleanSampleWeightInputFocus, otherColorsFocus)
                Spacer(modifier = Modifier.height(16.dp))
                NumberInputField(otherColorsGrainsWeight, onOtherColorsGrainsWeightChange, "Peso grãos outras cores (g)", otherColorsFocus, null)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(value = yellowGrainsDisplay, onValueChange = {}, label = { Text("Peso de grãos amarelos (g) (Calculado)") }, modifier = Modifier.fillMaxWidth(), readOnly = true, enabled = false)
            }
        }

        if (isMilho) {
            // --- 1º CLASSE (AMARELA/BRANCA/CORES) ---
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = milhoClasse, onCheckedChange = onMilhoClasseToggle)
                Text(" Definir Classe (Cor) do Milho", fontWeight = FontWeight.Bold)
            }
            if (milhoClasse) {
                NumberInputField(mAmarela, onMAmarela, "Peso Amarela (g)", remember { FocusRequester() }, null)
                NumberInputField(mBranca, onMBranca, "Peso Branca (g)", remember { FocusRequester() }, null)
                NumberInputField(mCores, onMCores, "Peso Cores (Misturada) (g)", remember { FocusRequester() }, null)
                val totalC = mAmarela.toFloatOrZero() + mBranca.toFloatOrZero() + mCores.toFloatOrZero()
                if (totalC > 0) {
                    val pA = (mAmarela.toFloatOrZero() / totalC) * 100
                    val pB = (mBranca.toFloatOrZero() / totalC) * 100
                    val resC = when {
                        pA >= 95f -> "CLASSE AMARELA"
                        pB >= 95f -> "CLASSE BRANCA"
                        else -> "CLASSE CORES"
                    }
                    Card(Modifier.padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) { Text("Resultado Classe: $resC", fontWeight = FontWeight.ExtraBold) }
                    }
                }
            }

            // --- 2º GRUPO (DURO/DENTADO/SEMIDURO) ---
            Spacer(Modifier.height(24.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = milhoGrupo, onCheckedChange = onMilhoGrupoToggle)
                Text(" Definir Grupo (Forma) do Milho", fontWeight = FontWeight.Bold)
            }
            if (milhoGrupo) {
                NumberInputField(mDuro, onMDuro, "Peso Duro (g)", remember { FocusRequester() }, null)
                NumberInputField(mDentado, onMDentado, "Peso Dentado (g)", remember { FocusRequester() }, null)
                NumberInputField(mSemi, onMSemi, "Peso Semiduro (g)", remember { FocusRequester() }, null)
                val totalG = mDuro.toFloatOrZero() + mDentado.toFloatOrZero() + mSemi.toFloatOrZero()
                if (totalG > 0) {
                    val pD = (mDuro.toFloatOrZero() / totalG) * 100
                    val pDt = (mDentado.toFloatOrZero() / totalG) * 100
                    val resG = when {
                        pD >= 85f -> "GRUPO DURO"
                        pDt >= 85f -> "GRUPO DENTADO"
                        else -> "GRUPO SEMIDURO"
                    }
                    Card(Modifier.padding(vertical = 8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) { Text("Resultado Grupo: $resG", fontWeight = FontWeight.ExtraBold) }
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------------------------------------------
// --- FUNÇÕES AUXILIARES ---
// -------------------------------------------------------------------------------------------------

@Composable
private fun NumberInputField(
    value: String, onValueChange: (String) -> Unit, label: String,
    focusRequester: FocusRequester, nextFocus: FocusRequester? = null,
    onDone: (() -> Unit)? = null, enabled: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.matches(Regex("^(\\d*\\.?\\d*)$"))) onValueChange(it) },
        label = { Text(label) }, modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done),
        keyboardActions = KeyboardActions(onNext = { nextFocus?.requestFocus() }, onDone = { onDone?.invoke(); keyboardController?.hide(); focusManager.clearFocus() }),
        singleLine = true, enabled = enabled, readOnly = !enabled
    )
}

private fun String.toFloatOrZero(): Float {
    return this.toBigDecimalOrNull()?.setScale(2, RoundingMode.HALF_UP)?.toFloat() ?: 0f
}

private fun calculateDamagedSum(damagedInput: String, piercingDamaged: String): String {
    val dInput = damagedInput.toFloatOrZero()
    val pDamaged = piercingDamaged.toFloatOrZero()
    val sum = dInput + pDamaged
    return "%.2f".format(sum)
}