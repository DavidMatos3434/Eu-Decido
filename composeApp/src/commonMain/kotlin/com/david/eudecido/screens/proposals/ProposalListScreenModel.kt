package com.david.eudecido.screens.proposals

import cafe.adriel.voyager.core.model.ScreenModel
import com.david.eudecido.models.ProposalUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ProposalListScreenModel : ScreenModel {
    private val _proposals = MutableStateFlow<List<ProposalUI>>(emptyList())
    val proposals: StateFlow<List<ProposalUI>> = _proposals.asStateFlow()

    init {
        // Mock data
        _proposals.value = listOf(
            ProposalUI("1", "Nova ciclovia na Avenida Principal", "Proposta para expandir a rede ciclável...", 120, 85),
            ProposalUI("2", "Iluminação LED no Parque Central", "Substituição das lâmpadas atuais por LED...", 45, 92),
            ProposalUI("3", "Orçamento Participativo 2024", "Votação das propostas selecionadas para este ano.", 300, 60),
            ProposalUI("4", "Requalificação da Escola Secundária", "Obras de melhoria nas infraestruturas escolares.", 80, 75),
            ProposalUI("5", "Novo Parque Infantil", "Criação de um espaço de lazer para crianças.", 200, 95)
        )
    }
}
