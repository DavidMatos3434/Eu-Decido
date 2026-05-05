package com.david.eudecido.screens.ai

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AIProposalHelperScreenModel : ScreenModel {
    private val _generatedTitle = MutableStateFlow<String?>(null)
    val generatedTitle = _generatedTitle.asStateFlow()

    private val _generatedSummary = MutableStateFlow<String?>(null)
    val generatedSummary = _generatedSummary.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    fun generateProposal(input: String) {
        if (input.isBlank()) return
        
        _isGenerating.value = true
        
        // Simulação de chamada à IA
        // No futuro, aqui chamaremos um serviço que interage com GPT/Gemini
        _generatedTitle.value = "Requalificação Sustentável: $input"
        _generatedSummary.value = "Esta proposta visa a implementação de melhorias baseadas na descrição: '$input', focando na eficiência e bem-estar dos cidadãos da freguesia."
        
        _isGenerating.value = false
    }
}
