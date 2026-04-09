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

@Composable
fun GroupSelectionScreen(
    onGroupSelected: (group: Int) -> Unit,
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
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                topBar()
            }

            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = if (isSmallScreen) 12.dp else 22.dp)
                    .padding(
                        top = if (isSmallScreen) 12.dp else 22.dp,
                        bottom = if (isSmallScreen) 12.dp else 22.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(
                    if (isSmallScreen) 12.dp else 20.dp,
                    Alignment.Top
                )
            ) {
                Text(
                    text = "Selecione o grupo conforme a finalidade de uso:",
                    style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                    textAlign = TextAlign.Center
                )

                OutlinedButton(
                    onClick = { onGroupSelected(1) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = if (isSmallScreen) 72.dp else 100.dp,
                            max = if (isSmallScreen) 100.dp else 140.dp
                        ),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Grupo I",
                            style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(if (isSmallScreen) 2.dp else 6.dp))
                        Text(
                            text = "Soja destinada ao consumo \n(in Natura)",
                            style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }

                OutlinedButton(
                    onClick = { onGroupSelected(2) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(
                            min = if (isSmallScreen) 72.dp else 100.dp,
                            max = if (isSmallScreen) 100.dp else 140.dp
                        ),
                    border = BorderStroke(width = 1.dp, color = MaterialTheme.colorScheme.primary),
                    shape = RoundedCornerShape(20.dp),
                    contentPadding = PaddingValues(vertical = 16.dp, horizontal = 12.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "Grupo II",
                            style = if (isSmallScreen) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(if (isSmallScreen) 2.dp else 6.dp))
                        Text(
                            text = "Soja destinada a outros usos \n(armazenamento, moagem, etc.)",
                            style = if (isSmallScreen) MaterialTheme.typography.bodyMedium else MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                    }
                }
            }
        }
    }
}