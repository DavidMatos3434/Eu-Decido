package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Proposals
import com.david.eudecido.db.Comments
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock

class ProposalRepositoryImpl(database: EuDecidoDatabase) : ProposalRepository {
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
        status: String
    ) {
        queries.insertProposal(
            id = id,
            user_id = userId,
            territory_id = territoryId,
            title = title,
            description = description,
            status = status, // SQLDelight 2.0 gera o nome correto se protegido no SQL
            created_at = Clock.System.now().toString()
        )
    }

    override fun getComments(proposalId: String): Flow<List<Comments>> {
        return queries.getCommentsByProposal(proposalId).asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun addComment(
        id: String,
        proposalId: String,
        userId: String,
        content: String
    ) {
        queries.insertComment(
            id = id,
            proposal_id = proposalId,
            user_id = userId,
            content = content,
            created_at = Clock.System.now().toString()
        )
    }

    override suspend fun vote(
        id: String,
        proposalId: String,
        voteValue: String,
        votingToken: String
    ) {
        queries.insertVote(
            id = id,
            proposal_id = proposalId,
            vote_value = voteValue,
            voting_token = votingToken,
            created_at = Clock.System.now().toString()
        )
    }

    override fun getVoteStats(proposalId: String): Flow<VoteStats> {
        return queries.countTotalVotes(proposalId).asFlow().mapToOneOrNull(Dispatchers.IO)
            .combine(queries.countVotesByValue(proposalId).asFlow().mapToList(Dispatchers.IO)) { total, byValue ->
                val totalCount = total ?: 0L
                // Usando o alias 'n_votes' que definimos no proposals.sq
                val yes = byValue.find { it.vote_value == "SIM" }?.n_votes ?: 0L
                val no = byValue.find { it.vote_value == "NAO" }?.n_votes ?: 0L
                val abs = byValue.find { it.vote_value == "ABSTENCAO" }?.n_votes ?: 0L
                
                VoteStats(totalCount, yes, no, abs)
            }
    }
}
