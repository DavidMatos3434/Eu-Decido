package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Elections
import com.david.eudecido.db.Candidacies
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class ElectionRepositoryImpl(
    private val database: EuDecidoDatabase,
    private val httpClient: HttpClient,
    private val syncRepository: SyncRepository,
    private val baseUrl: String = "http://10.0.2.2:8000"
) : ElectionRepository {
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
        userId: String,
        manifesto: String
    ) {
        queries.insertCandidacy(
            id = id,
            election_id = electionId,
            user_id = userId,
            status = "PENDING",
            manifesto = manifesto,
            created_at = Clock.System.now().toString()
        )
        
        val payload = """{"manifesto":"$manifesto"}"""
        syncRepository.addSyncItem("CANDIDACY:$electionId", payload)
    }

    override suspend fun syncElections(): Result<Unit> {
        return try {
            val remoteElections: List<ElectionResponse> = httpClient.get("$baseUrl/elections").body()
            database.transaction {
                remoteElections.forEach { remote ->
                    queries.insertElection(
                        id = remote.id,
                        title = remote.title,
                        territory_id = remote.territory_id ?: "",
                        role = remote.role,
                        status = remote.status,
                        created_at = remote.created_at
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncCandidacies(electionId: String): Result<Unit> {
        return try {
            val remoteCandidacies: List<CandidacyResponse> = httpClient.get("$baseUrl/elections/$electionId/candidacies").body()
            database.transaction {
                remoteCandidacies.forEach { remote ->
                    queries.insertCandidacy(
                        id = remote.id,
                        election_id = remote.election_id,
                        user_id = remote.user_id,
                        status = remote.status,
                        manifesto = remote.manifesto,
                        created_at = remote.created_at
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
