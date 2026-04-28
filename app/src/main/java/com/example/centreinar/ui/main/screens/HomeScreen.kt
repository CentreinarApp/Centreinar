package com.example.centreinar.ui.main.screens

import android.R.attr.textStyle
import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.example.centreinar.R
import com.example.centreinar.ui.classificationProcess.viewmodel.ClassificationViewModel
import com.example.centreinar.util.Routes
import kotlinx.coroutines.launch

private const val PREFS_NAME = "centreinar_prefs"
private const val KEY_TERMS_ACCEPTED = "terms_accepted"

@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: ClassificationViewModel = hiltViewModel()
) {
    val configuration = LocalConfiguration.current
    val isSmallScreen = configuration.screenHeightDp < 700

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var showTermsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val accepted = prefs.getBoolean(KEY_TERMS_ACCEPTED, false)
        if (!accepted) {
            showTermsDialog = true
        }
    }

    if (showTermsDialog) {
        TermsOfUseDialog(
            onAccept = {
                scope.launch {
                    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                    prefs.edit().putBoolean(KEY_TERMS_ACCEPTED, true).apply()
                    showTermsDialog = false
                }
            }
        )
    }

    val headlineMedium = MaterialTheme.typography.headlineMedium
    val headlineSmall = MaterialTheme.typography.headlineSmall
    var textStyle by remember { mutableStateOf(headlineMedium) }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            Column(
                modifier = Modifier.navigationBarsPadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
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
                Text(
                    text = "APLICATIVO EM FASE DE TESTE (BETA)",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 5.dp, bottom = 10.dp)
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
fun TermsOfUseDialog(
    onAccept: () -> Unit
) {
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = { /* Não permite fechar sem aceitar */ },
        title = {
            Text(
                text = "Termos de Uso",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                Text(
                    text = ("ATENÇÃO: FAVOR NÃO REPASSAR O ARQUIVO .APK DE INSTALAÇÃO PARA TERCEIROS.\n\nO APP ESTÁ EM FASE DE TESTE, PODENDO CONTER ALGUNS ERR0S QUE SERÃO CORRIGIDOS NO APLICATIVO OFICIAL.").trimIndent(),
                    style = MaterialTheme.typography.titleSmall,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Por favor, leia e aceite os Termos de Uso para continuar.",
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Justify,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 200.dp, max = 320.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .verticalScroll(scrollState)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = stringResource(R.string.use_term_app).trimIndent(),
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Justify
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424C91)),
            ) {
                Text("Aceitar e Continuar")
            }
        }
    )
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