package com.example.centreinar.ui.home


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.home.HomeViewModel

@Composable
fun LimitInputScreen(
    navController: NavController,
    viewModel: HomeViewModel= hiltViewModel()
) {

    // State variables
    var impurities by remember { mutableStateOf("") }
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var greenish by remember { mutableStateOf("") }
    var burnt by remember { mutableStateOf("") }
    var burntOrSour by remember { mutableStateOf("") }
    var moldy by remember { mutableStateOf("") }
    var spoiled by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Focus requesters
    val impuritiesFocus = remember { FocusRequester() }
    val brokenFocus = remember { FocusRequester() }
    val greenishFocus = remember { FocusRequester() }
    val burntFocus = remember { FocusRequester() }
    val burntOrSourFocus = remember { FocusRequester() }
    val moldyFocus = remember { FocusRequester() }
    val spoiledFocus = remember { FocusRequester() }

    // Get keyboard controller
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        impuritiesFocus.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {




        // Impurities
        NumberInputField(
            value = impurities,
            onValueChange = { impurities = it },
            label = "Impurezas (%)",
            focusRequester = impuritiesFocus,
            nextFocus = brokenFocus
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Broken/Cracked/Damaged
        NumberInputField(
            value = brokenCrackedDamaged,
            onValueChange = { brokenCrackedDamaged = it },
            label = "Quebrados/Trincados/Danificados (%)",
            focusRequester = brokenFocus,
            nextFocus = greenishFocus
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Greenish
        NumberInputField(
            value = greenish,
            onValueChange = { greenish = it },
            label = "Esverdeados (%)",
            focusRequester = greenishFocus,
            nextFocus = burntFocus
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Burnt
        NumberInputField(
            value = burnt,
            onValueChange = { burnt = it },
            label = "Queimados (%)",
            focusRequester = burntFocus,
            nextFocus = burntOrSourFocus
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Burnt or Sour
        NumberInputField(
            value = burntOrSour,
            onValueChange = { burntOrSour = it },
            label = "Ardidos ou Queimados (%)",
            focusRequester = burntOrSourFocus,
            nextFocus = moldyFocus
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Moldy
        NumberInputField(
            value = moldy,
            onValueChange = { moldy = it },
            label = "Mofados (%)",
            focusRequester = moldyFocus,
            nextFocus = spoiledFocus
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Spoiled (last field)
        OutlinedTextField(
            value = spoiled,
            onValueChange = { spoiled = sanitizeFloatInput(it) },
            label = { Text("Avariados (%)") },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(spoiledFocus),
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
                val allFields = listOf(
                    impurities, brokenCrackedDamaged, greenish,
                    burnt, burntOrSour, moldy, spoiled
                )

                // Validate all fields
                if (allFields.any { it.isEmpty() || it == "." }) {
                    errorMessage = "Por favor preencha todos os campos com valores válidos"
                    return@Button
                }

                try {
                    viewModel.setLimit(
                        impurities = impurities.toFloat(),
                        brokenCrackedDamaged = brokenCrackedDamaged.toFloat(),
                        greenish = greenish.toFloat(),
                        burnt = burnt.toFloat(),
                        burntOrSour = burntOrSour.toFloat(),
                        moldy = moldy.toFloat(),
                        spoiled = spoiled.toFloat()
                    )
                    navController.navigate("classification")
                } catch (e: NumberFormatException) {
                    errorMessage = "Valores numéricos inválidos detectados"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Enviar Limites")
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