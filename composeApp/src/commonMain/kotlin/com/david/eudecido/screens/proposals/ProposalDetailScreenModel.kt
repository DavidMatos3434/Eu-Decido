package com.david.eudecido.screens.proposals

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ProposalDetailScreenModel(
    private val proposalId: String,
    private val proposalRepository: ProposalRepository
) : ScreenModel {

    private val _title = MutableStateFlow("Carregando...")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _summary = MutableStateFlow("A gerar resumo...")
    val summary: StateFlow<String> = _summary.asStateFlow()

    private val _status = MutableStateFlow("")
    val status: StateFlow<String> = _status.asStateFlow()

    private val _totalVotes = MutableStateFlow(0L)
    val totalVotes: StateFlow<Long> = _totalVotes.asStateFlow()

    private val _yesPercent = MutableStateFlow(0)
    val yesPercent: StateFlow<Int> = _yesPercent.asStateFlow()

    private val _selectedVote = MutableStateFlow<String?>(null)
    val selectedVote: StateFlow<String?> = _selectedVote.asStateFlow()

    init {
        loadProposalDetails()
        loadVoteStats()
    }

    private fun loadProposalDetails() {
        screenModelScope.launch {
            proposalRepository.getProposal(proposalId).collectLatest { proposal ->
                proposal?.let {
                    _title.value = it.title
                    _description.value = it.description
                    // No futuro, o resumo virá da tabela agent_results
                    _summary.value = if (it.description.length > 100) it.description.take(100) + "..." else it.description
                    _status.value = it.status
                }
            }
        }
    }

    private fun loadVoteStats() {
        screenModelScope.launch {
            proposalRepository.getVoteStats(proposalId).collectLatest { stats ->
                _totalVotes.value = stats.total
                if (stats.total > 0) {
                    _yesPercent.value = ((stats.yes.toDouble() / stats.total) * 100).toInt()
                } else {
                    _yesPercent.value = 0
                }
            }
        }
    }

    fun onVoteSelect(vote: String) {
        _selectedVote.value = vote
    }
}
