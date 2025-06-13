package com.example.centreinar.ui.classificationProcess.screens

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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.util.Utilities

@Composable
fun ColorClassInput(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
){
    //state values
    var totalWeight by remember { mutableStateOf("") }
    var otherColorsWeight by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }



    // Focus requesters
    var totalWeightFocus = remember { FocusRequester() }
    var otherColorsFocus = remember { FocusRequester() }

    val result = calculateResult(totalWeight, otherColorsWeight)

    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        totalWeightFocus.requestFocus()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(
            "Insira os dados para obter a classe",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(24.dp))


        NumberInputField(
            value = totalWeight,
            onValueChange = { totalWeight = it },
            label = "Peso da amostra isenta de defeitos(g)",
            focusRequester = totalWeightFocus,
            nextFocus = otherColorsFocus
        )

        Spacer(modifier = Modifier.height(16.dp))


        OutlinedTextField(
            value = otherColorsWeight,
            onValueChange = { otherColorsWeight = it },
            label = { Text("Peso dos grãos não amarelos(g)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(otherColorsFocus),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Number,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    keyboardController?.hide()
                }
            ),
            singleLine = true
        )
        Spacer(modifier = Modifier.height(24.dp))

        //just display result
        OutlinedTextField(
            value = result,
            onValueChange = {},
            label = { Text("Peso dos grãos amarelos (g)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(otherColorsFocus),
            singleLine = true,
            enabled = true,
            readOnly = true
        )



        errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                val tWeight = totalWeight.toFloatOrNull()
                val oWeight = otherColorsWeight.toFloatOrNull()

                if (tWeight == null || oWeight == null) {
                    errorMessage = "Por favor preencha todos os campos com valores válidos"
                    return@Button
                }

                viewModel.setClassColor(tWeight, oWeight)
                navController.navigate("classificationResult")
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar")
        }

    }
}

@Composable
private fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester
) {
    OutlinedTextField(
        value = value,
        onValueChange = { newValue ->
            onValueChange(sanitizeFloatInput(newValue))
        },
        label = { Text(label) },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Next
        ),
        keyboardActions = KeyboardActions(
            onNext = { nextFocus.requestFocus() }
        ),
        singleLine = true
    )
}

private fun sanitizeFloatInput(input: String): String {
    if (input.isEmpty()) return input

    // Filter non-digit/non-decimal characters
    var filtered = input.filter { it.isDigit() || it == '.' }

    // Handle multiple decimals
    val decimalCount = filtered.count { it == '.' }
    if (decimalCount > 1) {
        val firstDecimalIndex = filtered.indexOfFirst { it == '.' }
        // Get the part after the first decimal point
        val afterDecimal = filtered.substring(firstDecimalIndex + 1)
        // Remove any additional decimals from the part after the first decimal
        filtered = filtered.replaceRange(
            firstDecimalIndex + 1,
            filtered.length,
            afterDecimal.replace(".", "")
        )
    }

    // Prevent leading zero issues
    if (filtered.startsWith("00") && !filtered.startsWith("0.")) {
        filtered = "0" + filtered.drop(2)
    }

    return filtered
}
private fun calculateResult(num1: String, num2: String): String {
    val n1 = num1.toFloatOrNull() ?: 0.0f
    val n2 = num2.toFloatOrNull() ?: 0.0f
    return (n1 - n2).toString()
}