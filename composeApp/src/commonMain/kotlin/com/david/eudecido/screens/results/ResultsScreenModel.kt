package com.david.eudecido.screens.results

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import com.david.eudecido.data.VoteStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class ResultsState {
    object Loading : ResultsState()
    data class Success(
        val title: String,
        val totalVotes: Long,
        val yesPercent: Int,
        val noPercent: Int,
        val abstentionPercent: Int,
        val isApproved: Boolean
    ) : ResultsState()
    object Error : ResultsState()
}

class ResultsScreenModel(
    private val proposalId: String,
    private val proposalRepository: ProposalRepository
) : ScreenModel {
    private val _state = MutableStateFlow<ResultsState>(ResultsState.Loading)
    val state: StateFlow<ResultsState> = _state.asStateFlow()

    init {
        loadResults()
    }

    private fun loadResults() {
        screenModelScope.launch {
            try {
                // Observamos as estatísticas de voto e os detalhes da proposta em tempo real
                proposalRepository.getProposal(proposalId).collectLatest { proposal ->
                    if (proposal != null) {
                        proposalRepository.getVoteStats(proposalId).collectLatest { stats ->
                            _state.value = calculateResults(proposal.title, stats)
                        }
                    } else {
                        _state.value = ResultsState.Error
                    }
                }
            } catch (e: Exception) {
                _state.value = ResultsState.Error
            }
        }
    }

    private fun calculateResults(title: String, stats: VoteStats): ResultsState.Success {
        val total = if (stats.total == 0L) 1L else stats.total
        
        val yesP = ((stats.yes.toDouble() / total) * 100).toInt()
        val noP = ((stats.no.toDouble() / total) * 100).toInt()
        val absP = ((stats.abstention.toDouble() / total) * 100).toInt()
        
        return ResultsState.Success(
            title = title,
            totalVotes = stats.total,
            yesPercent = yesP,
            noPercent = noP,
            abstentionPercent = absP,
            isApproved = yesP > 50 // Critério simples de aprovação
        )
    }
}
