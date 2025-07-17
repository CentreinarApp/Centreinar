package com.example.centreinar.ui.discount.screens

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.discount.components.DiscountResultsTable
import com.example.centreinar.ui.discount.viewmodel.DiscountViewModel

@Composable
fun DiscountResultScreen(
    navController: NavController,
    viewModel: DiscountViewModel = hiltViewModel()
){
    val discounts by viewModel.discounts.collectAsState()
    if(discounts != null){
        DiscountResultsTable(discounts!!)
    }
    else {
        Text("Algo não deu certo")
    }
}