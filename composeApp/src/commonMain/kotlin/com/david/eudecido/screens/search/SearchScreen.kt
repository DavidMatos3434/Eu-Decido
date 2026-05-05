package com.david.eudecido.screens.search

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.components.ProposalCard
import com.david.eudecido.screens.proposals.ProposalDetailScreen

class SearchScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { SearchScreenModel() }
        
        val query by screenModel.query.collectAsState()
        val results by screenModel.results.collectAsState()

        SearchContent(
            query = query,
            results = results,
            onQueryChange = screenModel::onQueryChange,
            onProposalClick = { id -> navigator.push(ProposalDetailScreen(id)) },
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun SearchContent(
    query: String,
    results: List<com.david.eudecido.models.ProposalUI>,
    onQueryChange: (String) -> Unit,
    onProposalClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colors.surface,
                elevation = 0.dp,
                title = {
                    TextField(
                        value = query,
                        onValueChange = onQueryChange,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Pesquisar propostas...") },
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        singleLine = true
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<", color = MaterialTheme.colors.primary)
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (query.isBlank()) {
                Text(
                    "Escreve algo para começar a pesquisar...",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            } else if (results.isEmpty()) {
                Text(
                    "Nenhum resultado encontrado para '$query'.",
                    style = MaterialTheme.typography.body2,
                    color = Color.Gray
                )
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(results) { proposal ->
                        ProposalCard(proposal) {
                            onProposalClick(proposal.id)
                        }
                    }
                }
            }
        }
    }
}
