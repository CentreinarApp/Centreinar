package com.example.centreinar.ui.discount.screens

import android.util.Log
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import java.math.RoundingMode

@Composable
fun MilhoDiscountInputScreen(
    navController: NavController,
    classificationId: Int? = null,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        val scrollState = rememberScrollState()

        // Estados observados do ViewModel para Auto-Fill
        val loadedClassif by viewModel.loadedClassificationMilho.collectAsStateWithLifecycle()
        val loadedSample by viewModel.loadedSampleMilho.collectAsStateWithLifecycle()

        // --- ESTADOS DOS CAMPOS ---
        var lotWeight by remember { mutableStateOf("") }
        var priceBySack by remember { mutableStateOf("") }
        var moisture by remember { mutableStateOf("") }
        var impurities by remember { mutableStateOf("") }
        var broken by remember { mutableStateOf("") }
        var ardidos by remember { mutableStateOf("") }
        var carunchado by remember { mutableStateOf("") }
        var spoiled by remember { mutableStateOf("") }
        var daysOfStorage by remember { mutableStateOf("0") }
        var deductionValue by remember { mutableStateOf("0") }
        var doesTechnicalLoss by remember { mutableStateOf(false) }
        var doesDeduction by remember { mutableStateOf(false) }

        var errorMessage by remember { mutableStateOf<String?>(null) }

        // --- LÓGICA DE AUTO-PREENCHIMENTO ---

        LaunchedEffect(classificationId) {
            if (classificationId != null && classificationId > 0) {
                viewModel.loadClassificationMilhoData(classificationId)
            }
        }

        LaunchedEffect(loadedClassif, loadedSample) {
            loadedSample?.let { sample ->
                lotWeight = sample.lotWeight.toString()
                moisture = sample.moisture.toString()
            }
            loadedClassif?.let { classif ->
                impurities = classif.impuritiesPercentage.toString()
                broken = classif.brokenPercentage.toString()
                ardidos = classif.ardidoPercentage.toString()
                carunchado = classif.carunchadoPercentage.toString()
                spoiled = classif.spoiledTotalPercentage.toString()
            }
        }

        // FOCUS REQUESTERS
        val lotWeightFocus = remember { FocusRequester() }
        val priceBySackFocus = remember { FocusRequester() }
        val moistureFocus = remember { FocusRequester() }
        val impuritiesFocus = remember { FocusRequester() }
        val brokenFocus = remember { FocusRequester() }
        val ardidosFocus = remember { FocusRequester() }
        val carunchadoFocus = remember { FocusRequester() }
        val spoiledFocus = remember { FocusRequester() }
        val daysOfStorageFocus = remember { FocusRequester() }
        val deductionValueFocus = remember { FocusRequester() }

        val tabTitles = listOf("Básico", "Defeitos", "Extras")
        var selectedTab by remember { mutableStateOf(0) }

        LaunchedEffect(selectedTab) {
            when (selectedTab) {
                0 -> lotWeightFocus.requestFocus()
                1 -> brokenFocus.requestFocus()
                2 -> if (doesTechnicalLoss) daysOfStorageFocus.requestFocus()
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(text = title) }
                    )
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState)
            ) {
                Text(
                    "Dados do Milho",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                when (selectedTab) {
                    0 -> MilhoBasicInfoTab(
                        lotWeight = lotWeight, onLotWeightChange = { lotWeight = it },
                        priceBySack = priceBySack, onPriceBySackChange = { priceBySack = it },
                        moisture = moisture, onMoistureChange = { moisture = it },
                        impurities = impurities, onImpuritiesChange = { impurities = it },
                        lotWeightFocus = lotWeightFocus,
                        priceBySackFocus = priceBySackFocus,
                        moistureFocus = moistureFocus,
                        impuritiesFocus = impuritiesFocus
                    )

                    1 -> MilhoDefectsTab(
                        broken = broken, onBrokenChange = { broken = it },
                        ardidos = ardidos, onArdidosChange = { ardidos = it },
                        carunchado = carunchado, onCarunchadoChange = { carunchado = it },
                        spoiled = spoiled, onSpoiledChange = { spoiled = it },
                        brokenFocus = brokenFocus,
                        ardidosFocus = ardidosFocus,
                        carunchadoFocus = carunchadoFocus,
                        spoiledFocus = spoiledFocus
                    )

                    2 -> MilhoExtrasTab(
                        daysOfStorage = daysOfStorage,
                        onDaysOfStorageChange = { daysOfStorage = it },
                        deductionValue = deductionValue,
                        onDeductionValueChange = { deductionValue = it },
                        doesTechnicalLoss = doesTechnicalLoss,
                        onDoesTechnicalLossChange = { doesTechnicalLoss = it },
                        doesDeduction = doesDeduction,
                        onDoesDeductionChange = { doesDeduction = it },
                        daysOfStorageFocus = daysOfStorageFocus,
                        deductionValueFocus = deductionValueFocus
                    )
                }

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // BOTÕES DE NAVEGAÇÃO
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (selectedTab > 0) {
                    Button(onClick = { selectedTab-- }) { Text("Voltar") }
                } else {
                    Spacer(Modifier.weight(1f))
                }

                if (selectedTab < tabTitles.lastIndex) {
                    Button(onClick = { selectedTab++ }) { Text("Avançar") }
                } else {
                    Button(
                        onClick = {
                            val fLotWeight = lotWeight.toFloatOrZero()
                            val fPriceBySack = priceBySack.toFloatOrZero()

                            if (fLotWeight <= 0f || fPriceBySack <= 0f) {
                                errorMessage = "Peso e Preço são obrigatórios."
                                return@Button
                            }

                            val fLotPrice = (fLotWeight * fPriceBySack) / 60f

                            val input = InputDiscountMilho(
                                classificationId = loadedClassif?.id ?: classificationId,
                                grain = "Milho",
                                group = viewModel.selectedGroup ?: 1,
                                limitSource = 0,
                                daysOfStorage = daysOfStorage.toIntOrZero(),
                                lotWeight = fLotWeight,
                                lotPrice = fLotPrice,
                                impurities = impurities.toFloatOrZero(),
                                moisture = moisture.toFloatOrZero(),
                                broken = broken.toFloatOrZero(),
                                ardidos = ardidos.toFloatOrZero(),
                                spoiled = spoiled.toFloatOrZero(),
                                carunchado = carunchado.toFloatOrZero(),
                                deductionValue = deductionValue.toFloatOrZero()
                            )

                            try {
                                viewModel.setDiscount(input, doesTechnicalLoss, true, doesDeduction)
                                navController.navigate("milhoDiscountResult")
                            } catch (e: Exception) {
                                errorMessage = "Erro no cálculo: ${e.message}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth(0.5f)
                    ) {
                        Text("Calcular")
                    }
                }
            }
        }
    }
}

