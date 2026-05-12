package com.david.eudecido.screens.candidates

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ElectionRepository
import com.david.eudecido.models.Candidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CandidateListScreenModel(
    private val electionId: String,
    private val electionRepository: ElectionRepository
) : ScreenModel {
    private val _candidates = MutableStateFlow<List<Candidate>>(emptyList())
    val candidates: StateFlow<List<Candidate>> = _candidates.asStateFlow()

    init {
        loadCandidacies()
        refreshCandidacies()
    }

    private fun loadCandidacies() {
        screenModelScope.launch {
            electionRepository.getCandidacies(electionId).collectLatest { dbCandidacies ->
                _candidates.value = dbCandidacies.map { cand ->
                    Candidate(
                        id = cand.id,
                        name = "Candidato ${cand.user_id.takeLast(4)}", // Idealmente buscar username real
                        shortBio = cand.manifesto ?: "Sem manifesto disponível."
                    )
                }
            }
        }
    }

    fun refreshCandidacies() {
        screenModelScope.launch {
            electionRepository.syncCandidacies(electionId)
        }
    }
}
