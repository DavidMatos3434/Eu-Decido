package com.david.eudecido.screens.auth

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.IdentityRepository
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

    fun onEmailChange(value: String) { _email.value = value }
    fun onPasswordChange(value: String) { _password.value = value }

    fun login() {
        if (_isLoading.value) return
        _isLoading.value = true
        
        screenModelScope.launch {
            try {
                // Simulação de login no Supabase
                kotlinx.coroutines.delay(1000)
                
                val userId = "user_id_from_supabase"
                
                // Chamada corrigida com os novos parâmetros de segurança
                userRepository.insertUser(
                    id = userId,
                    identityId = "identity_hash_from_back",
                    username = "David Silva",
                    email = _email.value,
                    isCandidate = false
                )
                
                _loginSuccess.value = true
            } catch (e: Exception) {
                // Tratar erro de login
            } finally {
                _isLoading.value = false
            }
        }
    }
}
