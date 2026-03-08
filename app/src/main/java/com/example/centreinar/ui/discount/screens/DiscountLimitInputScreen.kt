package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.components.EditableFields
import com.example.centreinar.ui.components.NumberInputField
import com.example.centreinar.ui.components.OfficialLimitsTable

@Composable
fun DiscountLimitInputScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()
    val defaultLimits by viewModel.defaultLimits.collectAsStateWithLifecycle()

    val currentGrain = viewModel.selectedGrain
    val currentGroup = viewModel.selectedGroup
    val isOfficial = viewModel.isOfficial
    val isSoja = currentGrain == "Soja"

    val scrollState = rememberScrollState()
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val moistureFocus = remember { FocusRequester() }

    var moisture             by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var impurities           by remember { mutableStateOf("") }
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var greenish             by remember { mutableStateOf("") }
    var burnt                by remember { mutableStateOf("") }
    var burntOrSour          by remember { mutableStateOf("") }
    var moldy                by remember { mutableStateOf("") }
    var spoiled              by remember { mutableStateOf("") }
    var carunchado           by remember { mutableStateOf("") }

    LaunchedEffect(currentGrain, currentGroup, isOfficial) {
        viewModel.loadDefaultLimits()
    }

    LaunchedEffect(defaultLimits) {
        defaultLimits?.let { limits ->
            moisture = limits["umidade"]?.toString() ?: ""

            if (!isOfficial) {
                impurities = limits["impureza"]?.toString() ?: ""
                moldy      = limits["mofados"]?.toString() ?: ""
                spoiled    = limits["avariados"]?.toString() ?: ""

                if (isSoja) {
                    brokenCrackedDamaged = limits["quebrados"]?.toString() ?: ""
                    burnt                = limits["queimados"]?.toString() ?: ""
                    greenish             = limits["esverdeados"]?.toString() ?: ""
                    burntOrSour          = limits["ardidos"]?.toString() ?: ""
                } else {
                    brokenCrackedDamaged = limits["quebrados"]?.toString() ?: ""
                    burntOrSour          = limits["ardidos"]?.toString() ?: ""
                    carunchado           = limits["carunchados"]?.toString() ?: ""
                }
            }
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = if (!isOfficial) "Insira os limites de tolerância" else "Limites de Referência MAPA",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (isSoja) "$currentGrain - Grupo $currentGroup" else currentGrain,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            NumberInputField(
                label = "Umidade da Amostra (%)",
                value = moisture,
                onValueChange = { moisture = it },
                focusRequester = moistureFocus,
                nextFocus = null,
                enabled = !isOfficial,
                readOnly = isOfficial
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }

                    isOfficial -> {
                        if (allOfficialLimits.isNotEmpty()) {
                            OfficialLimitsTable(
                                grain = currentGrain,
                                group = currentGroup,
                                data  = allOfficialLimits
                            )
                        } else {
                            Text(
                                text     = "Tabela oficial não encontrada.",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                    }

                    else -> {
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            EditableFields(
                                isSoja              = isSoja,
                                impurities          = impurities,
                                onImpuritiesChange  = { impurities = it },
                                burnt               = burnt,
                                onBurntChange       = { burnt = it },
                                burntOrSour         = burntOrSour,
                                onBurntOrSourChange = { burntOrSour = it },
                                moldy               = moldy,
                                onMoldyChange       = { moldy = it },
                                spoiled             = spoiled,
                                onSpoiledChange     = { spoiled = it },
                                greenish            = greenish,
                                onGreenishChange    = { greenish = it },
                                broken              = brokenCrackedDamaged,
                                onBrokenChange      = { brokenCrackedDamaged = it },
                                carunchados         = carunchado,
                                onCarunchadoChange  = { carunchado = it }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            errorMessage?.let {
                Text(
                    text     = it,
                    color    = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (moisture.isEmpty() || moisture == ".") {
                        errorMessage = "Informe a umidade base."
                        return@Button
                    }

                    if (!isOfficial) {
                        viewModel.saveCustomLimit(
                            moisture    = moisture,
                            impurities  = impurities,
                            broken      = brokenCrackedDamaged,
                            burntOrSour = burntOrSour,
                            burnt       = burnt,
                            moldy       = moldy,
                            spoiled     = spoiled,
                            greenish    = greenish,
                            carunchado  = carunchado
                        )
                    }

                    when (currentGrain) {
                        "Soja"  -> navController.navigate("discountInputScreen")
                        "Milho" -> navController.navigate("milhoDiscountInputScreen?isOfficial=$isOfficial")
                        else    -> navController.navigate("grainSelection")
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text(if (!isOfficial) "Salvar e Continuar" else "Próximo")
            }
        }
    }
}