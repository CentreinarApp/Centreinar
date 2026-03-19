package com.example.centreinar.ui.classificationProcess.screens

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
import com.example.centreinar.ui.classificationProcess.components.*
import com.example.centreinar.ui.classificationProcess.strategy.BaseLimit
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.ui.components.ActionButtons
import com.example.centreinar.ui.components.InputDataTable
import com.example.centreinar.ui.components.OfficialReferenceTable
import com.example.centreinar.ui.discount.viewmodel.DiscountNavigationEvent
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.util.Routes

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
    val context = LocalContext.current

    // Controla a visibilidade do BottomSheet
    var showDiscountSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // One-shot event — Channel garante que a navegação acontece exatamente
    // uma vez por cálculo, sem depender de estado residual no ViewModel.
    LaunchedEffect(Unit) {
        discountViewModel.navigationEvent.collect { event ->
            when (event) {
                is DiscountNavigationEvent.NavigateToResults -> {
                    showDiscountSheet = false
                    navController.navigate(Routes.DISCOUNT_RESULTS) {
                        // Remove instâncias anteriores de DISCOUNT_RESULTS do back stack
                        // antes de empilhar a nova — garante que o usuário veja sempre
                        // o resultado do cálculo mais recente ao recalcular.
                        popUpTo(Routes.DISCOUNT_RESULTS) { inclusive = true }
                    }
                }
            }
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
            val moistureValue  = result.moisturePercentage
            val finalTypeValue = result.finalType
            val moistureLimit  = limits.moistureUpLim
            val groupLimit     = limits.group

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

                    // Desclassificação
                    uiState.disqualification?.let { disq ->
                        DisqualificationInfoCard(
                            badConservation = disq.badConservation == 1,
                            strangeSmell    = disq.strangeSmell    == 1,
                            insects         = disq.insects         == 1,
                            toxicGrains     = disq.toxicGrains     == 1,
                            toxicSeeds      = uiState.toxicSeeds
                        )
                    }

                    Spacer(Modifier.height(26.dp))

                    // Tabela de referência
                    val limitsForTable: List<BaseLimit> =
                        if (classificationViewModel.isOfficial && allOfficialLimits.isNotEmpty())
                            allOfficialLimits.filterIsInstance<BaseLimit>()
                        else
                            listOf(limits)

                    // Tabela de dados de entrada da amostra (pesos e defeitos em gramas)
                    if (uiState.sampleInputRows.isNotEmpty()) {
                        InputDataTable(
                            title = "DADOS DA AMOSTRA",
                            rows  = uiState.sampleInputRows.map { it.label to it.value }
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    OfficialReferenceTable(
                        group      = groupLimit,
                        isOfficial = classificationViewModel.isOfficial,
                        data       = limitsForTable
                    )
                }

                ActionButtons(
                    onBack            = { navController.popBackStack() },
                    primaryActionText = "Calcular Desconto",
                    onPrimaryAction   = {
                        val classifId = result.id

                        // SALVA O ID DA CLASSIFICAÇÃO NO VIEWMODEL DE DESCONTO PARA USO NO PDF
                        discountViewModel.setClassificationId(classifId)

                        discountViewModel.loadFromClassification(
                            lotWeight        = uiState.sampleLotWeight,
                            classification   = result,
                            grain            = classificationViewModel.selectedGrain ?: "Soja",
                            group            = classificationViewModel.selectedGroup ?: 1,
                            isOfficial       = classificationViewModel.isOfficial,
                            classificationId = classifId
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
                        initialLotWeight  = uiState.sampleLotWeight.takeIf { it > 0f }?.toString() ?: "",
                        discountViewModel = discountViewModel,
                        onDismiss         = { showDiscountSheet = false }
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

    val lotWeightFocus      = remember { FocusRequester() }
    val priceBySackFocus    = remember { FocusRequester() }
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

                    // A tela só conhece dados financeiros.
                    // Defeitos e grão vêm do prefill interno do ViewModel.
                    discountViewModel.calculateDiscountFromClassification(
                        lotWeight         = fLotWeight,
                        priceBySack       = fPriceBySack,
                        daysOfStorage     = daysOfStorage.toIntOrZero(),
                        doesTechnicalLoss = doesTechnicalLoss,
                        deductionValue    = deductionValue.toFloatOrZero(),
                        doesDeduction     = doesDeduction
                    )
                }
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = MaterialTheme.colorScheme.onPrimary
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