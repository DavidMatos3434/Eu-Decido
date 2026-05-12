package com.david.eudecido.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.AuthRepository
import com.david.eudecido.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenModel(
    private val authRepository: AuthRepository
) : ScreenModel {
    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess = _loginSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun login() {
        if (_isLoading.value) return
        if (_email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Preenche o email e a palavra-passe."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null

        screenModelScope.launch {
            authRepository.login(_email.value.trim(), _password.value)
                .onSuccess { response ->
                    SessionManager.login(
                        userId = response.user_id,
                        username = response.username,
                        email = response.email,
                        token = response.access_token
                    )
                    _loginSuccess.value = true
                }
                .onFailure { e ->
                    _errorMessage.value = "Erro ao iniciar sessão: ${e.message ?: "Credenciais incorretas"}"
                }
            _isLoading.value = false
        }
    }
}
