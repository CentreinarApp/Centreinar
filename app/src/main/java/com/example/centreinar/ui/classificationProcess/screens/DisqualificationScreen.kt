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
     * Usa parâmetros nulos para saber qual campo (Nome ou Quantidade) foi modificado.
     */
    fun updateSeedDetail(index: Int, newName: String? = null, newQuantity: String? = null) {
        // Ignora se o índice não for válido
        if (index < 0 || index >= toxicSeedDetails.size) return

        // 1. Cria uma cópia mutável da lista atual.
        val currentList = toxicSeedDetails.toMutableList()
        val currentPair = currentList[index]

        // 2. Define o novo par combinando os valores atuais com os novos (se existirem).
        val updatedPair = ToxicSeedDetail(
            newName ?: currentPair.first,
            newQuantity ?: currentPair.second
        )

        // 3. Atualiza o item na lista copiada.
        currentList[index] = updatedPair

        // 4. Atribui a nova lista (imutável) ao estado, disparando a recomposição segura.
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
                .padding(32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) { // Conteúdo superior com scroll
                Text(
                    "O lote apresenta:",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Checkboxes de Desqualificação
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = badConservation, onCheckedChange = { badConservation = it })
                    Text("Mal estado de conservação?", color = MaterialTheme.colorScheme.onBackground)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = strangeSmell, onCheckedChange = { strangeSmell = it })
                    Text("Cheiro estranho?", color = MaterialTheme.colorScheme.onBackground)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = insects, onCheckedChange = { insects = it })
                    Text("Insetos vivos ou mortos?", color = MaterialTheme.colorScheme.onBackground)
                }

                // CHECKBOX DE SEMENTES TÓXICAS
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = toxicGrains,
                        onCheckedChange = {
                            toxicGrains = it
                            // Reseta os campos ao desmarcar
                            if (!it) {
                                toxicTypesQuantity = "0"
                                toxicSeedDetails = emptyList() // Limpa a lista dinâmica
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
                            // Garante que o valor não seja negativo
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
                            // Acessa o par com segurança, usando o valor padrão se o índice não existir
                            val currentDetail = toxicSeedDetails.getOrElse(index) { ToxicSeedDetail("", "0") }

                            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                                Text("Tipo ${index + 1}", style = MaterialTheme.typography.bodyLarge)

                                // Campo 1: Nome da Semente
                                OutlinedTextField(
                                    value = currentDetail.first, // Nome
                                    onValueChange = { newName ->
                                        updateSeedDetail(index, newName = newName)
                                    },
                                    label = { Text("Qual semente? (Ex: Mamona)") },
                                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                    singleLine = true
                                )

                                // Campo 2: Unidades da Semente
                                NumberInputField(
                                    value = currentDetail.second, // Unidades
                                    onValueChange = { newQuantity ->
                                        updateSeedDetail(index, newQuantity = newQuantity)
                                    },
                                    label = "Quantas unidades?",
                                )
                            }
                        }
                    }
                }
            } // Fim da coluna com scroll

            // O botão é fixado na parte inferior
            Button(
                onClick = {
                    val badConservationInt = if (badConservation) 1 else 0
                    val strangeSmellInt = if (strangeSmell) 1 else 0
                    val insectsInt = if (insects) 1 else 0
                    val toxicGrainInt = if (toxicGrains) 1 else 0

                    // Se toxicGrains for true, toxicSeedDetails terá os dados (Nome e Quantidade de cada tipo)

                    viewModel.setDisqualification(
                        badConservationInt,
                        strangeSmellInt,
                        insectsInt,
                        toxicGrainInt
                    )
                    // TODO: Salvar toxicSeedDetails no ViewModel

                    navController.navigate("classification")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar")
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
            // Permite apenas números inteiros e impede números negativos
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