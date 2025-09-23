package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import com.example.centreinar.InputDiscountSoja
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import java.math.RoundingMode

@Composable
fun DiscountInputScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {

    val grain = viewModel.selectedGrain
    val group = viewModel.selectedGroup

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
    var doesTechnicalLoss by  remember { mutableStateOf(false) }
    var doesDeduction by remember { mutableStateOf(false) }


    var errorMessage by remember { mutableStateOf<String?>(null) }

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

    val keyboardController = LocalSoftwareKeyboardController.current

    val tabTitles = listOf("Informação Básica", "Defeitos 1", "Defeitos 2")
    var selectedTab by remember { mutableStateOf(0) }

    LaunchedEffect(selectedTab) {
        when (selectedTab) {
            0 -> lotWeightFocus.requestFocus()
            1 -> burntFocus.requestFocus()
            2 -> greenishFocus.requestFocus()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
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
            "Insira os dados",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )

        Spacer(modifier = Modifier.height(5.dp))

        //Lot weight
        when(selectedTab){

            0 -> BasicInfoTab(
                lotWeight = lotWeight,
                onLotWeightChange = { lotWeight = it },
                moisture = moisture,
                onMoistureChange = { moisture = it },
                impurities = impurities,
                onImpuritiesChange = { impurities = it },
                priceBySack = priceBySack,
                onPriceBySackChange = {priceBySack = it},
                lotWeightFocus = lotWeightFocus,
                moistureFocus = moistureFocus,
                impuritiesFocus = impuritiesFocus,
                priceBySackFocus = priceBySackFocus
            )

            1 ->GraveDefectsTab(
                burnt = burnt,
                onBurntChange = { burnt = it },
                burntOrSour = burntOrSour,
                onBurntOrSourChange = {burntOrSour = it},
                moldy = moldy,
                onMoldyChange = { moldy = it },
                spoiled = spoiled,
                onSpoiledChange = {spoiled = it},
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
                onDaysOfStorageChange = {daysOfStorage = it},
                deductionValue = deductionValue,
                onDeductionValueChange = {deductionValue = it},
                doesDeduction = doesDeduction,
                onDoesDeductionChange = {doesDeduction = it},
                doesTechnicalLoss = doesTechnicalLoss,
                onDoesTechnicalLossChange = {doesTechnicalLoss = it},
                daysOfStorageFocus = daysOfStorageFocus,
                deductionValueFocus = deductionValueFocus
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

                        val inputDiscount = InputDiscountSoja(
                            grain = grain!!,
                            group = group!!,
                            limitSource = 0,
                            classificationId = null,
                            daysOfStorage = daysOfStorage.toInt(),
                            lotWeight = lotWeight
                                .toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            lotPrice = ((lotWeight
                                .toBigDecimal()
                                .setScale(2, RoundingMode.HALF_UP)
                                    * priceBySack.toBigDecimal()
                                .setScale(2, RoundingMode.HALF_UP)
                                    ).toFloat() / 60) ?: 0f,
                            foreignMattersAndImpurities = impurities.toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            humidity = moisture.toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            burnt = burnt.toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            burntOrSour = burntOrSour.toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            moldy = moldy.toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            spoiled = spoiled.toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            greenish = greenish.toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            brokenCrackedDamaged = brokenCrackedDamaged.toBigDecimalOrNull()
                                ?.setScale(2, RoundingMode.HALF_UP)
                                ?.toFloat() ?: 0f,
                            deductionValue = deductionValue.toFloat()
                        )

                        try {
                            viewModel.setDiscount(inputDiscount,true,true,true)
                        } catch (e: NumberFormatException) {
                            errorMessage = "Valores numéricos inválidos detectados"
                        }
                        navController.navigate("discountResultsScreen")
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Calcular Desconto")
                }
            }
        }


    }
}

