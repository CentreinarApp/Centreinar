package com.example.centreinar.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

/**
 * Popup de informação genérico — título, descrição e botão de fechar.
 *
 * Uso:
 * ```
 * var showInfo by remember { mutableStateOf(false) }
 *
 * IconButton(onClick = { showInfo = true }) {
 *     Icon(Icons.Outlined.Info, contentDescription = "Informação")
 * }
 *
 * if (showInfo) {
 *     InfoDialog(
 *         title       = "O que é Quebra Técnica?",
 *         description = "Desconto aplicado por dias de armazenamento...",
 *         onDismiss   = { showInfo = false }
 *     )
 * }
 * ```
 */
@Composable
fun InfoDialog(
    title: String,
    description: String,
    onDismiss: () -> Unit,
    closeButtonLabel: String = "Entendi"
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape  = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text       = title,
                    textAlign = TextAlign.Center,
                    style      = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color      = MaterialTheme.colorScheme.onSurface
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    text     = description,
                    style    = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Justify,
                    color    = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(rememberScrollState())
                )

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick  = onDismiss,
                    modifier = Modifier.align(Alignment.End),
                    shape = RoundedCornerShape(5.dp)
                ) {
                    Text(closeButtonLabel)
                }
            }
        }
    }
}