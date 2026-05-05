package com.david.eudecido.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.IdentityRepository
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

    fun onNameChange(value: String) { _name.value = value }
    fun onEmailChange(value: String) { _email.value = value }
    fun onNifChange(value: String) { _nif.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun register() {
        if (_isLoading.value) return
        _isLoading.value = true

        screenModelScope.launch {
            try {
                val nifHash = identityRepository.hashNif(_nif.value)
                val initialToken = identityRepository.verifyAndRegisterIdentity(nifHash)

                if (initialToken != null) {
                    // Chamada corrigida com os novos parâmetros de segurança
                    userRepository.insertUser(
                        id = "temp_user_id_" + nifHash.takeLast(4),
                        identityId = nifHash,
                        username = _name.value,
                        email = _email.value,
                        isCandidate = false
                    )
                    _registrationSuccess.value = true
                }
            } catch (e: Exception) {
                // Tratar erros
            } finally {
                _isLoading.value = false
            }
        }
    }
}
