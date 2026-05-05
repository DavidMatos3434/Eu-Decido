package com.david.eudecido.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class AIInsightsScreen(
    private val summary: String,
    private val consensus: String,
    private val impact: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { 
            AIInsightsScreenModel(summary, consensus, impact) 
        }

        val summaryText by screenModel.summary.collectAsState()
        val consensusText by screenModel.consensus.collectAsState()
        val impactText by screenModel.impact.collectAsState()

        AIInsightsContent(
            summary = summaryText,
            consensus = consensusText,
            impact = impactText,
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun AIInsightsContent(
    summary: String,
    consensus: String,
    impact: String,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Insights da IA") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<", color = androidx.compose.ui.graphics.Color.White)
                    }
                },
                backgroundColor = MaterialTheme.colors.primary,
                contentColor = androidx.compose.ui.graphics.Color.White
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
                text = "Análise Estruturada",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )

            InsightCard(
                title = "Resumo",
                content = summary,
                color = MaterialTheme.colors.primary
            )

            InsightCard(
                title = "Consenso",
                content = consensus,
                color = androidx.compose.ui.graphics.Color(0xFF2E7D32) // Verde para consenso
            )

            InsightCard(
                title = "Impacto",
                content = impact,
                color = androidx.compose.ui.graphics.Color(0xFFE65100) // Laranja para impacto
            )
        }
    }
}

@Composable
fun InsightCard(title: String, content: String, color: androidx.compose.ui.graphics.Color) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 4.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title, 
                style = MaterialTheme.typography.subtitle1,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = color.copy(alpha = 0.2f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.body2,
                lineHeight = 20.sp
            )
        }
    }
}
