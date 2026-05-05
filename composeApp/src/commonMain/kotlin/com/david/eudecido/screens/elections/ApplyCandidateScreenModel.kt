package com.david.eudecido.screens.elections

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ElectionRepository
import com.david.eudecido.data.SessionManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

sealed class ApplyStep {
    object Info : ApplyStep()
    object Verifying : ApplyStep()
    object Success : ApplyStep()
    data class Error(val message: String) : ApplyStep()
}

class ApplyCandidateScreenModel(
    private val electionId: String,
    private val electionRepository: ElectionRepository
) : ScreenModel {

    private val _step = MutableStateFlow<ApplyStep>(ApplyStep.Info)
    val step: StateFlow<ApplyStep> = _step.asStateFlow()

    fun startVerification() {
        _step.value = ApplyStep.Verifying

        screenModelScope.launch {
            try {
                delay(3000)

                val candidacyId = "cand_" + Clock.System.now().toEpochMilliseconds()
                electionRepository.applyForCandidacy(
                    id = candidacyId,
                    electionId = electionId,
                    userId = SessionManager.currentUserId
                )

                _step.value = ApplyStep.Success
            } catch (e: Exception) {
                _step.value = ApplyStep.Error("Falha na verificação de identidade.")
            }
        }
    }
}
