package com.david.eudecido.screens.voting

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.screens.results.ResultsScreen
import org.koin.core.parameter.parametersOf

class VotingScreen(val proposalId: String, val proposalTitle: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<VotingScreenModel> { parametersOf(proposalId, proposalTitle) }
        
        val selectedVote by screenModel.selectedVote.collectAsState()
        val success by screenModel.voteSuccess.collectAsState()

        LaunchedEffect(success) {
            if (success) {
                navigator.push(ResultsScreen(proposalId))
            }
        }

        VotingContent(
            proposalTitle = proposalTitle,
            selectedVote = selectedVote,
            onVoteSelect = screenModel::onVoteSelect,
            onVoteConfirm = { screenModel.submitVote() },
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun VotingContent(
    proposalTitle: String,
    selectedVote: String?,
    onVoteSelect: (String) -> Unit,
    onVoteConfirm: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Votação") },
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
                text = proposalTitle,
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Concorda com esta proposta?",
                style = MaterialTheme.typography.body1
            )

            Spacer(modifier = Modifier.height(8.dp))

            VoteOption("SIM", selectedVote == "SIM") { onVoteSelect("SIM") }
            VoteOption("NÃO", selectedVote == "NAO") { onVoteSelect("NAO") }
            VoteOption("ABSTENÇÃO", selectedVote == "ABSTENCAO") { onVoteSelect("ABSTENCAO") }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onVoteConfirm,
                enabled = selectedVote != null,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Registar voto", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VoteOption(
    label: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    OutlinedButton(
        onClick = onSelect,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = if (isSelected) {
            ButtonDefaults.outlinedButtonColors(
                backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f),
                contentColor = MaterialTheme.colors.primary
            )
        } else {
            ButtonDefaults.outlinedButtonColors()
        },
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colors.primary)
        } else {
            androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colors.onSurface.copy(alpha = 0.12f))
        }
    ) {
        Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
    }
}
