package com.david.eudecido.screens.elections

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ElectionRepository
import com.david.eudecido.db.Elections
import com.david.eudecido.db.Candidacies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

sealed class ElectionDetailState {
    object Loading : ElectionDetailState()
    data class Success(
        val election: Elections,
        val candidacies: List<Candidacies>
    ) : ElectionDetailState()
    object Error : ElectionDetailState()
}

class ElectionDetailScreenModel(
    private val electionId: String,
    private val electionRepository: ElectionRepository
) : ScreenModel {
    private val _state = MutableStateFlow<ElectionDetailState>(ElectionDetailState.Loading)
    val state: StateFlow<ElectionDetailState> = _state.asStateFlow()

    init {
        loadElectionDetails()
    }

    private fun loadElectionDetails() {
        screenModelScope.launch {
            try {
                electionRepository.getElectionById(electionId).collectLatest { election ->
                    if (election != null) {
                        electionRepository.getCandidacies(electionId).collectLatest { candidacies ->
                            _state.value = ElectionDetailState.Success(election, candidacies)
                        }
                    } else {
                        _state.value = ElectionDetailState.Error
                    }
                }
            } catch (e: Exception) {
                _state.value = ElectionDetailState.Error
            }
        }
    }
}
