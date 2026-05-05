package com.david.eudecido.screens.voting

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class VotingScreenModel(
    private val proposalId: String,
    private val proposalTitle: String,
    private val proposalRepository: ProposalRepository
) : ScreenModel {
    private val _selectedVote = MutableStateFlow<String?>(null)
    val selectedVote = _selectedVote.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _voteSuccess = MutableStateFlow(false)
    val voteSuccess = _voteSuccess.asStateFlow()

    fun onVoteSelect(vote: String) {
        _selectedVote.value = vote
    }

    fun submitVote() {
        val voteValue = _selectedVote.value ?: return
        _isSubmitting.value = true

        screenModelScope.launch {
            try {
                // No fluxo seguro, o token seria buscado na tabela 'voting_tokens'
                // Aqui geramos um para o protótipo que respeita o anonimato
                val tempToken = "token_${Clock.System.now().toEpochMilliseconds()}"
                
                proposalRepository.vote(
                    id = Clock.System.now().toEpochMilliseconds().toString(),
                    proposalId = proposalId,
                    voteValue = voteValue,
                    votingToken = tempToken
                )
                
                _voteSuccess.value = true
            } catch (e: Exception) {
                // Tratar erro
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
