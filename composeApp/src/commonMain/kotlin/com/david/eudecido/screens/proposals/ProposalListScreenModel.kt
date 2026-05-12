package com.david.eudecido.screens.proposals

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import com.david.eudecido.models.ProposalUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProposalListScreenModel(
    private val proposalRepository: ProposalRepository
) : ScreenModel {
    private val _proposals = MutableStateFlow<List<ProposalUI>>(emptyList())
    val proposals: StateFlow<List<ProposalUI>> = _proposals.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadProposals()
        refreshProposals()
    }

    private fun loadProposals() {
        screenModelScope.launch {
            proposalRepository.getProposals().collectLatest { dbProposals ->
                _proposals.value = dbProposals.map { dbProposal ->
                    val summary = if (dbProposal.description.length > 100) {
                        dbProposal.description.take(100) + "..."
                    } else {
                        dbProposal.description
                    }
                    ProposalUI(
                        id = dbProposal.id,
                        title = dbProposal.title,
                        summary = summary,
                        votes = 0, // No futuro buscar via estatísticas reais
                        approval = 0
                    )
                }
            }
        }
    }

    fun refreshProposals() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        screenModelScope.launch {
            proposalRepository.syncProposals()
            _isRefreshing.value = false
        }
    }
}
