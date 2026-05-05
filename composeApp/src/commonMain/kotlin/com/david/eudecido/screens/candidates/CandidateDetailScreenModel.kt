package com.david.eudecido.screens.candidates

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class CandidateDetailScreenModel(val candidateId: String) : ScreenModel {
    // Mock data - Futuramente virá de um CandidateRepository
    private val _name = MutableStateFlow("António Silva")
    val name = _name.asStateFlow()

    private val _bio = MutableStateFlow("Especialista em Urbanismo com mais de 15 anos de experiência em planeamento de cidades sustentáveis. Defende a priorização do transporte público e ciclovias.")
    val bio = _bio.asStateFlow()

    private val _proposals = MutableStateFlow(listOf(
        "Expansão da rede de ciclovias em 20km",
        "Implementação de autocarros elétricos gratuitos no centro",
        "Criação de 3 novos parques urbanos"
    ))
    val proposals = _proposals.asStateFlow()
}