@Composable
fun MilhoBasicInfoTab(
    lotWeight: String, onLotWeightChange: (String) -> Unit,
    priceBySack: String, onPriceBySackChange: (String) -> Unit,
    moisture: String, onMoistureChange: (String) -> Unit,
    impurities: String, onImpuritiesChange: (String) -> Unit,
    lotWeightFocus: FocusRequester,
    priceBySackFocus: FocusRequester,
    moistureFocus: FocusRequester,
    impuritiesFocus: FocusRequester
) {
    Column {
        NumberInputField(lotWeight, onLotWeightChange, "Peso do lote (kg)", lotWeightFocus, priceBySackFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(priceBySack, onPriceBySackChange, "Preço por Saca (60kg)", priceBySackFocus, moistureFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(moisture, onMoistureChange, "Umidade (%)", moistureFocus, impuritiesFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(impurities, onImpuritiesChange, "Matéria Estranha e Impurezas (%)", impuritiesFocus, null)
    }
}

@Composable
fun MilhoDefectsTab(
    broken: String, onBrokenChange: (String) -> Unit,
    ardidos: String, onArdidosChange: (String) -> Unit,
    carunchado: String, onCarunchadoChange: (String) -> Unit,
    spoiled: String, onSpoiledChange: (String) -> Unit,
    brokenFocus: FocusRequester,
    ardidosFocus: FocusRequester,
    carunchadoFocus: FocusRequester,
    spoiledFocus: FocusRequester
) {
    Column {
        NumberInputField(broken, onBrokenChange, "Quebrados (%)", brokenFocus, ardidosFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(ardidos, onArdidosChange, "Ardidos (%)", ardidosFocus, carunchadoFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(carunchado, onCarunchadoChange, "Carunchados (%)", carunchadoFocus, spoiledFocus)
        Spacer(Modifier.height(16.dp))
        NumberInputField(spoiled, onSpoiledChange, "Total de Avariados (%)", spoiledFocus, null)
    }
}

@Composable
fun MilhoExtrasTab(
    daysOfStorage: String, onDaysOfStorageChange: (String) -> Unit,
    deductionValue: String, onDeductionValueChange: (String) -> Unit,
    doesTechnicalLoss: Boolean, onDoesTechnicalLossChange: (Boolean) -> Unit,
    doesDeduction: Boolean, onDoesDeductionChange: (Boolean) -> Unit,
    daysOfStorageFocus: FocusRequester,
    deductionValueFocus: FocusRequester
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesTechnicalLoss, onCheckedChange = onDoesTechnicalLossChange)
            Spacer(Modifier.width(8.dp))
            Text("Aplicar Quebra Técnica?")
        }
        if (doesTechnicalLoss) {
            Spacer(Modifier.height(8.dp))
            NumberInputField(daysOfStorage, onDaysOfStorageChange, "Dias de armazenamento", daysOfStorageFocus, deductionValueFocus)
        }

        Spacer(Modifier.height(24.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesDeduction, onCheckedChange = onDoesDeductionChange)
            Spacer(Modifier.width(8.dp))
            Text("Aplicar Deságio?")
        }
        if (doesDeduction) {
            Spacer(Modifier.height(8.dp))
            NumberInputField(deductionValue, onDeductionValueChange, "Valor do Deságio (%)", deductionValueFocus, null)
        }
    }
}

@Composable
private fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester? = null
) {
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = value,
        onValueChange = {
            // Permite apenas números e UM ponto decimal
            if (it.isEmpty() || it.matches(Regex("^(\\d*\\.?\\d*)$"))) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
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

private fun String.toFloatOrZero(): Float {
    return this.replace(",", ".").toFloatOrNull() ?: 0f
}

private fun String.toIntOrZero(): Int {
    return this.toIntOrNull() ?: 0
}