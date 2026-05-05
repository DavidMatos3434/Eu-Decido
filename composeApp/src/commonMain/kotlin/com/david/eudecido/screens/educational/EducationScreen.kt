package com.david.eudecido.screens.educational

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow

class EducationScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Como funciona?") },
                    navigationIcon = {
                        IconButton(onClick = { navigator.pop() }) {
                            Text("<", color = Color.White)
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                EducationSection(
                    title = "1. Proposta",
                    content = "Qualquer cidadão pode apresentar uma ideia. O nosso assistente IA ajuda a estruturar o texto para que seja claro e objetivo."
                )

                EducationSection(
                    title = "2. Discussão",
                    content = "A comunidade debate a proposta. A IA analisa os comentários para identificar pontos de consenso e preocupações comuns."
                )

                EducationSection(
                    title = "3. Votação",
                    content = "Se a proposta for viável, passa a votação. O teu voto tem impacto direto na decisão final dos nossos representantes."
                )

                EducationSection(
                    title = "4. Transparência",
                    content = "Podes acompanhar todo o processo e ver como o teu representante votou nos órgãos oficiais baseado na decisão da comunidade."
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navigator.pop() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
                ) {
                    Text("Percebido!", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EducationSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.h6,
            color = MaterialTheme.colors.primary,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.body1,
            lineHeight = 24.sp
        )
    }
}
