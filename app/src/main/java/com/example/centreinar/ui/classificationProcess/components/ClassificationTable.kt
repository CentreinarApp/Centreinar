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
import com.example.centreinar.ClassificationMilho
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.LimitMilho

// Classe auxiliar para padronizar os dados antes de enviar para o layout compartilhado
private data class ClassificationRowData(
    val label: String,
    val limit: Float,
    val percentage: Float,
    val typeCode: Int // Se 0, exibe "-"
)

// VERSÃO PÚBLICA PARA SOJA
@Composable
fun ClassificationTable(
    classification: ClassificationSoja,
    typeTranslator: (Int) -> String,
    limits: LimitSoja,
    modifier: Modifier = Modifier
) {
    // --- LISTA DE DEFEITOS ---
    val rows = listOf(
        ClassificationRowData(
            "Matéria Estranha/Imp",
            limits.impuritiesUpLim,
            classification.foreignMattersPercentage,
            classification.foreignMatters
        ),
        ClassificationRowData(
            "Esverdeados",
            limits.greenishUpLim,
            classification.greenishPercentage,
            classification.greenish
        ),
        ClassificationRowData(
            "Partidos/Quebrados",
            limits.brokenCrackedDamagedUpLim,
            classification.brokenCrackedDamagedPercentage,
            classification.brokenCrackedDamaged
        ),
        ClassificationRowData(
            "Mofados",
            limits.moldyUpLim,
            classification.moldyPercentage,
            classification.moldy
        ),
        ClassificationRowData(
            "Queimados",
            limits.burntUpLim,
            classification.burntPercentage,
            classification.burnt
        ),
        ClassificationRowData(
            "Queimados/Ardidos (Soma)",
            limits.burntOrSourUpLim,
            classification.burntOrSourPercentage,
            classification.burntOrSour
        ),
        ClassificationRowData(
            "Total de Avariados",
            limits.spoiledTotalUpLim,
            classification.spoiledPercentage,
            classification.spoiled
        ),

        // Defeitos que NÃO classificam (Onde deve aparecer o "-")
        ClassificationRowData(
            "Ardidos",
            0f,
            classification.sourPercentage,
            0
        ),
        ClassificationRowData(
            "Fermentados",
            0f,
            classification.fermentedPercentage,
            0
        ),
        ClassificationRowData(
            "Germinados",
            0f,
            classification.germinatedPercentage,
            0
        ),
        ClassificationRowData(
            "Imaturos",
            0f,
            classification.immaturePercentage,
            0
        ),
        ClassificationRowData(
            "Chochos",
            0f,
            classification.shriveledPercentage,
            0
        ),
        ClassificationRowData(
            "Danificados",
            0f,
            classification.damagedPercentage,
            0
        )
    )

    SharedTableLayout(
        title = "RESULTADO SOJA",
        rows = rows,
        finalTypeLabel = typeTranslator(classification.finalType),
        typeTranslator = typeTranslator,
        modifier = modifier
    )
}

// VERSÃO PÚBLICA PARA MILHO
@Composable
fun ClassificationTable(
    classification: ClassificationMilho,
    limits: LimitMilho,
    typeTranslator: (Int) -> String,
    modifier: Modifier = Modifier
) {
    // Cálculo visual do total para exibir na tabela (Float)
    val totalAvariados = classification.ardidoPercentage + classification.mofadoPercentage +
            classification.carunchadoPercentage + classification.fermentedPercentage +
            classification.germinatedPercentage + classification.immaturePercentage +
            classification.gessadoPercentage

    val rows = listOf(
        ClassificationRowData(
            "Umidade",
            limits.moistureUpLim,
            classification.moisturePercentage,
            0
        ),
        ClassificationRowData(
            "Matéria Estranha/Imp",
            limits.impuritiesUpLim,
            classification.impuritiesPercentage,
            classification.impuritiesType
        ),
        ClassificationRowData(
            "Quebrados",
            limits.brokenUpLim,
            classification.brokenPercentage,
            classification.brokenType
        ),
        ClassificationRowData(
            "Total Avariados",
            limits.spoiledTotalUpLim,
            classification.spoiledTotalPercentage,
            classification.spoiledTotalType
        ),
        ClassificationRowData(
            "Ardidos",
            limits.ardidoUpLim,
            classification.ardidoPercentage,
            classification.ardidoType
        ),
        ClassificationRowData(
            "Carunchados",
            limits.carunchadoUpLim,
            classification.carunchadoPercentage,
            classification.carunchadoType
        ),
        ClassificationRowData(
            "Mofados",
            limits.mofadoUpLim,
            classification.mofadoPercentage,
            0
        ),
        ClassificationRowData(
            "Fermentados",
            0f,
            classification.fermentedPercentage,
            0
        ),
        ClassificationRowData(
            "Germinados",
            0f,
            classification.germinatedPercentage,
            0
        ),
        ClassificationRowData(
            "Chochos/Imaturos",
            0f,
            classification.immaturePercentage,
            0
        ),
        ClassificationRowData(
            "Gessados",
            0f,
            classification.gessadoPercentage,
            0
        )
    )

    SharedTableLayout(
        title = "RESULTADO MILHO",
        rows = rows,
        finalTypeLabel = typeTranslator(classification.finalType),
        typeTranslator = typeTranslator,
        modifier = modifier
    )
}

// LAYOUT COMPARTILHADO
@Composable
private fun SharedTableLayout(
    title: String,
    rows: List<ClassificationRowData>,
    finalTypeLabel: String,
    typeTranslator: (Int) -> String,
    modifier: Modifier
) {
    Card(
        modifier = modifier.padding(16.dp).fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            TableHeader(title)
            Column(
                modifier = Modifier.fillMaxWidth().border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primaryContainer).padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("DEFEITO", fontSize = 13.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f))
                    Text("LIMITE (%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("%", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("TIPO", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
                rows.forEachIndexed { index, row ->
                    TableRow(
                        label = row.label,
                        limit = row.limit,
                        percentage = row.percentage,
                        quantity = if (row.typeCode == 0) "-" else typeTranslator(row.typeCode),
                        isLast = index == rows.size - 1
                    )
                }
            }
            Spacer(Modifier.height(16.dp))
            FinalTypeRow(value = finalTypeLabel)
        }
    }
}

@Composable
private fun TableRow(label: String, limit: Float, percentage: Float, quantity: String, isLast: Boolean = false) {
    val isExceeded = limit > 0f && percentage > limit
    val textColor = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val weightFont = if (isExceeded) FontWeight.Bold else FontWeight.Normal

    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 13.sp, modifier = Modifier.weight(2f), fontWeight = weightFont)
        Text(text = if (limit > 0f) "%.2f%%".format(limit) else "-", fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = "%.2f%%".format(percentage), fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = textColor, fontWeight = weightFont)
        Text(text = quantity, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
    if (!isLast) Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
}

@Composable
private fun FinalTypeRow(value: String) {
    Row(modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.secondaryContainer).padding(12.dp), horizontalArrangement = Arrangement.Center) {
        Text(text = "Tipo Final: $value", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
private fun TableHeader(title: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
}