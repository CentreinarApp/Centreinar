package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    var doesTechnicalLoss by  remember { mutableStateOf(false) }
    var doesDeduction by remember { mutableStateOf(false) }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    val priceBySackFocus = remember { FocusRequester() }
    val daysOfStorageFocus = remember { FocusRequester() }
    val deductionValueFocus = remember { FocusRequester() }


    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Text(
            "Insira os dados",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(16.dp)
        )
        Spacer(modifier = Modifier.height(5.dp))

        NumberInputField(
            value = priceBySack,
            onValueChange = {priceBySack = it} ,
            label = "Preço por Saca (60kg)",
            focusRequester = priceBySackFocus,
            nextFocus = null,
        )
        Spacer(modifier = Modifier.height(5.dp))

        Switch(
            checked = doesTechnicalLoss,
            onCheckedChange = { doesTechnicalLoss = it }
        )

        if(doesTechnicalLoss){
            NumberInputField(
                value = daysOfStorage,
                onValueChange = {daysOfStorage = it} ,
                label = "Dias de armazenamento",
                focusRequester = daysOfStorageFocus,
                nextFocus = null
            )
        }

        Switch(
            checked = doesDeduction,
            onCheckedChange = { doesDeduction = it },
        )

        if(doesDeduction){
            NumberInputField(
                value = deductionValue,
                onValueChange = {deductionValue = it},
                label = "Valor de Deságio",
                focusRequester = deductionValueFocus,
                nextFocus = null
            )
        }

        Button(
            onClick = {
                viewModel.getDiscountForClassification(priceBySack = priceBySack.toFloat(), daysOfStorage = daysOfStorage.toInt(), deductionValue = deductionValue.toFloat())
                try {
                    viewModel.getDiscountForClassification(priceBySack = priceBySack.toFloat(), daysOfStorage = daysOfStorage.toInt(), deductionValue = deductionValue.toFloat())
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