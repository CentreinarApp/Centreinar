package com.example.centreinar.ui.classificationProcess.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.ClassificationSoja
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.ui.classificationProcess.components.*
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.components.ActionButtons
import com.example.centreinar.ui.components.OfficialReferenceTable
import com.example.centreinar.ui.discount.strategy.FinancialDiscountPayload
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassificationResultScreen(
    navController: NavController,
    classificationViewModel: ClassificationViewModel = hiltViewModel(),
    discountViewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState           by classificationViewModel.uiState.collectAsStateWithLifecycle()
    val error             by classificationViewModel.error.collectAsStateWithLifecycle()
    val allOfficialLimits by classificationViewModel.allOfficialLimits.collectAsStateWithLifecycle()
    val discountResult    by discountViewModel.discountResult.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Controla a visibilidade do BottomSheet
    var showDiscountSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Navega para o resultado do desconto assim que o cálculo terminar
    LaunchedEffect(discountResult) {
        if (discountResult != null) {
            showDiscountSheet = false
            navController.navigate("discountResultsScreen")
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (!error.isNullOrBlank()) {
            Box(
                Modifier.fillMaxSize().padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        "Ops! Algo deu errado no cálculo:",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text     = error!!,
                        color    = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { navController.popBackStack() }) { Text("Voltar") }
                }
            }

        } else if (uiState.classification != null && uiState.limitUsed != null) {
            val result = uiState.classification!!
            val limits = uiState.limitUsed!!

            val moistureValue = when (result) {
                is ClassificationSoja  -> result.moisturePercentage
                is ClassificationMilho -> result.moisturePercentage
                else -> 0f
            }

            val finalTypeValue = when (result) {
                is ClassificationSoja  -> result.finalType
                is ClassificationMilho -> result.finalType
                else -> 0
            }

            val moistureLimit = when (limits) {
                is LimitSoja  -> limits.moistureUpLim
                is LimitMilho -> limits.moistureUpLim
                else -> 0f
            }

            val groupLimit = when (limits) {
                is LimitSoja  -> limits.group
                is LimitMilho -> limits.group
                else -> 0
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp)
            ) {
                Text("Resultado da Classificação", style = MaterialTheme.typography.headlineSmall)
                Text(if (classificationViewModel.isOfficial) "Referência Oficial" else "Referência Não Oficial")

                Spacer(Modifier.height(16.dp))

                Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {

                    MoistureInfoCard(moisture = moistureValue, limit = moistureLimit)

                    Spacer(Modifier.height(8.dp))

                    ClassificationTable(
                        title          = "RESULTADO ${classificationViewModel.selectedGrain?.uppercase() ?: ""}",
                        finalTypeLabel = classificationViewModel.getFinalTypeLabel(finalTypeValue),
                        rows           = uiState.tableRows,
                        typeTranslator = { classificationViewModel.getFinalTypeLabel(it) }
                    )

                    uiState.complementaryCards.forEach { card ->
                        Spacer(Modifier.height(8.dp))
                        ColorAndGroupCard(
                            title          = card.title,
                            subtitle       = card.subtitle,
                            containerColor = when (card.colorType) {
                                "secondary" -> MaterialTheme.colorScheme.secondaryContainer
                                else        -> MaterialTheme.colorScheme.tertiaryContainer
                            }
                        )
                    }

                    Spacer(Modifier.height(26.dp))

                    uiState.disqualification?.let { disq ->
                        DisqualificationInfoCard(
                            badConservation = when (disq) {
                                is DisqualificationSoja  -> disq.badConservation == 1
                                is DisqualificationMilho -> disq.badConservation == 1
                                else -> false
                            },
                            strangeSmell = when (disq) {
                                is DisqualificationSoja  -> disq.strangeSmell == 1
                                is DisqualificationMilho -> disq.strangeSmell == 1
                                else -> false
                            },
                            insects = when (disq) {
                                is DisqualificationSoja  -> disq.insects == 1
                                is DisqualificationMilho -> disq.insects == 1
                                else -> false
                            },
                            toxicGrains = when (disq) {
                                is DisqualificationSoja  -> disq.toxicGrains == 1
                                is DisqualificationMilho -> disq.toxicGrains == 1
                                else -> false
                            },
                            toxicSeeds = uiState.toxicSeeds
                        )
                    }

                    Spacer(Modifier.height(26.dp))

                    OfficialReferenceTable(
                        grain      = classificationViewModel.selectedGrain ?: "",
                        group      = groupLimit,
                        isOfficial = classificationViewModel.isOfficial,
                        data       = if (classificationViewModel.isOfficial && allOfficialLimits.isNotEmpty())
                            allOfficialLimits
                        else listOf(limits)
                    )
                }

                ActionButtons(
                    onBack            = { navController.popBackStack() },
                    primaryActionText = "Calcular Desconto",
                    onPrimaryAction   = {
                        // Extrai o ID concreto da classificação para vinculá-lo ao desconto
                        val classifId = when (result) {
                            is ClassificationSoja  -> result.id
                            is ClassificationMilho -> result.id
                            else -> -1
                        }

                        // SALVA O ID DA CLASSIFICAÇÃO NO VIEWMODEL DE DESCONTO PARA USO NO PDF
                        discountViewModel.setClassificationId(classifId)

                        discountViewModel.loadFromClassification(
                            lotWeight          = uiState.sampleLotWeight,
                            classification     = result,
                            grain              = classificationViewModel.selectedGrain ?: "Soja",
                            group              = classificationViewModel.selectedGroup ?: 1,
                            isOfficial         = classificationViewModel.isOfficial,
                            classificationId   = classifId
                        )
                        showDiscountSheet = true
                    },
                    onExportPdf = { classificationViewModel.exportPdf(context) }
                )
            }

            // =================================================================
            // BOTTOM SHEET — campos que faltam para calcular o desconto
            // =================================================================
            if (showDiscountSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showDiscountSheet = false },
                    sheetState       = sheetState
                ) {
                    DiscountBottomSheetContent(
                        initialLotWeight = uiState.sampleLotWeight.takeIf { it > 0f }?.toString() ?: "",
                        discountViewModel = discountViewModel,
                        onDismiss = { showDiscountSheet = false }
                    )
                }
            }
        }
    }
}

