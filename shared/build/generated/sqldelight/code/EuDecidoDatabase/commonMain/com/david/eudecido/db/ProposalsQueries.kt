package com.david.eudecido.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class ProposalsQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getAllProposals(mapper: (
    id: String,
    user_id: String,
    territory_id: String,
    title: String,
    description: String,
    status: String,
    created_at: String,
  ) -> T): Query<T> = Query(-961_914_434, arrayOf("proposals"), driver, "proposals.sq",
      "getAllProposals",
      "SELECT proposals.id, proposals.user_id, proposals.territory_id, proposals.title, proposals.description, proposals.\"status\", proposals.created_at FROM proposals ORDER BY created_at DESC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!
    )
  }

  public fun getAllProposals(): Query<Proposals> = getAllProposals { id, user_id, territory_id,
      title, description, status, created_at ->
    Proposals(
      id,
      user_id,
      territory_id,
      title,
      description,
      status,
      created_at
    )
  }

  public fun <T : Any> getProposalById(id: String, mapper: (
    id: String,
    user_id: String,
    territory_id: String,
    title: String,
    description: String,
    status: String,
    created_at: String,
  ) -> T): Query<T> = GetProposalByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!,
      cursor.getString(6)!!
    )
  }

  public fun getProposalById(id: String): Query<Proposals> = getProposalById(id) { id_, user_id,
      territory_id, title, description, status, created_at ->
    Proposals(
      id_,
      user_id,
      territory_id,
      title,
      description,
      status,
      created_at
    )
  }

  public fun <T : Any> getCommentsByProposal(proposal_id: String, mapper: (
    id: String,
    proposal_id: String,
    user_id: String,
    content: String,
    created_at: String,
  ) -> T): Query<T> = GetCommentsByProposalQuery(proposal_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun getCommentsByProposal(proposal_id: String): Query<Comments> =
      getCommentsByProposal(proposal_id) { id, proposal_id_, user_id, content, created_at ->
    Comments(
      id,
      proposal_id_,
      user_id,
      content,
      created_at
    )
  }

  public fun <T : Any> countVotesByValue(proposal_id: String, mapper: (vote_value: String,
      n_votes: Long) -> T): Query<T> = CountVotesByValueQuery(proposal_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getLong(1)!!
    )
  }

  public fun countVotesByValue(proposal_id: String): Query<CountVotesByValue> =
      countVotesByValue(proposal_id) { vote_value, n_votes ->
    CountVotesByValue(
      vote_value,
      n_votes
    )
  }

  public fun countTotalVotes(proposal_id: String): Query<Long> = CountTotalVotesQuery(proposal_id) {
      cursor ->
    cursor.getLong(0)!!
  }

  public fun <T : Any> getPendingSyncItems(mapper: (
    id: String,
    type: String,
    payload: String,
    status: String,
    created_at: String,
  ) -> T): Query<T> = Query(-534_924_884, arrayOf("sync_queue"), driver, "proposals.sq",
      "getPendingSyncItems",
      "SELECT sync_queue.id, sync_queue.\"type\", sync_queue.payload, sync_queue.\"status\", sync_queue.created_at FROM sync_queue WHERE \"status\" = 'PENDING' ORDER BY created_at ASC") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun getPendingSyncItems(): Query<Sync_queue> = getPendingSyncItems { id, type, payload,
      status, created_at ->
    Sync_queue(
      id,
      type,
      payload,
      status,
      created_at
    )
  }

  public fun insertProposal(
    id: String,
    user_id: String,
    territory_id: String,
    title: String,
    description: String,
    status: String,
    created_at: String,
  ) {
    driver.execute(234_503_075, """
        |INSERT OR REPLACE INTO proposals(id, user_id, territory_id, title, description, "status", created_at)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          bindString(0, id)
          bindString(1, user_id)
          bindString(2, territory_id)
          bindString(3, title)
          bindString(4, description)
          bindString(5, status)
          bindString(6, created_at)
        }
    notifyQueries(234_503_075) { emit ->
      emit("proposals")
    }
  }

  public fun insertComment(
    id: String,
    proposal_id: String,
    user_id: String,
    content: String,
    created_at: String,
  ) {
    driver.execute(-1_780_956_274, """
        |INSERT OR REPLACE INTO comments(id, proposal_id, user_id, content, created_at)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindString(0, id)
          bindString(1, proposal_id)
          bindString(2, user_id)
          bindString(3, content)
          bindString(4, created_at)
        }
    notifyQueries(-1_780_956_274) { emit ->
      emit("comments")
    }
  }

  public fun insertVote(
    id: String,
    proposal_id: String,
    vote_value: String,
    voting_token: String,
    created_at: String,
  ) {
    driver.execute(805_119_003, """
        |INSERT INTO votes(id, proposal_id, vote_value, voting_token, created_at)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindString(0, id)
          bindString(1, proposal_id)
          bindString(2, vote_value)
          bindString(3, voting_token)
          bindString(4, created_at)
        }
    notifyQueries(805_119_003) { emit ->
      emit("votes")
    }
  }

  public fun insertVotingToken(
    id: String,
    identity_id: String,
    proposal_id: String,
    token_hash: String,
    used: Long,
    created_at: String,
  ) {
    driver.execute(-142_921_023, """
        |INSERT INTO voting_tokens(id, identity_id, proposal_id, token_hash, "used", created_at)
        |VALUES (?, ?, ?, ?, ?, ?)
        """.trimMargin(), 6) {
          bindString(0, id)
          bindString(1, identity_id)
          bindString(2, proposal_id)
          bindString(3, token_hash)
          bindLong(4, used)
          bindString(5, created_at)
        }
    notifyQueries(-142_921_023) { emit ->
      emit("voting_tokens")
    }
  }

  public fun markTokenAsUsed(token_hash: String) {
    driver.execute(2_143_011_203, """UPDATE voting_tokens SET "used" = 1 WHERE token_hash = ?""", 1)
        {
          bindString(0, token_hash)
        }
    notifyQueries(2_143_011_203) { emit ->
      emit("voting_tokens")
    }
  }

  public fun updateSyncStatus(status: String, id: String) {
    driver.execute(-1_035_419_410, """UPDATE sync_queue SET "status" = ? WHERE id = ?""", 2) {
          bindString(0, status)
          bindString(1, id)
        }
    notifyQueries(-1_035_419_410) { emit ->
      emit("sync_queue")
    }
  }

  public fun insertSyncItem(
    id: String,
    type: String,
    payload: String,
    status: String,
    created_at: String,
  ) {
    driver.execute(-1_251_207_937, """
        |INSERT INTO sync_queue(id, "type", payload, "status", created_at)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindString(0, id)
          bindString(1, type)
          bindString(2, payload)
          bindString(3, status)
          bindString(4, created_at)
        }
    notifyQueries(-1_251_207_937) { emit ->
      emit("sync_queue")
    }
  }

  private inner class GetProposalByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("proposals", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("proposals", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_628_050_302,
        """SELECT proposals.id, proposals.user_id, proposals.territory_id, proposals.title, proposals.description, proposals."status", proposals.created_at FROM proposals WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "proposals.sq:getProposalById"
  }

  private inner class GetCommentsByProposalQuery<out T : Any>(
    public val proposal_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("comments", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("comments", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-647_890_533,
        """SELECT comments.id, comments.proposal_id, comments.user_id, comments.content, comments.created_at FROM comments WHERE proposal_id = ?""",
        mapper, 1) {
      bindString(0, proposal_id)
    }

    override fun toString(): String = "proposals.sq:getCommentsByProposal"
  }

  private inner class CountVotesByValueQuery<out T : Any>(
    public val proposal_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("votes", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("votes", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_167_212_840, """
    |SELECT vote_value, COUNT(*) AS n_votes
    |FROM votes 
    |WHERE proposal_id = ? 
    |GROUP BY vote_value
    """.trimMargin(), mapper, 1) {
      bindString(0, proposal_id)
    }

    override fun toString(): String = "proposals.sq:countVotesByValue"
  }

  private inner class CountTotalVotesQuery<out T : Any>(
    public val proposal_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("votes", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("votes", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(304_478_332, """SELECT COUNT(*) FROM votes WHERE proposal_id = ?""",
        mapper, 1) {
      bindString(0, proposal_id)
    }

    override fun toString(): String = "proposals.sq:countTotalVotes"
  }
}
