package com.david.eudecido.screens.territory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.screens.proposals.ProposalListScreen

class TerritoryOverviewScreen(
    private val freguesiaName: String,
    private val municipioName: String,
    private val regiaoName: String
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { 
            TerritoryOverviewScreenModel(freguesiaName, municipioName, regiaoName) 
        }

        val freguesia by screenModel.freguesia.collectAsState()
        val municipio by screenModel.municipio.collectAsState()
        val regiao by screenModel.regiao.collectAsState()

        TerritoryOverviewContent(
            freguesia = freguesia,
            municipio = municipio,
            regiao = regiao,
            onLevelClick = {
                navigator.push(ProposalListScreen())
            },
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun TerritoryOverviewContent(
    freguesia: String,
    municipio: String,
    regiao: String,
    onLevelClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Estrutura") },
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
                text = "Estrutura do Movimento",
                style = MaterialTheme.typography.h5,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Text(
                text = "Participa em decisões a diferentes níveis.",
                style = MaterialTheme.typography.body2
            )

            TerritoryCard(
                title = "Freguesia",
                subtitle = freguesia,
                onClick = onLevelClick
            )

            TerritoryCard(
                title = "Município",
                subtitle = municipio,
                onClick = onLevelClick
            )

            TerritoryCard(
                title = "Região",
                subtitle = regiao,
                onClick = onLevelClick
            )

            TerritoryCard(
                title = "Nacional",
                subtitle = "Portugal",
                onClick = onLevelClick
            )
        }
    }
}

@Composable
fun TerritoryCard(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = 4.dp,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.subtitle1,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = subtitle,
                style = MaterialTheme.typography.body2,
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
