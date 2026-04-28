package com.example.centreinar.ui.main.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.centreinar.domain.model.ClassificationInputField
import com.example.centreinar.domain.model.ClassificationInputField.FieldType
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationInputState
import com.example.centreinar.ui.components.InfoDialog
import com.example.centreinar.ui.discount.strategy.DiscountInputField
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.roundToOneDecimal
import com.example.centreinar.util.sanitizeNumericInput
import com.example.centreinar.util.toFloatOrZero
import com.example.centreinar.util.toUniversalString

// =============================================================================
// MODO DA TELA — define qual fluxo está sendo executado
// =============================================================================

/**
 * Define o comportamento da [InputScreen] para cada fluxo.
 *
 * [Classification] — aba básica com peso/amostra/impurezas, campos dinâmicos
 * via [ClassificationInputField], botão "Classificar".
 *
 * [Discount] — aba básica com lote/preço/umidade, campos dinâmicos via
 * [DiscountInputField], botão "Calcular Desconto".
 */
sealed class InputScreenMode {

    data class Classification(
        val descriptor: GrainDescriptor,
        val inputFields: List<ClassificationInputField>,
        val currentGroup: Int,
        val calculateCleanWeight: (sampleWeight: Float, foreignMatters: Float, fieldValues: Map<String, Float>) -> Float,
        val cleanWeightCalculationText: String = "Peso da Amostra Limpa (Peso da Amostra de Trabalho - Peso da MEI):",
        val onClassify: (ClassificationInputState) -> Unit
    ) : InputScreenMode()

    data class Discount(
        val defectFields: List<DiscountInputField>,
        val selectedGroup: Int,
        val errorMessage: String?,
        val onCalculate: (
            fullMap: Map<String, Float>,
            priceBySack: Float,
            lotWeight: Float,
            daysOfStorage: Int,
            doesTechnicalLoss: Boolean,
            deductionValue: Float,
            doesDeduction: Boolean
        ) -> Unit
    ) : InputScreenMode()
}

// =============================================================================
// TELA GENÉRICA
// =============================================================================

/**
 * Tela genérica de entrada de dados — reutilizada no fluxo de classificação
 * e no fluxo de desconto.
 *
 * O [mode] determina qual aba básica exibir, quais campos dinâmicos carregar,
 * qual rótulo colocar no botão de confirmação e qual callback invocar.
 *
 * @param topBar TopBar do fluxo específico
 * @param mode   [InputScreenMode.Classification] ou [InputScreenMode.Discount]
 */
