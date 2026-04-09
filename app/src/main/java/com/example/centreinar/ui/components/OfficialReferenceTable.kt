package com.example.centreinar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
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
import com.example.centreinar.ui.classificationProcess.strategy.BaseLimit

@Composable
fun OfficialReferenceTable(
    group: Int?,
    isOfficial: Boolean,
    data: List<BaseLimit>,
    isDiscount: Boolean = false
) {
    val rows = data.firstOrNull()?.toDisplayRows() ?: return

    val columnWeightLabel = 2.5f

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(6.dp)) {
            Box(
                modifier         = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text       = "Limites Utilizados no Cálculo",
                    maxLines = 2,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                // Cabeçalho
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(vertical = 5.dp, horizontal = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text       = "Defeito",
                        modifier   = Modifier.weight(columnWeightLabel),
                        style      = MaterialTheme.typography.labelLarge,
                        color      = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold,
                        textAlign  = TextAlign.Center
                    )

                    data.forEachIndexed { index, _ ->
                        val textoCabecalho = if (group == 2 && isOfficial) {
                            "Padrão Básico"
                        } else if (group == 1 && (index + 1) == 4) {
                            "Fora de Tipo"
                        } else {
                            "Tipo ${index + 1}"
                        }

                        Text(
                            text       = textoCabecalho,
                            modifier   = Modifier.weight(1f),
                            textAlign  = TextAlign.Center,
                            style      = MaterialTheme.typography.labelLarge,
                            color      = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Linhas
                rows.forEachIndexed { rowIndex, (label, _) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text     = label,
                            modifier = Modifier.weight(columnWeightLabel),
                            style    = MaterialTheme.typography.bodyLarge,
                            color    = MaterialTheme.colorScheme.onSurface,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                        data.forEachIndexed { colIndex, item ->
                            val value     = item.toDisplayRows().getOrNull(rowIndex)?.second ?: 0f
                            val highlight = isDiscount && colIndex == 0
                            Text(
                                text       = "$value%",
                                modifier   = Modifier
                                    .weight(1f)
                                    .padding(vertical = 4.dp),
                                textAlign  = TextAlign.Center,
                                style      = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (highlight) FontWeight.Bold else FontWeight.Normal,
                                color      = if (highlight) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    if (rowIndex < rows.lastIndex) {
                        HorizontalDivider(
                            color     = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                            thickness = 1.dp
                        )
                    }
                }
            }
        }
    }
}