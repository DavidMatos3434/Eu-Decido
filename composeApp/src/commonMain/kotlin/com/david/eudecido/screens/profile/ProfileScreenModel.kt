package com.david.eudecido.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.SessionManager
import com.david.eudecido.data.UserRepository
import com.david.eudecido.models.UserActivity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProfileScreenModel(
    private val userRepository: UserRepository
) : ScreenModel {
    private val _userName = MutableStateFlow(SessionManager.currentUsername)
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow(SessionManager.currentEmail)
    val userEmail = _userEmail.asStateFlow()

    private val _userFreguesia = MutableStateFlow("Não definida")
    val userFreguesia = _userFreguesia.asStateFlow()

    private val _participationPoints = MutableStateFlow(0)
    val participationPoints: StateFlow<Int> = _participationPoints.asStateFlow()

    private val _activities = MutableStateFlow<List<UserActivity>>(emptyList())
    val activities = _activities.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        screenModelScope.launch {
            userRepository.getUser(SessionManager.currentUserId).collectLatest { user ->
                user?.let {
                    _userName.value = it.username
                    _userEmail.value = it.email ?: ""
                    // No futuro, buscar freguesia real do território associado
                    _userFreguesia.value = "Pendente" 
                }
            }
        }
        
        // Atividades serão carregadas do backend em fases futuras
        _activities.value = emptyList()
    }

    fun logout() {
        SessionManager.logout()
    }
}