@Composable
fun BasicInfoTab(
    lotWeight: String,
    onLotWeightChange: (String) -> Unit,
    priceBySack: String,
    onPriceBySackChange:(String) -> Unit,
    moisture: String,
    onMoistureChange: (String) -> Unit,
    impurities: String,
    onImpuritiesChange: (String) -> Unit,
    lotWeightFocus: FocusRequester,
    moistureFocus: FocusRequester,
    impuritiesFocus: FocusRequester,
    priceBySackFocus: FocusRequester
) {
    Column(modifier = Modifier.padding(16.dp)) {
        NumberInputField(
            value = lotWeight,
            onValueChange = onLotWeightChange,
            label = "Peso do lote (kg)",
            focusRequester = lotWeightFocus,
            nextFocus = priceBySackFocus,
        )

        Spacer(modifier = Modifier.height(16.dp))

        //Lot Price
        NumberInputField(
            value = priceBySack,
            onValueChange = onPriceBySackChange,
            label = "Preço por Saca (60kg)",
            focusRequester = priceBySackFocus,
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

        // Impurities
        NumberInputField(
            value = impurities,
            onValueChange = onImpuritiesChange,
            label = "Matéria estranha e Impurezas (%)",
            focusRequester = impuritiesFocus,
            nextFocus = null
        )

        Spacer(modifier = Modifier.height(16.dp))


    }
}

@Composable
fun GraveDefectsTab(
    burnt: String,
    onBurntChange: (String) -> Unit,
    burntOrSour: String,
    onBurntOrSourChange: (String) -> Unit,
    moldy: String,
    onMoldyChange: (String) -> Unit,
    spoiled: String,
    onSpoiledChange: (String) -> Unit,
    burntFocus: FocusRequester,
    burntOrSourFocus: FocusRequester,
    moldyFocus: FocusRequester,
    spoiledFocus: FocusRequester,
) {
    Column(modifier = Modifier.padding(16.dp)) {
        // Burnt
        NumberInputField(
            value = burnt,
            onValueChange = onBurntChange,
            label = "Queimados (%)",
            focusRequester = burntFocus,
            nextFocus = burntOrSourFocus,
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Burnt or Sour
        NumberInputField(
            value = burntOrSour,
            onValueChange = onBurntOrSourChange,
            label = "Ardidos e Queimados (%)",
            focusRequester = burntOrSourFocus,
            nextFocus = moldyFocus,
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Moldy
        NumberInputField(
            value = moldy,
            onValueChange = onMoldyChange,
            label = "Mofados (%)",
            focusRequester = moldyFocus,
            nextFocus = spoiledFocus,
        )

        Spacer(modifier = Modifier.height(16.dp))
        // spoiled
        NumberInputField(
            value = spoiled,
            onValueChange = onSpoiledChange,
            label = "Total de Avariados (%)",
            focusRequester = spoiledFocus,
            nextFocus = null
        )
    }
}

@Composable
fun FinalDefectsTab(
    greenish: String,
    onGreenishChange: (String) -> Unit,
    brokenCrackedDamaged: String,
    onBrokenCrackedDamagedChange: (String) -> Unit,
    daysOfStorage : String,
    onDaysOfStorageChange: (String) -> Unit,
    deductionValue: String,
    onDeductionValueChange: (String) -> Unit,
    greenishFocus: FocusRequester,
    brokenCrackedDamagedFocus: FocusRequester,
    daysOfStorageFocus: FocusRequester,
    deductionValueFocus: FocusRequester,
    doesTechnicalLoss: Boolean,
    onDoesTechnicalLossChange: (Boolean) ->Unit,
    doesDeduction:Boolean,
    onDoesDeductionChange:(Boolean) ->Unit
) {
    Column(modifier = Modifier.padding(16.dp)) {

        NumberInputField(
            value = greenish,
            onValueChange = onGreenishChange,
            label = "Esverdeados (g)",
            focusRequester = greenishFocus,
            nextFocus = brokenCrackedDamagedFocus
        )

        Spacer(modifier = Modifier.height(16.dp))

        NumberInputField(
            value = brokenCrackedDamaged,
            onValueChange = onBrokenCrackedDamagedChange,
            label = "Partidos, Quebrados e Amassados (g)",
            focusRequester = brokenCrackedDamagedFocus,
            nextFocus = null
        )

        Spacer(modifier = Modifier.height(16.dp))

        Switch(
            checked = doesTechnicalLoss,
            onCheckedChange = onDoesTechnicalLossChange
        )

        if(doesTechnicalLoss){
            NumberInputField(
                value = daysOfStorage,
                onValueChange = onDaysOfStorageChange ,
                label = "Dias de armazenamento",
                focusRequester = daysOfStorageFocus,
                nextFocus = null
            )
        }

        Switch(
            checked = doesDeduction,
            onCheckedChange = onDoesDeductionChange,
        )

        if(doesDeduction){
            NumberInputField(
                value = deductionValue,
                onValueChange = onDeductionValueChange,
                label = "Valor de Deságio",
                focusRequester = deductionValueFocus,
                nextFocus = null
            )
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
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
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
