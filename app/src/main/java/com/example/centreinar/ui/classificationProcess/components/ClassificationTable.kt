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
import com.example.centreinar.DisqualificationSoja
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.dao.DisqualificationMilhoDao
import com.example.centreinar.data.local.entity.DisqualificationMilho
import com.example.centreinar.data.local.entity.LimitMilho

// Classe auxiliar para padronizar os dados antes de enviar para o layout compartilhado
private data class ClassificationRowData(
    val label: String,
    val limit: Float,
    val percentage: Float,
    val typeCode: Int, // Se 0, exibe "-"
    val shouldHighlight: Boolean = true // Por padrão, todos destacam
)

// Função auxiliar para pegar a desclassificação
private fun getDisqualificationMotives(
    badConservation: Int,
    strangeSmell: Int,
    insects: Int,
    toxicGrains: Int,
    graveDefectSum: Int = 0
): List<String> {
    return buildList {
        if (badConservation == 1) add("Mau estado de conservação")
        if (strangeSmell == 1) add("Odor estranho / impróprio")
        if (insects == 1) add("Presença de insetos vivos")
        if (toxicGrains == 1) add("Sementes tóxicas ou prejudiciais")
        if (graveDefectSum == 1) add("Excesso de defeitos graves")
    }
}

// VERSÃO PÚBLICA PARA SOJA
@Composable
fun ClassificationTable(
    classification: ClassificationSoja,
    disqualificationSoja: DisqualificationSoja,
    typeTranslator: (Int) -> String,
    limits: LimitSoja,
    modifier: Modifier = Modifier
) {
    // --- Lista de Desclassificação ---
    val disqualificationReasons = getDisqualificationMotives(
        badConservation = disqualificationSoja.badConservation,
        strangeSmell = disqualificationSoja.strangeSmell,
        insects = disqualificationSoja.insects,
        toxicGrains = disqualificationSoja.toxicGrains,
        graveDefectSum = disqualificationSoja.graveDefectSum
    )

    // --- LISTA DE DEFEITOS ---
    val rows = listOf(
        ClassificationRowData(
            "Matéria Estranha/Imp",
            limits.impuritiesUpLim,
            classification.impuritiesPercentage,
            classification.impuritiesType
        ),
        ClassificationRowData( // Não classifica => --
            "Ardidos",
            0f,
            classification.sourPercentage,
            0
        ),
        ClassificationRowData(
            "Queimados",
            limits.burntUpLim,
            classification.burntPercentage,
            classification.burntType
        ),
        ClassificationRowData(
            "Total de Ardidos + Queimados",
            limits.burntOrSourUpLim,
            classification.burntOrSourPercentage,
            classification.burntOrSourType
        ),
        ClassificationRowData(
            "Mofados",
            limits.moldyUpLim,
            classification.moldyPercentage,
            classification.moldyType
        ),
        ClassificationRowData( // Não classifica => --
            "Fermentados",
            0f,
            classification.fermentedPercentage,
            0
        ),
        ClassificationRowData( // Não classifica => --
            "Germinados",
            0f,
            classification.germinatedPercentage,
            0
        ),
        ClassificationRowData( // Não classifica => --
            "Imaturos",
            0f,
            classification.immaturePercentage,
            0
        ),
        ClassificationRowData( // Não classifica => --
            "Chochos",
            0f,
            classification.shriveledPercentage,
            0
        ),
        ClassificationRowData( // Não classifica => --
            "Danificados",
            0f,
            classification.damagedPercentage,
            0
        ),
        ClassificationRowData(
            "Total de Avariados",
            limits.spoiledTotalUpLim,
            classification.spoiledPercentage,
            classification.spoiledType
        ),
        ClassificationRowData(
            "Esverdeados",
            limits.greenishUpLim,
            classification.greenishPercentage,
            classification.greenishType
        ),
        ClassificationRowData(
            "Partidos/Quebrados",
            limits.brokenCrackedDamagedUpLim,
            classification.brokenCrackedDamagedPercentage,
            classification.brokenCrackedDamagedType
        )
    )

    SharedTableLayout(
        title = "RESULTADO SOJA",
        rows = rows,
        finalTypeLabel = typeTranslator(classification.finalType),
        disqualificationReasons = disqualificationReasons,
        typeTranslator = typeTranslator,
        modifier = modifier
    )
}

