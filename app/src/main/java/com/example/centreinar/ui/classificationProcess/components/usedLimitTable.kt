package com.example.centreinar.ui.classificationProcess.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.centreinar.LimitSoja

@Composable
fun UsedLimitTable(
    defectLimits: LimitSoja,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),  // Reduced horizontal padding
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            // Table Headers with increased vertical padding
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(vertical = 16.dp, horizontal = 16.dp),  // Increased vertical padding
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    "Defeito",
                    style = MaterialTheme.typography.titleMedium,  // Larger font
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1.5f)
                )
                Text(
                    "Limite de Tolerância (%)",
                    style = MaterialTheme.typography.titleMedium,  // Larger font
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }

            // Table Rows
            val defectPairs = listOf(
                "Impurezas" to Pair(defectLimits.impuritiesLowerLim, defectLimits.impuritiesUpLim),
                "Quebrados, Partidos e Amassados" to Pair(defectLimits.brokenCrackedDamagedLowerLim, defectLimits.brokenCrackedDamagedUpLim),
                "Esverdeados" to Pair(defectLimits.greenishLowerLim, defectLimits.greenishUpLim),
                "Queimados" to Pair(defectLimits.burntLowerLim, defectLimits.burntUpLim),
                "Mofados" to Pair(defectLimits.moldyLowerLim, defectLimits.moldyUpLim),
                "Ardidos e Queimados" to Pair(defectLimits.burntOrSourLowerLim, defectLimits.burntOrSourUpLim),
                "Total de Avariados" to Pair(defectLimits.spoiledTotalLowerLim, defectLimits.spoiledTotalUpLim)
            )

            defectPairs.forEachIndexed { index, (defectName, limits) ->
                DefectLimitsTableRow(
                    defectName = defectName,
                    lowerLimit = limits.first,
                    upperLimit = limits.second,
                    isLast = index == defectPairs.lastIndex
                )
            }
        }
    }
}


@Composable
private fun DefectLimitsTableRow(
    defectName: String,
    lowerLimit: Float,
    upperLimit: Float,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp, horizontal = 16.dp),  // Increased vertical padding
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = defectName,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1.5f),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "${"%.2f".format(lowerLimit)} - ${"%.2f".format(upperLimit)}",
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface
        )
    }

    if (!isLast) {
        Divider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
            thickness = 1.dp
        )
    }
}
