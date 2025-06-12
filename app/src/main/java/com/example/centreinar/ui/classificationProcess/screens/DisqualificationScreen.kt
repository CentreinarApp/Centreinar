package com.example.centreinar.ui.classificationProcess.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel


@Composable
public fun DisqualificationScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    var badConservation by remember { mutableStateOf(false) }
    var strangeSmell by remember { mutableStateOf(false) }
    var insects by remember { mutableStateOf(false) }
    var toxicGrains by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp)
        ) {
            Text(
                "O lote apresenta ",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = badConservation,
                    onCheckedChange = { badConservation = it }
                )
                Text(
                    "Mal estado de conservação?",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = strangeSmell,
                    onCheckedChange = { strangeSmell = it }
                )
                Text(
                    "Cheiro estranho?",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = insects,
                    onCheckedChange = { insects = it }
                )
                Text(
                    "Insetos vivos ou mortos?",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(
                    checked = toxicGrains,
                    onCheckedChange = { toxicGrains = it }
                )
                Text(
                    "Sementes tóxicas?",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    val badConservationInt = if (badConservation) 1 else 0
                    val strangeSmellInt = if (strangeSmell) 1 else 0
                    val insectsInt = if (insects) 1 else 0
                    val toxicGrainInt = if (toxicGrains) 1 else 0

                    viewModel.setDisqualification(
                        badConservationInt,
                        strangeSmellInt,
                        insectsInt,
                        toxicGrainInt
                    )
                    navController.navigate("classification")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Confirmar")
            }
        }
    }
}