// VERSÃO PÚBLICA PARA MILHO
@Composable
fun ClassificationTable(
    classification: ClassificationMilho,
    disqualificationMilho: DisqualificationMilho,
    limits: LimitMilho,
    typeTranslator: (Int) -> String,
    modifier: Modifier = Modifier
) {
    // Cálculo visual do total para exibir na tabela (Float)
    val totalAvariados = classification.ardidoPercentage + classification.mofadoPercentage +
            classification.carunchadoPercentage + classification.fermentedPercentage +
            classification.germinatedPercentage + classification.immaturePercentage +
            classification.gessadoPercentage

    val disqualificationReasons = getDisqualificationMotives(
        badConservation = disqualificationMilho.badConservation,
        strangeSmell = disqualificationMilho.strangeSmell,
        insects = disqualificationMilho.insects,
        toxicGrains = disqualificationMilho.toxicGrains,
    )

    val rows = listOf(
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
            "Ardidos",
            limits.ardidoUpLim,
            classification.ardidoPercentage,
            classification.ardidoType
        ),
        ClassificationRowData(
            "Mofados",
            limits.mofadoUpLim,
            classification.mofadoPercentage,
            classification.mofadoType,
            false
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
        ),
        ClassificationRowData(
            "Total Avariados",
            limits.spoiledTotalUpLim,
            classification.spoiledTotalPercentage,
            classification.spoiledTotalType
        ),
        ClassificationRowData(
            "Carunchados",
            limits.carunchadoUpLim,
            classification.carunchadoPercentage,
            classification.carunchadoType
        ),
    )

    SharedTableLayout(
        title = "RESULTADO MILHO",
        rows = rows,
        finalTypeLabel = typeTranslator(classification.finalType),
        disqualificationReasons = disqualificationReasons,
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
    disqualificationReasons: List<String>,
    typeTranslator: (Int) -> String,
    modifier: Modifier
) {
    // Gerar a observação: filtrar itens onde typeCode == 7
    val outOfTypeItems = rows.filter { it.typeCode == 7 }

    Card(
        modifier = modifier.fillMaxWidth(),
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

                    // COLUNA LIMITE COMENTADA PARA NÃO EXIBIR
                    // Text("LIMITE (%)", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)

                    Text("%", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                    Text("TIPO", fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                }
                rows.forEachIndexed { index, row ->
                    TableRow(
                        label = row.label,
                        limit = row.limit,
                        percentage = row.percentage,
                        quantity = if (row.typeCode == 0) "-" else typeTranslator(row.typeCode),
                        isLast = index == rows.size - 1,
                        shouldHighlight = row.shouldHighlight
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            FinalTypeRow(
                finalType = finalTypeLabel,
                outOfTypeItems = outOfTypeItems,
                disqualificationReasons = disqualificationReasons
            )
        }
    }
}

@Composable
private fun TableRow(label: String, limit: Float, percentage: Float, quantity: String, isLast: Boolean = false, shouldHighlight: Boolean = true // Novo parâmetro
) {
    // A lógica agora só ativa o destaque se shouldHighlight for verdadeiro
    val isExceeded = shouldHighlight && limit > 0f && percentage > limit
    val textColor = if (isExceeded) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    val weightFont = if (isExceeded) FontWeight.Bold else FontWeight.Normal

    Row(modifier = Modifier.fillMaxWidth().padding(8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(text = label, fontSize = 13.sp, modifier = Modifier.weight(2f), fontWeight = weightFont)
        Text(text = "%.2f%%".format(percentage), fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End, color = textColor, fontWeight = weightFont)
        Text(text = quantity, fontSize = 13.sp, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
    }
    if (!isLast) Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), thickness = 1.dp)
}

@Composable
private fun FinalTypeRow(
    finalType: String,
    outOfTypeItems: List<ClassificationRowData>,
    disqualificationReasons: List<String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .padding(12.dp),
        horizontalAlignment = Alignment.Start // Alinhado à esquerda para listas
    ) {
        Text(
            text = "Tipo Final: $finalType",
            fontWeight = FontWeight.ExtraBold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSecondaryContainer,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        when (finalType) {
            "Fora de Tipo" -> {
                if (outOfTypeItems.isNotEmpty()) {
                    Text("Fora de Tipo por:", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    outOfTypeItems.forEach { item ->
                        Text("• ${item.label}: %.2f%%".format(item.percentage), color = MaterialTheme.colorScheme.error)
                    }
                }
            }
            "Desclassificada" -> {
                if (disqualificationReasons.isNotEmpty()) {
                    Text("Motivos da Desclassificação:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                    disqualificationReasons.forEach { reason ->
                        Text("• $reason", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Medium)
                    }
                } else {
                    Text("• Critérios de desclassificação atingidos.", color = MaterialTheme.colorScheme.error)
                }
            }
            else -> {
                Text(
                    text = "Padrão atendido conforme limites.",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun TableHeader(title: String) {
    Box(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
        Text(text = title, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
    }
    Divider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline)
}