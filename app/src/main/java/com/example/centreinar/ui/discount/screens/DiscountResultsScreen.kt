package com.example.centreinar.ui.discount.screens

import android.util.Log
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.centreinar.Discount
import com.example.centreinar.ui.discount.components.DiscountResultsTable
import com.example.centreinar.ui.discount.components.DiscountSimplifiedResultsTable
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel

@Composable
fun DiscountResultScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
){

    val discounts by viewModel.discounts.collectAsState()
    var showMoreDetails by  remember { mutableStateOf(false) }
    val lastUsedLimit by viewModel.lastUsedLimit.collectAsStateWithLifecycle()
    val context = LocalContext.current




    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(1.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(discounts != null){
            DiscountSimplifiedResultsTable(discounts!!)

            Spacer(Modifier.height(16.dp))

            Row {
                Switch(
                    checked = showMoreDetails,
                    onCheckedChange = {showMoreDetails = it}
                )
                Text("Mais detalhes")
            }

            if(showMoreDetails){
                DiscountResultsTable(discounts!!)
            }
        }
        else {
            Text("Algo não deu certo")
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                navController.navigate("home")
                viewModel.clearStates()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Nova Análise")

        }

        Button(onClick = {
            viewModel.loadLastUsedLimit()
            Log.e("DiscountResultsScreen","clicked the button")
            lastUsedLimit?.let{
                Log.e("DiscountResultsScreen","have last limit")
                viewModel.exportDiscount(context, discounts!!, lastUsedLimit!!)
            }
        }) {
            Text("Exportar PDF")
        }
    }

}