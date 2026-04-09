package com.example.centreinar.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ActionButtons(
    onBack: () -> Unit,
    onPrimaryAction: () -> Unit,
    primaryActionText: String = "Nova Análise",
    onExportPdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // PRIMEIRA LINHA: Dois botões lado a lado
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onBack,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(5.dp)
            ) {
                Text("Voltar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onPrimaryAction, // Chama a ação definida pela tela
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(5.dp)
            ) {
                // Mostra o texto que a tela pediu
                Text(primaryActionText, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        // SEGUNDA LINHA: Botão PDF sozinho embaixo
        Button(
            onClick = onExportPdf,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp)
        ) {
            Text("Exportar PDF", fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}