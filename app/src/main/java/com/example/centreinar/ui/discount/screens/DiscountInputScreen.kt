package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.strategy.DiscountInputField
import com.example.centreinar.ui.discount.strategy.FinancialDiscountPayload
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel

// =============================================================================
// STATE HOLDER
// =============================================================================

private class DiscountInputState(
    val lotWeight:         MutableState<String>  = mutableStateOf(""),
    val priceBySack:       MutableState<String>  = mutableStateOf(""),
    val moisture:          MutableState<String>  = mutableStateOf(""),
    val daysOfStorage:     MutableState<String>  = mutableStateOf("0"),
    val deductionValue:    MutableState<String>  = mutableStateOf("0"),
    val doesTechnicalLoss: MutableState<Boolean> = mutableStateOf(false),
    val doesDeduction:     MutableState<Boolean> = mutableStateOf(false),
    val errorMessage:      MutableState<String?> = mutableStateOf(null)
)

@Composable
private fun rememberDiscountInputState() = remember { DiscountInputState() }

// =============================================================================
// TELA PRINCIPAL
// =============================================================================

@Composable
fun DiscountInputScreen(
    navController: NavController,
    classificationId: Int? = null,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState               by viewModel.uiState.collectAsStateWithLifecycle()
    val discountResult        by viewModel.discountResult.collectAsStateWithLifecycle()
    val classificationPrefill by viewModel.classificationPrefill.collectAsStateWithLifecycle()
    val grain = viewModel.selectedGrain

    val strategy     = viewModel.getStrategy(grain)
    val defectFields = remember(grain) { strategy?.getDefectInputFields() ?: emptyList() }

    val basicTabExtraFields = remember(defectFields) { defectFields.filter { it.tabGroup == 0 } }
    val defects1Fields      = remember(defectFields) { defectFields.filter { it.tabGroup == 1 } }
    val defects2Fields      = remember(defectFields) { defectFields.filter { it.tabGroup == 2 } }

    var fieldValues by remember(grain) {
        val prefill = viewModel.classificationPrefill.value
        mutableStateOf(defectFields.associate { field ->
            field.key to (prefill?.defects?.get(field.key)?.toString() ?: "")
        })
    }


    val s = rememberDiscountInputState()

    // Pré-preenche TODOS os campos vindos da ClassificationResultScreen
    var prefillApplied by remember { mutableStateOf(false) }

    LaunchedEffect(grain) {
        if (!prefillApplied) {  // só aplica currentDefectsMap se o prefill ainda não rodou
            val saved = viewModel.currentDefectsMap
            if (saved.isNotEmpty()) {
                fieldValues = fieldValues.mapValues { (key, _) -> saved[key]?.toString() ?: "" }
                saved["umidade"]?.let { s.moisture.value = it.toString() }
            }
        }
    }

    LaunchedEffect(classificationPrefill) {
        val prefill = classificationPrefill ?: return@LaunchedEffect
        prefill.lotWeight?.let { s.lotWeight.value = it.toString() }
        s.moisture.value = prefill.moisture.toString()
        prefillApplied = true
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { s.errorMessage.value = it }
    }


    LaunchedEffect(discountResult) {
        if (discountResult != null) {
            navController.navigate("discountResultsScreen")
        }
    }

    val lotWeightFocus      = remember { FocusRequester() }
    val priceBySackFocus    = remember { FocusRequester() }
    val moistureFocus       = remember { FocusRequester() }
    val daysOfStorageFocus  = remember { FocusRequester() }
    val deductionValueFocus = remember { FocusRequester() }

    val defectFocusMap = remember(defectFields) {
        defectFields.associate { it.key to FocusRequester() }
    }

    val tabTitles = listOf("Informação Básica", "Defeitos", "Extras")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> lotWeightFocus.requestFocus()
            1 -> defects1Fields.firstOrNull()?.key?.let { defectFocusMap[it]?.requestFocus() }
            2 -> defects2Fields.firstOrNull()?.key?.let { defectFocusMap[it]?.requestFocus() }
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

            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Text(
                    text     = "Insira os dados",
                    style    = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(16.dp)
                )

                s.errorMessage.value?.let {
                    Text(
                        text     = it,
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                when (selectedTab) {
                    0 -> BasicInfoTab(
                        lotWeight              = s.lotWeight.value,
                        onLotWeightChange      = { s.lotWeight.value = it },
                        priceBySack            = s.priceBySack.value,
                        onPriceBySackChange    = { s.priceBySack.value = it },
                        moisture               = s.moisture.value,
                        onMoistureChange       = { s.moisture.value = it },
                        lotWeightFocus         = lotWeightFocus,
                        priceBySackFocus       = priceBySackFocus,
                        moistureFocus          = moistureFocus,
                        nextFocusAfterMoisture = basicTabExtraFields.firstOrNull()
                            ?.key?.let { defectFocusMap[it] },
                        extraFields            = basicTabExtraFields,
                        extraFieldValues       = fieldValues,
                        onExtraFieldChange     = { key, value -> fieldValues = fieldValues + (key to value) },
                        extraFocusMap          = defectFocusMap
                    )

                    1 -> DynamicDefectsTab(
                        fields        = defects1Fields,
                        fieldValues   = fieldValues,
                        onValueChange = { key, value -> fieldValues = fieldValues + (key to value) },
                        focusMap      = defectFocusMap,
                        showSwitches  = false
                    )

                    2 -> DynamicDefectsTab(
                        fields                    = defects2Fields,
                        fieldValues               = fieldValues,
                        onValueChange             = { key, value -> fieldValues = fieldValues + (key to value) },
                        focusMap                  = defectFocusMap,
                        showSwitches              = true,
                        daysOfStorage             = s.daysOfStorage.value,
                        onDaysOfStorageChange     = { s.daysOfStorage.value = it },
                        deductionValue            = s.deductionValue.value,
                        onDeductionValueChange    = { s.deductionValue.value = it },
                        doesTechnicalLoss         = s.doesTechnicalLoss.value,
                        onDoesTechnicalLossChange = { s.doesTechnicalLoss.value = it },
                        doesDeduction             = s.doesDeduction.value,
                        onDoesDeductionChange     = { s.doesDeduction.value = it },
                        daysOfStorageFocus        = daysOfStorageFocus,
                        deductionValueFocus       = deductionValueFocus
                    )
                }
            }

            Row(
                modifier              = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (selectedTab > 0) {
                    Button(onClick = { selectedTab-- }) { Text("Voltar") }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                if (selectedTab < tabTitles.lastIndex) {
                    Button(onClick = { selectedTab++ }) { Text("Avançar") }
                } else {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick  = {
                            val fLotWeight   = s.lotWeight.value.toFloatOrZero()
                            val fPriceBySack = s.priceBySack.value.toFloatOrZero()

                            if (fLotWeight <= 0f || fPriceBySack <= 0f) {
                                s.errorMessage.value =
                                    "Insira valores válidos para Peso do Lote e Preço por Saca."
                                return@Button
                            }

                            val fullMap = fieldValues.mapValues { it.value.toFloatOrZero() } + mapOf(
                                "umidade"   to s.moisture.value.toFloatOrZero(),
                                "lotWeight" to fLotWeight,
                                "lotPrice"  to (fLotWeight * fPriceBySack) / 60f
                            )

                            val resolvedStrategy = viewModel.getStrategy(grain)
                                ?: run {
                                    s.errorMessage.value = "Grão não suportado: $grain"
                                    return@Button
                                }

                            val defectsPayload   = resolvedStrategy.createDefectsPayload(fullMap)
                            val financialPayload = FinancialDiscountPayload(
                                priceBySack       = fPriceBySack,
                                lotWeight         = fLotWeight,
                                group             = viewModel.selectedGroup,
                                daysOfStorage     = s.daysOfStorage.value.toIntOrZero(),
                                doesTechnicalLoss = s.doesTechnicalLoss.value,
                                deductionValue    = s.deductionValue.value.toFloatOrZero(),
                                doesDeduction     = s.doesDeduction.value
                            )

                            viewModel.calculateDiscount(defectsPayload, financialPayload)
                        }
                    ) {
                        Text("Calcular Desconto")
                    }
                }
            }
        }
    }
}

