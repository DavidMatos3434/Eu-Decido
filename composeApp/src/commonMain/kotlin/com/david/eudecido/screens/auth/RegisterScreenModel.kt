package com.david.eudecido.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.AuthRepository
import com.david.eudecido.data.IdentityRepository
import com.david.eudecido.data.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterScreenModel(
    private val authRepository: AuthRepository,
    private val identityRepository: IdentityRepository
) : ScreenModel {
    private val _name = MutableStateFlow("")
    val name = _name.asStateFlow()

    private val _email = MutableStateFlow("")
    val email = _email.asStateFlow()

    private val _nif = MutableStateFlow("")
    val nif = _nif.asStateFlow()

    private val _password = MutableStateFlow("")
    val password = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _registrationSuccess = MutableStateFlow(false)
    val registrationSuccess = _registrationSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun onNameChange(value: String) { _name.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onNifChange(value: String) { _nif.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun register() {
        if (_isLoading.value) return
        if (_name.value.isBlank() || _email.value.isBlank() || _password.value.isBlank()) {
            _errorMessage.value = "Preenche todos os campos obrigatórios."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null

        screenModelScope.launch {
            try {
                // Se o NIF for fornecido, fazemos o hash antes de enviar
                val nifHash = if (_nif.value.isNotBlank()) identityRepository.hashNif(_nif.value) else null
                
                authRepository.register(
                    username = _name.value.trim(),
                    email = _email.value.trim(),
                    password = _password.value,
                    nifHash = nifHash
                ).onSuccess { response ->
                    SessionManager.login(
                        userId = response.user_id,
                        username = response.username,
                        email = response.email,
                        token = response.access_token
                    )
                    _registrationSuccess.value = true
                }.onFailure { e ->
                    _errorMessage.value = "Erro no registo: ${e.message ?: "Tenta novamente"}"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro ao processar registo."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
