package com.david.eudecido.screens.report

import cafe.adriel.voyager.core.model.ScreenModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportScreenModel(
    val reportTitle: String,
    val reportDescription: String,
    val participationCount: Int,
    val arguments: List<String>
) : ScreenModel {
    private val _title = MutableStateFlow(reportTitle)
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow(reportDescription)
    val description = _description.asStateFlow()

    private val _participation = MutableStateFlow(participationCount)
    val participation = _participation.asStateFlow()

    private val _topArguments = MutableStateFlow(arguments)
    val topArguments = _topArguments.asStateFlow()
}
