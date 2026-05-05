package com.david.eudecido.screens.territory

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.model.rememberScreenModel
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class TerritorySelectionScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = rememberScreenModel { TerritorySelectionScreenModel() }

        val freguesia by screenModel.freguesia.collectAsState()
        val municipio by screenModel.municipio.collectAsState()
        val regiao by screenModel.regiao.collectAsState()

        TerritorySelectionContent(
            freguesia = freguesia,
            municipio = municipio,
            regiao = regiao,
            onFreguesiaChange = screenModel::onFreguesiaChange,
            onMunicipioChange = screenModel::onMunicipioChange,
            onRegiaoChange = screenModel::onRegiaoChange,
            onConfirm = {
                screenModel.confirmSelection()
                navigator.push(TerritoryOverviewScreen(freguesia, municipio, regiao))
            }
        )
    }
}

@Composable
fun TerritorySelectionContent(
    freguesia: String,
    municipio: String,
    regiao: String,
    onFreguesiaChange: (String) -> Unit,
    onMunicipioChange: (String) -> Unit,
    onRegiaoChange: (String) -> Unit,
    onConfirm: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "Define a tua localização",
            style = MaterialTheme.typography.h5
        )

        Text(
            text = "Isto permite-te participar nas decisões da tua comunidade.",
            style = MaterialTheme.typography.body2
        )

        OutlinedTextField(
            value = freguesia,
            onValueChange = onFreguesiaChange,
            label = { Text("Freguesia") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = municipio,
            onValueChange = onMunicipioChange,
            label = { Text("Município") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = regiao,
            onValueChange = onRegiaoChange,
            label = { Text("Região") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("Confirmar")
        }
    }
}
