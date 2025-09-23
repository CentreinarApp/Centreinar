package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
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
    var lotWeight by remember { mutableStateOf("") }
    var priceBySack by remember { mutableStateOf("") }
    var impurities by remember { mutableStateOf("") }
    var broken by remember { mutableStateOf("") }
    var ardidos by remember { mutableStateOf("") }
    var mofados by remember { mutableStateOf("") }
    var carunchado by remember { mutableStateOf("") }
    var daysOfStorage by remember { mutableStateOf("0") }
    var deductionValue by remember { mutableStateOf("0") }
    var doesTechnicalLoss by remember { mutableStateOf(false) }
    var doesDeduction by remember { mutableStateOf(false) }

    val lotFocus = remember { FocusRequester() }
    val priceFocus = remember { FocusRequester() }
    val impuritiesFocus = remember { FocusRequester() }

    val keyboardController = LocalSoftwareKeyboardController.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Cálculo de Desconto — Milho", style = MaterialTheme.typography.headlineMedium)

        OutlinedTextField(value = lotWeight, onValueChange = { lotWeight = sanitize(it) }, label = { Text("Peso do lote (kg)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).focusRequester(lotFocus), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { priceFocus.requestFocus() }))
        OutlinedTextField(value = priceBySack, onValueChange = { priceBySack = sanitize(it) }, label = { Text("Preço por saca (60kg)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).focusRequester(priceFocus), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Next))

        OutlinedTextField(value = impurities, onValueChange = { impurities = sanitize(it) }, label = { Text("Impurezas (%)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).focusRequester(impuritiesFocus))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(value = broken, onValueChange = { broken = sanitize(it) }, label = { Text("Partidos (%)") }, modifier = Modifier.weight(1f))
            OutlinedTextField(value = ardidos, onValueChange = { ardidos = sanitize(it) }, label = { Text("Ardidos (%)") }, modifier = Modifier.weight(1f))
        }
        OutlinedTextField(value = mofados, onValueChange = { mofados = sanitize(it) }, label = { Text("Mofados (%)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        OutlinedTextField(value = carunchado, onValueChange = { carunchado = sanitize(it) }, label = { Text("Carunchado (%)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))

        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = doesTechnicalLoss, onCheckedChange = { doesTechnicalLoss = it })
            Text("Perda técnica")
        }
        if (doesTechnicalLoss) {
            OutlinedTextField(value = daysOfStorage, onValueChange = { daysOfStorage = sanitize(it) }, label = { Text("Dias de armazenamento") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }

        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Switch(checked = doesDeduction, onCheckedChange = { doesDeduction = it })
            Text("Aplicar deságio")
        }
        if (doesDeduction) {
            OutlinedTextField(value = deductionValue, onValueChange = { deductionValue = sanitize(it) }, label = { Text("Valor do deságio (%)") }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp))
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            val input = InputDiscountMilho(
                classificationId = null,
                grain = "milho",
                group = viewModel.selectedGroup ?: 1,
                limitSource = 0,
                daysOfStorage = daysOfStorage.toIntOrNull() ?: 0,
                lotWeight = lotWeight.toFloatOrNull() ?: 0f,
                lotPrice = ((lotWeight.toFloatOrNull() ?: 0f) * (priceBySack.toFloatOrNull() ?: 0f) / 60f),
                impurities = impurities.toFloatOrNull() ?: 0f,
                humidity = 0f,
                broken = broken.toFloatOrNull() ?: 0f,
                ardidos = ardidos.toFloatOrNull() ?: 0f,
                mofados = mofados.toFloatOrNull() ?: 0f,
                deductionValue = deductionValue.toFloatOrNull() ?: 0f
            )
            // call repository via DiscountViewModel. You need DiscountRepositoryMilho implementation registered.
            viewModel.setDiscount(input, doesTechnicalLoss, doesClassificationLoss = true, doesDeduction)
            navController.navigate("milhoDiscountResult")
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Calcular Desconto")
        }
    }
}

private fun sanitize(input:String):String = input.filter { it.isDigit() || it == '.' }
