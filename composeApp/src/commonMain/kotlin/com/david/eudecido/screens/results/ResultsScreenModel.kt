package com.david.eudecido.screens.results

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import com.david.eudecido.data.VoteStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
            proposalRepository.getProposal(proposalId)
                .flatMapLatest { proposal ->
                    if (proposal != null) {
                        proposalRepository.getVoteStats(proposalId).map { stats ->
                            calculateResults(proposal.title, stats)
                        }
                    } else {
                        flow { emit(ResultsState.Error as ResultsState) }
                    }
                }
                .catch { _state.value = ResultsState.Error }
                .collect { _state.value = it }
        }
    }

    private fun calculateResults(title: String, stats: VoteStats): ResultsState.Success {
        val total = stats.total

        val yesP = if (total > 0) ((stats.yes.toDouble() / total) * 100).toInt() else 0
        val noP = if (total > 0) ((stats.no.toDouble() / total) * 100).toInt() else 0
        val absP = if (total > 0) 100 - yesP - noP else 0

        return ResultsState.Success(
            title = title,
            totalVotes = total,
            yesPercent = yesP,
            noPercent = noP,
            abstentionPercent = absP,
            isApproved = yesP > 50
        )
    }
}
