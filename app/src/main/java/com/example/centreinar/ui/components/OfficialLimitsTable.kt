package com.example.centreinar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.centreinar.ui.classificationProcess.strategy.BaseLimit

// =============================================================================
// TABELA DE LIMITES OFICIAIS
// =============================================================================

@Composable
fun OfficialLimitsTable(
    group: Int,
    data: List<BaseLimit>
) {
    if (data.isEmpty()) return

    val firstLimit = data.first()
    val rowLabels  = firstLimit.toDisplayRows().map { it.first }

    Card(
        modifier = Modifier.fillMaxSize(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier            = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Text(
                    text       = "Defeito",
                    modifier   = Modifier.weight(1f),
                    style      = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign  = TextAlign.Start
                )
                data.forEachIndexed { index, _ ->
                    val header = when {
                        group == 2                      -> "Padrão Básico"
                        group == 1 && (index + 1) == 4 -> "Fora de Tipo"
                        else                            -> "Tipo ${index + 1}"
                    }
                    Text(
                        text       = header,
                        modifier   = Modifier.weight(1f),
                        textAlign  = TextAlign.Center,
                        style      = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

            rowLabels.forEachIndexed { rowIndex, label ->
                Row(
                    modifier              = Modifier.fillMaxWidth().weight(1f),
                    verticalAlignment     = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Text(
                        text     = label,
                        modifier = Modifier.weight(1f),
                        style    = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    data.forEach { limit ->
                        val value = limit.toDisplayRows().getOrNull(rowIndex)?.second ?: 0f
                        Text(
                            text       = "$value%",
                            modifier   = Modifier.weight(1f),
                            textAlign  = TextAlign.Center,
                            style      = MaterialTheme.typography.bodyLarge.copy(fontSize = 14.sp),
                            fontWeight = FontWeight.SemiBold,
                            color      = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                if (rowIndex < rowLabels.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color     = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// =============================================================================
// CAMPO NUMÉRICO COMPARTILHADO
// =============================================================================

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
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager       = LocalFocusManager.current
    OutlinedTextField(
        value         = value,
        onValueChange = { raw ->
            val sanitized = raw.replace(',', '.').filter { it.isDigit() || it == '.' }
            if (sanitized.count { it == '.' } <= 1) onValueChange(sanitized)
        },
        label         = { Text(label) },
        modifier      = Modifier.fillMaxWidth().focusRequester(focusRequester),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Decimal,
            imeAction    = if (nextFocus != null) ImeAction.Next else ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onNext = { nextFocus?.requestFocus() },
            onDone = { keyboardController?.hide(); focusManager.clearFocus() }
        ),
        singleLine = true,
        enabled    = enabled,
        readOnly   = readOnly
    )
}