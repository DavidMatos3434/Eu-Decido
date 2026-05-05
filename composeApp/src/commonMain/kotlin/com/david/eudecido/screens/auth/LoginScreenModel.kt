package com.david.eudecido.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.IdentityRepository
import com.david.eudecido.data.SessionManager
import com.david.eudecido.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LoginScreenModel(
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository
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
            try {
                val email = _email.value.trim()
                val username = email.substringBefore('@')
                val userId = "user_${identityRepository.hashNif(email).takeLast(8)}"

                userRepository.insertUser(
                    id = userId,
                    identityId = null,
                    username = username,
                    email = email,
                    isCandidate = false
                )

                SessionManager.login(
                    userId = userId,
                    username = username,
                    email = email
                )

                _loginSuccess.value = true
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao iniciar sessão. Tenta novamente."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
