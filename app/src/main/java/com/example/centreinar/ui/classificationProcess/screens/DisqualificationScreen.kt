package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.domain.model.ToxicSeedDetail
import com.example.centreinar.ui.classificationProcess.components.ClassificationTopBar
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.util.Routes

@Composable
fun DisqualificationScreen(
    navController: NavController,
    classificationId: Int,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    var badConservation by remember { mutableStateOf(false) }
    var strangeSmell by remember { mutableStateOf(false) }
    var insects by remember { mutableStateOf(false) }
    var toxicGrains by remember { mutableStateOf(false) }
    var toxicSeedDetails by remember { mutableStateOf(listOf<ToxicSeedDetail>()) }
    var toxicTypesQuantity by remember { mutableStateOf("0") }
    var toxicSeedError by remember { mutableStateOf<String?>(null) }

    val typesQuantity = toxicTypesQuantity.toIntOrNull() ?: 0
    val focusManager = LocalFocusManager.current
    val typesCountFocus = remember { FocusRequester() }

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700

    fun updateSeedDetail(index: Int, newName: String? = null, newQuantity: String? = null) {
        if (index < 0 || index >= toxicSeedDetails.size) return
        val list = toxicSeedDetails.toMutableList()
        val current = list[index]
        list[index] = ToxicSeedDetail(newName ?: current.first, newQuantity ?: current.second)
        toxicSeedDetails = list.toList()
    }

    LaunchedEffect(typesQuantity) {
        val currentSize = toxicSeedDetails.size
        when {
            typesQuantity > currentSize -> {
                toxicSeedDetails = toxicSeedDetails +
                        List(typesQuantity - currentSize) { ToxicSeedDetail("", "0") }
            }
            typesQuantity < currentSize -> {
                toxicSeedDetails = toxicSeedDetails.take(typesQuantity)
            }
        }
    }

    val seedFocusRequesters = remember(typesQuantity) {
        List(typesQuantity) { FocusRequester() to FocusRequester() }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding())
        ) {
            // TopBar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ClassificationTopBar(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Conteúdo com scroll
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = if (isSmallScreen) 12.dp else 22.dp)
                    .padding(
                        top = if (isSmallScreen) 12.dp else 22.dp,
                        bottom = if (isSmallScreen) 12.dp else 22.dp
                    ),
            ) {
                Text(
                    "Selecione as características que o lote apresenta:",
                    style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(Modifier.height(if (isSmallScreen) 4.dp else 5.dp))
                Text(
                    "*Caso não apresente nenhuma das características abaixo, toque no botão Continuar.",
                    style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Justify
                )
                Spacer(Modifier.height(if (isSmallScreen) 12.dp else 24.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = badConservation, onCheckedChange = { badConservation = it })
                    Text(
                        "Mal estado de conservação?",
                        style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = strangeSmell, onCheckedChange = { strangeSmell = it })
                    Text(
                        "Cheiro estranho?",
                        style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = insects, onCheckedChange = { insects = it })
                    Text(
                        "Insetos vivos ou mortos?",
                        style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = toxicGrains,
                        onCheckedChange = {
                            toxicGrains = it
                            if (!it) {
                                toxicTypesQuantity = "0"
                                toxicSeedDetails = emptyList()
                            }
                        }
                    )
                    Text(
                        "Sementes tóxicas?",
                        style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (toxicGrains) {
                    Spacer(Modifier.height(if (isSmallScreen) 12.dp else 24.dp))
                    Text(
                        "Detalhes dos Tipos de Sementes Tóxicas",
                        style = if (isSmallScreen)
                            MaterialTheme.typography.titleSmall.copy(color = MaterialTheme.colorScheme.primary)
                        else
                            MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))

                    DisqNumberField(
                        value = toxicTypesQuantity,
                        onValueChange = {
                            if ((it.toIntOrNull() ?: 0) >= 0) toxicTypesQuantity = it
                        },
                        label = "Quantidade de TIPOS Encontrados",
                        focusRequester = typesCountFocus,
                        nextFocus = seedFocusRequesters.firstOrNull()?.first,
                        imeAction = if (typesQuantity > 0) ImeAction.Next else ImeAction.Done,
                        onDone = { focusManager.clearFocus() }
                    )

                    Spacer(Modifier.height(if (isSmallScreen) 12.dp else 24.dp))

                    if (typesQuantity > 0) {
                        Text(
                            "Detalhes por Tipo:",
                            style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.titleSmall
                        )
                        Spacer(Modifier.height(8.dp))

                        repeat(typesQuantity) { index ->
                            val currentDetail = toxicSeedDetails.getOrElse(index) {
                                ToxicSeedDetail("", "0")
                            }
                            val (nameFocus, qtyFocus) = seedFocusRequesters[index]
                            val nextNameFocus = seedFocusRequesters.getOrNull(index + 1)?.first
                            val isLastField = index == typesQuantity - 1

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = if (isSmallScreen) 8.dp else 16.dp)
                            ) {
                                Text(
                                    "Tipo ${index + 1}",
                                    style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge
                                )
                                OutlinedTextField(
                                    value = currentDetail.first,
                                    onValueChange = { updateSeedDetail(index, newName = it) },
                                    label = { Text("Qual semente? (Ex: Mamona)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp)
                                        .focusRequester(nameFocus),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                                    keyboardActions = KeyboardActions(
                                        onNext = { qtyFocus.requestFocus() }
                                    ),
                                    singleLine = true
                                )
                                DisqNumberField(
                                    value = currentDetail.second,
                                    onValueChange = { updateSeedDetail(index, newQuantity = it) },
                                    label = "Quantas unidades?",
                                    focusRequester = qtyFocus,
                                    nextFocus = nextNameFocus,
                                    imeAction = if (isLastField) ImeAction.Done else ImeAction.Next,
                                    onDone = { focusManager.clearFocus() }
                                )
                            }
                        }
                    }
                }

                Spacer(Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
            }

            // Erro e botão fixos fora do scroll
            toxicSeedError?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = if (isSmallScreen) 12.dp else 22.dp)
                )
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    if (toxicGrains && toxicSeedDetails.any {
                            (it.second.toIntOrNull() ?: 0) <= 0
                        }) {
                        toxicSeedError = "Informe a quantidade de cada semente tóxica."
                        return@Button
                    }
                    toxicSeedError = null
                    viewModel.saveDisqualificationData(
                        classificationId = classificationId,
                        badConservation = if (badConservation) 1 else 0,
                        strangeSmell = if (strangeSmell) 1 else 0,
                        insects = if (insects) 1 else 0,
                        toxicGrains = if (toxicGrains) 1 else 0,
                        toxicSeeds = toxicSeedDetails,
                        onSuccess = { navController.navigate(Routes.CLASSIFICATION_INPUT) }
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = if (isSmallScreen) 12.dp else 22.dp)
                    .padding(bottom = if (isSmallScreen) 12.dp else 22.dp)
                    .heightIn(min = if (isSmallScreen) 44.dp else 50.dp),
                shape = RoundedCornerShape(5.dp)
            ) {
                Text(
                    text = "Continuar",
                    style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
private fun DisqNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester?,
    imeAction: ImeAction,
    onDone: () -> Unit = {}
) {
    var isFocused by remember { mutableStateOf(false) }
    val displayValue = if (isFocused && value == "0") "" else value

    OutlinedTextField(
        value = displayValue,
        onValueChange = { raw ->
            when {
                raw.isEmpty() -> onValueChange("0")
                raw.matches(Regex("^(\\d*)$")) -> {
                    onValueChange(raw.trimStart('0').ifEmpty { "0" })
                }
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = imeAction
        ),
        keyboardActions = KeyboardActions(
            onNext = { nextFocus?.requestFocus() },
            onDone = { onDone() }
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .focusRequester(focusRequester)
            .onFocusChanged { state ->
                isFocused = state.isFocused
                if (!state.isFocused && value.isEmpty()) onValueChange("0")
            },
        singleLine = true
    )
}