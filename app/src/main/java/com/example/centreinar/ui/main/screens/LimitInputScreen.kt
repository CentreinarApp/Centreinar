package com.example.centreinar.ui.main.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.platform.LocalConfiguration
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
    topBar: @Composable () -> Unit,
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

    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700

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

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { topBar() }

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(if (isSmallScreen) 12.dp else 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (!isOfficial) "Insira os limites de tolerância" else "Limites de Referência MAPA",
                    style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
                )
                Text(
                    text = if (descriptor?.supportsGroups == true)
                        "$currentGrain - Grupo $currentGroup"
                    else
                        currentGrain ?: "",
                    style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))

                if (isOfficial) {
                    NumberInputField(
                        label = "Umidade da Amostra (%)",
                        value = moisture,
                        onValueChange = { moisture = it },
                        focusRequester = moistureFocus,
                        nextFocus = null,
                        enabled = !isOfficial,
                        readOnly = isOfficial
                    )
                    Spacer(modifier = Modifier.height(if (isSmallScreen) 8.dp else 16.dp))
                }

                Box(modifier = Modifier.weight(1f)) {
                    when {
                        isLoading -> CircularProgressIndicator(
                            modifier = Modifier.align(Alignment.Center)
                        )

                        isOfficial -> {
                            val limitsForTable = allOfficialLimits.filterIsInstance<BaseLimit>()
                            if (limitsForTable.isNotEmpty()) {
                                OfficialLimitsTable(
                                    group = currentGroup ?: 1,
                                    data = limitsForTable
                                )
                            } else {
                                Text(
                                    "Tabela oficial não encontrada.",
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        }

                        else -> {
                            Column(modifier = Modifier.verticalScroll(scrollState)) {
                                NumberInputField(
                                    label = "Umidade da Amostra (%)",
                                    value = moisture,
                                    onValueChange = { moisture = it },
                                    focusRequester = moistureFocus,
                                    nextFocus = null,
                                    enabled = true,
                                    readOnly = false
                                )
                                Spacer(Modifier.height(22.dp))
                                val focusRequesters = remember(limitFields.size) {
                                    List(limitFields.size) { FocusRequester() }
                                }
                                limitFields.forEachIndexed { index, field ->
                                    NumberInputField(
                                        label = field.label,
                                        value = fieldValues[field.key] ?: "",
                                        onValueChange = {
                                            fieldValues = fieldValues + (field.key to it)
                                        },
                                        focusRequester = focusRequesters[index],
                                        nextFocus = focusRequesters.getOrNull(index + 1),
                                        enabled = true
                                    )
                                    Spacer(Modifier.height(22.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.height(if (isSmallScreen) 16.dp else 32.dp))

                errorMessage?.let {
                    Text(
                        it, color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Button(
                    onClick = {
                        if (moisture.isEmpty() || moisture == ".") {
                            errorMessage = "Informe a umidade da amostra."
                            return@Button
                        }
                        errorMessage = null
                        onSaveAndContinue(
                            buildMap {
                                put(FieldKeys.MOISTURE, moisture.toFloatOrDefault())
                                fieldValues.forEach { (key, value) ->
                                    put(key, value.toFloatOrDefault())
                                }
                            }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = if (isSmallScreen) 44.dp else 50.dp),
                    shape = RoundedCornerShape(8.dp),
                ) {
                    Text(
                        text = if (!isOfficial) "Salvar e Continuar" else "Continuar",
                        style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    }
}