@Composable
fun InputScreen(
    topBar: @Composable () -> Unit,
    mode: InputScreenMode
) {
    val focusManager = LocalFocusManager.current

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700

    // ── Derivações de modo ────────────────────────────────────────────────────
    val isClassification = mode is InputScreenMode.Classification
    val descriptor       = (mode as? InputScreenMode.Classification)?.descriptor
    val classFields      = (mode as? InputScreenMode.Classification)?.inputFields ?: emptyList()
    val discountFields   = (mode as? InputScreenMode.Discount)?.defectFields ?: emptyList()

    val tabTitles = listOf("Informação Básica", "Avariados", "Defeitos Finais")

    val submitLabel = if (isClassification) "Classificar" else "Calcular Desconto"

    val cleanWeightText = (mode as? InputScreenMode.Classification)?.cleanWeightCalculationText
        ?: "Peso da Amostra Limpa:"

    // ── Campos dinâmicos ─────────────────────────────────────────────────────
    // Classificação usa ClassificationInputField; Desconto usa DiscountInputField
    val classNumericFields = remember(classFields) {
        classFields.filter { it.fieldType == FieldType.NUMERIC || it.fieldType == FieldType.CALCULATED }
    }
    var classFieldValues by remember(classFields) {
        mutableStateOf(classNumericFields.associate { it.key to "" })
    }

    var discountFieldValues by remember(discountFields) {
        mutableStateOf(discountFields.associate { it.key to "" })
    }

    // ── Informação Básica — classificação (fixos) ────────────────────────────
    var lotWeightClass         by remember { mutableStateOf("") }
    var sampleWeight           by remember { mutableStateOf("") }
    var moistureClass          by remember { mutableStateOf("") }
    var foreignMatters         by remember { mutableStateOf("") }
    var cleanSampleWeightInput by remember { mutableStateOf("") }

    // ── Informação Básica — desconto (fixos) ─────────────────────────────────
    var lotWeightDiscount by remember { mutableStateOf("") }
    var priceBySack       by remember { mutableStateOf("") }
    var moistureDiscount  by remember { mutableStateOf("") }
    var daysOfStorage     by remember { mutableStateOf("0") }
    var deductionValue    by remember { mutableStateOf("0") }
    var doesTechnicalLoss by remember { mutableStateOf(false) }
    var doesDeduction     by remember { mutableStateOf(false) }

    // ── Seção Classe de Cor (Soja) ───────────────────────────────────────────
    var doesDefineColorClass    by remember { mutableStateOf(false) }
    var otherColorsGrainsWeight by remember { mutableStateOf("") }

    // ── Seção Classe e Grupo (Milho) ─────────────────────────────────────────
    var defineClasseMilho      by remember { mutableStateOf(false) }
    var cleanWeightMilho       by remember { mutableStateOf("") }
    var weightAmarela          by remember { mutableStateOf("") }
    var weightBranca           by remember { mutableStateOf("") }
    var weightCores            by remember { mutableStateOf("") }
    var defineGrupoMilho       by remember { mutableStateOf(false) }
    var weightDuro             by remember { mutableStateOf("") }
    var weightDentado          by remember { mutableStateOf("") }
    var weightSemiduro         by remember { mutableStateOf("") }

    // ── Controle de bloqueio por limite de peso (Milho) ──────────────────────
    var classeInvalid by remember { mutableStateOf(false) }
    var grupoInvalid  by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Propaga erro externo (desconto)
    LaunchedEffect((mode as? InputScreenMode.Discount)?.errorMessage) {
        (mode as? InputScreenMode.Discount)?.errorMessage?.let { errorMessage = it }
    }

    // ── Cálculos derivados (classificação) ───────────────────────────────────
    val calculateCleanWeight = (mode as? InputScreenMode.Classification)?.calculateCleanWeight

    // Valor bruto — sem arredondamento, usado nos cálculos de defeito
    val calculatedCleanWeight: Float by remember(sampleWeight, foreignMatters, classFieldValues) {
        val sw     = sampleWeight.toFloatOrZero()
        val fm     = foreignMatters.toFloatOrZero()
        val fields = classFieldValues.mapValues { it.value.toFloatOrZero() }
        val result = calculateCleanWeight?.invoke(sw, fm, fields) ?: (sw - fm)
        mutableFloatStateOf(result)
    }

    // Valor arredondado — apenas para exibição na tela
    val calculatedCleanWeightDisplay = remember(calculatedCleanWeight) {
        if (calculatedCleanWeight > 0)
            calculatedCleanWeight.roundToOneDecimal().toUniversalString()
        else "0.00"
    }

    // Peso limpo para classificação (base dos percentuais de defeitos) — sempre o valor bruto
    val cleanWeightForClassification: Float = calculatedCleanWeight

    // Peso limpo exclusivo para cálculo de classe/cor/grupo — pode usar valor manual
    val baseCleanWeightForColor: Float = remember(
        doesDefineColorClass, cleanSampleWeightInput, calculatedCleanWeight,
        defineClasseMilho, defineGrupoMilho, cleanWeightMilho
    ) {
        when {
            descriptor?.supportsColorClass == true && doesDefineColorClass ->
                cleanSampleWeightInput.toFloatOrZero()
            descriptor?.supportsCarunchado == true && (defineClasseMilho || defineGrupoMilho) ->
                cleanWeightMilho.toFloatOrZero().takeIf { it > 0f } ?: calculatedCleanWeight
            else -> calculatedCleanWeight
        }
    }

    val yellowGrainsDisplay: String by remember(baseCleanWeightForColor, otherColorsGrainsWeight) {
        val yellow = baseCleanWeightForColor - otherColorsGrainsWeight.toFloatOrZero()
        mutableStateOf(if (yellow > 0) yellow.toUniversalString() else "0.00")
    }

    // Aviso de peso mínimo da amostra — apenas informativo, não bloqueia
    val minSampleWeight = descriptor?.minSampleWeight
    val sampleWeightBelowMin: Boolean by remember(sampleWeight, minSampleWeight) {
        val sw = sampleWeight.toFloatOrZero()
        mutableStateOf(minSampleWeight != null && sw > 0f && sw < minSampleWeight)
    }

    fun recalcDamaged() {
        val piercing = classFieldValues[FieldKeys.PIERCING]?.toFloatOrZero() ?: 0f
        val damInput = classFieldValues[FieldKeys.DAMAGED_INPUT]?.toFloatOrZero() ?: 0f
        classFieldValues = classFieldValues + (FieldKeys.DAMAGED to (damInput + piercing / 4f).toUniversalString())
    }

    // ── Focus requesters ──────────────────────────────────────────────────────
    val lotWeightFocus              = remember { FocusRequester() }
    val sampleWeightFocus           = remember { FocusRequester() }
    val moistureFocus               = remember { FocusRequester() }
    val impuritiesFocus             = remember { FocusRequester() }
    val priceBySackFocus            = remember { FocusRequester() }
    val cleanSampleWeightInputFocus = remember { FocusRequester() }
    val otherColorsFocus            = remember { FocusRequester() }
    val focusBranca                 = remember { FocusRequester() }
    val focusCores                  = remember { FocusRequester() }
    val cleanWeightMilhoFocus       = remember { FocusRequester() }
    val focusDuro                   = remember { FocusRequester() }
    val focusDentado                = remember { FocusRequester() }
    val focusSemi                   = remember { FocusRequester() }
    val daysOfStorageFocus          = remember { FocusRequester() }
    val deductionValueFocus         = remember { FocusRequester() }

    val classDynamicFocusMap = remember(classNumericFields) {
        classNumericFields.associate { it.key to FocusRequester() }
    }
    val discountFocusMap = remember(discountFields) {
        discountFields.associate { it.key to FocusRequester() }
    }

    // ── Campos por aba ────────────────────────────────────────────────────────
    val classTab1         = remember(classFields)    { classFields.filter { it.tabGroup == 1 } }
    val classTab2         = remember(classFields)    { classFields.filter { it.tabGroup == 2 } }
    val discountTab0Extra = remember(discountFields) { discountFields.filter { it.tabGroup == 0 } }
    val discountTab1      = remember(discountFields) { discountFields.filter { it.tabGroup == 1 } }
    val discountTab2      = remember(discountFields) { discountFields.filter { it.tabGroup == 2 } }

    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        when {
            isClassification && selectedTab == 0 -> lotWeightFocus.requestFocus()
            isClassification && selectedTab == 1 ->
                classTab1.firstOrNull { it.fieldType == FieldType.NUMERIC }
                    ?.key?.let { classDynamicFocusMap[it]?.requestFocus() }
            isClassification && selectedTab == 2 ->
                classTab2.firstOrNull { it.fieldType == FieldType.NUMERIC }
                    ?.key?.let { classDynamicFocusMap[it]?.requestFocus() }
            !isClassification && selectedTab == 0 -> lotWeightFocus.requestFocus()
            !isClassification && selectedTab == 1 ->
                discountTab1.firstOrNull()?.key?.let { discountFocusMap[it]?.requestFocus() }
            !isClassification && selectedTab == 2 ->
                discountTab2.firstOrNull()?.key?.let { discountFocusMap[it]?.requestFocus() }
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────────
    Scaffold(
        modifier = Modifier
            .fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Column( modifier = Modifier.fillMaxWidth()) { topBar() }

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // TabRow fica fixo fora do scroll
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor   = MaterialTheme.colorScheme.background,
                    contentColor     = MaterialTheme.colorScheme.onBackground
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick  = { selectedTab = index },
                            text     = { Text(title) }
                        )
                    }
                }

                // Conteúdo scrollável — ocupa todo o espaço restante acima dos botões
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = if (isSmallScreen) 8.dp else 16.dp, bottom = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = if (isSmallScreen) 4.dp else 8.dp, horizontal = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text      = if (isClassification) "Insira os dados de classificação" else "Insira os dados",
                            style     = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }

                    errorMessage?.let {
                        Text(it, color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp))
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        when (selectedTab) {
                            0 -> if (isClassification) {
                                ClassificationBasicTab(
                                    lotWeight            = lotWeightClass,
                                    onLotWeightChange    = { lotWeightClass = it },
                                    sampleWeight         = sampleWeight,
                                    onSampleWeightChange = { sampleWeight = it },
                                    moisture             = moistureClass,
                                    onMoistureChange     = { moistureClass = it },
                                    impurities           = foreignMatters,
                                    onImpuritiesChange   = { foreignMatters = it },
                                    cleanSampleWeight    = calculatedCleanWeightDisplay,
                                    cleanWeightText      = cleanWeightText,
                                    lotWeightFocus       = lotWeightFocus,
                                    sampleWeightFocus    = sampleWeightFocus,
                                    moistureFocus        = moistureFocus,
                                    impuritiesFocus      = impuritiesFocus,
                                    showBroken           = descriptor?.supportsCarunchado == true,
                                    brokenCrackedDamaged = classFieldValues[FieldKeys.BROKEN] ?: "",
                                    onBrokenChange       = { classFieldValues = classFieldValues + (FieldKeys.BROKEN to it) },
                                    brokenFocus          = classDynamicFocusMap[FieldKeys.BROKEN] ?: remember { FocusRequester() },
                                    sampleWeightBelowMin = sampleWeightBelowMin,
                                    minSampleWeight      = minSampleWeight,
                                    isSmallScreen             = isSmallScreen
                                )
                            } else {
                                DiscountBasicTab(
                                    lotWeight              = lotWeightDiscount,
                                    onLotWeightChange      = { lotWeightDiscount = it },
                                    priceBySack            = priceBySack,
                                    onPriceBySackChange    = { priceBySack = it },
                                    moisture               = moistureDiscount,
                                    onMoistureChange       = { moistureDiscount = it },
                                    lotWeightFocus         = lotWeightFocus,
                                    priceBySackFocus       = priceBySackFocus,
                                    moistureFocus          = moistureFocus,
                                    nextFocusAfterMoisture = discountTab0Extra.firstOrNull()?.key?.let { discountFocusMap[it] },
                                    extraFields            = discountTab0Extra,
                                    extraFieldValues       = discountFieldValues,
                                    onExtraFieldChange     = { key, value -> discountFieldValues = discountFieldValues + (key to value) },
                                    extraFocusMap          = discountFocusMap,
                                    isSmallScreen             = isSmallScreen
                                )
                            }

                            1 -> if (isClassification) {
                                ClassificationDynamicTab(
                                    fields        = classTab1,
                                    fieldValues   = classFieldValues,
                                    onValueChange = { key, value ->
                                        classFieldValues = classFieldValues + (key to value)
                                        if (key == FieldKeys.PIERCING || key == FieldKeys.DAMAGED_INPUT) recalcDamaged()
                                    },
                                    focusMap = classDynamicFocusMap,
                                    isSmallScreen             = isSmallScreen
                                )
                            } else {
                                DiscountDynamicTab(
                                    fields        = discountTab1,
                                    fieldValues   = discountFieldValues,
                                    onValueChange = { key, value -> discountFieldValues = discountFieldValues + (key to value) },
                                    focusMap      = discountFocusMap,
                                    isSmallScreen             = isSmallScreen
                                )
                            }

                            2 -> if (isClassification) {
                                ClassificationDefectsTab2(
                                    descriptor                   = descriptor!!,
                                    tab2Fields                   = classTab2,
                                    fieldValues                  = classFieldValues,
                                    onValueChange                = { key, value -> classFieldValues = classFieldValues + (key to value) },
                                    focusMap                     = classDynamicFocusMap,
                                    doesDefineColorClass         = doesDefineColorClass,
                                    onDoesDefineColorClassChange = { doesDefineColorClass = it; if (!it) cleanSampleWeightInput = "" },
                                    cleanSampleWeightInput       = cleanSampleWeightInput,
                                    onCleanSampleWeightChange    = { cleanSampleWeightInput = it },
                                    cleanSampleWeightInputFocus  = cleanSampleWeightInputFocus,
                                    yellowGrainsDisplay          = yellowGrainsDisplay,
                                    otherColorsGrainsWeight      = otherColorsGrainsWeight,
                                    onOtherColorsChange          = { otherColorsGrainsWeight = it },
                                    otherColorsFocus             = otherColorsFocus,
                                    milhoClasse                  = defineClasseMilho,
                                    onMilhoClasseToggle          = { defineClasseMilho = it },
                                    mAmarela = weightAmarela,  onMAmarela = { weightAmarela = it },
                                    mBranca  = weightBranca,   onMBranca  = { weightBranca = it },
                                    mCores   = weightCores,    onMCores   = { weightCores = it },
                                    focusBranca                  = focusBranca,
                                    focusCores                   = focusCores,
                                    cleanWeight                  = cleanWeightMilho,
                                    onCleanWeightChange          = { cleanWeightMilho = it },
                                    cleanWeightFocus             = cleanWeightMilhoFocus,
                                    milhoGrupo                   = defineGrupoMilho,
                                    onMilhoGrupoToggle           = { defineGrupoMilho = it },
                                    mDuro    = weightDuro,     onMDuro    = { weightDuro = it },
                                    mDentado = weightDentado,  onMDentado = { weightDentado = it },
                                    mSemi    = weightSemiduro, onMSemi    = { weightSemiduro = it },
                                    focusDuro                    = focusDuro,
                                    focusDentado                 = focusDentado,
                                    focusSemi                    = focusSemi,
                                    onClasseInvalid              = { classeInvalid = it },
                                    onGrupoInvalid               = { grupoInvalid = it },
                                    isSmallScreen                = isSmallScreen
                                )
                            } else {
                                DiscountDynamicTab(
                                    fields                    = discountTab2,
                                    fieldValues               = discountFieldValues,
                                    onValueChange             = { key, value -> discountFieldValues = discountFieldValues + (key to value) },
                                    focusMap                  = discountFocusMap,
                                    showSwitches              = true,
                                    daysOfStorage             = daysOfStorage,
                                    onDaysOfStorageChange     = { daysOfStorage = it },
                                    deductionValue            = deductionValue,
                                    onDeductionValueChange    = { deductionValue = it },
                                    doesTechnicalLoss         = doesTechnicalLoss,
                                    onDoesTechnicalLossChange = { doesTechnicalLoss = it },
                                    doesDeduction             = doesDeduction,
                                    onDoesDeductionChange     = { doesDeduction = it },
                                    daysOfStorageFocus        = daysOfStorageFocus,
                                    deductionValueFocus       = deductionValueFocus,
                                    isSmallScreen             = isSmallScreen
                                )
                            }
                        }
                    }

                } // fim Column scrollável

                // ── Navegação entre abas e botão de confirmação (fixos em baixo) ──
                Row(
                    modifier              = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (selectedTab > 0)
                        Button(onClick = { selectedTab-- }, modifier = Modifier.heightIn(min = if (isSmallScreen) 44.dp else 50.dp), shape = RoundedCornerShape(8.dp)) {
                            Text("Voltar", style = MaterialTheme.typography.titleMedium)
                        }
                    else
                        Spacer(modifier = Modifier.weight(1f))

                    if (selectedTab < tabTitles.lastIndex) {
                        Button(onClick = { selectedTab++ }, modifier = Modifier.heightIn(min = if (isSmallScreen) 44.dp else 50.dp), shape = RoundedCornerShape(8.dp)) {
                            Text("Avançar", style = MaterialTheme.typography.titleMedium)
                        }
                    } else {
                        Button(
                            modifier = Modifier.heightIn(min = 50.dp),
                            enabled  = !classeInvalid && !grupoInvalid,
                            onClick  = {
                                focusManager.clearFocus()
                                if (isClassification) {
                                    val m = mode as InputScreenMode.Classification
                                    if (cleanWeightForClassification <= 0f) {
                                        errorMessage = "O Peso da Amostra Limpa não pode ser zero ou negativo."
                                        return@Button
                                    }
                                    m.onClassify(
                                        ClassificationInputState(
                                            group                = m.currentGroup,
                                            lotWeight            = lotWeightClass.toFloatOrZero(),
                                            sampleWeight         = sampleWeight.toFloatOrZero(),
                                            moisture             = moistureClass.toFloatOrZero(),
                                            foreignMatters       = foreignMatters.toFloatOrZero(),
                                            cleanSampleWeight    = cleanWeightForClassification,
                                            sour                 = classFieldValues[FieldKeys.SOUR]?.toFloatOrZero()       ?: 0f,
                                            burnt                = classFieldValues[FieldKeys.BURNT]?.toFloatOrZero()      ?: 0f,
                                            moldy                = classFieldValues[FieldKeys.MOLDY]?.toFloatOrZero()      ?: 0f,
                                            fermented            = classFieldValues[FieldKeys.FERMENTED]?.toFloatOrZero()  ?: 0f,
                                            germinated           = classFieldValues[FieldKeys.GERMINATED]?.toFloatOrZero() ?: 0f,
                                            immature             = classFieldValues[FieldKeys.IMMATURE]?.toFloatOrZero()   ?: 0f,
                                            shriveled            = classFieldValues[FieldKeys.SHRIVELED]?.toFloatOrZero()  ?: 0f,
                                            damaged              = classFieldValues[FieldKeys.DAMAGED]?.toFloatOrZero()    ?: 0f,
                                            gessado              = classFieldValues[FieldKeys.GESSADO]?.toFloatOrZero()    ?: 0f,
                                            brokenCrackedDamaged = classFieldValues[FieldKeys.BROKEN]?.toFloatOrZero()    ?: 0f,
                                            greenish             = classFieldValues[FieldKeys.GREENISH]?.toFloatOrZero()   ?: 0f,
                                            carunchado           = classFieldValues[FieldKeys.CARUNCHADO]?.toFloatOrZero() ?: 0f,
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
                                    )
                                } else {
                                    val m = mode as InputScreenMode.Discount
                                    val fLotWeight   = lotWeightDiscount.toFloatOrZero()
                                    val fPriceBySack = priceBySack.toFloatOrZero()
                                    if (fLotWeight <= 0f || fPriceBySack <= 0f) {
                                        errorMessage = "Insira valores válidos para Peso do Lote e Preço por Saca."
                                        return@Button
                                    }
                                    errorMessage = null
                                    val fullMap = discountFieldValues.mapValues { it.value.toFloatOrZero() } +
                                            mapOf(
                                                FieldKeys.MOISTURE   to moistureDiscount.toFloatOrZero(),
                                                FieldKeys.LOT_WEIGHT to fLotWeight,
                                                FieldKeys.LOT_PRICE  to (fLotWeight * fPriceBySack) / 60f
                                            )
                                    m.onCalculate(
                                        fullMap, fPriceBySack, fLotWeight,
                                        daysOfStorage.toIntOrZero(),
                                        doesTechnicalLoss, deductionValue.toFloatOrZero(), doesDeduction
                                    )
                                }
                            },
                            shape = RoundedCornerShape(8.dp)
                        ) { Text(submitLabel, style = MaterialTheme.typography.titleMedium) }
                    }
                }
            }
        }
    }
}

