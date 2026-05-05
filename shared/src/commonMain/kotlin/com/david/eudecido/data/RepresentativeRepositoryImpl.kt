package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Representatives
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class RepresentativeRepositoryImpl(database: EuDecidoDatabase) : RepresentativeRepository {
    private val queries = database.governanceQueries

    override fun getRepresentatives(): Flow<List<Representatives>> {
        return queries.getAllRepresentatives().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun addRepresentative(id: String, userId: String, territoryId: String, role: String) {
        queries.insertRepresentative(
            id = id,
            user_id = userId,
            territory_id = territoryId,
            role = role,
            created_at = Clock.System.now().toString()
        )
    }
}
