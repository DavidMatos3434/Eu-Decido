package com.david.eudecido.screens.ai

import cafe.adriel.voyager.core.model.ScreenModel
import com.david.eudecido.models.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class GlobalAIScreenModel : ScreenModel {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Olá! Sou o teu assistente global. Em que posso ajudar?", false))
    )
    val messages = _messages.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        val current = _messages.value.toMutableList()
        current.add(ChatMessage(text, true))
        _messages.value = current
        
        // Simulação de resposta
        current.add(ChatMessage("Estou a analisar o teu pedido no contexto atual...", false))
        _messages.value = current.toList()
    }
}
