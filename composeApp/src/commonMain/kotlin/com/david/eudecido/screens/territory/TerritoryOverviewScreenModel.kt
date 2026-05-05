package com.david.eudecido.screens.territory

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TerritoryOverviewScreenModel(
    val freguesiaName: String,
    val municipioName: String,
    val regiaoName: String
) : ScreenModel {
    private val _freguesia = MutableStateFlow(freguesiaName)
    val freguesia = _freguesia.asStateFlow()

    private val _municipio = MutableStateFlow(municipioName)
    val municipio = _municipio.asStateFlow()

    private val _regiao = MutableStateFlow(regiaoName)
    val regiao = _regiao.asStateFlow()
}
