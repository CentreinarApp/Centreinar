package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.NumberFormat
import java.util.Locale
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.strategy.BaseLimit
import com.example.centreinar.ui.components.ActionButtons
import com.example.centreinar.ui.components.InfoDialog
import com.example.centreinar.ui.components.InputDataTable
import com.example.centreinar.ui.components.OfficialReferenceTable
import com.example.centreinar.ui.discount.components.DiscountTopBar
import com.example.centreinar.ui.discount.strategy.DiscountResultRow
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.util.Routes

@Composable
fun DiscountResultScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState           by viewModel.uiState.collectAsStateWithLifecycle()
    val discountInputRows = uiState.discountInputRows
    val discountResult    by viewModel.discountResult.collectAsStateWithLifecycle()
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

    val context     = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDefaultLimits()
    }

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        // Tela inteira
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TopBar
            Column( modifier = Modifier.fillMaxWidth() ) {
                DiscountTopBar(
                    onBackClick = { navController.popBackStack() }
                )
            }

            // Conteúdo da tela
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 22.dp)
                    .padding(top = 16.dp, bottom = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }

                    discountResult != null -> {
                        Text(
                            text = "Resultado dos Descontos",
                            maxLines = 1,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Referência: ${viewModel.grainDescriptor.displayName}" +
                                    (if (viewModel.grainDescriptor.displayName != "Milho") {
                                        " Grupo ${viewModel.selectedGroup}"
                                    } else { " " }) +
                                    (if (viewModel.isOfficial)
                                        " (${viewModel.grainDescriptor.limitRegulation})"
                                    else { " Conforme contrato comercial" }),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(16.dp))

                        GenericDiscountTable(
                            title = "RESUMO DOS DESCONTOS",
                            rows  = discountResult!!.summaryRows
                        )

                        Spacer(Modifier.height(16.dp))

                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Switch(
                                checked         = showDetails,
                                onCheckedChange = { showDetails = it }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Mais detalhes")
                        }

                        if (showDetails && discountResult!!.detailRows.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            GenericDiscountTable(
                                title      = "DESCONTOS POR DEFEITO",
                                headerCol1 = "DEFEITO",
                                rows       = discountResult!!.detailRows
                            )
                        }

                        if (discountInputRows.isNotEmpty()) {
                            Spacer(Modifier.height(16.dp))
                            InputDataTable(
                                title = "DADOS UTILIZADOS NO CÁLCULO",
                                rows  = discountInputRows.map { it.label to it.value }
                            )
                        }

                        val limitsForTable: List<BaseLimit> =
                            if (viewModel.isOfficial && allOfficialLimits.isNotEmpty()) {
                                allOfficialLimits.filterIsInstance<BaseLimit>()
                            }
                            else {
                                allOfficialLimits.filterIsInstance<BaseLimit>()
                            }


                        if (limitsForTable.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            OfficialReferenceTable(
                                group      = viewModel.selectedGroup ?: 1,
                                isOfficial = viewModel.isOfficial,
                                data       = limitsForTable,
                                isDiscount = true
                            )
                        }
                    }

                    else -> {
                        Box(
                            modifier         = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Algo não deu certo.")
                        }
                    }
                }
            }

            ActionButtons(
                onBack            = { navController.popBackStack() },
                primaryActionText = "Nova Análise",
                onPrimaryAction   = {
                    navController.navigate(Routes.HOME) {
                        popUpTo("main_flow") { inclusive = true }
                    }
                },
                onExportPdf = { viewModel.exportPdf(context) },
                modifier = Modifier.padding(start = 10.dp, end = 10.dp)
            )
        }
    }
}

// =============================================================================
// Tabela genérica para exibir os resultados de descontos
// =============================================================================

@Composable
private fun GenericDiscountTable(
    title: String,
    rows: List<DiscountResultRow>,
    headerCol1: String = "Tipo de Desconto",
    modifier: Modifier = Modifier
) {
    // Controla qual linha está com o popup aberto
    var openInfoIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(6.dp)) {

            Box(
                modifier         = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = title,
                    maxLines = 1,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 5.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = headerCol1,
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary,
                        modifier   = Modifier.weight(2f)
                    )
                    Text(
                        text       = "Qtd (kg)",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.End
                    )
                    Text(
                        text       = "Valor (R$)",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimary,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.End
                    )
                }

                rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically
                    ) {
                        // Label + ícone de info quando disponível
                        Row(
                            modifier          = Modifier.weight(2f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text     = row.label,
                                style    = MaterialTheme.typography.bodyLarge,
                                color    = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f, fill = false)
                            )
                            if (row.infoText != null) {
                                CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                    IconButton(
                                        onClick  = { openInfoIndex = index },
                                        modifier = Modifier.size(20.dp)
                                    ) {
                                        Icon(
                                            imageVector        = Icons.Outlined.Info,
                                            contentDescription = "Informação sobre ${row.label}",
                                            modifier           = Modifier.size(20.dp),
                                            tint               = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text      = row.massKg.toCurrency(),
                            style     = MaterialTheme.typography.bodyLarge,
                            modifier  = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color     = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text      = row.valueRS.toCurrency(),
                            style     = MaterialTheme.typography.bodyLarge,
                            modifier  = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color     = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (index < rows.lastIndex) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                    }
                }
            }
        }
    }

    // Popup fora do Card
    openInfoIndex?.let { idx ->
        val row = rows.getOrNull(idx)
        if (row?.infoText != null) {
            InfoDialog(
                title       = "O que é " + row.label + "?",
                description = row.infoText,
                onDismiss   = { openInfoIndex = null }
            )
        }
    }
}

// Formata Float como moeda
private fun Float.toCurrency(): String =
    NumberFormat.getNumberInstance(Locale("pt", "BR")).apply {
        minimumFractionDigits = 2
        maximumFractionDigits = 2
    }.format(this)