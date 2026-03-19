package com.example.centreinar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Tabela genérica para exibir dados de entrada (amostra ou desconto).
 *
 * Usada em:
 * - ClassificationResultScreen: pesos e defeitos em gramas da amostra
 * - DiscountResultsScreen: defeitos em % e dados financeiros usados no cálculo
 *
 * Recebe List<Pair<String, String>> para ser completamente agnóstica ao domínio.
 * Cada strategy/ViewModel é responsável por formatar os valores com a unidade correta.
 */
@Composable
fun InputDataTable(
    title: String,
    rows: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    if (rows.isEmpty()) return

    Card(
        modifier = modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text       = title,
                style      = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary,
                modifier   = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                textAlign  = TextAlign.Center
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                // Cabeçalho
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 4.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text       = "Defeito",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier   = Modifier.weight(2f)
                    )
                    Text(
                        text       = "Valor",
                        style      = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color      = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.End
                    )
                }

                rows.forEachIndexed { index, (label, value) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp, horizontal = 8.dp),
                        verticalAlignment          = Alignment.CenterVertically,
                        horizontalArrangement      = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text     = label,
                            style    = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.weight(2f),
                            color    = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text      = value,
                            style     = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            modifier  = Modifier.weight(1f),
                            textAlign = TextAlign.End,
                            color     = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (index < rows.lastIndex) {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                        )
                    }
                }
            }
        }
    }
}