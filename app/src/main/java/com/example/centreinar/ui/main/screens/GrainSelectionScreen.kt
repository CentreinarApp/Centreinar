package com.example.centreinar.ui.main.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.centreinar.domain.model.GrainDescriptor

/**
 * Tela genérica de seleção de grão — reutilizada no fluxo de classificação
 * e no fluxo de desconto.
 *
 * Recebe apenas os dados e callbacks necessários — sem acoplamento a nenhum
 * ViewModel específico. Quem chama decide o que fazer ao selecionar um grão.
 *
 * Para adicionar um novo grão: crie a Strategy com o descriptor preenchido
 * e registre-a no módulo Hilt correspondente. Nenhuma alteração é necessária aqui.
 *
 * @param grainDescriptors Lista de descritores de grãos disponíveis
 * @param onGrainSelected  Callback chamado com o descriptor selecionado
 */
@Composable
fun GrainSelectionScreen(
    grainDescriptors: List<GrainDescriptor>,
    onGrainSelected: (GrainDescriptor) -> Unit
) {
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            grainDescriptors.forEachIndexed { index, descriptor ->
                if (index > 0) Spacer(modifier = Modifier.height(24.dp))

                val colors = when (descriptor.colorScheme) {
                    "secondary" -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    "tertiary"  -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    else        -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor   = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Button(
                    onClick  = { onGrainSelected(descriptor) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                        .padding(vertical = 8.dp),
                    colors = colors
                ) {
                    Text(descriptor.displayName, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}