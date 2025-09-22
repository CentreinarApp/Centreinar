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
import com.example.centreinar.ClassificationSoja

@Composable
fun ClassificationTable(
    classification: ClassificationSoja,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            TableHeader("RESULTADO DA CLASSIFICAÇÃO")

            // Main table with 3 columns
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                // Table Headers
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 12.dp, horizontal = 16.dp),
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
                        "%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        "TIPO",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }
                // Table Rows
                listOf(
                    Triple("Matéria Estranha e Impurezas", classification.foreignMattersPercentage, typeNumberToString(classification.foreignMatters)),
                    Triple("Queimados", classification.burntPercentage, typeNumberToString(classification.burnt)),
                    Triple("Ardidos e Queimados", classification.burntOrSourPercentage, typeNumberToString(classification.burntOrSour)),
                    Triple("Mofados", classification.moldyPercentage, typeNumberToString(classification.moldy)),
                    Triple("Fermentados",classification.fermentedPercentage,"-"),
                    Triple("Germinados",classification.germinatedPercentage,"-"),
                    Triple("Imaturos",classification.immaturePercentage,"-"),
                    Triple("Chochos",classification.shriveledPercentage,"-"),
                    Triple("Fermentados",classification.fermentedPercentage,"-"),
                    Triple("Total de Avariados", classification.spoiledPercentage, typeNumberToString(classification.spoiled)),
                    Triple("Esverdeados", classification.greenishPercentage, typeNumberToString(classification.greenish)),
                    Triple("Partidos, Quebrados e Amassados", classification.brokenCrackedDamagedPercentage, typeNumberToString(classification.brokenCrackedDamaged))
                ).forEachIndexed { index, (label, percentage, quantity) ->
                    TableRow(
                        label = label,
                        percentage = percentage,
                        quantity = quantity,
                        isLast = index == 6
                    )
                }
            }

            // Final Type
            Spacer(Modifier.height(16.dp))
            FinalTypeRow(
                value = typeNumberToString(classification.finalType)
            )
        }
    }
}

@Composable
private fun TableRow(
    label: String,
    percentage: Float,
    quantity: String,
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
            text = "%.2f%%".format(percentage),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = quantity.toString(),
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
private fun FinalTypeRow(value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(16.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tipo Final: $value",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSecondaryContainer
        )
    }
}
@Composable
private fun TableHeader(title: String) {
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
fun typeNumberToString(typeNum:Int):String{
    if(typeNum == 0){
        return "Desclassificado"
    }
    if(typeNum == 7){
        return "Fora de Tipo"
    }
    return typeNum.toString()
}