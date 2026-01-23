package com.example.centreinar.ui.discount.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.milho.components.MilhoDiscountResultsTable

@Composable
fun MilhoDiscountResultScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {
    val discountsMilho by viewModel.discountsMilho.collectAsState()
    val lastUsedLimit by viewModel.lastUsedLimitMilho.collectAsStateWithLifecycle()

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            "Resultado do Desconto — Milho",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(Modifier.height(12.dp))

        if (discountsMilho != null) {
            MilhoDiscountResultsTable(discounts = discountsMilho!!)
        } else {
            Text(
                "Nenhum resultado encontrado. Verifique se os dados foram preenchidos corretamente na tela anterior.",
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(Modifier.height(24.dp))

        // Botões de ação
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                modifier = Modifier
                    .weight(1f),
                onClick = { navController.popBackStack() }
            ) {
                Text("Voltar")
            }

            Button(
                modifier = Modifier
                    .weight(1f),
                onClick = { navController.navigate("home") }
            ) {
                Text("Realizar Nova Análise")
            }
        }
        Button(modifier = Modifier.fillMaxWidth(), onClick = {
            viewModel.loadLastUsedLimit()
            Log.e("DiscountResultsScreen","clicked the button")
            lastUsedLimit?.let{
                Log.e("DiscountResultsScreen","have last limit")
                viewModel.exportDiscountMilho(context, discountsMilho!!, lastUsedLimit!!)
            }
        }) {
            Text("Exportar PDF")
        }

    }
}