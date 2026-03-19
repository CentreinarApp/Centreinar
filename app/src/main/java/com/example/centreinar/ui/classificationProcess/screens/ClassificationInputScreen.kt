package com.example.centreinar.ui.classificationProcess.screens

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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationInputState
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.util.Routes
import com.example.centreinar.util.roundToOneDecimal
import com.example.centreinar.util.toFloatOrZero
import com.example.centreinar.util.toUniversalString
import kotlin.math.roundToInt

// =============================================================================
// TELA PRINCIPAL
// =============================================================================

@Composable
fun ClassificationInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val descriptor = viewModel.currentDescriptor ?: return
    val currentGroup = viewModel.selectedGroup ?: 1
    val focusManager = LocalFocusManager.current

    // Informação Básica
    var lotWeight     by remember { mutableStateOf("") }
    var sampleWeight  by remember { mutableStateOf("") }
    var moisture      by remember { mutableStateOf("") }
    var foreignMatters by remember { mutableStateOf("") }
    var cleanSampleWeightInput by remember { mutableStateOf("") }

    // Avariados
    var sour       by remember { mutableStateOf("") }
    var burnt      by remember { mutableStateOf("") }
    var moldy      by remember { mutableStateOf("") }
    var fermented  by remember { mutableStateOf("") }
    var germinated by remember { mutableStateOf("") }
    var immature   by remember { mutableStateOf("") }
    var shriveled  by remember { mutableStateOf("") }
    var gessado    by remember { mutableStateOf("") }
    var piercingInput by remember { mutableStateOf("") }
    var damagedInput  by remember { mutableStateOf("") }
    var damaged       by remember { mutableStateOf("") }

    // Defeitos Finais
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var greenish   by remember { mutableStateOf("") }
    var carunchado by remember { mutableStateOf("") }

    // Classe de Cor
    var doesDefineColorClass    by remember { mutableStateOf(false) }
    var otherColorsGrainsWeight by remember { mutableStateOf("") }

    // Classe e Grupo
    var defineClasseMilho by remember { mutableStateOf(false) }
    var weightAmarela     by remember { mutableStateOf("") }
    var weightBranca      by remember { mutableStateOf("") }
    var weightCores       by remember { mutableStateOf("") }
    var defineGrupoMilho  by remember { mutableStateOf(false) }
    var weightDuro        by remember { mutableStateOf("") }
    var weightDentado     by remember { mutableStateOf("") }
    var weightSemiduro    by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Cálculos derivados
    val calculatedCleanWeight: Float by remember(sampleWeight, foreignMatters) {
        val sw = sampleWeight.toFloatOrZero()
        val fm = foreignMatters.toFloatOrZero()
        mutableFloatStateOf((sw - fm).roundToOneDecimal())
    }

    val calculatedCleanWeightDisplay: String = remember(calculatedCleanWeight) {
        if (calculatedCleanWeight > 0) calculatedCleanWeight.toUniversalString() else "0.00"
    }

    val baseCleanWeightForColor: Float = remember(
        doesDefineColorClass, cleanSampleWeightInput, calculatedCleanWeight
    ) {
        if (descriptor.supportsColorClass && doesDefineColorClass)
            cleanSampleWeightInput.toFloatOrZero()
        else
            calculatedCleanWeight
    }

    val yellowGrainsDisplay: String by remember(baseCleanWeightForColor, otherColorsGrainsWeight) {
        val yellow = baseCleanWeightForColor - otherColorsGrainsWeight.toFloatOrZero()
        mutableStateOf(if (yellow > 0) yellow.toUniversalString() else "0.00")
    }

    // Focus requesters
    val lotWeightFocus              = remember { FocusRequester() }
    val sampleWeightFocus           = remember { FocusRequester() }
    val moistureFocus               = remember { FocusRequester() }
    val impuritiesFocus             = remember { FocusRequester() }
    val cleanSampleWeightInputFocus = remember { FocusRequester() }
    val brokenFocus                 = remember { FocusRequester() }
    val greenishFocus               = remember { FocusRequester() }
    val sourFocus                   = remember { FocusRequester() }
    val burntFocus                  = remember { FocusRequester() }
    val moldyFocus                  = remember { FocusRequester() }
    val carunchadoFocus             = remember { FocusRequester() }
    val insectFocus                 = remember { FocusRequester() }
    val damagedFocus                = remember { FocusRequester() }
    val otherColorsFocus            = remember { FocusRequester() }
    val gessadoFocus                = remember { FocusRequester() }
    val focusBranca                 = remember { FocusRequester() }
    val focusCores                  = remember { FocusRequester() }
    val focusDuro                   = remember { FocusRequester() }
    val focusDentado                = remember { FocusRequester() }
    val focusSemi                   = remember { FocusRequester() }

    val tabTitles = listOf("Informação Básica", "Avariados", "Defeitos Finais")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> lotWeightFocus.requestFocus()
            1 -> sourFocus.requestFocus()
            2 -> if (descriptor.supportsCarunchado) carunchadoFocus.requestFocus()
            else brokenFocus.requestFocus()
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = { Text(title) }
                    )
                }
            }

            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                Text("Insira os dados de classificação",
                    style = MaterialTheme.typography.headlineSmall)
                Spacer(modifier = Modifier.height(5.dp))
            }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(8.dp)
            ) {
                when (selectedTab) {
                    0 -> BasicInfoTab(
                        lotWeight               = lotWeight,
                        onLotWeightChange       = { lotWeight = it },
                        sampleWeight            = sampleWeight,
                        onSampleWeightChange    = { sampleWeight = it },
                        moisture                = moisture,
                        onMoistureChange        = { moisture = it },
                        impurities              = foreignMatters,
                        onImpuritiesChange      = { foreignMatters = it },
                        cleanSampleWeight       = calculatedCleanWeightDisplay,
                        lotWeightFocus          = lotWeightFocus,
                        sampleWeightFocus       = sampleWeightFocus,
                        moistureFocus           = moistureFocus,
                        impuritiesFocus         = impuritiesFocus,
                        showBroken              = descriptor.supportsCarunchado,
                        brokenCrackedDamaged    = brokenCrackedDamaged,
                        onBrokenChange          = { brokenCrackedDamaged = it },
                        brokenFocus             = brokenFocus
                    )

                    1 -> DefectsTab1(
                        supportsCarunchado      = descriptor.supportsCarunchado,
                        burnt                   = burnt,   onBurntChange    = { burnt = it },
                        sour                    = sour,    onSourChange     = { sour = it },
                        moldy                   = moldy,   onMoldyChange    = { moldy = it },
                        shriveled               = shriveled, onShriveledChange = { shriveled = it },
                        fermented               = fermented, onFermentedChange = { fermented = it },
                        germinated              = germinated, onGerminatedChange = { germinated = it },
                        immature                = immature, onImmatureChange  = { immature = it },
                        gessado                 = gessado, onGessadoChange   = { gessado = it },
                        burntFocus = burntFocus, sourFocus = sourFocus, moldyFocus = moldyFocus,
                        shriveledFocus          = remember { FocusRequester() },
                        fermentedFocus          = remember { FocusRequester() },
                        germinatedFocus         = remember { FocusRequester() },
                        immatureFocus           = remember { FocusRequester() },
                        gessadoFocus            = gessadoFocus,
                        piercingInput           = piercingInput,
                        onPiercingInputChange   = {
                            piercingInput = it
                            damaged = calculateDamagedSum(damagedInput, (it.toFloatOrZero() / 4f).toString())
                        },
                        damagedInput            = damagedInput,
                        onDamagedInputChange    = {
                            damagedInput = it
                            damaged = calculateDamagedSum(it, (piercingInput.toFloatOrZero() / 4f).toString())
                        },
                        damaged                 = damaged,
                        onDamagedChange         = { damaged = it },
                        insectFocus             = insectFocus,
                        damagedFocus            = damagedFocus
                    )

                    2 -> DefectsTab2(
                        descriptor              = descriptor,
                        brokenCrackedDamaged    = brokenCrackedDamaged,
                        onBrokenChange          = { brokenCrackedDamaged = it },
                        greenish                = greenish,
                        onGreenishChange        = { greenish = it },
                        carunchado              = carunchado,
                        onCarunchadoChange      = { carunchado = it },
                        carunchadoFocus         = carunchadoFocus,
                        brokenFocus             = brokenFocus,
                        greenishFocus           = greenishFocus,
                        doesDefineColorClass    = doesDefineColorClass,
                        onDoesDefineColorClassChange = {
                            doesDefineColorClass = it
                            if (!it) cleanSampleWeightInput = ""
                        },
                        cleanSampleWeightInput  = cleanSampleWeightInput,
                        onCleanSampleWeightChange = { cleanSampleWeightInput = it },
                        cleanSampleWeightInputFocus = cleanSampleWeightInputFocus,
                        yellowGrainsDisplay     = yellowGrainsDisplay,
                        otherColorsGrainsWeight = otherColorsGrainsWeight,
                        onOtherColorsChange     = { otherColorsGrainsWeight = it },
                        otherColorsFocus        = otherColorsFocus,
                        milhoClasse             = defineClasseMilho,
                        onMilhoClasseToggle     = { defineClasseMilho = it },
                        mAmarela = weightAmarela, onMAmarela = { weightAmarela = it },
                        mBranca  = weightBranca,  onMBranca  = { weightBranca = it },
                        mCores   = weightCores,   onMCores   = { weightCores = it },
                        milhoGrupo              = defineGrupoMilho,
                        onMilhoGrupoToggle      = { defineGrupoMilho = it },
                        mDuro    = weightDuro,    onMDuro    = { weightDuro = it },
                        mDentado = weightDentado, onMDentado = { weightDentado = it },
                        mSemi    = weightSemiduro, onMSemi   = { weightSemiduro = it },
                        focusBranca = focusBranca, focusCores = focusCores,
                        focusDuro   = focusDuro,   focusDentado = focusDentado,
                        focusSemi   = focusSemi
                    )
                }
            }

            // Navegação entre tabs e envio
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (selectedTab > 0)
                    Button(onClick = { selectedTab-- }) { Text("Voltar") }
                else
                    Spacer(modifier = Modifier.weight(1f))

                if (selectedTab < tabTitles.lastIndex) {
                    Button(onClick = { selectedTab++ }) { Text("Avançar") }
                } else {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick  = {
                            focusManager.clearFocus()

                            if (baseCleanWeightForColor <= 0f) {
                                errorMessage = "O Peso da Amostra Limpa não pode ser zero ou negativo."
                                return@Button
                            }

                            val inputState = ClassificationInputState(
                                group                = currentGroup,
                                lotWeight            = lotWeight.toFloatOrZero(),
                                sampleWeight         = sampleWeight.toFloatOrZero(),
                                moisture             = moisture.toFloatOrZero(),
                                foreignMatters       = foreignMatters.toFloatOrZero(),
                                cleanSampleWeight    = baseCleanWeightForColor,
                                sour                 = sour.toFloatOrZero(),
                                burnt                = burnt.toFloatOrZero(),
                                moldy                = moldy.toFloatOrZero(),
                                fermented            = fermented.toFloatOrZero(),
                                germinated           = germinated.toFloatOrZero(),
                                immature             = immature.toFloatOrZero(),
                                shriveled            = shriveled.toFloatOrZero(),
                                damaged              = damaged.toFloatOrZero(),
                                gessado              = gessado.toFloatOrZero(),
                                brokenCrackedDamaged = brokenCrackedDamaged.toFloatOrZero(),
                                greenish             = greenish.toFloatOrZero(),
                                carunchado           = carunchado.toFloatOrZero(),
                                isColorDefined       = doesDefineColorClass,
                                otherColorsWeight    = otherColorsGrainsWeight.toFloatOrZero(),
                                baseWeightForColor   = baseCleanWeightForColor,
                                shouldDefineClass    = defineClasseMilho,
                                weightYellow         = weightAmarela.toFloatOrZero(),
                                weightWhite          = weightBranca.toFloatOrZero(),
                                weightMixedColors    = weightCores.toFloatOrZero(),
                                shouldDefineGroup    = defineGrupoMilho,
                                weightHard           = weightDuro.toFloatOrZero(),
                                weightDent           = weightDentado.toFloatOrZero(),
                                weightSemiHard       = weightSemiduro.toFloatOrZero()
                            )

                            val payload = viewModel.buildPayload(inputState)
                                ?: run { errorMessage = "Grão não selecionado."; return@Button }

                            viewModel.classifySample(payload)
                            navController.navigate(Routes.CLASSIFICATION_RESULT)
                        }
                    ) { Text("Classificar") }
                }
            }

            errorMessage?.let {
                Text(
                    text     = it,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

// =============================================================================
// TAB 0 — INFORMAÇÃO BÁSICA
// =============================================================================

@Composable
private fun BasicInfoTab(
    lotWeight: String, onLotWeightChange: (String) -> Unit,
    sampleWeight: String, onSampleWeightChange: (String) -> Unit,
    moisture: String, onMoistureChange: (String) -> Unit,
    impurities: String, onImpuritiesChange: (String) -> Unit,
    cleanSampleWeight: String,
    lotWeightFocus: FocusRequester, sampleWeightFocus: FocusRequester,
    moistureFocus: FocusRequester, impuritiesFocus: FocusRequester,
    showBroken: Boolean,
    brokenCrackedDamaged: String, onBrokenChange: (String) -> Unit,
    brokenFocus: FocusRequester
) {
    Column(modifier = Modifier.padding(16.dp)) {
        NumberInputField(lotWeight, onLotWeightChange, "Peso do Lote (kg)", lotWeightFocus, sampleWeightFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(sampleWeight, onSampleWeightChange, "Peso da amostra de trabalho (g)", sampleWeightFocus, moistureFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(moisture, onMoistureChange, "Umidade (%)", moistureFocus, impuritiesFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(impurities, onImpuritiesChange, "Matéria Estranha e Impurezas (g)", impuritiesFocus, if (showBroken) brokenFocus else null)
        Spacer(Modifier.height(16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text("Peso da Amostra Limpa (Trabalho - Impurezas):", style = MaterialTheme.typography.bodyMedium)
            Text("$cleanSampleWeight g",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary)
        }
        if (showBroken) {
            Spacer(Modifier.height(24.dp))
            NumberInputField(brokenCrackedDamaged, onBrokenChange, "Grãos Quebrados (g)", brokenFocus, null)
        }
    }
}

// =============================================================================
// TAB 1 — AVARIADOS
// =============================================================================

@Composable
private fun DefectsTab1(
    supportsCarunchado: Boolean,
    burnt: String, onBurntChange: (String) -> Unit,
    sour: String, onSourChange: (String) -> Unit,
    moldy: String, onMoldyChange: (String) -> Unit,
    shriveled: String, onShriveledChange: (String) -> Unit,
    fermented: String, onFermentedChange: (String) -> Unit,
    germinated: String, onGerminatedChange: (String) -> Unit,
    immature: String, onImmatureChange: (String) -> Unit,
    gessado: String, onGessadoChange: (String) -> Unit,
    burntFocus: FocusRequester, sourFocus: FocusRequester, moldyFocus: FocusRequester,
    shriveledFocus: FocusRequester, fermentedFocus: FocusRequester,
    germinatedFocus: FocusRequester, immatureFocus: FocusRequester,
    gessadoFocus: FocusRequester,
    piercingInput: String, onPiercingInputChange: (String) -> Unit,
    damagedInput: String, onDamagedInputChange: (String) -> Unit,
    damaged: String, onDamagedChange: (String) -> Unit,
    insectFocus: FocusRequester, damagedFocus: FocusRequester
) {
    Column(modifier = Modifier.padding(16.dp)) {
        NumberInputField(sour, onSourChange, "Ardidos (g)", sourFocus,
            if (!supportsCarunchado) burntFocus else moldyFocus)
        Spacer(Modifier.height(16.dp))

        if (!supportsCarunchado) {
            NumberInputField(burnt, onBurntChange, "Queimados (g)", burntFocus, moldyFocus)
            Spacer(Modifier.height(16.dp))
        }

        NumberInputField(moldy, onMoldyChange, "Mofados (g)", moldyFocus, fermentedFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(fermented, onFermentedChange, "Fermentados (g)", fermentedFocus, germinatedFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(germinated, onGerminatedChange, "Germinados (g)", germinatedFocus, immatureFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(
            immature, onImmatureChange,
            if (supportsCarunchado) "Chochos e Imaturos (g)" else "Imaturos (g)",
            immatureFocus,
            if (supportsCarunchado) gessadoFocus else shriveledFocus
        )
        Spacer(Modifier.height(16.dp))

        if (supportsCarunchado) {
            NumberInputField(gessado, onGessadoChange, "Gessados (g)", gessadoFocus, null)
        } else {
            NumberInputField(shriveled, onShriveledChange, "Chochos (g)", shriveledFocus, insectFocus)
            Spacer(Modifier.height(24.dp))
            Text("Danificados", style = MaterialTheme.typography.titleMedium)
            NumberInputField(piercingInput, onPiercingInputChange, "Grãos picados (a)", insectFocus, damagedFocus)
            Spacer(Modifier.height(16.dp))
            NumberInputField(damagedInput, onDamagedInputChange, "Demais grãos danificados (b)", damagedFocus, null)
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value         = damaged,
                onValueChange = onDamagedChange,
                label         = { Text("Total de Danificados [(a)+(b)]") },
                modifier      = Modifier.fillMaxWidth(),
                readOnly      = true,
                enabled       = false
            )
        }
    }
}

// =============================================================================
// TAB 2 — DEFEITOS FINAIS
// =============================================================================

@Composable
private fun DefectsTab2(
    descriptor: GrainDescriptor,
    brokenCrackedDamaged: String, onBrokenChange: (String) -> Unit,
    greenish: String, onGreenishChange: (String) -> Unit,
    carunchado: String, onCarunchadoChange: (String) -> Unit,
    carunchadoFocus: FocusRequester, brokenFocus: FocusRequester,
    greenishFocus: FocusRequester,
    // Soja — classe de cor
    doesDefineColorClass: Boolean, onDoesDefineColorClassChange: (Boolean) -> Unit,
    cleanSampleWeightInput: String, onCleanSampleWeightChange: (String) -> Unit,
    cleanSampleWeightInputFocus: FocusRequester,
    yellowGrainsDisplay: String,
    otherColorsGrainsWeight: String, onOtherColorsChange: (String) -> Unit,
    otherColorsFocus: FocusRequester,
    // Milho — classe
    milhoClasse: Boolean, onMilhoClasseToggle: (Boolean) -> Unit,
    mAmarela: String, onMAmarela: (String) -> Unit,
    mBranca: String, onMBranca: (String) -> Unit, focusBranca: FocusRequester,
    mCores: String, onMCores: (String) -> Unit, focusCores: FocusRequester,
    // Milho — grupo
    milhoGrupo: Boolean, onMilhoGrupoToggle: (Boolean) -> Unit,
    mDuro: String, onMDuro: (String) -> Unit, focusDuro: FocusRequester,
    mDentado: String, onMDentado: (String) -> Unit, focusDentado: FocusRequester,
    mSemi: String, onMSemi: (String) -> Unit, focusSemi: FocusRequester
) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text("Defeitos Finais", style = MaterialTheme.typography.titleMedium)

        // Carunchados — controlado por descriptor.supportsCarunchado
        if (descriptor.supportsCarunchado) {
            NumberInputField(carunchado, onCarunchadoChange, "Grãos Carunchados (g)", carunchadoFocus, null)
            Spacer(Modifier.height(16.dp))
        }

        // PQA + Esverdeados + Classe de Cor — controlado por descriptor.supportsColorClass
        if (!descriptor.supportsCarunchado) {
            NumberInputField(brokenCrackedDamaged, onBrokenChange, "Partidos, Quebrados e Amassados (g)", brokenFocus, greenishFocus)
            Spacer(Modifier.height(16.dp))
            NumberInputField(greenish, onGreenishChange, "Esverdeados (g)", greenishFocus, null)
        }

        if (descriptor.supportsColorClass) {
            Spacer(Modifier.height(24.dp))
            SojaColorClassSection(
                doesDefineColorClass         = doesDefineColorClass,
                onDoesDefineColorClassChange = onDoesDefineColorClassChange,
                cleanSampleWeightInput       = cleanSampleWeightInput,
                onCleanSampleWeightChange    = onCleanSampleWeightChange,
                cleanSampleWeightInputFocus  = cleanSampleWeightInputFocus,
                yellowGrainsDisplay          = yellowGrainsDisplay,
                otherColorsGrainsWeight      = otherColorsGrainsWeight,
                onOtherColorsChange          = onOtherColorsChange,
                otherColorsFocus             = otherColorsFocus
            )
        }

        // Classe e Grupo do Milho — controlado por descriptor.supportsCarunchado
        if (descriptor.supportsCarunchado) {
            MilhoClasseGroupSection(
                milhoClasse        = milhoClasse,
                onMilhoClasseToggle = onMilhoClasseToggle,
                mAmarela = mAmarela, onMAmarela = onMAmarela,
                mBranca  = mBranca,  onMBranca  = onMBranca,  focusBranca = focusBranca,
                mCores   = mCores,   onMCores   = onMCores,   focusCores  = focusCores,
                milhoGrupo         = milhoGrupo,
                onMilhoGrupoToggle = onMilhoGrupoToggle,
                mDuro    = mDuro,    onMDuro    = onMDuro,    focusDuro    = focusDuro,
                mDentado = mDentado, onMDentado = onMDentado, focusDentado = focusDentado,
                mSemi    = mSemi,    onMSemi    = onMSemi,    focusSemi    = focusSemi
            )
        }
    }
}

// =============================================================================
// SEÇÃO CLASSE DE COR — SOJA
// =============================================================================

@Composable
private fun SojaColorClassSection(
    doesDefineColorClass: Boolean, onDoesDefineColorClassChange: (Boolean) -> Unit,
    cleanSampleWeightInput: String, onCleanSampleWeightChange: (String) -> Unit,
    cleanSampleWeightInputFocus: FocusRequester,
    yellowGrainsDisplay: String,
    otherColorsGrainsWeight: String, onOtherColorsChange: (String) -> Unit,
    otherColorsFocus: FocusRequester
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = doesDefineColorClass, onCheckedChange = onDoesDefineColorClassChange)
        Spacer(Modifier.width(8.dp))
        Text("Definir Classe?")
    }
    if (doesDefineColorClass) {
        Spacer(Modifier.height(16.dp))
        NumberInputField(cleanSampleWeightInput, onCleanSampleWeightChange,
            "Peso da Amostra Limpa (Base Cor) (g)", cleanSampleWeightInputFocus, otherColorsFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(otherColorsGrainsWeight, onOtherColorsChange,
            "Peso grãos outras cores (g)", otherColorsFocus, null)
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value         = yellowGrainsDisplay,
            onValueChange = {},
            label         = { Text("Peso de grãos amarelos (g) (Calculado)") },
            modifier      = Modifier.fillMaxWidth(),
            readOnly      = true,
            enabled       = false
        )
    }
}

// =============================================================================
// SEÇÃO CLASSE E GRUPO — MILHO
// =============================================================================

@Composable
private fun MilhoClasseGroupSection(
    milhoClasse: Boolean, onMilhoClasseToggle: (Boolean) -> Unit,
    mAmarela: String, onMAmarela: (String) -> Unit,
    mBranca: String, onMBranca: (String) -> Unit, focusBranca: FocusRequester,
    mCores: String, onMCores: (String) -> Unit, focusCores: FocusRequester,
    milhoGrupo: Boolean, onMilhoGrupoToggle: (Boolean) -> Unit,
    mDuro: String, onMDuro: (String) -> Unit, focusDuro: FocusRequester,
    mDentado: String, onMDentado: (String) -> Unit, focusDentado: FocusRequester,
    mSemi: String, onMSemi: (String) -> Unit, focusSemi: FocusRequester
) {
    // Classe
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = milhoClasse, onCheckedChange = onMilhoClasseToggle)
        Text(" Definir Classe (Cor) do Milho", fontWeight = FontWeight.Bold)
    }
    if (milhoClasse) {
        NumberInputField(mAmarela, onMAmarela, "Peso Amarela (g)", FocusRequester(), focusBranca)
        NumberInputField(mBranca,  onMBranca,  "Peso Branca (g)",  focusBranca,      focusCores)
        NumberInputField(mCores,   onMCores,   "Peso Cores (Misturada) (g)", focusCores, null)

        val totalC = mAmarela.toFloatOrZero() + mBranca.toFloatOrZero() + mCores.toFloatOrZero()
        if (totalC > 0) {
            val pA = (mAmarela.toFloatOrZero() / totalC) * 100
            val pB = (mBranca.toFloatOrZero()  / totalC) * 100
            val resC = when {
                pA >= 95f -> "CLASSE AMARELA"
                pB >= 95f -> "CLASSE BRANCA"
                else      -> "CLASSE CORES"
            }
            Card(
                modifier = Modifier.padding(vertical = 8.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)
            ) {
                Column(Modifier.padding(12.dp).fillMaxWidth()) {
                    Text("Resultado Classe: $resC", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }

    // Grupo
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = milhoGrupo, onCheckedChange = onMilhoGrupoToggle)
        Text(" Definir Grupo (Forma) do Milho", fontWeight = FontWeight.Bold)
    }
    if (milhoGrupo) {
        NumberInputField(mDuro,    onMDuro,    "Peso Duro (g)",     focusDuro,    focusDentado)
        NumberInputField(mDentado, onMDentado, "Peso Dentado (g)",  focusDentado, focusSemi)
        NumberInputField(mSemi,    onMSemi,    "Peso Semiduro (g)", focusSemi,    null)

        val totalG = mDuro.toFloatOrZero() + mDentado.toFloatOrZero() + mSemi.toFloatOrZero()
        if (totalG > 0) {
            val pD  = (mDuro.toFloatOrZero()    / totalG) * 100
            val pDt = (mDentado.toFloatOrZero() / totalG) * 100
            val resG = when {
                pD  >= 85f -> "GRUPO DURO"
                pDt >= 85f -> "GRUPO DENTADO"
                else       -> "GRUPO SEMIDURO"
            }
            Card(
                modifier = Modifier.padding(vertical = 8.dp),
                colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
            ) {
                Column(Modifier.padding(12.dp).fillMaxWidth()) {
                    Text("Resultado Grupo: $resG", fontWeight = FontWeight.ExtraBold)
                }
            }
        }
    }
}

// =============================================================================
// CAMPO NUMÉRICO
// =============================================================================

@Composable
private fun NumberInputField(
    value: String, onValueChange: (String) -> Unit, label: String,
    focusRequester: FocusRequester, nextFocus: FocusRequester? = null,
    onDone: (() -> Unit)? = null, enabled: Boolean = true
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager       = LocalFocusManager.current
    OutlinedTextField(
        value         = value,
        onValueChange = { raw ->
            val sanitized = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
            if (sanitized.count { it == '.' } <= 1) onValueChange(sanitized)
        },
        label           = { Text(label) },
        modifier        = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction    = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { nextFocus?.requestFocus() },
            onDone = { onDone?.invoke(); keyboardController?.hide(); focusManager.clearFocus() }
        ),
        singleLine = true,
        enabled    = enabled,
        readOnly   = !enabled
    )
}

// =============================================================================
// AUXILIAR
// =============================================================================

private fun calculateDamagedSum(damagedInput: String, piercingDamaged: String): String {
    val sum = damagedInput.toFloatOrZero() + piercingDamaged.toFloatOrZero()
    return sum.toUniversalString()
}