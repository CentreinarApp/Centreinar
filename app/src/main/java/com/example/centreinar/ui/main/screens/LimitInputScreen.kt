package com.example.centreinar.ui.main.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.centreinar.domain.model.GrainDescriptor
import com.example.centreinar.domain.model.LimitField
import com.example.centreinar.ui.classificationProcess.strategy.BaseLimit
import com.example.centreinar.ui.components.NumberInputField
import com.example.centreinar.ui.components.OfficialLimitsTable
import com.example.centreinar.util.FieldKeys
import com.example.centreinar.util.toFloatOrDefault
import kotlinx.coroutines.flow.StateFlow

/**
 * Tela genérica de entrada de limites — reutilizada no fluxo de classificação
 * e no fluxo de desconto.
 *
 * Os campos editáveis são inteiramente determinados por [limitFields], que vem
 * da Strategy via getLimitFields(). A tela não sabe nada sobre Soja, Milho ou
 * qualquer outro grão — adicionar um novo grão não requer nenhuma alteração aqui.
 *
 * @param limitFields         Campos declarados pela Strategy — ordem e conteúdo
 * @param descriptor          Metadados do grão (nome, grupos)
 * @param currentGrain        Nome do grão selecionado
 * @param currentGroup        Grupo selecionado
 * @param isOfficial          Se os limites são oficiais (MAPA)
 * @param isLoading           Estado de carregamento
 * @param defaultLimitsFlow   StateFlow de limites padrão para prefill
 * @param allOfficialLimits   Lista de limites oficiais para tabela
 * @param onLoadLimits        Chamado ao entrar na tela para carregar os limites
 * @param onSaveAndContinue   Chamado ao confirmar — recebe o mapa com FieldKeys.*
 */
@Composable
fun LimitInputScreen(
    limitFields: List<LimitField>,
    descriptor: GrainDescriptor?,
    currentGrain: String?,
    currentGroup: Int?,
    isOfficial: Boolean,
    isLoading: Boolean,
    defaultLimitsFlow: StateFlow<Map<String, Float>?>,
    allOfficialLimits: List<Any>,
    onLoadLimits: () -> Unit,
    onSaveAndContinue: (limits: Map<String, Float>) -> Unit
) {
    val defaultLimits by defaultLimitsFlow.collectAsStateWithLifecycle()
    val scrollState   = rememberScrollState()
    val moistureFocus = remember { FocusRequester() }
    var errorMessage  by remember { mutableStateOf<String?>(null) }

    // Estado dinâmico: um mapa chave→valor para cada field declarado pela Strategy
    // + umidade, que é sempre exibida separadamente no topo
    var moisture    by remember(currentGrain, currentGroup) { mutableStateOf("") }
    var fieldValues by remember(currentGrain, currentGroup) {
        mutableStateOf(limitFields.associate { it.key to "" })
    }

    LaunchedEffect(currentGrain, currentGroup, isOfficial) { onLoadLimits() }

    LaunchedEffect(defaultLimits) {
        defaultLimits?.let { limits ->
            moisture = limits[FieldKeys.MOISTURE]?.toString() ?: ""
            if (!isOfficial) {
                fieldValues = fieldValues.mapValues { (key, _) ->
                    limits[key]?.toString() ?: ""
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
                text  = if (!isOfficial) "Insira os limites de tolerância" else "Limites de Referência MAPA",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text  = if (descriptor?.supportsGroups == true)
                    "$currentGrain - Grupo $currentGroup"
                else
                    currentGrain ?: "",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Umidade — sempre visível no topo, independente do grão
            NumberInputField(
                label          = "Umidade da Amostra (%)",
                value          = moisture,
                onValueChange  = { moisture = it },
                focusRequester = moistureFocus,
                nextFocus      = null,
                enabled        = !isOfficial,
                readOnly       = isOfficial
            )

            Spacer(modifier = Modifier.height(16.dp))

            Box(modifier = Modifier.weight(1f)) {
                when {
                    isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    isOfficial -> {
                        val limitsForTable = allOfficialLimits.filterIsInstance<BaseLimit>()
                        if (limitsForTable.isNotEmpty()) {
                            OfficialLimitsTable(group = currentGroup ?: 1, data = limitsForTable)
                        } else {
                            Text("Tabela oficial não encontrada.", modifier = Modifier.align(Alignment.Center))
                        }
                    }
                    else -> {
                        // Renderização dinâmica
                        Column(modifier = Modifier.verticalScroll(scrollState)) {
                            limitFields.forEach { field ->
                                NumberInputField(
                                    label          = field.label,
                                    value          = fieldValues[field.key] ?: "",
                                    onValueChange  = { fieldValues = fieldValues + (field.key to it) },
                                    focusRequester = remember { FocusRequester() },
                                    nextFocus      = null,
                                    enabled        = true
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            errorMessage?.let {
                Text(it, color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = 4.dp))
            }

            Button(
                onClick = {
                    if (moisture.isEmpty() || moisture == ".") {
                        errorMessage = "Informe a umidade da amostra."
                        return@Button
                    }
                    errorMessage = null
                    // Monta o mapa final: umidade + todos os fields da Strategy
                    onSaveAndContinue(
                        buildMap {
                            put(FieldKeys.MOISTURE, moisture.toFloatOrDefault())
                            fieldValues.forEach { (key, value) ->
                                put(key, value.toFloatOrDefault())
                            }
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
            ) {
                Text(if (!isOfficial) "Salvar e Continuar" else "Próximo")
            }
        }
    }
}