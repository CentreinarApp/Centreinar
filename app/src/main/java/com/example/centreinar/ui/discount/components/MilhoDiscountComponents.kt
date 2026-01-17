package com.example.centreinar.ui.milho.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.example.centreinar.data.local.entity.DiscountMilho

@Composable
fun MilhoDiscountResultsTable(
    discounts: DiscountMilho,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(6.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(6.dp)) {

            TableHeaderMilho("DESCONTOS")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {

                // Cabeçalho da tabela
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 5.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "DEFEITO",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        "Quantia (kg)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        "Valor (R$)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                listOf(
                    Triple("Matéria Estranha e Impurezas", discounts.impuritiesLoss, discounts.impuritiesLossPrice),
                    Triple("Umidade", discounts.humidityLoss, discounts.humidityLossPrice),
                    Triple("Quebra Técnica", discounts.technicalLoss, discounts.technicalLossPrice),
                    Triple("Quebrados", discounts.brokenLoss, discounts.brokenLossPrice),
                    Triple("Ardidos", discounts.ardidoLoss, discounts.ardidoLossPrice),
                    Triple("Carunchados", discounts.carunchadoLoss, discounts.carunchadoLossPrice),
                    Triple("Total de Avariados", discounts.spoiledLoss, discounts.spoiledLossPrice)
                ).forEachIndexed { index, (label, mass, price) ->
                    TableRowMilho(
                        label = label,
                        mass = mass,
                        price = price,
                        isLast = index == 7
                    )
                }
            }
        }
    }
}

@Composable
private fun TableRowMilho(
    label: String,
    mass: Float,
    price: Float,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(2f),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "%.2f".format(mass),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "%.2f".format(price),
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

@Composable
private fun TableHeaderMilho(title: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Divider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outline
    )
}
