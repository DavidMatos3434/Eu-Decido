package com.david.eudecido.screens.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import cafe.adriel.voyager.core.screen.Screen
import cafe.adriel.voyager.koin.getScreenModel
import cafe.adriel.voyager.navigator.LocalNavigator
import cafe.adriel.voyager.navigator.currentOrThrow
import com.david.eudecido.screens.territory.TerritorySelectionScreen

class RegisterScreen : Screen {

    @Composable
    override fun Content() {
        val navigator = LocalNavigator.currentOrThrow
        val screenModel = getScreenModel<RegisterScreenModel>()
        
        val name by screenModel.name.collectAsState()
        val email by screenModel.email.collectAsState()
        val nif by screenModel.nif.collectAsState()
        val password by screenModel.password.collectAsState()
        val isLoading by screenModel.isLoading.collectAsState()
        val success by screenModel.registrationSuccess.collectAsState()

        LaunchedEffect(success) {
            if (success) {
                navigator.push(TerritorySelectionScreen())
            }
        }

        RegisterContent(
            name = name,
            email = email,
            nif = nif,
            password = password,
            isLoading = isLoading,
            onNameChange = screenModel::onNameChange,
            onEmailChange = screenModel::onEmailChange,
            onNifChange = screenModel::onNifChange,
            onPasswordChange = screenModel::onPasswordChange,
            onRegisterClick = { screenModel.register() },
            onBack = { navigator.pop() }
        )
    }
}

@Composable
fun RegisterContent(
    name: String,
    email: String,
    nif: String,
    password: String,
    isLoading: Boolean,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onNifChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegisterClick: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Criar conta") },
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
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Identidade Digital",
                style = MaterialTheme.typography.h4,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "1 Pessoa = 1 Voto. O seu NIF é encriptado localmente.",
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
            
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = name,
                onValueChange = onNameChange,
                label = { Text("Nome completo") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = email,
                onValueChange = onEmailChange,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nif,
                onValueChange = { if (it.length <= 9) onNifChange(it) },
                label = { Text("NIF (9 dígitos)") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = onPasswordChange,
                label = { Text("Palavra-passe (mín. 6 caracteres)") },
                modifier = Modifier.fillMaxWidth(),
                visualTransformation = PasswordVisualTransformation(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            val isFormValid = name.isNotBlank() && 
                             email.contains("@") && 
                             nif.length == 9 && 
                             password.length >= 6

            Button(
                onClick = onRegisterClick,
                enabled = !isLoading && isFormValid,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text("Criar Identidade Segura", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
