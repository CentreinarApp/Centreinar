package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

// Tipo de dado para armazenar Nome e Quantidade de cada semente tóxica
typealias ToxicSeedDetail = Pair<String, String> // Pair<Nome, Quantidade>

@Composable
public fun DisqualificationScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        var badConservation by remember { mutableStateOf(false) }
        var strangeSmell by remember { mutableStateOf(false) }
        var insects by remember { mutableStateOf(false) }
        var toxicGrains by remember { mutableStateOf(false) }

        // Lista para armazenar os detalhes de cada tipo de semente
        var toxicSeedDetails by remember { mutableStateOf(listOf<ToxicSeedDetail>()) }

        // Campo para a quantidade de TIPOS de sementes (que define o tamanho da lista)
        var toxicTypesQuantity by remember { mutableStateOf("0") }

        // Converte a quantidade de TIPOS para Int, usando 0 se for inválido
        val typesQuantity = toxicTypesQuantity.toIntOrNull() ?: 0

        /**
         * Função local para atualizar um par específico na lista com segurança.
         */
        fun updateSeedDetail(index: Int, newName: String? = null, newQuantity: String? = null) {
            // Ignora se o índice não for válido
            if (index < 0 || index >= toxicSeedDetails.size) return

            val currentList = toxicSeedDetails.toMutableList()
            val currentPair = currentList[index]

            val updatedPair = ToxicSeedDetail(
                newName ?: currentPair.first,
                newQuantity ?: currentPair.second
            )

            currentList[index] = updatedPair

            toxicSeedDetails = currentList.toList()
        }

        // LÓGICA DE SINCRONIZAÇÃO DE LISTA: Sincroniza a lista de detalhes com a quantidade de tipos
        LaunchedEffect(typesQuantity) {
            val currentSize = toxicSeedDetails.size

            when {
                typesQuantity > currentSize -> {
                    // Adiciona pares vazios (Nome="", Quantidade="0") se a quantidade aumentar
                    val newItems = List(typesQuantity - currentSize) { ToxicSeedDetail("", "0") }
                    toxicSeedDetails = toxicSeedDetails + newItems
                }

                typesQuantity < currentSize -> {
                    // Trunca a lista se a quantidade diminuir
                    toxicSeedDetails = toxicSeedDetails.take(typesQuantity)
                }
            }
        }


        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(32.dp),
                // Usa SpaceBetween para fixar o botão no final
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Coluna de conteúdo com scroll que ocupa todo o espaço restante
                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                    Text(
                        "O lote apresenta:",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onBackground
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Checkboxes de Desqualificação
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = badConservation,
                            onCheckedChange = { badConservation = it })
                        Text(
                            "Mal estado de conservação?",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = strangeSmell, onCheckedChange = { strangeSmell = it })
                        Text("Cheiro estranho?", color = MaterialTheme.colorScheme.onBackground)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = insects, onCheckedChange = { insects = it })
                        Text(
                            "Insetos vivos ou mortos?",
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    // CHECKBOX DE SEMENTES TÓXICAS
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
                        Text("Sementes tóxicas?", color = MaterialTheme.colorScheme.onBackground)
                    }

                    // CAMPOS DINÂMICOS CONDICIONAIS
                    if (toxicGrains) {
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            "Detalhes dos Tipos de Sementes Tóxicas",
                            style = MaterialTheme.typography.titleMedium.copy(color = MaterialTheme.colorScheme.primary)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // CAMPO DE QUANTIDADE DE TIPOS
                        NumberInputField(
                            value = toxicTypesQuantity,
                            onValueChange = {
                                if ((it.toIntOrNull() ?: 0) >= 0) {
                                    toxicTypesQuantity = it
                                }
                            },
                            label = "Quantidade de TIPOS Encontrados"
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // CAMPOS DE DETALHE DINÂMICOS (Nome e Unidades)
                        if (typesQuantity > 0) {
                            Text("Detalhes por Tipo:", style = MaterialTheme.typography.titleSmall)
                            Spacer(modifier = Modifier.height(8.dp))

                            repeat(typesQuantity) { index ->
                                val currentDetail =
                                    toxicSeedDetails.getOrElse(index) { ToxicSeedDetail("", "0") }

                                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                    Text(
                                        "Tipo ${index + 1}",
                                        style = MaterialTheme.typography.bodyLarge
                                    )

                                    // Campo 1: Nome da Semente
                                    OutlinedTextField(
                                        value = currentDetail.first,
                                        onValueChange = { newName ->
                                            updateSeedDetail(index, newName = newName)
                                        },
                                        label = { Text("Qual semente? (Ex: Mamona)") },
                                        modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                        singleLine = true
                                    )

                                    // Campo 2: Unidades da Semente
                                    NumberInputField(
                                        value = currentDetail.second,
                                        onValueChange = { newQuantity ->
                                            updateSeedDetail(index, newQuantity = newQuantity)
                                        },
                                        label = "Quantas unidades?",
                                    )
                                }
                            }
                        }
                    }
                    // Garante que o scroll vá até o fim sem o botão cobrir os últimos campos
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // O botão é fixado na parte inferior graças ao Arrangement.SpaceBetween
                Button(
                    onClick = {
                        val badConservationInt = if (badConservation) 1 else 0
                        val strangeSmellInt = if (strangeSmell) 1 else 0
                        val insectsInt = if (insects) 1 else 0
                        val toxicGrainInt = if (toxicGrains) 1 else 0

                        // VERIFICA QUAL É O GRÃO SELECIONADO NO VIEWMODEL
                        if (viewModel.selectedGrain == "Milho") {
                            // Salva na tabela de MILHO
                            viewModel.saveDisqualificationDataMilho(
                                badConservation = badConservationInt,
                                strangeSmell = strangeSmellInt,
                                insects = insectsInt,
                                toxicGrains = toxicGrainInt,
                                toxicSeeds = toxicSeedDetails,
                                onSuccess = {
                                    navController.navigate("classification")
                                }
                            )
                        } else {
                            // Salva na tabela de SOJA (Caso não seja milho)
                            viewModel.saveDisqualificationDataSoja(
                                badConservation = badConservationInt,
                                strangeSmell = strangeSmellInt,
                                insects = insectsInt,
                                toxicGrains = toxicGrainInt,
                                toxicSeeds = toxicSeedDetails,
                                onSuccess = {
                                    navController.navigate("classification")
                                }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Confirmar")
                }
            }
        }
    }
}

// Composable auxiliar para campo de input de número inteiro
@Composable
private fun NumberInputField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
) {
    OutlinedTextField(
        value = value,
        onValueChange = {
            if (it.isEmpty() || (it.matches(Regex("^(\\d*)$")) && (it.toIntOrNull() ?: 0) >= 0)) {
                onValueChange(it)
            }
        },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
        singleLine = true
    )
}