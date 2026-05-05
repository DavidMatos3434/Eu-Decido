package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Sync_queue
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class SyncRepositoryImpl(database: EuDecidoDatabase) : SyncRepository {
    private val queries = database.proposalsQueries

    override fun getPendingItems(): Flow<List<Sync_queue>> {
        return queries.getPendingSyncItems().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun addSyncItem(type: String, payload: String) {
        val id = Clock.System.now().toEpochMilliseconds().toString()
        queries.insertSyncItem(
            id = id,
            type = type,
            payload = payload,
            status = "PENDING",
            created_at = Clock.System.now().toString()
        )
    }

    override suspend fun updateStatus(id: String, status: String) {
        queries.updateSyncStatus(status, id)
    }
}
