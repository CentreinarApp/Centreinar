package com.example.centreinar.ui.classificationProcess.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.centreinar.ClassificationSoja

// Estrutura de Limites
data class LimitSoja(
    val id: Int = 0,
    val impuritiesUpLim: Float = 1.0f,
    val burntUpLim: Float = 0.8f,
    val sourUpLim: Float = 1.0f,
    val moldyUpLim: Float = 2.0f,
    val spoiledUpLim: Float = 8.0f,
    val greenishUpLim: Float = 8.0f,
    val brokenCrackedDamagedUpLim: Float = 30.0f
)

// Quadruple auxiliar
private data class Quadruple<out A, out B, out C, out D>(
    val first: A, val second: B, val third: C, val fourth: D
)

@Composable
fun ClassificationTable(
    classification: ClassificationSoja,
    typeTranslator: (Int) -> String,
    limits: LimitSoja,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(16.dp)
            .fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {

        // 🔥 SCROLL ADICIONADO AQUI!
        Column(
            modifier = Modifier
                .padding(16.dp)
                .verticalScroll(rememberScrollState()) // <<<< AQUI
        ) {

            TableHeader("RESULTADO DA CLASSIFICAÇÃO")

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                // Cabeçalho das colunas
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(vertical = 8.dp, horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        "DEFEITO",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(2f)
                    )
                    Text(
                        "LIMITE (%)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        "%",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                    Text(
                        "TIPO",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.End
                    )
                }

                // Linhas da tabela
                listOf(
                    Quadruple("Ardidos", limits.sourUpLim, classification.sourPercentage, classification.sour),

                    Quadruple("Matéria Estranha e Impurezas", limits.impuritiesUpLim, classification.foreignMattersPercentage, classification.foreignMatters),
                    Quadruple("Queimados", limits.burntUpLim, classification.burntPercentage, classification.burnt),
                    Quadruple("Ardidos e Queimados (Soma)", limits.burntUpLim + limits.sourUpLim, classification.burntOrSourPercentage, classification.burntOrSour),
                    Quadruple("Mofados", limits.moldyUpLim, classification.moldyPercentage, classification.moldy),
                    Quadruple("Total de Avariados", limits.spoiledUpLim, classification.spoiledPercentage, classification.spoiled),
                    Quadruple("Esverdeados", limits.greenishUpLim, classification.greenishPercentage, classification.greenish),
                    Quadruple("Partidos/Quebrados/Amassados", limits.brokenCrackedDamagedUpLim, classification.brokenCrackedDamagedPercentage, classification.brokenCrackedDamaged),

                    Quadruple("Fermentados", 0f, classification.fermentedPercentage, classification.fermented),
                    Quadruple("Germinados", 0f, classification.germinatedPercentage, classification.germinated),
                    Quadruple("Imaturos", 0f, classification.immaturePercentage, classification.immature),
                    Quadruple("Chochos", 0f, classification.shriveledPercentage, classification.shriveled),

                    Quadruple("Fermentados", 0f, classification.fermentedPercentage, classification.fermented),
                ).forEachIndexed { index, (label, limit, percentage, typeCode) ->
                    TableRow(
                        label = label,
                        limit = limit,
                        percentage = percentage,
                        quantity = typeTranslator(typeCode),
                        isLast = index == 13
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            FinalTypeRow(
                value = typeTranslator(classification.finalType)
            )
        }
    }
}

@Composable
private fun TableRow(
    label: String,
    limit: Float,
    percentage: Float,
    quantity: String,
    isLast: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            modifier = Modifier.weight(2f)
        )

        Text(
            text = if (limit > 0f) "%.2f%%".format(limit) else "-",
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        Text(
            text = "%.2f%%".format(percentage),
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
        )

        Text(
            text = quantity,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.End
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
            .padding(12.dp),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Tipo Final: $value",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
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
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
    }
    Divider(
        modifier = Modifier.padding(vertical = 8.dp),
        color = MaterialTheme.colorScheme.outline
    )
}
