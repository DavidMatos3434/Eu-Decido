package com.david.eudecido.data

import com.david.eudecido.db.Proposals
import com.david.eudecido.db.Comments
import kotlinx.coroutines.flow.Flow

data class VoteStats(
    val total: Long,
    val yes: Long,
    val no: Long,
    val abstention: Long
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
        status: String
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
}
