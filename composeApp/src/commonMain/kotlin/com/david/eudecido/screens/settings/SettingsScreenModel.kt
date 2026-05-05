package com.david.eudecido.screens.settings

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsScreenModel(
    private val userRepository: UserRepository
) : ScreenModel {
    private val _notificationsEnabled = MutableStateFlow(true)
    val notificationsEnabled = _notificationsEnabled.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess = _deleteSuccess.asStateFlow()

    fun toggleNotifications(enabled: Boolean) {
        _notificationsEnabled.value = enabled
        // Futuramente: Gravar preferência no DataStore ou DB local
    }

    fun deleteAccount() {
        screenModelScope.launch {
            try {
                // No futuro: Chamar backend para eliminar identidade real
                // Por agora: Limpamos o estado de sucesso para navegação
                _deleteSuccess.value = true
            } catch (e: Exception) {
                // Tratar erro
            }
        }
    }
}