// =============================================================================
// COMPONENTES AUXILIARES
// =============================================================================

@Composable
private fun BasicInfoTab(
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
    extraFocusMap: Map<String, FocusRequester>
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        DiscountNumberInputField(lotWeight,   onLotWeightChange,   "Peso do lote (kg)",     lotWeightFocus,   priceBySackFocus)
        Spacer(Modifier.height(16.dp))
        DiscountNumberInputField(priceBySack, onPriceBySackChange, "Preço por Saca (60kg)", priceBySackFocus, moistureFocus)
        Spacer(Modifier.height(16.dp))
        DiscountNumberInputField(moisture,    onMoistureChange,    "Umidade (%)",           moistureFocus,    nextFocusAfterMoisture)
        Spacer(Modifier.height(16.dp))

        extraFields.forEachIndexed { index, field ->
            val nextFocus = extraFields.getOrNull(index + 1)?.key?.let { extraFocusMap[it] }
            DiscountNumberInputField(
                value          = extraFieldValues[field.key] ?: "",
                onValueChange  = { onExtraFieldChange(field.key, it) },
                label          = field.label,
                focusRequester = extraFocusMap[field.key] ?: remember { FocusRequester() },
                nextFocus      = nextFocus
            )
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DynamicDefectsTab(
    fields: List<DiscountInputField>,
    fieldValues: Map<String, String>,
    onValueChange: (String, String) -> Unit,
    focusMap: Map<String, FocusRequester>,
    showSwitches: Boolean = false,
    daysOfStorage: String = "0",
    onDaysOfStorageChange: (String) -> Unit = {},
    deductionValue: String = "0",
    onDeductionValueChange: (String) -> Unit = {},
    doesTechnicalLoss: Boolean = false,
    onDoesTechnicalLossChange: (Boolean) -> Unit = {},
    doesDeduction: Boolean = false,
    onDoesDeductionChange: (Boolean) -> Unit = {},
    daysOfStorageFocus: FocusRequester = remember { FocusRequester() },
    deductionValueFocus: FocusRequester = remember { FocusRequester() }
) {
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        fields.forEachIndexed { index, field ->
            val nextFocus = fields.getOrNull(index + 1)?.key?.let { focusMap[it] }
            DiscountNumberInputField(
                value          = fieldValues[field.key] ?: "",
                onValueChange  = { onValueChange(field.key, it) },
                label          = field.label,
                focusRequester = focusMap[field.key] ?: remember { FocusRequester() },
                nextFocus      = nextFocus
            )
            Spacer(Modifier.height(16.dp))
        }

        if (showSwitches) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = doesTechnicalLoss, onCheckedChange = onDoesTechnicalLossChange)
                Spacer(Modifier.width(8.dp))
                Text("Aplicar Quebra Técnica?")
            }
            if (doesTechnicalLoss) {
                Spacer(Modifier.height(8.dp))
                DiscountNumberInputField(daysOfStorage, onDaysOfStorageChange, "Dias de armazenamento", daysOfStorageFocus, deductionValueFocus)
            }
            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Switch(checked = doesDeduction, onCheckedChange = onDoesDeductionChange)
                Spacer(Modifier.width(8.dp))
                Text("Aplicar Deságio?")
            }
            if (doesDeduction) {
                Spacer(Modifier.height(8.dp))
                DiscountNumberInputField(deductionValue, onDeductionValueChange, "Valor de Deságio (%)", deductionValueFocus, null)
            }
        }
    }
}

@Composable
private fun DiscountNumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            val sanitized = newValue.replace(',', '.').filter { it.isDigit() || it == '.' }
            if (sanitized.count { it == '.' } <= 1) {
                onValueChange(sanitized)
            }
        },
        label           = { Text(label) },
        modifier        = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction    = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onNext = { nextFocus?.requestFocus() }),
        singleLine      = true
    )
}

// =============================================================================
// EXTENSÕES PRIVADAS
// =============================================================================

private fun String.toFloatOrZero(): Float = replace(",", ".").toFloatOrNull() ?: 0f
private fun String.toIntOrZero(): Int     = toIntOrNull() ?: 0