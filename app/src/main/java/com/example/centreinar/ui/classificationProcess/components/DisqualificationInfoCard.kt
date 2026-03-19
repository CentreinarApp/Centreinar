package com.example.centreinar.ui.classificationProcess.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DisqualificationInfoCard(
    badConservation: Boolean,
    strangeSmell: Boolean,
    insects: Boolean,
    toxicGrains: Boolean,
    toxicSeeds: List<Pair<String, Int>>
) {
    // Se não tiver nenhum defeito de desclassificação, não mostra o Card
    if (!badConservation && !strangeSmell && !insects && !toxicGrains) {
        return
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Atenção: Esse lote apresentou os seguintes parâmetros com potencial de Desclassificação:",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(bottom = 8.dp),
                textAlign = TextAlign.Center
            )

            if (badConservation) {
                Text("• Mau estado de conservação", style = MaterialTheme.typography.bodyMedium)
            }
            if (strangeSmell) {
                Text("• Cheiro estranho", style = MaterialTheme.typography.bodyMedium)
            }
            if (insects) {
                Text("• Insetos vivos ou mortos", style = MaterialTheme.typography.bodyMedium)
            }
            if (toxicGrains) {
                Text("• Sementes tóxicas presentes", style = MaterialTheme.typography.bodyMedium)

                if (toxicSeeds.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "Detalhes das Sementes Tóxicas:",
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 2.dp)
                    )
                    toxicSeeds.forEach { seed ->
                        Text(
                            "- ${seed.first}: ${seed.second} unidade(s)",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 24.dp)
                        )
                    }
                }
            }
        }
    }
}