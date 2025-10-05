package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel

@Composable
fun MilhoDefeitosScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    var ardidos by remember { mutableStateOf("") }
    var queimados by remember { mutableStateOf("") }
    var mofados by remember { mutableStateOf("") }
    var fermentados by remember { mutableStateOf("") }
    var germinados by remember { mutableStateOf("") }
    var imaturos by remember { mutableStateOf("") }
    var chochos by remember { mutableStateOf("") }
    var danificados by remember { mutableStateOf("") }
    var esverdeados by remember { mutableStateOf("") }
    var partidos by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Etapa 2 - Defeitos", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(value = ardidos, onValueChange = { ardidos = it }, label = { Text("Ardidos (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = queimados, onValueChange = { queimados = it }, label = { Text("Queimados (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = mofados, onValueChange = { mofados = it }, label = { Text("Mofados (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = fermentados, onValueChange = { fermentados = it }, label = { Text("Fermentados (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = germinados, onValueChange = { germinados = it }, label = { Text("Germinados (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = imaturos, onValueChange = { imaturos = it }, label = { Text("Imaturos (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = chochos, onValueChange = { chochos = it }, label = { Text("Chochos (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = danificados, onValueChange = { danificados = it }, label = { Text("Danificados (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = esverdeados, onValueChange = { esverdeados = it }, label = { Text("Esverdeados (%)") })
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(value = partidos, onValueChange = { partidos = it }, label = { Text("Partidos/Quebrados (%)") })

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { navController.navigate("milhoClasse") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Próximo")
        }
    }
}
