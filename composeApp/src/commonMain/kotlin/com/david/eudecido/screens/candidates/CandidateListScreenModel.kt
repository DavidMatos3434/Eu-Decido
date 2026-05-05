package com.david.eudecido.screens.candidates

import cafe.adriel.voyager.core.model.ScreenModel
import com.david.eudecido.models.Candidate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class CandidateListScreenModel : ScreenModel {
    private val _candidates = MutableStateFlow<List<Candidate>>(emptyList())
    val candidates: StateFlow<List<Candidate>> = _candidates.asStateFlow()

    init {
        // Mock data
        _candidates.value = listOf(
            Candidate("1", "António Silva", "Especialista em Urbanismo e Mobilidade Sustentável."),
            Candidate("2", "Beatriz Santos", "Defensora de energias renováveis e espaços verdes."),
            Candidate("3", "Carlos Pereira", "Focado na transparência governativa e inovação digital.")
        )
    }
}
