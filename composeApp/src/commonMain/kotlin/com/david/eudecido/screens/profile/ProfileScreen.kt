package com.david.eudecido.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.models.UserActivity
import com.david.eudecido.screens.auth.WelcomeScreen
import com.david.eudecido.screens.settings.SettingsScreen

class ProfileScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<ProfileScreenModel>()
        
        val userName by screenModel.userName.collectAsState()
        val userEmail by screenModel.userEmail.collectAsState()
        val userFreguesia by screenModel.userFreguesia.collectAsState()
        val points by screenModel.participationPoints.collectAsState()
        val activities by screenModel.activities.collectAsState()

        ProfileContent(
            userName = userName,
            userEmail = userEmail,
            userFreguesia = userFreguesia,
            points = points,
            activities = activities,
            onSettingsClick = { navigator.push(SettingsScreen()) },
            onLogout = {
                screenModel.logout()
                navigator.replaceAll(WelcomeScreen())
            },
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun ProfileContent(
    userName: String,
    userEmail: String,
    userFreguesia: String,
    points: Int,
    activities: List<UserActivity>,
    onSettingsClick: () -> Unit,
    onLogout: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("O meu Perfil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Text("<", color = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Text("⚙️", color = Color.White)
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header: Avatar + Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colors.primary.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = userName.take(1).uppercase(),
                        style = MaterialTheme.typography.h4,
                        color = MaterialTheme.colors.primary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = userName, style = MaterialTheme.typography.h5, fontWeight = FontWeight.Bold)
                    Text(text = "Freguesia: $userFreguesia", style = MaterialTheme.typography.body2, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = "Pontos de Participação", style = MaterialTheme.typography.caption)
                        Text(text = points.toString(), style = MaterialTheme.typography.h6, color = MaterialTheme.colors.primary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Atividade Section
            Text(
                text = "Atividade Recente",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (activities.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Ainda não tens atividade registada.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(activities) { activity ->
                        ActivityItem(activity)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onLogout) {
                Text("Terminar sessão", color = Color.Red, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ActivityItem(activity: UserActivity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 1.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = activity.title, style = MaterialTheme.typography.subtitle2, fontWeight = FontWeight.Bold)
            Text(text = activity.subtitle, style = MaterialTheme.typography.caption, color = Color.Gray)
        }
    }
}
