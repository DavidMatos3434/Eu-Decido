package com.david.eudecido.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
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
    private val _userName = MutableStateFlow("Carregando...")
    val userName = _userName.asStateFlow()

    private val _userEmail = MutableStateFlow("")
    val userEmail = _userEmail.asStateFlow()

    private val _userFreguesia = MutableStateFlow("Não definida")
    val userFreguesia = _userFreguesia.asStateFlow()

    private val _participationPoints = MutableStateFlow(0)
    val participationPoints = _participationPoints.asStateFlow()

    private val _activities = MutableStateFlow<List<UserActivity>>(emptyList())
    val activities = _activities.asStateFlow()

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        screenModelScope.launch {
            // "current_user" é o ID temporário que usamos no Seeder/Login
            userRepository.getUser("current_user").collectLatest { user ->
                user?.let {
                    _userName.value = it.username
                    _userEmail.value = it.email ?: ""
                    // No futuro buscaremos o nome da freguesia via territory_id
                    _userFreguesia.value = "Arroios" 
                }
            }
        }
        
        // Mock de atividades por agora, até termos uma tabela de logs/atividades
        _activities.value = listOf(
            UserActivity("Votou na Proposta #12", "Há 2 dias"),
            UserActivity("Comentou em 'Nova Ciclovia'", "Há 3 dias"),
            UserActivity("Criou a proposta 'Horta Comunitária'", "Há 1 semana")
        )
    }

    fun logout() {
        // No futuro: limpar base de dados local e tokens do Supabase
    }
}
