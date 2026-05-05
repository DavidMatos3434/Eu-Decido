package com.david.eudecido.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.IdentityRepository
import com.david.eudecido.data.SessionManager
import com.david.eudecido.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RegisterScreenModel(
    private val identityRepository: IdentityRepository,
    private val userRepository: UserRepository
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
        if (_name.value.isBlank() || _email.value.isBlank() || _nif.value.isBlank()) {
            _errorMessage.value = "Preenche todos os campos obrigatórios."
            return
        }
        _isLoading.value = true
        _errorMessage.value = null

        screenModelScope.launch {
            try {
                val nifHash = identityRepository.hashNif(_nif.value)
                val initialToken = identityRepository.verifyAndRegisterIdentity(nifHash)

                if (initialToken != null) {
                    val userId = "user_${nifHash.takeLast(8)}"
                    val username = _name.value.trim()
                    val email = _email.value.trim()

                    userRepository.insertUser(
                        id = userId,
                        identityId = nifHash,
                        username = username,
                        email = email,
                        isCandidate = false
                    )

                    SessionManager.login(
                        userId = userId,
                        username = username,
                        email = email
                    )

                    _registrationSuccess.value = true
                } else {
                    _errorMessage.value = "Falha na validação de identidade. Verifica o teu NIF."
                }
            } catch (e: Exception) {
                _errorMessage.value = "Erro no registo. Tenta novamente."
            } finally {
                _isLoading.value = false
            }
        }
    }
}
