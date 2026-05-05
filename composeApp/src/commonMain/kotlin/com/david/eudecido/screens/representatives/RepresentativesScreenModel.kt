package com.david.eudecido.screens.representatives

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.RepresentativeRepository
import com.david.eudecido.models.Representative
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class RepresentativesScreenModel(
    private val representativeRepository: RepresentativeRepository
) : ScreenModel {
    private val _representatives = MutableStateFlow<List<Representative>>(emptyList())
    val representatives: StateFlow<List<Representative>> = _representatives.asStateFlow()

    init {
        loadRepresentatives()
    }

    private fun loadRepresentatives() {
        screenModelScope.launch {
            representativeRepository.getRepresentatives().collectLatest { dbReps ->
                _representatives.value = dbReps.map { dbRep ->
                    Representative(
                        id = dbRep.id,
                        name = "Representante", // No futuro, buscar o nome real pelo user_id
                        role = dbRep.role
                    )
                }
            }
        }
    }
}
