package com.example.centreinar.ui.classificationProcess.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.sp
import com.example.centreinar.ui.classificationProcess.strategy.ClassificationRow

@Composable
fun ClassificationTable(
    modifier: Modifier = Modifier,
    title: String,
    finalTypeLabel: String,
    rows: List<ClassificationRow>,
    typeTranslator: (Int) -> String
) {
    // Filtramos os itens Fora de Tipo (código 7)
    val outOfTypeItems = rows.filter { it.typeCode == 7 }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Cabeçalho da Tabela
            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            Divider(modifier = Modifier.padding(bottom = 8.dp), color = MaterialTheme.colorScheme.outline)

            // Corpo da Tabela
            Column(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DEFEITO", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                    Text("%", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("TIPO", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                rows.forEachIndexed { index, row ->
                    // Só destaca vermelho se tiver limite E ultrapassar ele
                    val isExceeded = row.limit > 0f && row.percentage > row.limit
                    val textColor = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    val weightFont = if (isExceeded) FontWeight.Bold else FontWeight.Normal
                    val quantityText = if (row.typeCode == 0) "-" else typeTranslator(row.typeCode)

                    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(text = row.label, fontSize = 13.sp, modifier = Modifier.weight(2f), fontWeight = weightFont)
                        // DICA: Se quiser, futuramente pode trocar esse "%.2f%%".format por aquela nossa extensão segura `.toUniversalString() + "%"`
                        Text(text = "%.2f%%".format(row.percentage), fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = textColor, fontWeight = weightFont)
                        Text(text = quantityText, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    }
                    if (index < rows.size - 1) {
                        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Rodapé com o Resultado Final
            Column(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Tipo Final: $finalTypeLabel",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (finalTypeLabel) {
                    "Fora de Tipo" -> {
                        if (outOfTypeItems.isNotEmpty()) {
                            Text("Fora de Tipo por:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            outOfTypeItems.forEach { item ->
                                Text("• ${item.label}: ${"%.2f".format(item.percentage)}%", color = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}