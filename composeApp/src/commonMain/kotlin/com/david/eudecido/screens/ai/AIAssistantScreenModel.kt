package com.david.eudecido.screens.ai

import cafe.adriel.voyager.core.model.ScreenModel
import com.david.eudecido.models.ChatMessage
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AIAssistantScreenModel : ScreenModel {
    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(
            ChatMessage("Olá! Sou o teu assistente político IA. Como posso ajudar-te a compreender esta proposta?", false)
        )
    )
    val messages = _messages.asStateFlow()

    fun sendMessage(text: String) {
        if (text.isBlank()) return
        
        // Adiciona mensagem do utilizador
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(text, true))
        _messages.value = currentMessages

        // Simulação de resposta da IA
        // No futuro, aqui chamaremos o agente de IA (OpenAI, Gemini, etc.)
        generateAIResponse(text)
    }

    private fun generateAIResponse(userText: String) {
        val response = when {
            userText.contains("custo", ignoreCase = true) -> "Esta proposta tem um impacto orçamental estimado de 50.000€ para a freguesia."
            userText.contains("prazo", ignoreCase = true) -> "A execução está prevista para começar no segundo semestre de 2024."
            else -> "Essa é uma excelente questão. Com base no texto da proposta, o objetivo principal é aumentar a qualidade de vida local."
        }
        
        val currentMessages = _messages.value.toMutableList()
        currentMessages.add(ChatMessage(response, false))
        _messages.value = currentMessages
    }
}
