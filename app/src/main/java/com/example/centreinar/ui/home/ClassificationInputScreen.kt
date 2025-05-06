package com.example.centreinar.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

import com.example.centreinar.Sample

@Composable
fun ClassificationInputScreen(
    viewModel: HomeViewModel = hiltViewModel()
) {
    // State from ViewModel
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val resultText by viewModel.text.collectAsState()

    // Local form state
    var grain by remember { mutableStateOf("") }
    var group by remember { mutableStateOf("") }
    var lotWeight by remember { mutableStateOf("") }
    var sampleWeight by remember { mutableStateOf("") }
    var cleanWeight by remember { mutableStateOf("") }
    var foreignMatters by remember { mutableStateOf("") }
    var humidity by remember { mutableStateOf("") }
    var greenish by remember { mutableStateOf("") }
    var brokenCrackedDamaged by remember { mutableStateOf("") }
    var damaged by remember { mutableStateOf("") }
    var damagedInput by remember { mutableStateOf("") }
    var piercingInput by remember { mutableStateOf("") }
    var piercingDamaged by remember { mutableStateOf("") }
    var burnt by remember { mutableStateOf("") }
    var sour by remember { mutableStateOf("") }
    var moldy by remember { mutableStateOf("") }
    var fermented by remember { mutableStateOf("") }
    var germinated by remember { mutableStateOf("") }
    var immature by remember { mutableStateOf("") }
    var shriveled by remember { mutableStateOf("") }


    // Clear form function
    fun clearForm() {
        grain = ""
        group = ""
        lotWeight = ""
        sampleWeight = ""
        cleanWeight = ""
        foreignMatters = ""
        humidity = ""
        greenish = ""
        brokenCrackedDamaged = ""
        piercingDamaged = ""
        damaged = ""
        burnt = ""
        sour = ""
        moldy = ""
        fermented = ""
        germinated = ""
        immature = ""
        shriveled = ""
    }

    val numericFields = setOf(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16) // Indices of numeric fields

    val fields = listOf(
        SampleInputField("Grão", grain) { grain = it },
        SampleInputField("Grupo", group) {
            if (it.isEmpty() || it.toIntOrNull() != null) group = it
        },
        SampleInputField("Umidade", humidity) {
            if (it.isEmpty() || it.toFloatOrNull() != null) humidity = it
        },
        SampleInputField("Peso do lote(kg)", lotWeight) {
            if (it.isEmpty() || it.toFloatOrNull() != null) lotWeight = it
        },
        SampleInputField("Peso da amostra(g)", sampleWeight) {
            if (it.isEmpty() || it.toFloatOrNull() != null) sampleWeight = it
        },
        SampleInputField("Máteria Estranha e Impurezas", foreignMatters) {
            if (it.isEmpty() || it.toFloatOrNull() != null) foreignMatters = it
        },

        //spoiled
        SampleInputField("Ardidos", sour) {
            if (it.isEmpty() || it.toFloatOrNull() != null) sour = it
        },
        SampleInputField("Queimados", burnt) {
            if (it.isEmpty() || it.toFloatOrNull() != null) burnt = it
        },
        SampleInputField("Mofados", moldy) {
            if (it.isEmpty() || it.toFloatOrNull() != null) moldy = it
        },
        SampleInputField("Fermentados", fermented) {
            if (it.isEmpty() || it.toFloatOrNull() != null) fermented = it
        },
        SampleInputField("Germinados", germinated) {
            if (it.isEmpty() || it.toFloatOrNull() != null) germinated = it
        },
        SampleInputField("Imaturos", immature) {
            if (it.isEmpty() || it.toFloatOrNull() != null) immature = it
        },
        SampleInputField("Chochos", shriveled) {
            if (it.isEmpty() || it.toFloatOrNull() != null) shriveled = it
        },
        SampleInputField("Picados", piercingInput) { newValue ->
            piercingInput = newValue // Store raw input
            val parsedPiercing = newValue.toFloatOrNull() ?: 0f
            piercingDamaged = (parsedPiercing / 4f).toString()
        },
        SampleInputField("Danificados", damagedInput) { newValue ->
            // Only allow numeric input
            if (newValue.isEmpty() || newValue.toFloatOrNull() != null) {
                damagedInput = newValue // Store raw input

                // Calculate damaged (sum with piercingDamaged)
                val parsedDamaged = newValue.toFloatOrNull() ?: 0f
                val parsedPiercing = piercingDamaged.toFloatOrNull() ?: 0f
                damaged = (parsedDamaged + parsedPiercing).toString()
            }
        },
        SampleInputField("Esverdeados", greenish) {
            if (it.isEmpty() || it.toFloatOrNull() != null) greenish = it
        },
        SampleInputField("Partidos, Quebrados e Amassados", brokenCrackedDamaged) {
            if (it.isEmpty() || it.toFloatOrNull() != null) brokenCrackedDamaged = it
        },

    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Preencha os dados da amostra", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
        }

        itemsIndexed(fields) { index, field ->
            if (field.label == "Picados") {
                // Custom layout for "Picados" field + calculation
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Input Field
                    OutlinedTextField(
                        value = field.value,
                        onValueChange = field.onValueChange,
                        label = { Text(field.label) },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = field.value.isNotEmpty() && field.value.toFloatOrNull() == null
                    )

                    // Calculation Result
                    Text(
                        text = if (piercingInput.isNotEmpty()) {
                            "$piercingInput / 4 = $piercingDamaged"
                        } else {
                            "" // Hide when empty
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }
            } else {
                OutlinedTextField(
                    value = field.value,
                    onValueChange = field.onValueChange,
                    label = { Text(field.label) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    keyboardOptions = if (index in numericFields) {
                        KeyboardOptions(keyboardType = KeyboardType.Number)
                    } else {
                        KeyboardOptions.Default
                    },
                    isError = when(index) {
                        1 -> field.value.isNotEmpty() && field.value.toIntOrNull() == null
                        in 2..14 -> field.value.isNotEmpty() && field.value.toFloatOrNull() == null
                        else -> false
                    }
                )
            }

        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    val sample = Sample(
                        grain = grain,
                        group = group.toIntOrNull() ?: 0,
                        lotWeight = lotWeight.toFloatOrNull() ?: 0f,
                        sampleWeight = sampleWeight.toFloatOrNull() ?: 0f,
                        cleanWeight = cleanWeight.toFloatOrNull() ?: 0f,
                        foreignMattersAndImpurities = foreignMatters.toFloatOrNull() ?: 0f,
                        humidity = humidity.toFloatOrNull() ?: 0f,
                        greenish = greenish.toFloatOrNull() ?: 0f,
                        brokenCrackedDamaged = brokenCrackedDamaged.toFloatOrNull() ?: 0f,
                        damaged = damaged.toFloatOrNull() ?: 0f,
                        burnt = burnt.toFloatOrNull() ?: 0f,
                        sour = sour.toFloatOrNull() ?: 0f,
                        moldy = moldy.toFloatOrNull() ?: 0f,
                        fermented = fermented.toFloatOrNull() ?: 0f,
                        germinated = germinated.toFloatOrNull() ?: 0f,
                        immature = immature.toFloatOrNull() ?: 0f,
                        shriveled = shriveled.toFloatOrNull() ?: 0f

                    )
                    viewModel.classifySample(sample)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isLoading && fields.all { field ->
                    when(fields.indexOf(field)) {
                        1 -> field.value.isEmpty() || field.value.toIntOrNull() != null
                        in 2..14 -> field.value.isEmpty() || field.value.toFloatOrNull() != null
                        else -> true
                    }
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Classificar")
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))

            if (error != null) {
                Text(
                    text = error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            if (resultText != null) {
                Column {
                    Text(
                        text = "  $resultText",
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { clearForm() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Nova Análise")
                    }
                }
            }
        }
    }
}

data class SampleInputField(
    val label: String,
    val value: String,
    val onValueChange: (String) -> Unit
)