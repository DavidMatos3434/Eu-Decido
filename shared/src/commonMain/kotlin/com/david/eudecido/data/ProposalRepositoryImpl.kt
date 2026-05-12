package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Proposals
import com.david.eudecido.db.Comments
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
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock

class ProposalRepositoryImpl(
    private val database: EuDecidoDatabase,
    private val httpClient: HttpClient,
    private val syncRepository: SyncRepository,
    private val baseUrl: String = "http://10.0.2.2:8000"
) : ProposalRepository {
    private val queries = database.proposalsQueries

    override fun getProposals(): Flow<List<Proposals>> {
        return queries.getAllProposals().asFlow().mapToList(Dispatchers.IO)
    }

    override fun getProposal(id: String): Flow<Proposals?> {
        return queries.getProposalById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    override suspend fun createProposal(
        id: String,
        userId: String,
        territoryId: String,
        title: String,
        description: String,
        status: String,
        type: String
    ) {
        queries.insertProposal(id, userId, territoryId, title, description, status, type, Clock.System.now().toString())
        
        val payload = """{"title":"$title","description":"$description","territory_id":"$territoryId","type":"$type"}"""
        syncRepository.addSyncItem("PROPOSAL", payload)
    }

    override fun getComments(proposalId: String): Flow<List<Comments>> {
        return queries.getCommentsByProposal(proposalId).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun addComment(id: String, proposalId: String, userId: String, content: String) {
        queries.insertComment(id, proposalId, userId, content, Clock.System.now().toString())
        
        val payload = """{"proposal_id":"$proposalId","content":"$content"}"""
        syncRepository.addSyncItem("COMMENT", payload)
    }

    override suspend fun vote(id: String, proposalId: String, voteValue: String, votingToken: String) {
        queries.insertVote(id, proposalId, voteValue, votingToken, Clock.System.now().toString())
        
        val payload = """{"proposal_id":"$proposalId","vote_value":"$voteValue","token_hash":"$votingToken"}"""
        syncRepository.addSyncItem("VOTE", payload)
    }

    override fun getVoteStats(proposalId: String): Flow<VoteStats> {
        return queries.countTotalVotes(proposalId).asFlow().mapToOneOrNull(Dispatchers.IO)
            .combine(queries.countVotesByValue(proposalId).asFlow().mapToList(Dispatchers.IO)) { total, byValue ->
                val totalCount = total ?: 0L
                val yes = byValue.find { it.vote_value == "SIM" }?.n_votes ?: 0L
                val no = byValue.find { it.vote_value == "NAO" }?.n_votes ?: 0L
                val abs = byValue.find { it.vote_value == "ABSTENCAO" }?.n_votes ?: 0L
                VoteStats(totalCount, yes, no, abs)
            }
    }

    override suspend fun syncProposals(): Result<Unit> {
        return try {
            val remoteProposals: List<ProposalResponse> = httpClient.get("$baseUrl/proposals").body()
            database.transaction {
                remoteProposals.forEach { remote ->
                    queries.insertProposal(remote.id, remote.user_id, remote.territory_id, remote.title, remote.description, remote.status, remote.type, remote.created_at)
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getVotingToken(proposalId: String?, electionId: String?): Result<VotingTokenResponse> {
        return try {
            val token = SessionManager.token ?: return Result.failure(Exception("Not logged in"))
            val response = httpClient.post("$baseUrl/voting/token") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(mapOf("proposal_id" to proposalId, "election_id" to electionId))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncComments(proposalId: String): Result<Unit> {
        return try {
            val remoteComments: List<CommentResponse> = httpClient.get("$baseUrl/proposals/$proposalId/comments").body()
            database.transaction {
                remoteComments.forEach { remote ->
                    queries.insertComment(
                        id = remote.id,
                        proposal_id = remote.proposal_id,
                        user_id = remote.user_id,
                        content = remote.content,
                        created_at = remote.created_at
                    )
                }
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncResults(proposalId: String?, electionId: String?): Result<VoteStats> {
        return try {
            val url = if (proposalId != null) "$baseUrl/voting/results?proposal_id=$proposalId" 
                      else "$baseUrl/voting/results?election_id=$electionId"
            
            val remoteResults: ResultsResponse = httpClient.get(url).body()
            
            val voteStats = VoteStats(
                total = remoteResults.total,
                yes = remoteResults.stats["yes"] ?: 0L,
                no = remoteResults.stats["no"] ?: 0L,
                abstention = remoteResults.stats["abstention"] ?: 0L
            )
            Result.success(voteStats)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
