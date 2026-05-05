package com.david.eudecido.screens.discussion

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.david.eudecido.models.Comment
import org.koin.core.parameter.parametersOf

class DiscussionScreen(private val proposalId: String) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        // Injeção via Koin com o ID da proposta
        val screenModel = getScreenModel<DiscussionScreenModel> { parametersOf(proposalId) }
        val comments by screenModel.comments.collectAsState()

        DiscussionContent(
            comments = comments,
            onSendComment = screenModel::sendComment,
            onBack = { navigator.pop() }
        )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DiscussionContent(
    comments: List<Comment>,
    onSendComment: (String) -> Unit,
    onBack: () -> Unit
) {
    var input by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Discussão") },
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
                .padding(16.dp)
        ) {

            Text(
                text = "Participação Comunitária",
                style = MaterialTheme.typography.h6,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filtros (UX placeholder)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Chip(onClick = {}, colors = ChipDefaults.chipColors(backgroundColor = MaterialTheme.colors.primary.copy(alpha = 0.1f))) {
                    Text("Mais relevantes", style = MaterialTheme.typography.caption)
                }
                Chip(onClick = {}) {
                    Text("Recentes", style = MaterialTheme.typography.caption)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (comments.isEmpty()) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text("Sê o primeiro a comentar!", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(comments) { comment ->
                        CommentItem(comment)
                    }
                }
            }

            Surface(
                elevation = 8.dp,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = input,
                        onValueChange = { input = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Escreve um comentário...") },
                        colors = TextFieldDefaults.textFieldColors(backgroundColor = Color.Transparent)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (input.isNotBlank()) {
                                onSendComment(input)
                                input = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Enviar")
                    }
                }
            }
        }
    }
}

@Composable
fun CommentItem(comment: Comment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = 2.dp,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.author,
                    style = MaterialTheme.typography.subtitle2,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colors.primary
                )
                Text(
                    text = "👍 ${comment.likes}",
                    style = MaterialTheme.typography.caption
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.body2
            )
        }
    }
}
