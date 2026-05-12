package com.david.eudecido.screens.proposals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.components.ProposalCard
import com.david.eudecido.models.ProposalUI

class ProposalListScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<ProposalListScreenModel>()
        val proposals by screenModel.proposals.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Todas as Propostas") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Text("<", color = androidx.compose.ui.graphics.Color.White)
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = androidx.compose.ui.graphics.Color.White
                )
            }
        ) { paddingValues ->
            ProposalListContent(
                modifier = Modifier.padding(paddingValues),
                proposals = proposals,
                onProposalClick = { proposalId ->
                    navigator.push(ProposalDetailScreen(proposalId))
                }
            )
        }
    }
}

@Composable
fun ProposalListContent(
    modifier: Modifier = Modifier,
    proposals: List<ProposalUI>,
    onProposalClick: (String) -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            items(proposals) { proposal ->
                ProposalCard(proposal) {
                    onProposalClick(proposal.id)
                }
            }
        }
    }
}
