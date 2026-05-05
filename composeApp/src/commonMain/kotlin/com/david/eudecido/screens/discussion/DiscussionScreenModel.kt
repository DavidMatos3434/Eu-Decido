package com.david.eudecido.screens.discussion

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
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

    init {
        loadComments()
    }

    private fun loadComments() {
        screenModelScope.launch {
            proposalRepository.getComments(proposalId).collectLatest { dbComments ->
                _comments.value = dbComments.map { dbComment ->
                    Comment(
                        id = dbComment.id,
                        author = "Utilizador", // No futuro virá do perfil associado
                        text = dbComment.content,
                        likes = 0 // Campo a ser implementado na Camada 3
                    )
                }
            }
        }
    }

    fun sendComment(text: String) {
        if (text.isBlank()) return
        screenModelScope.launch {
            val id = Clock.System.now().toEpochMilliseconds().toString()
            proposalRepository.addComment(
                id = id,
                proposalId = proposalId,
                userId = "current_user",
                content = text
            )
        }
    }
}
