package com.example.centreinar.ui.main.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.centreinar.domain.model.GrainDescriptor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrainSelectionScreen(
    grainDescriptors: List<GrainDescriptor>,
    onGrainSelected: (GrainDescriptor) -> Unit,
    topBar: @Composable () -> Unit,
    description: String,
    callToActionText: String
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700

    Scaffold(
        modifier = Modifier.fillMaxSize(),
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // TopBar
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                topBar()
            }

            // Seção de conteúdo
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .padding(horizontal = 22.dp)
                    .padding(top = 22.dp, bottom = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween // Distribui proporcionalmente
            ) {
                // Bloco de texto (description + callToAction)
                Column(
                    modifier = Modifier.weight(1f), // Ocupa espaço proporcional
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    // Bloco de texto
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Text(
                            text = description,
                            textAlign = TextAlign.Justify,
                            style = if (isSmallScreen)
                                MaterialTheme.typography.bodySmall  // Menor em telas pequenas
                            else
                                MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                // Bloco dos botões
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(
                        if (isSmallScreen) 8.dp else 12.dp,
                        Alignment.Top
                    )
                ) {
                    Text(
                        text = callToActionText,
                        textAlign = TextAlign.Center,
                        style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge
                    )

                    grainDescriptors.forEach { descriptor ->
                        OutlinedButton(
                            onClick = { onGrainSelected(descriptor) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(
                                    min = if (isSmallScreen) 56.dp else 40.dp,
                                    max = if (isSmallScreen) 72.dp else 120.dp
                                ),
                            contentPadding = PaddingValues(vertical = 16.dp, horizontal = 16.dp),
                            border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                            shape = RoundedCornerShape(20.dp),
                        ) {
                            Text(
                                text = descriptor.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.onBackground,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}