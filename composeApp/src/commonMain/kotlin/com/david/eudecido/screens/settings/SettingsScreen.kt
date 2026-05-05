package com.david.eudecido.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.screens.auth.WelcomeScreen
import com.david.eudecido.screens.territory.TerritorySelectionScreen

class SettingsScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<SettingsScreenModel>()
        
        val notificationsEnabled by screenModel.notificationsEnabled.collectAsState()
        val deleteSuccess by screenModel.deleteSuccess.collectAsState()
        
        var showDeleteDialog by remember { mutableStateOf(false) }

        LaunchedEffect(deleteSuccess) {
            if (deleteSuccess) {
                navigator.replaceAll(WelcomeScreen())
            }
        }

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Eliminar Conta") },
                text = { Text("Tens a certeza que desejas eliminar a tua conta? Esta ação é irreversível e todos os teus dados locais serão apagados.") },
                confirmButton = {
                    TextButton(onClick = { screenModel.deleteAccount() }) {
                        Text("ELIMINAR", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("CANCELAR")
                    }
                }
            )
        }

        SettingsContent(
            notificationsEnabled = notificationsEnabled,
            onNotificationsToggle = screenModel::toggleNotifications,
            onChangeFreguesia = { navigator.push(TerritorySelectionScreen()) },
            onTermsClick = { /* Futuramente abrir ecrã ou PDF de Termos */ },
            onDeleteClick = { showDeleteDialog = true },
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun SettingsContent(
    notificationsEnabled: Boolean,
    onNotificationsToggle: (Boolean) -> Unit,
    onChangeFreguesia: () -> Unit,
    onTermsClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Definições") },
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Preferências da App",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Notificações", style = MaterialTheme.typography.body1)
                    Text("Receber alertas de novas propostas", style = MaterialTheme.typography.caption, color = Color.Gray)
                }
                Switch(checked = notificationsEnabled, onCheckedChange = onNotificationsToggle)
            }

            Divider()

            TextButton(onClick = onChangeFreguesia, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Column {
                        Text("Mudar Freguesia", color = Color.Black, style = MaterialTheme.typography.body1)
                        Text("Atualizar a tua área de residência", style = MaterialTheme.typography.caption, color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Legal e Transparência",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onTermsClick, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text("Termos e Condições", color = Color.Black)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Conta e Segurança",
                style = MaterialTheme.typography.subtitle2,
                color = MaterialTheme.colors.primary,
                fontWeight = FontWeight.Bold
            )

            TextButton(onClick = onDeleteClick, modifier = Modifier.fillMaxWidth()) {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterStart) {
                    Text("Eliminar conta e dados pessoais", color = Color.Red)
                }
            }
        }
    }
}
