package com.example.centreinar.ui.discount.screens

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import java.math.RoundingMode

@Composable
fun DiscountInputScreen(
    navController: NavController,
    classificationId: Int? = null, // Recebe o ID opcional da classificação
    viewModel: DiscountViewModel = hiltViewModel()
) {
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        val scrollState = rememberScrollState()

        // Estados observados do ViewModel
        val loadedClassif by viewModel.loadedClassification.collectAsStateWithLifecycle()
        val loadedSample by viewModel.loadedSample.collectAsStateWithLifecycle()

        val grain = viewModel.selectedGrain
        val group = viewModel.selectedGroup

        // Estados dos Campos de Input
        var lotWeight by remember { mutableStateOf("") }
        var priceBySack by remember { mutableStateOf("") }
        var moisture by remember { mutableStateOf("") }
        var impurities by remember { mutableStateOf("") }
        var daysOfStorage by remember { mutableStateOf("0") }
        var deductionValue by remember { mutableStateOf("0") }
        var brokenCrackedDamaged by remember { mutableStateOf("") }
        var greenish by remember { mutableStateOf("") }
        var burnt by remember { mutableStateOf("") }
        var burntOrSour by remember { mutableStateOf("") }
        var moldy by remember { mutableStateOf("") }
        var spoiled by remember { mutableStateOf("") }
        var doesTechnicalLoss by remember { mutableStateOf(false) }
        var doesDeduction by remember { mutableStateOf(false) }

        var errorMessage by remember { mutableStateOf<String?>(null) }

        // --- LÓGICA DE PREENCHIMENTO AUTOMÁTICO ---

        // Dispara o carregamento se houver um ID
        LaunchedEffect(classificationId) {
            if (classificationId != null && classificationId > 0) {
                viewModel.loadClassificationData(classificationId)
            }
        }

        // Escuta mudanças nos dados carregados e preenche os campos
        LaunchedEffect(loadedClassif, loadedSample) {
            // Preenche dados da Amostra (Peso e Umidade)
            loadedSample?.let { sample ->
                lotWeight = sample.lotWeight.toString()
                moisture = sample.humidity.toString()
            }
            // Preenche dados da Classificação (Impurezas e Defeitos)
            loadedClassif?.let { classif ->
                impurities = classif.impuritiesPercentage.toString()
                burnt = classif.burntPercentage.toString()
                burntOrSour = classif.burntOrSourPercentage.toString()
                moldy = classif.moldyPercentage.toString()
                spoiled = classif.spoiledPercentage.toString()
                greenish = classif.greenishPercentage.toString()
                brokenCrackedDamaged = classif.brokenCrackedDamagedPercentage.toString()
            }
        }

        // --- CONFIGURAÇÃO DE FOCO ---
        val lotWeightFocus = remember { FocusRequester() }
        val priceBySackFocus = remember { FocusRequester() }
        val moistureFocus = remember { FocusRequester() }
        val impuritiesFocus = remember { FocusRequester() }
        val brokenFocus = remember { FocusRequester() }
        val greenishFocus = remember { FocusRequester() }
        val burntFocus = remember { FocusRequester() }
        val burntOrSourFocus = remember { FocusRequester() }
        val moldyFocus = remember { FocusRequester() }
        val spoiledFocus = remember { FocusRequester() }
        val daysOfStorageFocus = remember { FocusRequester() }
        val deductionValueFocus = remember { FocusRequester() }

        val tabTitles = listOf("Informação Básica", "Defeitos 1", "Defeitos 2")
        var selectedTab by remember { mutableStateOf(0) }

        LaunchedEffect(selectedTab) {
            when (selectedTab) {
                0 -> lotWeightFocus.requestFocus()
                1 -> burntFocus.requestFocus()
                2 -> greenishFocus.requestFocus()
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)
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

            Column(modifier = Modifier.weight(1f).fillMaxWidth()) {
                Text(
                    "Insira os dados",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(16.dp)
                )

                errorMessage?.let {
                    Text(
                        it,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(5.dp))

                when (selectedTab) {
                    0 -> BasicInfoTab(
                        lotWeight = lotWeight,
                        onLotWeightChange = { lotWeight = it },
                        moisture = moisture,
                        onMoistureChange = { moisture = it },
                        impurities = impurities,
                        onImpuritiesChange = { impurities = it },
                        priceBySack = priceBySack,
                        onPriceBySackChange = { priceBySack = it },
                        lotWeightFocus = lotWeightFocus,
                        moistureFocus = moistureFocus,
                        impuritiesFocus = impuritiesFocus,
                        priceBySackFocus = priceBySackFocus
                    )

                    1 -> GraveDefectsTab(
                        burnt = burnt,
                        onBurntChange = { burnt = it },
                        burntOrSour = burntOrSour,
                        onBurntOrSourChange = { burntOrSour = it },
                        moldy = moldy,
                        onMoldyChange = { moldy = it },
                        spoiled = spoiled,
                        onSpoiledChange = { spoiled = it },
                        burntFocus = burntFocus,
                        burntOrSourFocus = burntOrSourFocus,
                        moldyFocus = moldyFocus,
                        spoiledFocus = spoiledFocus
                    )

                    2 -> FinalDefectsTab(
                        greenish = greenish,
                        onGreenishChange = { greenish = it },
                        brokenCrackedDamaged = brokenCrackedDamaged,
                        onBrokenCrackedDamagedChange = { brokenCrackedDamaged = it },
                        greenishFocus = greenishFocus,
                        brokenCrackedDamagedFocus = brokenFocus,
                        daysOfStorage = daysOfStorage,
                        onDaysOfStorageChange = { daysOfStorage = it },
                        deductionValue = deductionValue,
                        onDeductionValueChange = { deductionValue = it },
                        doesDeduction = doesDeduction,
                        onDoesDeductionChange = { doesDeduction = it },
                        doesTechnicalLoss = doesTechnicalLoss,
                        onDoesTechnicalLossChange = { doesTechnicalLoss = it },
                        daysOfStorageFocus = daysOfStorageFocus,
                        deductionValueFocus = deductionValueFocus
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
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
                        onClick = {
                            val fLotWeight = lotWeight.toFloatOrZero()
                            val fPriceBySack = priceBySack.toFloatOrZero()
                            val fImp = impurities.toFloatOrZero()
                            val fMoisture = moisture.toFloatOrZero()
                            val fBroken = brokenCrackedDamaged.toFloatOrZero()
                            val fGreenish = greenish.toFloatOrZero()
                            val fBurnt = burnt.toFloatOrZero()
                            val fBurntOrSour = burntOrSour.toFloatOrZero()
                            val fMoldy = moldy.toFloatOrZero()
                            val fSpoiled = spoiled.toFloatOrZero()
                            val fDays = daysOfStorage.toIntOrZero()
                            val fDeduction = deductionValue.toFloatOrZero()

                            if (fLotWeight <= 0f || fPriceBySack <= 0f) {
                                errorMessage =
                                    "Por favor, insira valores válidos para Peso do Lote e Preço por Saca."
                                return@Button
                            }

                            val fLotPrice = (fLotWeight * fPriceBySack) / 60f

                            val inputDiscount = InputDiscountSoja(
                                grain = grain ?: "Soja",
                                group = group ?: 1,
                                limitSource = 0,
                                classificationId = loadedClassif?.id, // Vincula à classificação carregada
                                daysOfStorage = fDays,
                                lotWeight = fLotWeight,
                                lotPrice = fLotPrice,
                                foreignMattersAndImpurities = fImp,
                                humidity = fMoisture,
                                burnt = fBurnt,
                                burntOrSour = fBurntOrSour,
                                moldy = fMoldy,
                                spoiled = fSpoiled,
                                greenish = fGreenish,
                                brokenCrackedDamaged = fBroken,
                                deductionValue = fDeduction
                            )

                            try {
                                viewModel.setDiscount(
                                    inputDiscount,
                                    doesTechnicalLoss,
                                    true,
                                    doesDeduction
                                )
                                navController.navigate("discountResultsScreen")
                            } catch (e: Exception) {
                                errorMessage = "Erro no cálculo: ${e.message}"
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Calcular Desconto")
                    }
                }
            }
        }
    }
}

// --- COMPONENTES AUXILIARES (TABS) ---

@Composable
fun BasicInfoTab(
    lotWeight: String, onLotWeightChange: (String) -> Unit,
    priceBySack: String, onPriceBySackChange: (String) -> Unit,
    moisture: String, onMoistureChange: (String) -> Unit,
    impurities: String, onImpuritiesChange: (String) -> Unit,
    lotWeightFocus: FocusRequester, moistureFocus: FocusRequester,
    impuritiesFocus: FocusRequester, priceBySackFocus: FocusRequester
) {
    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        NumberInputField(lotWeight, onLotWeightChange, "Peso do lote (kg)", lotWeightFocus, priceBySackFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(priceBySack, onPriceBySackChange, "Preço por Saca (60kg)", priceBySackFocus, moistureFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(moisture, onMoistureChange, "Umidade (%)", moistureFocus, impuritiesFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(impurities, onImpuritiesChange, "Matéria estranha e Impurezas (%)", impuritiesFocus, null)
    }
}

@Composable
fun GraveDefectsTab(
    burnt: String, onBurntChange: (String) -> Unit,
    burntOrSour: String, onBurntOrSourChange: (String) -> Unit,
    moldy: String, onMoldyChange: (String) -> Unit,
    spoiled: String, onSpoiledChange: (String) -> Unit,
    burntFocus: FocusRequester, burntOrSourFocus: FocusRequester,
    moldyFocus: FocusRequester, spoiledFocus: FocusRequester,
) {
    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        NumberInputField(burnt, onBurntChange, "Queimados (%)", burntFocus, burntOrSourFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(burntOrSour, onBurntOrSourChange, "Ardidos e Queimados (%)", burntOrSourFocus, moldyFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(moldy, onMoldyChange, "Mofados (%)", moldyFocus, spoiledFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(spoiled, onSpoiledChange, "Total de Avariados (%)", spoiledFocus, null)
    }
}

@Composable
fun FinalDefectsTab(
    greenish: String, onGreenishChange: (String) -> Unit,
    brokenCrackedDamaged: String, onBrokenCrackedDamagedChange: (String) -> Unit,
    daysOfStorage: String, onDaysOfStorageChange: (String) -> Unit,
    deductionValue: String, onDeductionValueChange: (String) -> Unit,
    greenishFocus: FocusRequester, brokenCrackedDamagedFocus: FocusRequester,
    daysOfStorageFocus: FocusRequester, deductionValueFocus: FocusRequester,
    doesTechnicalLoss: Boolean, onDoesTechnicalLossChange: (Boolean) -> Unit,
    doesDeduction: Boolean, onDoesDeductionChange: (Boolean) -> Unit
) {
    Column(modifier = Modifier.padding(16.dp).verticalScroll(rememberScrollState())) {
        NumberInputField(greenish, onGreenishChange, "Esverdeados (%)", greenishFocus, brokenCrackedDamagedFocus)
        Spacer(modifier = Modifier.height(16.dp))
        NumberInputField(brokenCrackedDamaged, onBrokenCrackedDamagedChange, "Partidos/Quebrados (%)", brokenCrackedDamagedFocus, daysOfStorageFocus)
        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesTechnicalLoss, onCheckedChange = onDoesTechnicalLossChange)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Aplicar Quebra Técnica?")
        }

        if (doesTechnicalLoss) {
            NumberInputField(daysOfStorage, onDaysOfStorageChange, "Dias de armazenamento", daysOfStorageFocus, deductionValueFocus)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesDeduction, onCheckedChange = onDoesDeductionChange)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Aplicar Deságio?")
        }

        if (doesDeduction) {
            NumberInputField(deductionValue, onDeductionValueChange, "Valor de Deságio", deductionValueFocus, null)
        }
    }
}

@Composable
private fun NumberInputField(
    value: String, onValueChange: (String) -> Unit, label: String,
    focusRequester: FocusRequester, nextFocus: FocusRequester? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done),
        keyboardActions = KeyboardActions(onNext = { nextFocus?.requestFocus() }),
        singleLine = true
    )
}

// --- FUNÇÕES DE EXTENSÃO ---

private fun String.toFloatOrZero(): Float {
    return this.replace(",", ".").toFloatOrNull() ?: 0f
}

private fun String.toIntOrZero(): Int {
    return this.toIntOrNull() ?: 0
}