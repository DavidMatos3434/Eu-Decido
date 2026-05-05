package com.david.eudecido.screens.proposals

import androidx.compose.foundation.layout.*
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
import com.david.eudecido.screens.ai.AIProposalHelperScreen

class CreateProposalScreen(
    private val initialTitle: String = "",
    private val initialDescription: String = ""
) : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<CreateProposalScreenModel>()
        
        // Inicializar o modelo com valores se vierem da IA
        LaunchedEffect(Unit) {
            if (initialTitle.isNotBlank()) screenModel.onTitleChange(initialTitle)
            if (initialDescription.isNotBlank()) screenModel.onDescriptionChange(initialDescription)
        }

        val title by screenModel.title.collectAsState()
        val description by screenModel.description.collectAsState()
        val isSubmitting by screenModel.isSubmitting.collectAsState()
        val success by screenModel.success.collectAsState()

        LaunchedEffect(success) {
            if (success) {
                navigator.pop()
            }
        }

        CreateProposalContent(
            title = title,
            description = description,
            isSubmitting = isSubmitting,
            onTitleChange = screenModel::onTitleChange,
            onDescriptionChange = screenModel::onDescriptionChange,
            onUseAI = {
                navigator.push(AIProposalHelperScreen())
            },
            onSubmit = {
                screenModel.submitProposal()
            },
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun CreateProposalContent(
    title: String,
    description: String,
    isSubmitting: Boolean,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onUseAI: () -> Unit,
    onSubmit: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar Proposta") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            Text(
                text = "Nova Proposta",
                style = MaterialTheme.typography.h5,
                fontWeight = FontWeight.Bold
            )

            OutlinedTextField(
                value = title,
                onValueChange = onTitleChange,
                label = { Text("Título") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting
            )

            OutlinedTextField(
                value = description,
                onValueChange = onDescriptionChange,
                label = { Text("Descrição detalhada") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = !isSubmitting
            )

            Button(
                onClick = onUseAI,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = Color(0xFF6366F1), // Indigo para IA
                    contentColor = Color.White
                ),
                enabled = !isSubmitting
            ) {
                Text("✨ Melhorar com IA")
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onSubmit,
                enabled = !isSubmitting && title.isNotBlank() && description.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isSubmitting) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Publicar proposta", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
