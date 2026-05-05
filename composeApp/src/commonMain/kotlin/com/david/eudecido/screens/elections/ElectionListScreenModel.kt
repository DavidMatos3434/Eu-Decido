package com.david.eudecido.screens.elections

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ElectionRepository
import com.david.eudecido.db.Elections
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ElectionListScreenModel(
    private val electionRepository: ElectionRepository
) : ScreenModel {
    private val _elections = MutableStateFlow<List<Elections>>(emptyList())
    val elections: StateFlow<List<Elections>> = _elections.asStateFlow()

    init {
        loadElections()
    }

    private fun loadElections() {
        screenModelScope.launch {
            electionRepository.getActiveElections().collectLatest {
                _elections.value = it
            }
        }
    }
}
