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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.LimitSoja
import com.example.centreinar.ui.discount.components.DiscountResultsTable
import com.example.centreinar.ui.discount.components.DiscountSimplifiedResultsTable
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.classificationProcess.components.OfficialReferenceTable

@Composable
fun DiscountResultScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
){
    // 1. O Scaffold envolve toda a tela
    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding -> // Esse innerPadding contém as medidas da barra de status e navegação

        val discounts by viewModel.discounts.collectAsStateWithLifecycle()
        val lastUsedLimit by viewModel.lastUsedLimit.collectAsStateWithLifecycle()
        val currentGroup = viewModel.selectedGroup
        val allOfficialLimits by viewModel.allOfficialLimits.collectAsStateWithLifecycle()
        var showMoreDetails by remember { mutableStateOf(false) }
        val context = LocalContext.current

        // Verificação se é classificação oficial
        val isOfficial = viewModel.isOfficial == true

        // Carrega os limites utilizados assim que a tela abre
        LaunchedEffect(Unit) {
            viewModel.loadLastUsedLimit()
            viewModel.loadDefaultLimits()
        }

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
                if (discounts != null) {
                    DiscountSimplifiedResultsTable(discounts!!)

                    Spacer(Modifier.height(16.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Switch(
                            checked = showMoreDetails,
                            onCheckedChange = { showMoreDetails = it }
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Mais detalhes")
                    }

                    if (showMoreDetails) {
                        Spacer(Modifier.height(8.dp))
                        DiscountResultsTable(discounts!!)
                    }

                    // TABELA DE LIMITES
                    if (lastUsedLimit != null) {
                        Spacer(Modifier.height(24.dp))
                        Text(
                            text = "Limites Utilizados no Cálculo",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            textAlign = TextAlign.Center
                        )

                        // Se não houver limites oficiais carregados, usamos o limite que foi usado no cálculo.
                        val dadosParaTabela = allOfficialLimits.ifEmpty { listOf(lastUsedLimit!!) }

                        OfficialReferenceTable(
                            grain = "Soja",
                            group = currentGroup ?: 1,
                            isOfficial = isOfficial,
                            data = dadosParaTabela
                        )
                    }

                } else {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("Algo não deu certo")
                    }
                }

                Spacer(Modifier.height(16.dp))
            }

            // Botões de ação
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        viewModel.loadLastUsedLimit()
                        val currentLimit = lastUsedLimit
                        if (currentLimit != null && discounts != null) {
                            viewModel.exportDiscount(context, discounts!!, currentLimit)
                        } else {
                            Log.e("DiscountResultsScreen", "Limit is null, trying to load...")
                        }
                    }
                ) {
                    Text("Exportar PDF")
                }
            }
        }
    }
}