package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Elections
import com.david.eudecido.db.Candidacies
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class ElectionRepositoryImpl(database: EuDecidoDatabase) : ElectionRepository {
    private val queries = database.governanceQueries

    override fun getActiveElections(): Flow<List<Elections>> {
        return queries.getElectionsByStatus("OPEN").asFlow().mapToList(Dispatchers.IO)
    }

    override fun getElectionById(id: String): Flow<Elections?> {
        return queries.getElectionById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    override fun getCandidacies(electionId: String): Flow<List<Candidacies>> {
        return queries.getCandidaciesByElection(electionId).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun createElection(
        id: String,
        title: String,
        territoryId: String,
        role: String,
        status: String
    ) {
        queries.insertElection(
            id = id,
            title = title,
            territory_id = territoryId,
            role = role,
            status = status,
            created_at = Clock.System.now().toString()
        )
    }

    override suspend fun applyForCandidacy(
        id: String,
        electionId: String,
        userId: String
    ) {
        queries.insertCandidacy(
            id = id,
            election_id = electionId,
            user_id = userId,
            status = "PENDING",
            created_at = Clock.System.now().toString()
        )
    }
}
