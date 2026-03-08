package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.strategy.CustomLimitPayload
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.components.EditableFields
import com.example.centreinar.ui.components.NumberInputField
import com.example.centreinar.ui.components.OfficialLimitsTable
import com.example.centreinar.util.toFloatOrDefault


@Composable
fun LimitInputScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        val defaultLimits by viewModel.defaultLimits.collectAsStateWithLifecycle()
        val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

        val currentGrain = viewModel.selectedGrain
        val currentGroup = viewModel.selectedGroup
        val isOfficial = viewModel.isOfficial == true
        val isSoja = currentGrain == "Soja"

        var moisture by remember(currentGrain, currentGroup) { mutableStateOf("") }
        var impurities by remember { mutableStateOf("") }
        var brokenCrackedDamaged by remember { mutableStateOf("") }
        var greenish by remember { mutableStateOf("") }
        var burnt by remember { mutableStateOf("") }
        var burntOrSour by remember { mutableStateOf("") }
        var moldy by remember { mutableStateOf("") }
        var spoiled by remember { mutableStateOf("") }
        var carunchado by remember { mutableStateOf("")}

        var errorMessage by remember { mutableStateOf<String?>(null) }
        val moistureFocus = remember { FocusRequester() }
        val scrollState = rememberScrollState()

        LaunchedEffect(currentGrain, currentGroup) {
            if (currentGrain != null) {
                viewModel.loadDefaultLimits()
            }
        }

        LaunchedEffect(defaultLimits) {
            defaultLimits?.let { limits ->
                // Voltamos para as chaves originais que o seu banco de dados usa
                moisture = limits["moistureUpLim"]?.toString() ?: ""

                if (!isOfficial) {
                    impurities = limits["impuritiesUpLim"]?.toString() ?: ""
                    moldy = limits["moldyUpLim"]?.toString() ?: ""
                    spoiled = limits["spoiledTotalUpLim"]?.toString() ?: ""

                    // Tratamos os nomes específicos de Soja e Milho com segurança
                    if (isSoja) {
                        brokenCrackedDamaged = limits["brokenCrackedDamagedUpLim"]?.toString() ?: limits["brokenUpLim"]?.toString() ?: ""
                        burnt = limits["burntUpLim"]?.toString() ?: ""
                        greenish = limits["greenishUpLim"]?.toString() ?: ""
                        burntOrSour = limits["burntOrSourUpLim"]?.toString() ?: ""
                    } else {
                        // Milho
                        brokenCrackedDamaged = limits["brokenUpLim"]?.toString() ?: ""
                        burntOrSour = limits["ardidoUpLim"]?.toString() ?: limits["ardidosUpLim"]?.toString() ?: ""
                        carunchado = limits["carunchadoUpLim"]?.toString() ?: ""
                    }
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = if (!isOfficial) "Insira os limites de tolerância" else "Limites de Referência MAPA",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = if (currentGrain == "Soja") "$currentGrain - Grupo $currentGroup" else "$currentGrain",
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
                if (isOfficial) {
                    if (allOfficialLimits.isNotEmpty()) {
                        OfficialLimitsTable(
                            grain = currentGrain ?: "",
                            group = currentGroup ?: 1,
                            data = allOfficialLimits
                        )
                    }
                } else {
                    Column(modifier = Modifier.verticalScroll(scrollState)) {
                        EditableFields(
                            isSoja = isSoja,
                            impurities = impurities,
                            onImpuritiesChange = { impurities = it },
                            burnt = burnt,
                            onBurntChange = { burnt = it },
                            burntOrSour = burntOrSour,
                            onBurntOrSourChange = { burntOrSour = it },
                            moldy = moldy,
                            onMoldyChange = { moldy = it },
                            spoiled = spoiled,
                            onSpoiledChange = { spoiled = it },
                            greenish = greenish,
                            onGreenishChange = { greenish = it },
                            broken = brokenCrackedDamaged,
                            onBrokenChange = { brokenCrackedDamaged = it },
                            carunchados = carunchado,
                            onCarunchadoChange = { carunchado = it }
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))

            errorMessage?.let {
                Text(
                    it,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            Button(
                onClick = {
                    if (moisture.isEmpty() || moisture == ".") {
                        errorMessage = "Informe a umidade da amostra."
                        return@Button
                    }

                    if (!isOfficial) {
                        // Criamos o Payload e passamos para a ViewModel
                        val payload = CustomLimitPayload(
                            group = currentGroup ?: 1,
                            moisture = moisture.toFloatOrDefault(),
                            impurities = impurities.toFloatOrDefault(),
                            brokenCrackedDamaged = brokenCrackedDamaged.toFloatOrDefault(),
                            greenish = greenish.toFloatOrDefault(),
                            burnt = burnt.toFloatOrDefault(),
                            burntOrSour = burntOrSour.toFloatOrDefault(),
                            moldy = moldy.toFloatOrDefault(),
                            spoiled = spoiled.toFloatOrDefault(),
                            carunchado = carunchado.toFloatOrDefault()
                        )
                        viewModel.setLimit(payload)
                    }
                    navController.navigate("disqualification?classificationId=-1")
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(if (!isOfficial) "Salvar e Continuar" else "Próximo")
            }
        }
    }
}