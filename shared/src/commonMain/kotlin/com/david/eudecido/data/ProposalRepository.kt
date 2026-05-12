package com.david.eudecido.data

import com.david.eudecido.db.Proposals
import com.david.eudecido.db.Comments
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.Serializable

@Serializable
data class VoteStats(
    val total: Long,
    val yes: Long,
    val no: Long,
    val abstention: Long
)

@Serializable
data class VotingTokenResponse(
    val token: String,
    val token_hash: String
)

interface ProposalRepository {
    fun getProposals(): Flow<List<Proposals>>
    fun getProposal(id: String): Flow<Proposals?>
    
    suspend fun createProposal(
        id: String,
        userId: String,
        territoryId: String,
        title: String,
        description: String,
        status: String,
        type: String = "IDEIA"
    )

    fun getComments(proposalId: String): Flow<List<Comments>>
    
    suspend fun addComment(
        id: String,
        proposalId: String,
        userId: String,
        content: String
    )

    suspend fun vote(
        id: String,
        proposalId: String,
        voteValue: String,
        votingToken: String
    )

    fun getVoteStats(proposalId: String): Flow<VoteStats>
    
    suspend fun syncProposals(): Result<Unit>

    suspend fun getVotingToken(proposalId: String?, electionId: String?): Result<VotingTokenResponse>

    suspend fun syncComments(proposalId: String): Result<Unit>

    // Sincronização de resultados globais
    suspend fun syncResults(proposalId: String?, electionId: String?): Result<VoteStats>
}
