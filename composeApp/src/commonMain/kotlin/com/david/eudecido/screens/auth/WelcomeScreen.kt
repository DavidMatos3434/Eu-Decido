package com.david.eudecido.screens.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.screens.main.MainScreen

class WelcomeScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow

        WelcomeContent(
            onLoginClick = { 
                navigator.push(LoginScreen()) 
            },
            onRegisterClick = { 
                navigator.push(RegisterScreen()) 
            },
            onExploreClick = { 
                navigator.replaceAll(MainScreen()) 
            }
        )
    }
}

@Composable
fun WelcomeContent(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onExploreClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // dark blue
                        Color(0xFF1E293B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {

            // 🔝 TOP: Branding
            Column {
                Spacer(modifier = Modifier.height(40.dp))

                Text(
                    text = "EU DECIDO",
                    style = MaterialTheme.typography.h4,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Decisões nas mãos dos cidadãos",
                    style = MaterialTheme.typography.body1,
                    color = Color(0xFF94A3B8)
                )
            }

            // 🧠 CENTER: Message
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Participa • Propõe • Decide       Representa",
                    style = MaterialTheme.typography.h5,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Uma nova forma de fazer política, baseada na participação direta e apoiada por inteligência artificial.",
                    style = MaterialTheme.typography.body2,
                    color = Color(0xFFCBD5F5)
                )
            }

            // 🔻 BOTTOM: Actions
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {

                Button(
                    onClick = onRegisterClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = Color(0xFF3B82F6), // Azul vibrante para destacar
                        contentColor = Color.White
                    )
                ) {
                    Text("Criar conta", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = onLoginClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(
                        backgroundColor = Color.Transparent, // Garantir que não fica branco
                        contentColor = Color.White
                    )
                ) {
                    Text("Entrar", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(12.dp))

                TextButton(
                    onClick = onExploreClick,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = "Explorar sem conta",
                        color = Color(0xFF94A3B8)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
