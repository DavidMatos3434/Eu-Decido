package com.david.eudecido.screens.search

import cafe.adriel.voyager.core.model.ScreenModel
import com.david.eudecido.models.ProposalUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class SearchScreenModel : ScreenModel {
    private val _query = MutableStateFlow("")
    val query = _query.asStateFlow()

    private val _results = MutableStateFlow<List<ProposalUI>>(emptyList())
    val results = _results.asStateFlow()

    private val allProposals = listOf(
        ProposalUI("1", "Nova ciclovia na Avenida Principal", "Proposta para expandir a rede ciclável...", 120, 85),
        ProposalUI("2", "Iluminação LED no Parque Central", "Substituição das lâmpadas atuais por LED...", 45, 92),
        ProposalUI("3", "Orçamento Participativo 2024", "Votação das propostas selecionadas para este ano.", 300, 60),
        ProposalUI("4", "Requalificação da Escola Secundária", "Obras de melhoria nas infraestruturas escolares.", 80, 75)
    )

    fun onQueryChange(newQuery: String) {
        _query.value = newQuery
        _results.value = if (newQuery.isBlank()) {
            emptyList()
        } else {
            allProposals.filter { 
                it.title.contains(newQuery, ignoreCase = true) || 
                it.summary.contains(newQuery, ignoreCase = true) 
            }
        }
    }
}
