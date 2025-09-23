package com.example.centreinar.ui.milho.components

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
import com.example.centreinar.data.local.entity.DiscountMilho

@Composable
fun MilhoDiscountResultsTable(
    discounts: DiscountMilho,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.padding(12.dp).fillMaxWidth(), shape = MaterialTheme.shapes.medium) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Descontos — Milho", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))

            Column(modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)) {
                Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tipo", modifier = Modifier.weight(2f), fontWeight = FontWeight.Bold)
                    Text("Kg", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                    Text("R$", modifier = Modifier.weight(1f), textAlign = TextAlign.End, fontWeight = FontWeight.Bold)
                }

                MilhoDiscountRow("Impurezas", discounts.impuritiesLoss, discounts.impuritiesLoss) // price placeholder
                MilhoDiscountRow("Quebra técnica", discounts.technicalLoss, discounts.technicalLoss)
                MilhoDiscountRow("Quebra por Partidos", discounts.brokenLoss, discounts.brokenLoss)
                MilhoDiscountRow("Quebra Ardidos", discounts.ardidoLoss, discounts.ardidoLoss)
                MilhoDiscountRow("Quebra Mofados", discounts.mofadoLoss, discounts.mofadoLoss)
                MilhoDiscountRow("Quebra Carunchado", discounts.carunchadoLoss, discounts.carunchadoLoss)
                MilhoDiscountRow("Peso final do lote (kg)", discounts.finalWeight, discounts.finalWeight)
            }
        }
    }
}

@Composable
private fun MilhoDiscountRow(label:String, kg:Float, price:Float) {
    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, modifier = Modifier.weight(2f))
        Text("%.2f".format(kg), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
        Text("%.2f".format(price), modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
    Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
}