// =============================================================================
// TAB 0 — INFORMAÇÃO BÁSICA CLASSIFICAÇÃO
// =============================================================================

@Composable
private fun ClassificationBasicTab(
    lotWeight: String,            onLotWeightChange: (String) -> Unit,
    sampleWeight: String,         onSampleWeightChange: (String) -> Unit,
    moisture: String,             onMoistureChange: (String) -> Unit,
    impurities: String,           onImpuritiesChange: (String) -> Unit,
    cleanSampleWeight: String,
    cleanWeightText: String,
    lotWeightFocus: FocusRequester, sampleWeightFocus: FocusRequester,
    moistureFocus: FocusRequester,  impuritiesFocus: FocusRequester,
    showBroken: Boolean,
    brokenCrackedDamaged: String, onBrokenChange: (String) -> Unit,
    brokenFocus: FocusRequester,
    sampleWeightBelowMin: Boolean,
    minSampleWeight: Float?,
    isSmallScreen: Boolean
) {
    Column(modifier = Modifier.padding(if (isSmallScreen) 8.dp else 16.dp)) {
        InputNumberField(lotWeight, onLotWeightChange, "Peso do Lote (kg)", lotWeightFocus, sampleWeightFocus)
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        InputNumberField(sampleWeight, onSampleWeightChange, "Peso da Amostra de Trabalho (g)", sampleWeightFocus, moistureFocus)
        // Aviso de peso mínimo — exibido imediatamente abaixo do campo, apenas informativo
        if (sampleWeightBelowMin && minSampleWeight != null) {
            Spacer(Modifier.height(4.dp))
            Text(
                text  = "⚠ Peso abaixo do recomendado. Mínimo recomendado: ${minSampleWeight.toInt()} g",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        InputNumberField(moisture, onMoistureChange, "Umidade (%)", moistureFocus, impuritiesFocus)
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        InputNumberField(impurities, onImpuritiesChange, "Matérias Estranhas e Impurezas (MEI) (g)",
            impuritiesFocus, if (showBroken) brokenFocus else null)
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        if (showBroken) {
            InputNumberField(brokenCrackedDamaged, onBrokenChange, "Grãos Quebrados (g)", brokenFocus, null)
        }
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text  = cleanWeightText,
                style = MaterialTheme.typography.bodyMedium
            )
            Text("$cleanSampleWeight g",
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary)
        }
    }
}

