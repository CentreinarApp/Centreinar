package com.example.centreinar.ui.main.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.R
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.util.Routes

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .height(70.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_centreinar),
                    contentDescription = "Logo do Centreinar",
                    modifier = Modifier.size(150.dp),
                    contentScale = ContentScale.Fit
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                // Título e Subtítulo
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    val headlineMedium = MaterialTheme.typography.headlineMedium
                    val headlineSmall = MaterialTheme.typography.headlineSmall
                    var textStyle by remember { mutableStateOf(headlineMedium) }

                    Text(
                        text = "Olá, seja bem vindo! \uD83D\uDC4B\uD83C\uDF31",
                        style = textStyle,
                        textAlign = TextAlign.Start,
                        maxLines = 1,
                        overflow = TextOverflow.Clip,
                        onTextLayout = { result ->
                            if (result.hasVisualOverflow) {
                                textStyle = headlineSmall
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "Utilize os cards abaixo para navegar.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }

                // Coluna dos Cards
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f), // A coluna ainda ocupa o espaço central
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Top
                ) {
                    FeatureCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false) // O 'fill = false' impede que ele estique obrigatoriamente
                            .heightIn(min = 150.dp, max = 220.dp), // Define um teto para telas grandes
                        title = "Classificação de Grãos",
                        subtitle = "Sua Ferramenta Essencial para o Controle de Qualidade.",
                        buttonText = "Realizar Classificação",
                        backgroundResId = R.drawable.classification_card_background,
                        onClick = { navController.navigate(Routes.GRAIN_SELECTION) }
                    )

                    Spacer(modifier = Modifier.height(16.dp)) // Espaçamento entre cards

                    FeatureCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .heightIn(min = 150.dp, max = 220.dp),
                        title = "Cálculo de Descontos",
                        subtitle = "Transparência no Valor do Seu Grão.",
                        buttonText = "Realizar Cálculo",
                        backgroundResId = R.drawable.discount_card_background,
                        onClick = { navController.navigate(Routes.GRAIN_SELECTION_DISCOUNT) }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FeatureCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    buttonText: String,
    backgroundResId: Int,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(id = backgroundResId),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f)))

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Bloco superior: título + subtítulo juntos
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = title,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        maxLines = 1
                    )
                    Text(
                        text = subtitle,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth(0.9f),
                        maxLines = 2
                    )
                }
                Spacer(modifier = Modifier.height(5.dp))

                // Botão ancorado na base do card
                Button(
                    onClick = onClick,
                    modifier = Modifier.heightIn(min = 36.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424C91))
                ) {
                    Text(text = buttonText, style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}