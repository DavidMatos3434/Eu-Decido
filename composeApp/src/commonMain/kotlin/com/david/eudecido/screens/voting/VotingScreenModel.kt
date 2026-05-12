package com.david.eudecido.screens.voting

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import com.david.eudecido.data.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class VotingScreenModel(
    private val proposalId: String,
    private val proposalTitle: String,
    private val proposalRepository: ProposalRepository,
    private val syncRepository: SyncRepository
) : ScreenModel {
    private val _selectedVote = MutableStateFlow<String?>(null)
    val selectedVote = _selectedVote.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _voteSuccess = MutableStateFlow(false)
    val voteSuccess = _voteSuccess.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

    fun onVoteSelect(vote: String) {
        _selectedVote.value = vote
    }

    fun submitVote() {
        val voteValue = _selectedVote.value ?: return
        _isSubmitting.value = true
        _errorMessage.value = null

        screenModelScope.launch {
            // 1. Obter um token de votação real e anónimo do backend
            proposalRepository.getVotingToken(proposalId, null)
                .onSuccess { tokenResponse ->
                    try {
                        val voteId = "vote_${Clock.System.now().toEpochMilliseconds()}"
                        val realToken = tokenResponse.token

                        // 2. Guardar localmente (offline-first)
                        proposalRepository.vote(
                            id = voteId,
                            proposalId = proposalId,
                            voteValue = voteValue,
                            votingToken = tokenResponse.token_hash // Guardamos o hash localmente para auditoria
                        )

                        // 3. Enfileirar para sincronização real (voto anónimo)
                        val payload = """
                            {
                                "proposal_id": "$proposalId",
                                "vote_value": "$voteValue",
                                "token_hash": "$realToken"
                            }
                        """.trimIndent()

                        syncRepository.addSyncItem(
                            type = "VOTE",
                            payload = payload
                        )

                        _voteSuccess.value = true
                    } catch (e: Exception) {
                        _errorMessage.value = "Erro ao processar voto localmente."
                    }
                }
                .onFailure { e ->
                    _errorMessage.value = "Não foi possível obter permissão para votar: ${e.message}"
                }
            
            _isSubmitting.value = false
        }
    }
}
