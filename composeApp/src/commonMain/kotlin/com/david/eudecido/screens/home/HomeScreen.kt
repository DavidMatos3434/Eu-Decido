package com.david.eudecido.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.components.ProposalCard
import com.david.eudecido.models.ProposalUI
import com.david.eudecido.screens.ai.AIProposalHelperScreen
import com.david.eudecido.screens.educational.EducationScreen
import com.david.eudecido.screens.profile.ProfileScreen
import com.david.eudecido.screens.proposals.ProposalDetailScreen
import com.david.eudecido.screens.proposals.ProposalListScreen
import com.david.eudecido.screens.search.SearchScreen

class HomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        
        // Corrigido: Usamos getScreenModel para injeção via Koin no Voyager
        val screenModel = getScreenModel<HomeScreenModel>()

        val userName by screenModel.userName.collectAsState()
        val proposals by screenModel.proposals.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("EU DECIDO") },
                    actions = {
                        IconButton(onClick = { navigator.push(SearchScreen()) }) {
                            Text("🔍", color = androidx.compose.ui.graphics.Color.White)
                        }
                        IconButton(onClick = { navigator.push(ProfileScreen()) }) {
                            Text("👤", color = androidx.compose.ui.graphics.Color.White)
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { navigator.push(AIProposalHelperScreen()) },
                    backgroundColor = MaterialTheme.colors.primary
                ) {
                    Text("＋", color = androidx.compose.ui.graphics.Color.White, fontSize = 24.sp)
                }
            }
        ) { paddingValues ->
            HomeContent(
                modifier = Modifier.padding(paddingValues),
                userName = userName,
                proposals = proposals,
                onProposalClick = { proposalId ->
                    navigator.push(ProposalDetailScreen(proposalId))
                },
                onSeeMoreClick = {
                    navigator.push(ProposalListScreen())
                },
                onHelpClick = {
                    navigator.push(EducationScreen())
                }
            )
        }
    }
}

@Composable
fun HomeContent(
    modifier: Modifier = Modifier,
    userName: String,
    proposals: List<ProposalUI>,
    onProposalClick: (String) -> Unit,
    onSeeMoreClick: () -> Unit,
    onHelpClick: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Olá, $userName 👋",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )
            TextButton(onClick = onHelpClick) {
                Text("Como funciona?", style = MaterialTheme.typography.caption)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 4.dp,
            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${proposals.size} decisões em aberto",
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Participa agora",
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Propostas Relevantes",
                style = MaterialTheme.typography.h6
            )
            TextButton(onClick = onSeeMoreClick) {
                Text("Ver todas")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (proposals.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Sem propostas de momento.", color = androidx.compose.ui.graphics.Color.Gray)
            }
        } else {
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
}
