package com.david.eudecido.screens.profile

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class Delegate(val id: String, val name: String, val specialty: String)

class DelegationScreenModel : ScreenModel {
    private val _delegates = MutableStateFlow(listOf(
        Delegate("1", "Ana Marta", "Especialista em Educação"),
        Delegate("2", "Ricardo Jorge", "Urbanista"),
        Delegate("3", "Sílvia Antunes", "Direitos Civis")
    ))
    val delegates = _delegates.asStateFlow()

    fun delegateVote(toId: String) {
        // Lógica para delegar voto
    }
}
