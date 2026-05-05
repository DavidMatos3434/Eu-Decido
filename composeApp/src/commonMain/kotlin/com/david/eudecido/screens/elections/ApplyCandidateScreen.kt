package com.david.eudecido.screens.elections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.core.parameter.parametersOf

class ApplyCandidateScreen(private val electionId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<ApplyCandidateScreenModel> { parametersOf(electionId) }
        val step by screenModel.step.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Candidatura") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Text("<", color = Color.White)
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                when (val currentStep = step) {
                    is ApplyStep.Info -> {
                        Text(text = "Candidatura a Representante", style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Para te candidatares a um cargo público, o sistema exige uma Identidade Forte verificada pelo Estado Português.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body1
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { screenModel.startVerification() },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Iniciar Verificação com Chave Móvel Digital")
                        }
                    }
                    is ApplyStep.Verifying -> {
                        Text(text = "Autenticação CMD", style = MaterialTheme.typography.h6)
                        Spacer(modifier = Modifier.height(16.dp))
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "A aguardar confirmação no teu telemóvel...", color = Color.Gray)
                    }
                    is ApplyStep.Success -> {
                        Text(text = "✅ Identidade Verificada", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "A tua candidatura foi submetida com sucesso e está agora em fase de validação pelo Agente de Integridade.",
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { navigator.popUntilRoot() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Voltar ao Início")
                        }
                    }
                    is ApplyStep.Error -> {
                        Text(text = "❌ Erro", color = Color.Red, fontWeight = FontWeight.Bold)
                        Text(text = currentStep.message)
                        Button(onClick = { screenModel.startVerification() }) {
                            Text("Tentar novamente")
                        }
                    }
                }
            }
        }
    }
}
