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

    private val _summary = MutableStateFlow("")
    val summary: StateFlow<String> = _summary.asStateFlow()

    private val _votes = MutableStateFlow(0)
    val votes: StateFlow<Int> = _votes.asStateFlow()

    private val _approval = MutableStateFlow(0)
    val approval: StateFlow<Int> = _approval.asStateFlow()

    private val _selectedVote = MutableStateFlow<String?>(null)
    val selectedVote = _selectedVote.asStateFlow()

    init {
        loadProposalDetails()
    }

    private fun loadProposalDetails() {
        screenModelScope.launch {
            proposalRepository.getProposal(proposalId).collectLatest { proposal ->
                proposal?.let {
                    _title.value = it.title
                    _description.value = it.description
                    _summary.value = it.description.take(100) + "..."
                }
            }
        }
    }

    fun onVoteSelect(vote: String) {
        _selectedVote.value = vote
    }
}
