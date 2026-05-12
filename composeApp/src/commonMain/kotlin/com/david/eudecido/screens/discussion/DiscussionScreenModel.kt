package com.david.eudecido.screens.discussion

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import com.david.eudecido.data.SessionManager
import com.david.eudecido.models.Comment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class DiscussionScreenModel(
    private val proposalId: String,
    private val proposalRepository: ProposalRepository
) : ScreenModel {
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments = _comments.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    init {
        loadComments()
        refreshComments()
    }

    private fun loadComments() {
        screenModelScope.launch {
            proposalRepository.getComments(proposalId).collectLatest { dbComments ->
                _comments.value = dbComments.map { dbComment ->
                    Comment(
                        id = dbComment.id,
                        author = "Utilizador", // No futuro buscar username via JOIN ou cache
                        text = dbComment.content,
                        likes = 0
                    )
                }
            }
        }
    }

    fun refreshComments() {
        if (_isRefreshing.value) return
        _isRefreshing.value = true
        screenModelScope.launch {
            proposalRepository.syncComments(proposalId)
            _isRefreshing.value = false
        }
    }

    fun sendComment(text: String) {
        if (text.isBlank()) return
        screenModelScope.launch {
            val id = "c_" + Clock.System.now().toEpochMilliseconds().toString()
            proposalRepository.addComment(
                id = id,
                proposalId = proposalId,
                userId = SessionManager.currentUserId,
                content = text
            )
            // A sincronização de saída é gerida pelo SyncManager automaticamente
        }
    }
}
