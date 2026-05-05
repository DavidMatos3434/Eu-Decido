package com.david.eudecido.screens.proposals

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.ProposalRepository
import com.david.eudecido.data.SyncRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock

class CreateProposalScreenModel(
    private val proposalRepository: ProposalRepository,
    private val syncRepository: SyncRepository
) : ScreenModel {
    private val _title = MutableStateFlow("")
    val title = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting = _isSubmitting.asStateFlow()

    private val _success = MutableStateFlow(false)
    val success = _success.asStateFlow()

    fun onTitleChange(value: String) { _title.value = value }
    fun onDescriptionChange(value: String) { _description.value = value }

    fun submitProposal() {
        if (_title.value.isBlank() || _description.value.isBlank()) return
        _isSubmitting.value = true

        screenModelScope.launch {
            try {
                val proposalId = "p_" + Clock.System.now().toEpochMilliseconds().toString()
                val userId = "current_user" // No futuro virá do Auth
                val territoryId = "t1" // No futuro virá do perfil do utilizador

                // 1. Guardar localmente (Cache Camada 2)
                proposalRepository.createProposal(
                    id = proposalId,
                    userId = userId,
                    territoryId = territoryId,
                    title = _title.value,
                    description = _description.value,
                    status = "DISCUSSION"
                )

                // 2. Adicionar à fila de sincronização para o Backend (Offline-First)
                val payload = """
                    {
                        "id": "$proposalId",
                        "user_id": "$userId",
                        "territory_id": "$territoryId",
                        "title": "${_title.value.replace("\"", "\\\"")}",
                        "description": "${_description.value.replace("\"", "\\\"")}",
                        "status": "DISCUSSION"
                    }
                """.trimIndent()

                syncRepository.addSyncItem(
                    type = "CREATE_PROPOSAL",
                    payload = payload
                )

                _success.value = true
            } catch (e: Exception) {
                // Tratar erro
            } finally {
                _isSubmitting.value = false
            }
        }
    }
}
