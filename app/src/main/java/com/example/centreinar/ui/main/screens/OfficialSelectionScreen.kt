package com.example.centreinar.ui.main.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.centreinar.domain.model.GrainDescriptor

@Composable
fun OfficialSelectionScreen(
    titleOfficial: String,
    titleCustom: String,
    grainDescriptor: GrainDescriptor,
    onOptionSelected: (isOfficial: Boolean) -> Unit,
    topBar: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700

    Scaffold(
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = innerPadding.calculateBottomPadding()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(modifier = Modifier.fillMaxWidth()) { topBar() }

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(if (isSmallScreen) 12.dp else 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    if (isSmallScreen) 16.dp else 30.dp,
                    Alignment.Top
                )
            ) {
                Text(
                    text = "Escolha a referência dos limites de tolerância para definição de Tipo:",
                    style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                OutlinedButton(
                    onClick = { onOptionSelected(true) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = if (isSmallScreen) 56.dp else 60.dp,
                            max = if (isSmallScreen) 70.dp else 100.dp
                        ),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = titleOfficial,
                            style = if (isSmallScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = grainDescriptor.limitRegulation,
                            style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onOptionSelected(false) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = if (isSmallScreen) 56.dp else 60.dp,
                            max = if (isSmallScreen) 70.dp else 100.dp
                        ),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = titleCustom,
                            style = if (isSmallScreen) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Conforme o contrato comercial",
                            style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }
            }
        }
    }
}