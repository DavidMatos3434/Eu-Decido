package com.david.eudecido.screens.results

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import org.koin.core.parameter.parametersOf

class ResultsScreen(private val proposalId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<ResultsScreenModel> { parametersOf(proposalId) }
        val state by screenModel.state.collectAsState()

        when (val currentState = state) {
            is ResultsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is ResultsState.Success -> {
                ResultsContent(
                    title = currentState.title,
                    approved = currentState.isApproved,
                    yesPercent = currentState.yesPercent,
                    noPercent = currentState.noPercent,
                    abstentionPercent = currentState.abstentionPercent,
                    totalVotes = currentState.totalVotes.toInt(),
                    onBack = { navigator.pop() }
                )
            }
            is ResultsState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Erro ao carregar resultados.", color = Color.Red)
                }
            }
        }
    }
}

@Composable
fun ResultsContent(
    title: String,
    approved: Boolean,
    yesPercent: Int,
    noPercent: Int,
    abstentionPercent: Int,
    totalVotes: Int,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resultados") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )

            Card(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                backgroundColor = if (approved) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ) {
                Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                    Text(
                        text = if (approved) "APROVADA" else "REJEITADA",
                        style = MaterialTheme.typography.h6,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (approved) Color(0xFF2E7D32) else Color(0xFFC62828)
                    )
                    Text(
                        text = "Resultado atual da consulta",
                        style = MaterialTheme.typography.caption
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            ResultBar("Sim", yesPercent, Color(0xFF4CAF50))
            ResultBar("Não", noPercent, Color(0xFFF44336))
            ResultBar("Abstenção", abstentionPercent, Color(0xFF9E9E9E))

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Total de votos: $totalVotes",
                style = MaterialTheme.typography.body1,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { /* Implementar navegação para relatório completo */ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Ver relatório completo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ResultBar(label: String, percent: Int, color: Color) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(label, style = MaterialTheme.typography.body2, fontWeight = FontWeight.SemiBold)
            Text("$percent%", style = MaterialTheme.typography.body2, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = percent / 100f,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            backgroundColor = color.copy(alpha = 0.2f)
        )
        Spacer(modifier = Modifier.height(8.dp))
    }
}
