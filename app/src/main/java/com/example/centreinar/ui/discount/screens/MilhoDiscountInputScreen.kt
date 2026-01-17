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
import androidx.navigation.NavController
import com.example.centreinar.data.local.entity.InputDiscountMilho
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import java.math.RoundingMode

@Composable
fun MilhoDiscountInputScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val scrollState = rememberScrollState()

    // --- ESTADOS DOS CAMPOS ---
    var lotWeight by remember { mutableStateOf("") }
    var priceBySack by remember { mutableStateOf("") }
    var moisture by remember { mutableStateOf("") }     // Umidade
    var impurities by remember { mutableStateOf("") }   // Impurezas
    var broken by remember { mutableStateOf("") }       // Quebrados
    var ardidos by remember { mutableStateOf("") }      // Ardidos
    var mofados by remember { mutableStateOf("") }      // Mofados
    var carunchado by remember { mutableStateOf("") }   // Carunchados
    var spoiled by remember { mutableStateOf("") }      // Avariados (Total)
    var daysOfStorage by remember { mutableStateOf("0") }
    var deductionValue by remember { mutableStateOf("0") }
    var doesTechnicalLoss by remember { mutableStateOf(false) }
    var doesDeduction by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // FOCUS REQUESTERS
    val lotWeightFocus = remember { FocusRequester() }
    val priceBySackFocus = remember { FocusRequester() }
    val moistureFocus = remember { FocusRequester() }
    val impuritiesFocus = remember { FocusRequester() }
    val brokenFocus = remember { FocusRequester() }
    val ardidosFocus = remember { FocusRequester() }
    val mofadosFocus = remember { FocusRequester() }
    val carunchadoFocus = remember { FocusRequester() }
    val spoiledFocus = remember { FocusRequester() }

    val daysOfStorageFocus = remember { FocusRequester() }
    val deductionValueFocus = remember { FocusRequester() }

    // Configuração das Abas
    val tabTitles = listOf("Básico", "Defeitos", "Extras")
    var selectedTab by remember { mutableStateOf(0) }

    // Foco inicial ao trocar de aba
    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> lotWeightFocus.requestFocus()
            1 -> brokenFocus.requestFocus()
            2 -> if (doesTechnicalLoss) daysOfStorageFocus.requestFocus()
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // BARRA DE ABAS
        TabRow(selectedTabIndex = selectedTab) {
            tabTitles.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(text = title) }
                )
            }
        }

        // CONTEÚDO DAS ABAS
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
                // ABA 1: Informações Básicas
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

                // ABA 2: Defeitos
                1 -> MilhoDefectsTab(
                    broken = broken, onBrokenChange = { broken = it },
                    ardidos = ardidos, onArdidosChange = { ardidos = it },
                    carunchado = carunchado, onCarunchadoChange = { carunchado = it },
                    spoiled = spoiled, onSpoiledChange = { spoiled = it },
                    brokenFocus = brokenFocus,
                    ardidosFocus = ardidosFocus,
                    mofadosFocus = mofadosFocus,
                    carunchadoFocus = carunchadoFocus,
                    spoiledFocus = spoiledFocus
                )

                // ABA 3: Extras
                2 -> MilhoExtrasTab(
                    daysOfStorage = daysOfStorage, onDaysOfStorageChange = { daysOfStorage = it },
                    deductionValue = deductionValue, onDeductionValueChange = { deductionValue = it },
                    doesTechnicalLoss = doesTechnicalLoss, onDoesTechnicalLossChange = { doesTechnicalLoss = it },
                    doesDeduction = doesDeduction, onDoesDeductionChange = { doesDeduction = it },
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Botão Voltar
            if (selectedTab > 0) {
                Button(onClick = { selectedTab-- }) {
                    Text("Voltar")
                }
            } else {
                Spacer(Modifier.weight(1f))
            }

            // Botão Avançar ou Calcular
            if (selectedTab < tabTitles.lastIndex) {
                Button(onClick = { selectedTab++ }) {
                    Text("Avançar")
                }
            } else {
                Button(
                    onClick = {
                        // Conversões
                        val fLotWeight = lotWeight.toFloatOrZero()
                        val fPriceBySack = priceBySack.toFloatOrZero()

                        if (fLotWeight <= 0f || fPriceBySack <= 0f) {
                            errorMessage = "Peso e Preço são obrigatórios."
                            return@Button
                        }

                        val fLotPrice = (fLotWeight * fPriceBySack) / 60f

                        // Criação do Objeto de Entrada do Milho
                        val input = InputDiscountMilho(
                            classificationId = null,
                            grain = "milho",
                            group = viewModel.selectedGroup ?: 1,
                            limitSource = 0,
                            daysOfStorage = daysOfStorage.toIntOrZero(),
                            lotWeight = fLotWeight,
                            lotPrice = fLotPrice,

                            impurities = impurities.toFloatOrZero(),
                            moisture = moisture.toFloatOrZero(), // Umidade
                            broken = broken.toFloatOrZero(),     // Quebrados
                            ardidos = ardidos.toFloatOrZero(),   // Ardidos
                            spoiled = spoiled.toFloatOrZero(), // Total de Avariados
                            carunchado = carunchado.toFloatOrZero(), // Carunchados
                            deductionValue = deductionValue.toFloatOrZero()
                        )

                        try {
                            viewModel.setDiscount(
                                input,
                                doesTechnicalLoss,
                                doesClassificationLoss = true,
                                doesDeduction
                            )
                            navController.navigate("milhoDiscountResult")
                        } catch (e: Exception) {
                            errorMessage = "Erro no cálculo: ${e.message}"
                            Log.e("MilhoInput", "Erro", e)
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
        NumberInputField(
            value = lotWeight, onValueChange = onLotWeightChange,
            label = "Peso do lote (kg)",
            focusRequester = lotWeightFocus, nextFocus = priceBySackFocus
        )
        Spacer(Modifier.height(16.dp))

        NumberInputField(
            value = priceBySack, onValueChange = onPriceBySackChange,
            label = "Preço por Saca (60kg)",
            focusRequester = priceBySackFocus, nextFocus = moistureFocus
        )
        Spacer(Modifier.height(16.dp))

        NumberInputField(
            value = impurities, onValueChange = onImpuritiesChange,
            label = "Matéria Estranha e Impurezas (%)",
            focusRequester = impuritiesFocus, nextFocus = null
        )
        Spacer(Modifier.height(16.dp))

        NumberInputField(
            value = moisture, onValueChange = onMoistureChange,
            label = "Umidade (%)",
            focusRequester = moistureFocus, nextFocus = impuritiesFocus
        )
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
    mofadosFocus: FocusRequester,
    carunchadoFocus: FocusRequester,
    spoiledFocus: FocusRequester
) {
    Column {
        NumberInputField(
            value = broken, onValueChange = onBrokenChange,
            label = "Quebrados (%)",
            focusRequester = brokenFocus, nextFocus = ardidosFocus
        )
        Spacer(Modifier.height(16.dp))

        NumberInputField(
            value = ardidos, onValueChange = onArdidosChange,
            label = "Ardidos (%)",
            focusRequester = ardidosFocus, nextFocus = mofadosFocus
        )
        Spacer(Modifier.height(16.dp))

        NumberInputField(
            value = spoiled, onValueChange = onSpoiledChange,
            label = "Total de Avariados (%)",
            focusRequester = spoiledFocus, nextFocus = null
        )
        Spacer(Modifier.height(16.dp))

        NumberInputField(
            value = carunchado, onValueChange = onCarunchadoChange,
            label = "Carunchados (%)",
            focusRequester = carunchadoFocus, nextFocus = spoiledFocus
        )
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
        // Quebra Técnica
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesTechnicalLoss, onCheckedChange = onDoesTechnicalLossChange)
            Spacer(Modifier.width(8.dp))
            Text("Aplicar Quebra Técnica?")
        }
        if (doesTechnicalLoss) {
            Spacer(Modifier.height(8.dp))
            NumberInputField(
                value = daysOfStorage, onValueChange = onDaysOfStorageChange,
                label = "Dias de armazenamento",
                focusRequester = daysOfStorageFocus, nextFocus = null
            )
        }

        Spacer(Modifier.height(24.dp))

        // Deságio
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesDeduction, onCheckedChange = onDoesDeductionChange)
            Spacer(Modifier.width(8.dp))
            Text("Aplicar Deságio?")
        }
        if (doesDeduction) {
            Spacer(Modifier.height(8.dp))
            NumberInputField(
                value = deductionValue, onValueChange = onDeductionValueChange,
                label = "Valor do Deságio (%)",
                focusRequester = deductionValueFocus, nextFocus = null
            )
        }
    }
}

// --- FUNÇÕES AUXILIARES ---

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
            // Validação simples: números e um ponto decimal
            if (it.all { char -> char.isDigit() || char == '.' }) {
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
        singleLine = true
    )
}

private fun String.toFloatOrZero(): Float {
    return this.toBigDecimalOrNull()
        ?.setScale(2, RoundingMode.HALF_UP)
        ?.toFloat() ?: 0f
}

private fun String.toIntOrZero(): Int {
    return this.toBigDecimalOrNull()?.toInt() ?: 0
}