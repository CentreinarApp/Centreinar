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
import com.example.centreinar.ClassificationMilho

@Composable
fun MilhoClassificationTable(
    classification: ClassificationMilho,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.padding(12.dp).fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Resultado da Classificação — Milho", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Defeito", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                    Text("%", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("Tipo", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }

                // rows
                MilhoTableRow("Matéria Estranha e Impurezas", classification.impuritiesPercentage, "-")
                MilhoTableRow("Partidos/Quebrados", classification.brokenPercentage, "-")
                MilhoTableRow("Ardidos", classification.ardidoPercentage, "-")
                MilhoTableRow("Mofados", classification.mofadoPercentage, "-")
                MilhoTableRow("Carunchado", classification.carunchadoPercentage, "-")
                MilhoTableRow("Germinados", classification.germinatedPercentage, "-")
                MilhoTableRow("Imaturos", classification.immaturePercentage, "-")

            }

            Spacer(Modifier.height(12.dp))
            Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp), horizontalArrangement = Arrangement.Center) {
                Text("Tipo Final: ${typeNumberToString(classification.finalType)}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun MilhoTableRow(label: String, percentage: Float, type: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(2f))
        Text("%.2f%%".format(percentage), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text(type, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
}

private fun typeNumberToString(typeNum:Int):String{
    return when(typeNum){
        0 -> "Desclassificado"
        7 -> "Fora de Tipo"
        else -> typeNum.toString()
    }
}