// =============================================================================
// TAB 0 — INFORMAÇÃO BÁSICA DESCONTO
// =============================================================================

@Composable
private fun DiscountBasicTab(
    lotWeight: String,              onLotWeightChange: (String) -> Unit,
    priceBySack: String,            onPriceBySackChange: (String) -> Unit,
    moisture: String,               onMoistureChange: (String) -> Unit,
    lotWeightFocus: FocusRequester,
    priceBySackFocus: FocusRequester,
    moistureFocus: FocusRequester,
    nextFocusAfterMoisture: FocusRequester?,
    extraFields: List<DiscountInputField>,
    extraFieldValues: Map<String, String>,
    onExtraFieldChange: (String, String) -> Unit,
    extraFocusMap: Map<String, FocusRequester>,
    isSmallScreen: Boolean
) {
    Column(modifier = Modifier.padding(if (isSmallScreen) 8.dp else 16.dp)) {
        InputNumberField(lotWeight,   onLotWeightChange,   "Peso do lote (kg)",     lotWeightFocus,   priceBySackFocus)
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        InputNumberField(priceBySack, onPriceBySackChange, "Preço por Saca (60kg)", priceBySackFocus, moistureFocus)
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        InputNumberField(moisture,    onMoistureChange,    "Umidade (%)",           moistureFocus,    nextFocusAfterMoisture)
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        extraFields.forEachIndexed { index, field ->
            val nextFocus = extraFields.getOrNull(index + 1)?.key?.let { extraFocusMap[it] }
            InputNumberField(extraFieldValues[field.key] ?: "", { onExtraFieldChange(field.key, it) },
                field.label, extraFocusMap[field.key] ?: remember { FocusRequester() }, nextFocus)
            Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        }
    }
}

