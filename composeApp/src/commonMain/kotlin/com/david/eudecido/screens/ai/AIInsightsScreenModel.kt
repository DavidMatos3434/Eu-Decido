package com.david.eudecido.screens.ai

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AIInsightsScreenModel(
    val summaryInitial: String,
    val consensusInitial: String,
    val impactInitial: String
) : ScreenModel {
    private val _summary = MutableStateFlow(summaryInitial)
    val summary = _summary.asStateFlow()

    private val _consensus = MutableStateFlow(consensusInitial)
    val consensus = _consensus.asStateFlow()

    private val _impact = MutableStateFlow(impactInitial)
    val impact = _impact.asStateFlow()
}
