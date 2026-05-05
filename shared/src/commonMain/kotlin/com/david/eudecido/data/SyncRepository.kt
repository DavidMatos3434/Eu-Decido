package com.david.eudecido.data

import com.david.eudecido.db.Sync_queue
import kotlinx.coroutines.flow.Flow

interface SyncRepository {
    fun getPendingItems(): Flow<List<Sync_queue>>
    suspend fun addSyncItem(type: String, payload: String)
    suspend fun updateStatus(id: String, status: String)
}
