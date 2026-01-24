package com.example.centreinar.ui.discount.screens

import android.util.Log
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
import com.example.centreinar.data.local.entity.LimitMilho
import com.example.centreinar.ui.classificationProcess.screens.OfficialReferenceTable
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.milho.components.MilhoDiscountResultsTable


@Composable
fun MilhoDiscountResultScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val discountsMilho by viewModel.discountsMilho.collectAsStateWithLifecycle()
    val lastUsedLimit by viewModel.lastUsedLimitMilho.collectAsStateWithLifecycle()
    val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()

    val context = LocalContext.current

    // Carrega os limites utilizados assim que a tela abre
    LaunchedEffect(Unit) {
        viewModel.loadLastUsedLimit()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                "Resultado do Desconto — Milho",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(12.dp))

            // Tabelas
            if (discountsMilho != null) {
                // TABELA DE RESULTADO
                MilhoDiscountResultsTable(discounts = discountsMilho!!)

                // TABELA DE LIMITES MILHO
                if (allOfficialLimits.isNotEmpty()) {
                    OfficialReferenceTable(
                        grain = "Milho",
                        data = allOfficialLimits
                    )
                } else {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Carregando limites oficiais...", style = MaterialTheme.typography.bodySmall)
                    }
                }

            } else {
                Text(
                    "Nenhum resultado encontrado. Verifique se os dados foram preenchidos corretamente na tela anterior.",
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.height(16.dp))
        }

        // 2. RODAPÉ FIXO
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.popBackStack() }
                ) {
                    Text("Voltar")
                }

                Button(
                    modifier = Modifier.weight(1f),
                    onClick = { navController.navigate("home") }
                ) {
                    Text("Nova Análise")
                }
            }

            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                viewModel.loadLastUsedLimit()

                val currentLimit = lastUsedLimit
                if (currentLimit != null && discountsMilho != null) {
                    viewModel.exportDiscountMilho(context, discountsMilho!!, currentLimit)
                } else {
                    Log.e("DiscountResultsScreen", "Limit or Discount is null")
                }
            }) {
                Text("Exportar PDF")
            }
        }
    }
}