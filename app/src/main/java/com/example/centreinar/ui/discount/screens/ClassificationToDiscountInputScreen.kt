package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel

@Composable
fun ClassificationToDiscountInputScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    var priceBySack by remember { mutableStateOf("") }
    var daysOfStorage by remember { mutableStateOf("0") }
    var deductionValue by remember { mutableStateOf("0") }
    var doesTechnicalLoss by remember { mutableStateOf(false) }
    var doesDeduction by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val priceFocus = remember { FocusRequester() }
    val daysFocus = remember { FocusRequester() }
    val deductionFocus = remember { FocusRequester() }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        Text(
            "Insira os dados",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(Modifier.height(10.dp))

        NumberInputField(
            value = priceBySack,
            onValueChange = { priceBySack = it },
            label = "Preço por Saca (60kg)",
            focusRequester = priceFocus
        )

        Spacer(Modifier.height(10.dp))

        Text("Desconto por Perda Técnica?")
        Switch(
            checked = doesTechnicalLoss,
            onCheckedChange = { doesTechnicalLoss = it }
        )

        if (doesTechnicalLoss) {
            NumberInputField(
                value = daysOfStorage,
                onValueChange = { daysOfStorage = it },
                label = "Dias de armazenamento",
                focusRequester = daysFocus
            )
        }

        Spacer(Modifier.height(10.dp))

        Text("Aplicar Deságio?")
        Switch(
            checked = doesDeduction,
            onCheckedChange = { doesDeduction = it }
        )

        if (doesDeduction) {
            NumberInputField(
                value = deductionValue,
                onValueChange = { deductionValue = it },
                label = "Valor de Deságio (R$)",
                focusRequester = deductionFocus
            )
        }

        Spacer(Modifier.height(20.dp))

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                val price = priceBySack.toFloatOrNull() ?: 0f
                val days = daysOfStorage.toIntOrNull() ?: 0
                val deduction = deductionValue.toFloatOrNull() ?: 0f

                try {
                    viewModel.getDiscountForClassification(
                        price = price,
                        days= days,
                        deduction = deduction
                    )
                    navController.navigate("discountResultsScreen")
                } catch (e: Exception) {
                    errorMessage = "Erro ao calcular o desconto"
                }
            }
        ) {
            Text("Calcular Desconto")
        }

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
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
        enabled = enabled,
        singleLine = true
    )
}
