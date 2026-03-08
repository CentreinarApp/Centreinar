package com.example.centreinar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.centreinar.LimitSoja
import com.example.centreinar.data.local.entity.LimitMilho

@Composable
fun OfficialLimitsTable(grain: String, group: Int, data: List<Any>) {
    val labels = if (grain == "Soja") {
        listOf("Ardidos/Queim.", "Queimados", "Mofados", "Avariados Total", "Esverdeados", "Partidos/Quebr./Amassados", "Matérias Estranhas e Impurezas")
    } else {
        listOf("Ardidos", "Avariados Total", "Quebrados", "Matérias Estranhas e Impurezas", "Carunchados")
    }

    val columnWeightLabel = 1.0f

    Card(
        modifier = Modifier.fillMaxSize(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text = "Defeito",
                    modifier = Modifier.weight(columnWeightLabel),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Start
                )

                data.forEachIndexed { index, _ ->
                    val textoCabecalho = if (group == 2) {
                        "Padrão Básico"
                    } else if (group == 1 && ((index + 1) == 4)) {
                        "Fora de Tipo"
                    } else {
                        "Tipo ${index + 1}"
                    }

                    Text(
                        text = textoCabecalho,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            labels.forEachIndexed { rowIndex, label ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.weight(columnWeightLabel),
                        style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    data.forEach { item ->
                        val value = when (item) {
                            is LimitSoja -> listOf(item.burntOrSourUpLim, item.burntUpLim, item.moldyUpLim, item.spoiledTotalUpLim, item.greenishUpLim, item.brokenCrackedDamagedUpLim, item.impuritiesUpLim)
                            is LimitMilho -> listOf(item.ardidoUpLim, item.spoiledTotalUpLim, item.brokenUpLim, item.impuritiesUpLim, item.carunchadoUpLim)
                            else -> emptyList()
                        }.getOrNull(rowIndex) ?: 0f

                        Text(
                            text = "$value%",
                            modifier = Modifier.weight(columnWeightLabel),
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (rowIndex < labels.lastIndex) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
                }
            }
        }
    }
}

@Composable
fun EditableFields(
    isSoja: Boolean,
    impurities: String, onImpuritiesChange: (String) -> Unit,
    burnt: String, onBurntChange: (String) -> Unit,
    burntOrSour: String, onBurntOrSourChange: (String) -> Unit,
    moldy: String, onMoldyChange: (String) -> Unit,
    spoiled: String, onSpoiledChange: (String) -> Unit,
    greenish: String, onGreenishChange: (String) -> Unit,
    broken: String, onBrokenChange: (String) -> Unit,
    carunchados: String, onCarunchadoChange: (String) -> Unit
) {
    Column {
        NumberInputField("Matérias Estranhas e Impurezas (%)", impurities, onImpuritiesChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))
        if (isSoja) {
            NumberInputField("Queimados (%)", burnt, onBurntChange, FocusRequester(), null, true)
            Spacer(Modifier.height(8.dp))
        }
        NumberInputField(if (isSoja) "Ardidos e Queimados (%)" else "Ardidos (%)", burntOrSour, onBurntOrSourChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))
        NumberInputField("Mofados (%)", moldy, onMoldyChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))
        NumberInputField("Total Avariados (%)", spoiled, onSpoiledChange, FocusRequester(), null, true)
        Spacer(Modifier.height(8.dp))
        if (isSoja) {
            NumberInputField("Esverdeados (%)", greenish, onGreenishChange, FocusRequester(), null, true)
            Spacer(Modifier.height(8.dp))
        }
        NumberInputField("Quebrados (%)", broken, onBrokenChange, FocusRequester(), null, true)
        if (!isSoja) {
            Spacer(Modifier.height(8.dp))
            NumberInputField("Carunchados (%)", carunchados, onCarunchadoChange, FocusRequester(), null, true)
        }
    }
}

@Composable
fun NumberInputField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    focusRequester: FocusRequester,
    nextFocus: FocusRequester?,
    enabled: Boolean,
    readOnly: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = { if (it.isEmpty() || it.matches(Regex("^(\\d*\\.?\\d*)$"))) onValueChange(it) },
        label = { Text(label) },
        modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = if (nextFocus != null) ImeAction.Next else ImeAction.Done),
        singleLine = true,
        enabled = enabled,
        readOnly = readOnly
    )
}