// =============================================================================
// TAB 1 — AVARIADOS CLASSIFICAÇÃO (dinâmico)
// =============================================================================

@Composable
private fun ClassificationDynamicTab(
    fields: List<ClassificationInputField>,
    fieldValues: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    focusMap: Map<String, FocusRequester>,
    isSmallScreen: Boolean
) {
    Column(modifier = Modifier.padding(if (isSmallScreen) 8.dp else 16.dp)) {
        val numericFields = fields.filter {
            it.fieldType == FieldType.NUMERIC || it.fieldType == FieldType.CALCULATED
        }
        var shownDamagedHeader = false

        numericFields.forEachIndexed { index, field ->
            if (field.key == FieldKeys.PIERCING && !shownDamagedHeader) {
                shownDamagedHeader = true
                Spacer(Modifier.height(8.dp))
                Text("Danificados", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            // Busca apenas o próximo campo que seja editável
            val nextEditableField = numericFields.drop(index + 1).firstOrNull { it.fieldType == FieldType.NUMERIC }
            val nextFocus = nextEditableField?.key?.let { focusMap[it] }

            if (field.fieldType == FieldType.CALCULATED) {
                OutlinedTextField(
                    value = fieldValues[field.key] ?: "",
                    onValueChange = {},
                    label = { Text(field.label) },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    enabled = false
                )
            } else {
                InputNumberField(
                    value = fieldValues[field.key] ?: "",
                    onValueChange = { onValueChange(field.key, it) },
                    label = field.label,
                    focusRequester = focusMap[field.key] ?: remember { FocusRequester() },
                    nextFocus = nextFocus
                )
            }
            Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        }
    }
}

// =============================================================================
// TAB 2 — DEFEITOS FINAIS CLASSIFICAÇÃO
// =============================================================================

@Composable
private fun ClassificationDefectsTab2(
    descriptor: GrainDescriptor,
    tab2Fields: List<ClassificationInputField>,
    fieldValues: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    focusMap: Map<String, FocusRequester>,
    doesDefineColorClass: Boolean, onDoesDefineColorClassChange: (Boolean) -> Unit,
    cleanSampleWeightInput: String, onCleanSampleWeightChange: (String) -> Unit,
    cleanSampleWeightInputFocus: FocusRequester,
    yellowGrainsDisplay: String,
    otherColorsGrainsWeight: String, onOtherColorsChange: (String) -> Unit,
    otherColorsFocus: FocusRequester,
    milhoClasse: Boolean, onMilhoClasseToggle: (Boolean) -> Unit,
    mAmarela: String, onMAmarela: (String) -> Unit,
    mBranca: String,  onMBranca: (String) -> Unit, focusBranca: FocusRequester,
    mCores: String,   onMCores: (String) -> Unit,  focusCores: FocusRequester,
    cleanWeight: String, onCleanWeightChange: (String) -> Unit,
    cleanWeightFocus: FocusRequester,
    milhoGrupo: Boolean, onMilhoGrupoToggle: (Boolean) -> Unit,
    mDuro: String,    onMDuro: (String) -> Unit,    focusDuro: FocusRequester,
    mDentado: String, onMDentado: (String) -> Unit, focusDentado: FocusRequester,
    mSemi: String,    onMSemi: (String) -> Unit,    focusSemi: FocusRequester,
    onClasseInvalid: (Boolean) -> Unit,
    onGrupoInvalid: (Boolean) -> Unit,
    isSmallScreen: Boolean
) {
    Column(modifier = Modifier.padding(if (isSmallScreen) 8.dp else 16.dp)) {
        Text("Defeitos Finais", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        val numericFields = tab2Fields.filter { it.fieldType == FieldType.NUMERIC }
        numericFields.forEachIndexed { index, field ->
            val nextFocus = numericFields.getOrNull(index + 1)?.key?.let { focusMap[it] }
            InputNumberField(fieldValues[field.key] ?: "", { onValueChange(field.key, it) },
                field.label, focusMap[field.key] ?: remember { FocusRequester() }, nextFocus)
            Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        }
        if (tab2Fields.any { it.fieldType == FieldType.COLOR_CLASS }) {
            Spacer(Modifier.height(8.dp))
            SojaColorClassSection(doesDefineColorClass, onDoesDefineColorClassChange,
                cleanSampleWeightInput, onCleanSampleWeightChange, cleanSampleWeightInputFocus,
                yellowGrainsDisplay, otherColorsGrainsWeight, onOtherColorsChange, otherColorsFocus)
        }
        if (tab2Fields.any { it.fieldType == FieldType.MILHO_CLASSE || it.fieldType == FieldType.MILHO_GRUPO }) {
            MilhoClasseGroupSection(
                milhoClasse               = milhoClasse,
                onMilhoClasseToggle       = onMilhoClasseToggle,
                mAmarela = mAmarela,      onMAmarela = onMAmarela,
                mBranca  = mBranca,       onMBranca  = onMBranca,  focusBranca = focusBranca,
                mCores   = mCores,        onMCores   = onMCores,   focusCores  = focusCores,
                cleanWeight               = cleanWeight,
                onCleanWeightChange       = onCleanWeightChange,
                cleanWeightFocus          = cleanWeightFocus,
                milhoGrupo                = milhoGrupo,
                onMilhoGrupoToggle        = onMilhoGrupoToggle,
                mDuro    = mDuro,         onMDuro    = onMDuro,    focusDuro    = focusDuro,
                mDentado = mDentado,      onMDentado = onMDentado, focusDentado = focusDentado,
                mSemi    = mSemi,         onMSemi    = onMSemi,    focusSemi    = focusSemi,
                onClasseInvalid           = onClasseInvalid,
                onGrupoInvalid            = onGrupoInvalid
            )
        }
    }
}

// =============================================================================
// TABS DEFEITOS DESCONTO (dinâmico)
// =============================================================================

@Composable
private fun DiscountDynamicTab(
    fields: List<DiscountInputField>,
    fieldValues: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    focusMap: Map<String, FocusRequester>,
    showSwitches: Boolean = false,
    daysOfStorage: String = "0",          onDaysOfStorageChange: (String) -> Unit = {},
    deductionValue: String = "0",         onDeductionValueChange: (String) -> Unit = {},
    doesTechnicalLoss: Boolean = false,   onDoesTechnicalLossChange: (Boolean) -> Unit = {},
    doesDeduction: Boolean = false,       onDoesDeductionChange: (Boolean) -> Unit = {},
    daysOfStorageFocus: FocusRequester = remember { FocusRequester() },
    deductionValueFocus: FocusRequester = remember { FocusRequester() },
    isSmallScreen: Boolean
) {
    Column(modifier = Modifier.padding(if (isSmallScreen) 8.dp else 16.dp)) {
        fields.forEachIndexed { index, field ->
            val nextFocus = fields.getOrNull(index + 1)?.key?.let { focusMap[it] }
            InputNumberField(fieldValues[field.key] ?: "", { onValueChange(field.key, it) },
                field.label, focusMap[field.key] ?: remember { FocusRequester() }, nextFocus)
            Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
        }
        if (showSwitches) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Switch(checked = doesTechnicalLoss, onCheckedChange = onDoesTechnicalLossChange)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Quebra Técnica", style = MaterialTheme.typography.bodyLarge)
                    Text("Desconto por dias de armazenamento",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                var showInfo by remember { mutableStateOf(false) }

                IconButton(onClick = { showInfo = true }) {
                    Icon(
                        imageVector        = Icons.Outlined.Info,
                        contentDescription = "O que é Quebra técnica?"
                    )
                }

                if (showInfo) {
                    InfoDialog(
                        title       = "O que é Quebra Técnica?",
                        description = "Quebra técnica é a perda de peso do grão armazenado causada pela respiração celular, que consome matéria seca. O limite máximo considerado é de 0,15% por quinzena. Ao marcar esta opção, o campo de dias de armazenamento será habilitado e o valor será incluído no cálculo do desconto.",
                        onDismiss   = { showInfo = false }
                    )
                }
            }
            if (doesTechnicalLoss) {
                Spacer(Modifier.height(8.dp))
                InputNumberField(daysOfStorage, onDaysOfStorageChange,
                    "Dias de armazenamento", daysOfStorageFocus, deductionValueFocus)
            }
            /* TODO: Quando for implementar o deságio só descomentar...
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Switch(checked = doesDeduction, onCheckedChange = onDoesDeductionChange)
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text("Deságio", style = MaterialTheme.typography.bodyLarge)
                    Text("Desconto percentual sobre o valor final",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            if (doesDeduction) {
                Spacer(Modifier.height(8.dp))
                InputNumberField(deductionValue, onDeductionValueChange,
                    "Valor de Deságio (%)", deductionValueFocus, null)
            }
            */
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
        Column {
            Text("Definir Classe?")
            Text("Classificação de acordo com a coloração dos grãos")
        }
    }
    if (doesDefineColorClass) {
        Spacer(Modifier.height(16.dp))
        InputNumberField(cleanSampleWeightInput, onCleanSampleWeightChange,
            "Peso da Amostra Limpa (g)", cleanSampleWeightInputFocus, otherColorsFocus)
        Spacer(Modifier.height(5.dp))
        Text("Peso da Amostra Limpa: Grãos inteiros, sadios e isentos de MEI",
            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        InputNumberField(otherColorsGrainsWeight, onOtherColorsChange,
            "Peso de grãos outras cores (g)", otherColorsFocus, null)
        Spacer(Modifier.height(5.dp))
        Text("Peso de grãos outras cores: Grãos com coloração do tegumento que não seja amarela, verde ou pérola",
            style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Start,
            modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(value = yellowGrainsDisplay, onValueChange = {},
            label = { Text("Peso de grãos amarelos (g) (Calculado)") },
            modifier = Modifier.fillMaxWidth(), readOnly = true, enabled = false)
    }
}

// =============================================================================
// SEÇÃO CLASSE E GRUPO — MILHO
// =============================================================================

@Composable
private fun MilhoClasseGroupSection(
    milhoClasse: Boolean, onMilhoClasseToggle: (Boolean) -> Unit,
    mAmarela: String, onMAmarela: (String) -> Unit,
    mBranca: String,  onMBranca: (String) -> Unit, focusBranca: FocusRequester,
    mCores: String,   onMCores: (String) -> Unit,  focusCores: FocusRequester,

    cleanWeight: String, onCleanWeightChange: (String) -> Unit,
    cleanWeightFocus: FocusRequester,

    milhoGrupo: Boolean, onMilhoGrupoToggle: (Boolean) -> Unit,
    mDuro: String,    onMDuro: (String) -> Unit,    focusDuro: FocusRequester,
    mDentado: String, onMDentado: (String) -> Unit, focusDentado: FocusRequester,
    mSemi: String,    onMSemi: (String) -> Unit,    focusSemi: FocusRequester,

    onClasseInvalid: (Boolean) -> Unit,
    onGrupoInvalid: (Boolean) -> Unit
) {
    // Rastreia qual switch foi ativado primeiro ("classe" ou "grupo")
    var firstActivated by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(milhoClasse, milhoGrupo) {
        if (!milhoClasse && !milhoGrupo) {
            firstActivated = null
        } else if (milhoClasse && firstActivated == null) {
            firstActivated = "classe"
        } else if (milhoGrupo && firstActivated == null) {
            firstActivated = "grupo"
        } else if (!milhoClasse && firstActivated == "classe") {
            firstActivated = if (milhoGrupo) "grupo" else null
        } else if (!milhoGrupo && firstActivated == "grupo") {
            firstActivated = if (milhoClasse) "classe" else null
        }
    }

    val limitePeso = cleanWeight.toFloatOrZero()

    // ── Classe (Cor) ──────────────────────────────────────────────────────────
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = milhoClasse, onCheckedChange = onMilhoClasseToggle)
        Text(" Definir Classe (Cor) do Milho", fontWeight = FontWeight.Bold)
    }
    val focusAmarela = remember { FocusRequester() }
    if (milhoClasse) {
        val isClasseEditable = firstActivated == "classe"

        Spacer(Modifier.height(8.dp))
        InputNumberField(
            value = cleanWeight,
            onValueChange = onCleanWeightChange,
            label = "Peso da Amostra Limpa — Classe (g)",
            focusRequester = cleanWeightFocus,
            nextFocus = focusAmarela,
            enabled = isClasseEditable
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Peso da Amostra Limpa: Grãos inteiros, sadios e isentos de MEI",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        InputNumberField(mAmarela, onMAmarela, "Peso Amarela (g)", focusAmarela, focusBranca)
        Spacer(Modifier.height(4.dp))
        InputNumberField(mBranca,  onMBranca,  "Peso Branca (g)",  focusBranca,  focusCores)
        Spacer(Modifier.height(4.dp))
        InputNumberField(mCores,   onMCores,   "Peso Cores (Misturada) (g)", focusCores, null)

        // Validação — Classe (Maior ou Menor)
        val somaClasse = mAmarela.toFloatOrZero() + mBranca.toFloatOrZero() + mCores.toFloatOrZero()
        val classeInvalida = limitePeso > 0f && kotlin.math.abs(somaClasse - limitePeso) > 0.01f

        onClasseInvalid(classeInvalida)

        if (classeInvalida) {
            Spacer(Modifier.height(6.dp))
            val msgClasse = if (somaClasse > limitePeso) {
                "⚠ A soma dos pesos (%.1fg) ultrapassa o peso da amostra limpa (%.1fg)."
            } else {
                "⚠ A soma dos pesos (%.1fg) é menor que o peso da amostra limpa (%.1fg)."
            }
            Text(
                text = msgClasse.format(somaClasse, limitePeso) + " Corrija os valores.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        onClasseInvalid(false)
    }

    // ── Grupo (Forma) ─────────────────────────────────────────────────────────
    Spacer(Modifier.height(24.dp))
    Row(verticalAlignment = Alignment.CenterVertically) {
        Switch(checked = milhoGrupo, onCheckedChange = onMilhoGrupoToggle)
        Text(" Definir Grupo (Forma) do Milho", fontWeight = FontWeight.Bold)
    }
    if (milhoGrupo) {
        val isGrupoEditable = firstActivated == "grupo"

        Spacer(Modifier.height(8.dp))
        InputNumberField(
            value = cleanWeight,
            onValueChange = onCleanWeightChange,
            label = "Peso da Amostra Limpa — Grupo (g)",
            focusRequester = cleanWeightFocus,
            nextFocus = focusDuro,
            enabled = isGrupoEditable
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Peso da Amostra Limpa: Grãos inteiros, sadios e isentos de MEI",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Justify,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        InputNumberField(mDuro,    onMDuro,    "Peso Duro (g)",     focusDuro,    focusDentado)
        Spacer(Modifier.height(4.dp))
        InputNumberField(mDentado, onMDentado, "Peso Dentado (g)",  focusDentado, focusSemi)
        Spacer(Modifier.height(4.dp))
        InputNumberField(mSemi,    onMSemi,    "Peso Semiduro (g)", focusSemi,    null)

        // Validação — Grupo (Maior ou Menor)
        val somaGrupo = mDuro.toFloatOrZero() + mDentado.toFloatOrZero() + mSemi.toFloatOrZero()
        val grupoInvalido = limitePeso > 0f && kotlin.math.abs(somaGrupo - limitePeso) > 0.01f

        onGrupoInvalid(grupoInvalido)

        if (grupoInvalido) {
            Spacer(Modifier.height(6.dp))
            val msgGrupo = if (somaGrupo > limitePeso) {
                "⚠ A soma dos pesos (%.1fg) ultrapassa o peso da amostra limpa (%.1fg)."
            } else {
                "⚠ A soma dos pesos (%.1fg) é menor que o peso da amostra limpa (%.1fg)."
            }
            Text(
                text = msgGrupo.format(somaGrupo, limitePeso) + " Corrija os valores.",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else {
        onGrupoInvalid(false)
    }
}

// =============================================================================
// CAMPO NUMÉRICO COMPARTILHADO
// =============================================================================

@Composable
fun InputNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester?,
    enabled: Boolean = true
) {
    val focusManager = LocalFocusManager.current
    // 1. Pegue a referência do controlador do teclado
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = { raw ->
            val sanitized = sanitizeNumericInput(raw)
            onValueChange(sanitized)
        },
        label = { Text(label) },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = {
                try {
                    nextFocus?.requestFocus()
                } catch (e: IllegalStateException) {
                    focusManager.moveFocus(FocusDirection.Next)
                }
            },
            onDone = {
                // 2. Esconda o teclado antes de limpar o foco
                keyboardController?.hide()
                // 3. Limpe o foco
                focusManager.clearFocus(force = true)
            }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    )
}

// =============================================================================
// EXTENSÕES PRIVADAS
// =============================================================================
private fun String.toIntOrZero(): Int     = toIntOrNull() ?: 0