// =============================================================================
// CONTEÚDO DO BOTTOM SHEET
// =============================================================================

@Composable
private fun DiscountBottomSheetContent(
    initialLotWeight: String,
    discountViewModel: DiscountViewModel,
    onDismiss: () -> Unit
) {
    val uiState by discountViewModel.uiState.collectAsStateWithLifecycle()

    var lotWeight         by remember { mutableStateOf(initialLotWeight) }
    var priceBySack       by remember { mutableStateOf("") }
    var daysOfStorage     by remember { mutableStateOf("0") }
    var deductionValue    by remember { mutableStateOf("0") }
    var doesTechnicalLoss by remember { mutableStateOf(false) }
    var doesDeduction     by remember { mutableStateOf(false) }
    var errorMessage      by remember { mutableStateOf<String?>(null) }

    val lotWeightFocus   = remember { FocusRequester() }
    val priceBySackFocus = remember { FocusRequester() }
    val daysOfStorageFocus  = remember { FocusRequester() }
    val deductionValueFocus = remember { FocusRequester() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text  = "Calcular Desconto",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
        Text(
            text  = "Os valores dos defeitos foram preenchidos automaticamente pela classificação.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(Modifier.height(20.dp))

        // Peso do lote
        SheetNumberField(
            value          = lotWeight,
            onValueChange  = { lotWeight = it },
            label          = "Peso do lote (kg)",
            focusRequester = lotWeightFocus,
            nextFocus      = priceBySackFocus
        )

        Spacer(Modifier.height(16.dp))

        // Preço por saca
        SheetNumberField(
            value          = priceBySack,
            onValueChange  = { priceBySack = it },
            label          = "Preço por Saca (60kg)",
            focusRequester = priceBySackFocus,
            nextFocus      = if (doesTechnicalLoss) daysOfStorageFocus else null
        )

        Spacer(Modifier.height(20.dp))

        // Switch — Quebra Técnica
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesTechnicalLoss, onCheckedChange = { doesTechnicalLoss = it })
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Quebra Técnica", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Desconto por dias de armazenamento",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (doesTechnicalLoss) {
            Spacer(Modifier.height(12.dp))
            SheetNumberField(
                value          = daysOfStorage,
                onValueChange  = { daysOfStorage = it },
                label          = "Dias de armazenamento",
                focusRequester = daysOfStorageFocus,
                nextFocus      = null,
                isInteger      = true
            )
        }

        Spacer(Modifier.height(16.dp))

        // Switch — Deságio TODO: Quando for implementar o deságio só descomentar essa linha
        /*Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(checked = doesDeduction, onCheckedChange = { doesDeduction = it })
            Spacer(Modifier.width(12.dp))
            Column {
                Text("Deságio", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Percentual adicional de desconto",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }*/

        if (doesDeduction) {
            Spacer(Modifier.height(12.dp))
            SheetNumberField(
                value          = deductionValue,
                onValueChange  = { deductionValue = it },
                label          = "Valor do Deságio (%)",
                focusRequester = deductionValueFocus,
                nextFocus      = null
            )
        }

        // Erro
        errorMessage?.let {
            Spacer(Modifier.height(12.dp))
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(24.dp))

        // Botões
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                onClick  = onDismiss
            ) {
                Text("Cancelar")
            }

            Button(
                modifier = Modifier.weight(1f),
                enabled  = !uiState.isLoading,
                onClick  = {
                    val fLotWeight   = lotWeight.toFloatOrZero()
                    val fPriceBySack = priceBySack.toFloatOrZero()

                    if (fLotWeight <= 0f) {
                        errorMessage = "Informe o peso do lote."
                        return@Button
                    }
                    if (fPriceBySack <= 0f) {
                        errorMessage = "Informe o preço por saca."
                        return@Button
                    }
                    errorMessage = null

                    val grain    = discountViewModel.selectedGrain
                    val strategy = discountViewModel.getStrategy(grain)
                        ?: run { errorMessage = "Grão não suportado: $grain"; return@Button }

                    // Recupera o mapa de defeitos que foi preenchido pelo loadFromClassification
                    val prefill = discountViewModel.classificationPrefill.value

                    val defectsMap = (prefill?.defects ?: emptyMap()) + mapOf(
                        "umidade"   to (prefill?.moisture ?: 0f),
                        "lotWeight" to fLotWeight,
                        "lotPrice"  to (fLotWeight * fPriceBySack) / 60f
                    )

                    val defectsPayload   = strategy.createDefectsPayload(defectsMap)
                    val financialPayload = FinancialDiscountPayload(
                        priceBySack       = fPriceBySack,
                        lotWeight         = fLotWeight,
                        group             = discountViewModel.selectedGroup,
                        daysOfStorage     = daysOfStorage.toIntOrZero(),
                        doesTechnicalLoss = doesTechnicalLoss,
                        deductionValue    = deductionValue.toFloatOrZero(),
                        doesDeduction     = doesDeduction
                    )

                    discountViewModel.calculateDiscount(defectsPayload, financialPayload)
                }
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier  = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color     = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Calcular")
                }
            }
        }

        Spacer(Modifier.height(8.dp))
    }
}

// =============================================================================
// CAMPO NUMÉRICO DO SHEET
// =============================================================================

@Composable
private fun SheetNumberField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester?,
    isInteger: Boolean = false
) {
    OutlinedTextField(
        value         = value,
        onValueChange = { raw ->
            val sanitized = raw.replace(',', '.')
                .filter { it.isDigit() || (!isInteger && it == '.') }
            if (isInteger || sanitized.count { it == '.' } <= 1) {
                onValueChange(sanitized)
            }
        },
        label           = { Text(label) },
        modifier        = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = if (isInteger) KeyboardType.Number else KeyboardType.Decimal,
            imeAction    = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onNext = { nextFocus?.requestFocus() }),
        singleLine      = true
    )
}

// =============================================================================
// EXTENSÕES PRIVADAS
// =============================================================================

private fun String.toFloatOrZero(): Float = replace(",", ".").toFloatOrNull() ?: 0f
private fun String.toIntOrZero(): Int     = toIntOrNull() ?: 0