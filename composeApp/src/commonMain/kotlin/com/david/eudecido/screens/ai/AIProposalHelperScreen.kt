package com.david.eudecido.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.screens.proposals.CreateProposalScreen

class AIProposalHelperScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { AIProposalHelperScreenModel() }

        val generatedTitle by screenModel.generatedTitle.collectAsState()
        val generatedSummary by screenModel.generatedSummary.collectAsState()
        val isGenerating by screenModel.isGenerating.collectAsState()

        AIProposalHelperContent(
            generatedTitle = generatedTitle,
            generatedSummary = generatedSummary,
            isGenerating = isGenerating,
            onGenerate = screenModel::generateProposal,
            onBack = { navigator.pop() },
            onAccept = {
                // Navega para o ecrã de criação com os dados gerados pela IA
                navigator.push(CreateProposalScreen(
                    initialTitle = generatedTitle ?: "",
                    initialDescription = generatedSummary ?: ""
                ))
            }
        )
    }
}

@Composable
fun AIProposalHelperContent(
    generatedTitle: String?,
    generatedSummary: String?,
    isGenerating: Boolean,
    onGenerate: (String) -> Unit,
    onBack: () -> Unit,
    onAccept: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assistente de Propostas") },
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
                text = "Descreve a tua ideia",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "A IA ajuda-te a transformar uma ideia simples numa proposta estruturada e clara para a comunidade.",
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )

            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                placeholder = { Text("Ex: Gostaria de melhorar a iluminação na rua X e criar um pequeno jardim...") },
                label = { Text("A tua ideia") },
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )

            Button(
                onClick = { onGenerate(input) },
                enabled = input.isNotBlank() && !isGenerating,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Gerar Estrutura com IA ✨")
                }
            }

            if (generatedTitle != null || generatedSummary != null) {
                Divider()
                
                Text(
                    text = "Sugestão da IA",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )

                generatedTitle?.let {
                    Card(elevation = 2.dp, backgroundColor = MaterialTheme.colors.surface) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Título", 
                                style = MaterialTheme.typography.overline,
                                color = MaterialTheme.colors.primary
                            )
                            Text(it, style = MaterialTheme.typography.subtitle1, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                generatedSummary?.let {
                    Card(elevation = 2.dp) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Resumo", 
                                style = MaterialTheme.typography.overline,
                                color = MaterialTheme.colors.primary
                            )
                            Text(it, style = MaterialTheme.typography.body2)
                        }
                    }
                }
                
                Button(
                    onClick = onAccept,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(backgroundColor = androidx.compose.ui.graphics.Color(0xFF2E7D32), contentColor = androidx.compose.ui.graphics.Color.White)
                ) {
                    Text("Usar esta estrutura")
                }
            }
        }
    }
}
