package com.david.eudecido.screens.voting

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import com.david.eudecido.data.SessionManager
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

    fun onVoteSelect(vote: String) {
        _selectedVote.value = vote
    }

    fun submitVote() {
        val voteValue = _selectedVote.value ?: return
        _isSubmitting.value = true

        screenModelScope.launch {
            try {
                val voteId = "vote_${Clock.System.now().toEpochMilliseconds()}"
                // O token local serve apenas como referência de integridade local.
                // A validação real do token anónimo é feita pela Edge Function no backend.
                val localTokenRef = "local_${SessionManager.currentUserId}_${proposalId}"

                // 1. Guardar localmente (offline-first)
                proposalRepository.vote(
                    id = voteId,
                    proposalId = proposalId,
                    voteValue = voteValue,
                    votingToken = localTokenRef
                )

                // 2. Enfileirar para sincronização com o backend (Edge Function de votação anónima)
                val payload = """
                    {
                        "proposal_id": "$proposalId",
                        "vote_value": "$voteValue",
                        "token_ref": "$localTokenRef"
                    }
                """.trimIndent()

                syncRepository.addSyncItem(
                    type = "SUBMIT_VOTE",
                    payload = payload
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
