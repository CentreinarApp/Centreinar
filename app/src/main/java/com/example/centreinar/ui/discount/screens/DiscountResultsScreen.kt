package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.components.ActionButtons
import com.example.centreinar.ui.components.OfficialReferenceTable
import com.example.centreinar.ui.discount.strategy.DiscountResultRow
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel

@Composable
fun DiscountResultScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val discountResult by viewModel.discountResult.collectAsStateWithLifecycle()

    // Coletamos os limites brutos da base de dados
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var showDetails by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDefaultLimits()
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when {
                    uiState.isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.padding(32.dp))
                    }

                    discountResult != null -> {
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

                        // Tabela de limites oficiais
                        if (allOfficialLimits.isNotEmpty()) {
                            Spacer(Modifier.height(24.dp))
                            Text(
                                text      = "Limites Utilizados no Cálculo",
                                style     = MaterialTheme.typography.titleMedium,
                                modifier  = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                                textAlign = TextAlign.Center
                            )

                            OfficialReferenceTable(
                                grain = viewModel.selectedGrain,
                                group = viewModel.selectedGroup ?: 1,
                                isOfficial = viewModel.isOfficial,
                                data = allOfficialLimits
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

                Spacer(Modifier.height(16.dp))
            }

            ActionButtons(
                onBack = { navController.popBackStack() },
                primaryActionText = "Nova Análise",
                onPrimaryAction = {
                    navController.navigate("home") {
                        popUpTo("main_flow") { inclusive = true }
                    }
                },
                onExportPdf = { viewModel.exportPdf(context) }
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
    headerCol1: String = "Tipo de Quebra",
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.padding(6.dp).fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(6.dp)) {

            Box(
                modifier         = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = title,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.primary
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 5.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = headerCol1,
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier   = Modifier.weight(2f)
                    )
                    Text(
                        text       = "Qtd (kg)",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.End
                    )
                    Text(
                        text       = "Valor (R$)",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.End
                    )
                }

                rows.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text     = row.label,
                            style    = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(2f),
                            color    = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text      = "%.2f".format(row.massKg),
                            style     = MaterialTheme.typography.bodyLarge,
                            modifier  = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color     = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text      = "%.2f".format(row.valueRS),
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
}