package com.david.eudecido.screens.home

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import com.david.eudecido.data.SessionManager
import com.david.eudecido.data.UserRepository
import com.david.eudecido.models.ProposalUI
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeScreenModel(
    private val proposalRepository: ProposalRepository,
    private val userRepository: UserRepository
) : ScreenModel {
    private val _proposals = MutableStateFlow<List<ProposalUI>>(emptyList())
    val proposals: StateFlow<List<ProposalUI>> = _proposals.asStateFlow()

    private val _userName = MutableStateFlow(SessionManager.currentUsername)
    val userName: StateFlow<String> = _userName.asStateFlow()

    init {
        loadProposals()
        loadUserData()
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
                        votes = 0,
                        approval = 0
                    )
                }
            }
        }
    }

    private fun loadUserData() {
        screenModelScope.launch {
            userRepository.getUser(SessionManager.currentUserId).collectLatest { user ->
                user?.let {
                    _userName.value = it.username
                    SessionManager.login(
                        userId = it.id,
                        username = it.username,
                        email = it.email ?: ""
                    )
                }
            }
        }
    }
}
