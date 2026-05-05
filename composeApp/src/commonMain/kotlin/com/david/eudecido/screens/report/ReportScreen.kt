package com.david.eudecido.screens.report

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class ReportScreen(
    private val reportTitle: String,
    private val reportDescription: String,
    private val participationCount: Int,
    private val arguments: List<String>
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel {
            ReportScreenModel(reportTitle, reportDescription, participationCount, arguments)
        }

        val title by screenModel.title.collectAsState()
        val description by screenModel.description.collectAsState()
        val participation by screenModel.participation.collectAsState()
        val topArguments by screenModel.topArguments.collectAsState()

        ReportContent(
            title = title,
            description = description,
            participation = participation,
            topArguments = topArguments,
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun ReportContent(
    title: String,
    description: String,
    participation: Int,
    topArguments: List<String>,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Relatório de Transparência") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Relatório Oficial",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = title,
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.primary
            )

            Text(
                text = description,
                style = MaterialTheme.typography.body1
            )

            Card(
                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp),
                backgroundColor = MaterialTheme.colors.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Participação Cívica",
                        style = MaterialTheme.typography.subtitle1,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$participation cidadãos participaram nesta decisão.",
                        style = MaterialTheme.typography.body2
                    )
                }
            }

            Text(
                text = "Principais Argumentos",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )

            topArguments.forEach { argument ->
                Card(
                    elevation = 1.dp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    backgroundColor = Color(0xFFF8FAFC)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(text = "• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)
                        Text(text = argument, style = MaterialTheme.typography.body2)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            OutlinedButton(
                onClick = { /* Lógica para exportar PDF ou partilhar */ },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Exportar para Auditoria (PDF)")
            }
        }
    }
}
