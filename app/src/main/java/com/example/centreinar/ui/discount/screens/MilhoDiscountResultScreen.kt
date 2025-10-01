package com.example.centreinar.ui.discount.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.data.local.entity.DiscountMilho
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel
import com.example.centreinar.ui.milho.components.MilhoDiscountResultsTable

@Composable
fun MilhoDiscountResultScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
) {

    val discountsMilho = remember { mutableStateOf<DiscountMilho?>(null) }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)) {
        Text("Resultado do Desconto — Milho", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(12.dp))

        if (discountsMilho.value != null) {
            MilhoDiscountResultsTable(discountsMilho.value!!)
        } else {
            Text("Nenhum desconto de milho carregado. Garanta que o ViewModel devolve o desconto do milho.")
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = { navController.navigate("home") }, modifier = Modifier.fillMaxWidth()) {
            Text("Nova Análise")
        }
    }
}
