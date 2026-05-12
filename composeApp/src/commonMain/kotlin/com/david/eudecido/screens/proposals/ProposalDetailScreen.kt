package com.david.eudecido.screens.proposals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.screens.ai.AIAssistantScreen
import com.david.eudecido.screens.ai.AIInsightsScreen
import com.david.eudecido.screens.discussion.DiscussionScreen
import com.david.eudecido.screens.voting.VotingScreen
import org.koin.core.parameter.parametersOf

class ProposalDetailScreen(val proposalId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<ProposalDetailScreenModel> { parametersOf(proposalId) }

        // ✅ Coleta de estados do Flow para valores reais do Model
        val title by screenModel.title.collectAsState()
        val summary by screenModel.summary.collectAsState()
        val description by screenModel.description.collectAsState()
        val totalVotes by screenModel.totalVotes.collectAsState()
        val yesPercent by screenModel.yesPercent.collectAsState()
        val selectedVote by screenModel.selectedVote.collectAsState()

        ProposalDetailContent(
            title = title,
            summary = summary,
            description = description,
            approval = yesPercent,
            votes = totalVotes,
            selectedVote = selectedVote,
            onVoteSelect = screenModel::onVoteSelect,
            onVoteConfirm = {
                navigator.push(VotingScreen(proposalId, title))
            },
            onAskAI = {
                navigator.push(AIAssistantScreen())
            },
            onSeeInsights = {
                navigator.push(AIInsightsScreen(
                    summary = summary,
                    consensus = "Há um forte acordo sobre a localização, mas dúvidas sobre o custo.",
                    impact = "Impacto positivo na mobilidade e redução de emissões."
                ))
            },
            onOpenDiscussion = {
                navigator.push(DiscussionScreen(proposalId))
            },
            onBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProposalDetailContent(
    title: String,
    summary: String,
    description: String,
    approval: Int,
    votes: Long, // Atualizado para Long para coincidir com a DB
    selectedVote: String?,
    onVoteSelect: (String) -> Unit,
    onVoteConfirm: () -> Unit,
    onAskAI: () -> Unit,
    onSeeInsights: () -> Unit,
    onOpenDiscussion: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Detalhe da Proposta") },
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
                .padding(16.dp)
                .verticalScroll(rememberScrollState()), // ✅ Permite scroll se o texto for longo
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h5,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                
                TextButton(onClick = onAskAI) {
                    Text("✨ Perguntar IA", color = MaterialTheme.colors.primary)
                }
            }

            // 🧠 IA Summary Card
            Card(
                elevation = 2.dp,
                backgroundColor = MaterialTheme.colors.surface,
                onClick = onSeeInsights
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Resumo (IA)",
                            style = MaterialTheme.typography.overline,
                            color = MaterialTheme.colors.primary
                        )
                        Text("Ver mais >", style = MaterialTheme.typography.caption)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary,
                        style = MaterialTheme.typography.body2
                    )
                }
            }

            // 📄 Description
            Text(
                text = description,
                style = MaterialTheme.typography.body1
            )

            // 📊 Results preview
            Card(
                elevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "Estado atual",
                        style = MaterialTheme.typography.subtitle2
                    )
                    Text(
                        text = "$approval% a favor • $votes votos",
                        style = MaterialTheme.typography.body2
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Button(onClick = onOpenDiscussion, shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)) {
                    Text("Ver Discussão 💬")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onVoteConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ) {
                Text("Votar nesta Proposta", fontWeight = FontWeight.Bold)
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
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
        Text(label)
    }
}
