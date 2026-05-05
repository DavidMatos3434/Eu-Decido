package com.david.eudecido.screens.territory

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class TerritorySelectionScreenModel : ScreenModel {
    private val _freguesia = MutableStateFlow("")
    val freguesia: StateFlow<String> = _freguesia.asStateFlow()

    private val _municipio = MutableStateFlow("")
    val municipio: StateFlow<String> = _municipio.asStateFlow()

    private val _regiao = MutableStateFlow("")
    val regiao: StateFlow<String> = _regiao.asStateFlow()

    fun onFreguesiaChange(value: String) {
        _freguesia.value = value
    }

    fun onMunicipioChange(value: String) {
        _municipio.value = value
    }

    fun onRegiaoChange(value: String) {
        _regiao.value = value
    }

    fun confirmSelection() {
        // Lógica para guardar a localização (ex: no Repositório ou SharedPreferences)
    }
}
