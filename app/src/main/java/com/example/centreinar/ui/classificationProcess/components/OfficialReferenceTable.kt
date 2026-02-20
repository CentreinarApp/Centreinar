package com.example.centreinar.ui.classificationProcess.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.LimitMilho

@Composable
fun OfficialReferenceTable(grain: String, group: Int, isOfficial: Boolean, data: List<Any>) {
    val labels = if (grain == "Soja") {
        listOf("Ardidos e Queimados", "Queimados", "Mofados", "Avariados Total", "Esverdeados", "Partidos/Quebrados e Amassados", "Matérias Estranhas e Impurezas")
    } else {
        listOf("Ardidos", "Avariados Total", "Quebrados", "Matérias Estranhas e Impurezas", "Carunchados",)
    }

    val columnWeightLabel = 1.0f
    val columnWeightValue = 1f

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Defeito",
                    modifier = Modifier.weight(columnWeightLabel), // Espaço maior para o nome
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )

                data.forEachIndexed { index, _ ->
                    val textoCabecalho = if (group == 2 && isOfficial) {
                        "Padrão Básico"
                    } else if (group == 1 && ((index + 1) == 4)) {
                        "Fora de Tipo"
                    } else {
                        "Tipo ${index + 1}"
                    }

                    Text(
                        text = textoCabecalho,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))


            labels.forEachIndexed { rowIndex, label ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(columnWeightLabel),
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    data.forEach { item ->
                        val value = when (item) {
                            is LimitSoja -> listOf(item.burntOrSourUpLim, item.burntUpLim, item.moldyUpLim, item.spoiledTotalUpLim, item.greenishUpLim, item.brokenCrackedDamagedUpLim, item.impuritiesUpLim)
                            is LimitMilho -> listOf(item.ardidoUpLim, item.spoiledTotalUpLim, item.brokenUpLim, item.impuritiesUpLim, item.carunchadoUpLim)
                            else -> emptyList()
                        }.getOrNull(rowIndex) ?: 0f

                        Text(
                            text = "$value%",
                            modifier = Modifier.weight(columnWeightLabel),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (rowIndex < labels.lastIndex) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
                }
            }
        }
    }
}