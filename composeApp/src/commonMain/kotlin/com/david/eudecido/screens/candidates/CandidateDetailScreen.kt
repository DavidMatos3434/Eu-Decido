package com.david.eudecido.screens.candidates

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class CandidateDetailScreen(val candidateId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { CandidateDetailScreenModel(candidateId) }

        val name by screenModel.name.collectAsState()
        val bio by screenModel.bio.collectAsState()
        val proposals by screenModel.proposals.collectAsState()

        CandidateDetailContent(
            name = name,
            bio = bio,
            proposals = proposals,
            onVote = {
                // Lógica de voto no candidato
                navigator.pop()
            },
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun CandidateDetailContent(
    name: String,
    bio: String,
    proposals: List<String>,
    onVote: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalhe do Candidato") },
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
                text = name,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = bio,
                style = MaterialTheme.typography.body1
            )

            Divider()

            Text(
                text = "Propostas do candidato",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.SemiBold
            )

            proposals.forEach { proposal ->
                Card(
                    elevation = 2.dp,
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
                ) {
                    Row(modifier = Modifier.padding(12.dp)) {
                        Text(text = "• ", fontWeight = FontWeight.Bold, color = MaterialTheme.colors.primary)
                        Text(text = proposal, style = MaterialTheme.typography.body2)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onVote,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("Votar neste candidato", fontWeight = FontWeight.Bold)
            }
        }
    }
}
