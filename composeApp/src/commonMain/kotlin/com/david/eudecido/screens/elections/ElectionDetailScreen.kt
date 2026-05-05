package com.david.eudecido.screens.elections

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.david.eudecido.db.Candidacies
import com.david.eudecido.db.Elections
import org.koin.core.parameter.parametersOf

class ElectionDetailScreen(private val electionId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<ElectionDetailScreenModel> { parametersOf(electionId) }
        val state by screenModel.state.collectAsState()

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Detalhes da Eleição") },
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
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
                when (val s = state) {
                    is ElectionDetailState.Loading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    is ElectionDetailState.Error -> Text("Erro ao carregar detalhes", modifier = Modifier.align(Alignment.Center))
                    is ElectionDetailState.Success -> {
                        ElectionDetailContent(
                            election = s.election,
                            candidacies = s.candidacies,
                            onApply = { navigator.push(ApplyCandidateScreen(electionId)) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ElectionDetailContent(
    election: Elections,
    candidacies: List<Candidacies>,
    onApply: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = 2.dp,
            shape = RoundedCornerShape(12.dp),
            backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.05f)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = election.title, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                Text(text = "Cargo: ${election.role}", style = MaterialTheme.typography.subtitle1)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Estado: ${election.status}", color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
            }
        }

        Divider()

        Text(text = "Candidatos Inscritos", style = MaterialTheme.typography.h6, fontWeight = FontWeight.Bold)

        if (candidacies.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("Ainda não existem candidatos.", color = Color.Gray)
            }
        } else {
            LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(candidacies) { candidacy ->
                    CandidacyItem(candidacy)
                }
            }
        }

        if (election.status == "OPEN") {
            Button(
                onClick = onApply,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Candidatar-me a este cargo", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun CandidacyItem(candidacy: Candidacies) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).background(Color.LightGray, RoundedCornerShape(20.dp)))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = "Candidato #${candidacy.user_id.takeLast(4)}", fontWeight = FontWeight.Bold)
                Text(text = "Estado: ${candidacy.status}", style = MaterialTheme.typography.caption)
            }
        }
    }
}
