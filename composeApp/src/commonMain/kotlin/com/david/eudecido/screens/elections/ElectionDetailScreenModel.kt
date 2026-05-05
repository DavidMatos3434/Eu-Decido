package com.david.eudecido.screens.elections

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ElectionRepository
import com.david.eudecido.db.Candidacies
import com.david.eudecido.db.Elections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
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
            electionRepository.getElectionById(electionId)
                .flatMapLatest { election ->
                    if (election != null) {
                        electionRepository.getCandidacies(electionId).map { candidacies ->
                            ElectionDetailState.Success(election, candidacies) as ElectionDetailState
                        }
                    } else {
                        flow { emit(ElectionDetailState.Error as ElectionDetailState) }
                    }
                }
                .catch { _state.value = ElectionDetailState.Error }
                .collect { _state.value = it }
        }
    }
}
