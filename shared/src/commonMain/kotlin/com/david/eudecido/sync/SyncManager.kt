package com.david.eudecido.sync

import com.david.eudecido.data.SyncRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SyncManager(
    private val syncRepository: SyncRepository
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        startSyncLoop()
    }

    private fun startSyncLoop() {
        scope.launch {
            // Observa itens pendentes na fila
            syncRepository.getPendingItems().collectLatest { pendingItems ->
                if (pendingItems.isNotEmpty()) {
                    println("SyncManager: Encontrados ${pendingItems.size} itens para sincronizar.")
                    for (item in pendingItems) {
                        processSyncItem(item.id, item.type, item.payload)
                    }
                }
            }
        }
    }

    private suspend fun processSyncItem(id: String, type: String, payload: String) {
        try {
            println("SyncManager: A sincronizar item $id ($type)...")
            
            // Simulação de chamada ao Supabase
            // No futuro, aqui usaremos o Supabase Client
            val success = simulateNetworkCall(type, payload)

            if (success) {
                syncRepository.updateStatus(id, "SENT")
                println("SyncManager: Item $id sincronizado com sucesso.")
            } else {
                syncRepository.updateStatus(id, "FAILED")
                println("SyncManager: Falha ao sincronizar item $id.")
            }
        } catch (e: Exception) {
            syncRepository.updateStatus(id, "FAILED")
            println("SyncManager: Erro crítico ao processar item $id: ${e.message}")
        }
    }

    private suspend fun simulateNetworkCall(type: String, payload: String): Boolean {
        delay(2000) // Simula latência de rede
        return true // Assume sucesso por agora
    }
}
