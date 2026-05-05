package com.david.eudecido.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class GovernanceQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getAllCandidates(mapper: (
    id: String,
    user_id: String,
    display_name: String?,
    bio: String?,
    photo_url: String?,
    verified: Long,
    created_at: String,
  ) -> T): Query<T> = Query(-1_978_517_534, arrayOf("candidate_profiles"), driver, "governance.sq",
      "getAllCandidates",
      "SELECT candidate_profiles.id, candidate_profiles.user_id, candidate_profiles.display_name, candidate_profiles.bio, candidate_profiles.photo_url, candidate_profiles.verified, candidate_profiles.created_at FROM candidate_profiles") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2),
      cursor.getString(3),
      cursor.getString(4),
      cursor.getLong(5)!!,
      cursor.getString(6)!!
    )
  }

  public fun getAllCandidates(): Query<Candidate_profiles> = getAllCandidates { id, user_id,
      display_name, bio, photo_url, verified, created_at ->
    Candidate_profiles(
      id,
      user_id,
      display_name,
      bio,
      photo_url,
      verified,
      created_at
    )
  }

  public fun <T : Any> getAllRepresentatives(mapper: (
    id: String,
    user_id: String,
    territory_id: String,
    role: String,
    created_at: String,
  ) -> T): Query<T> = Query(1_095_682_340, arrayOf("representatives"), driver, "governance.sq",
      "getAllRepresentatives",
      "SELECT representatives.id, representatives.user_id, representatives.territory_id, representatives.\"role\", representatives.created_at FROM representatives") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun getAllRepresentatives(): Query<Representatives> = getAllRepresentatives { id, user_id,
      territory_id, role, created_at ->
    Representatives(
      id,
      user_id,
      territory_id,
      role,
      created_at
    )
  }

  public fun <T : Any> getElectionsByStatus(`value`: String, mapper: (
    id: String,
    title: String,
    territory_id: String,
    role: String,
    status: String,
    created_at: String,
  ) -> T): Query<T> = GetElectionsByStatusQuery(value) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun getElectionsByStatus(value_: String): Query<Elections> = getElectionsByStatus(value_) {
      id, title, territory_id, role, status, created_at ->
    Elections(
      id,
      title,
      territory_id,
      role,
      status,
      created_at
    )
  }

  public fun <T : Any> getElectionById(id: String, mapper: (
    id: String,
    title: String,
    territory_id: String,
    role: String,
    status: String,
    created_at: String,
  ) -> T): Query<T> = GetElectionByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun getElectionById(id: String): Query<Elections> = getElectionById(id) { id_, title,
      territory_id, role, status, created_at ->
    Elections(
      id_,
      title,
      territory_id,
      role,
      status,
      created_at
    )
  }

  public fun <T : Any> getCandidaciesByElection(election_id: String, mapper: (
    id: String,
    election_id: String,
    user_id: String,
    status: String,
    created_at: String,
  ) -> T): Query<T> = GetCandidaciesByElectionQuery(election_id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)!!,
      cursor.getString(4)!!
    )
  }

  public fun getCandidaciesByElection(election_id: String): Query<Candidacies> =
      getCandidaciesByElection(election_id) { id, election_id_, user_id, status, created_at ->
    Candidacies(
      id,
      election_id_,
      user_id,
      status,
      created_at
    )
  }

  public fun insertCandidate(
    id: String,
    user_id: String,
    display_name: String?,
    bio: String?,
    photo_url: String?,
    verified: Long,
    created_at: String,
  ) {
    driver.execute(-1_530_626_525, """
        |INSERT OR REPLACE INTO candidate_profiles(id, user_id, display_name, bio, photo_url, verified, created_at)
        |VALUES (?, ?, ?, ?, ?, ?, ?)
        """.trimMargin(), 7) {
          bindString(0, id)
          bindString(1, user_id)
          bindString(2, display_name)
          bindString(3, bio)
          bindString(4, photo_url)
          bindLong(5, verified)
          bindString(6, created_at)
        }
    notifyQueries(-1_530_626_525) { emit ->
      emit("candidate_profiles")
    }
  }

  public fun insertRepresentative(
    id: String,
    user_id: String,
    territory_id: String,
    role: String,
    created_at: String,
  ) {
    driver.execute(2_116_408_573, """
        |INSERT OR REPLACE INTO representatives(id, user_id, territory_id, "role", created_at)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindString(0, id)
          bindString(1, user_id)
          bindString(2, territory_id)
          bindString(3, role)
          bindString(4, created_at)
        }
    notifyQueries(2_116_408_573) { emit ->
      emit("representatives")
    }
  }

  public fun insertElection(
    id: String,
    title: String,
    territory_id: String,
    role: String,
    status: String,
    created_at: String,
  ) {
    driver.execute(-1_606_936_711, """
        |INSERT INTO elections(id, title, territory_id, "role", "status", created_at)
        |VALUES (?, ?, ?, ?, ?, ?)
        """.trimMargin(), 6) {
          bindString(0, id)
          bindString(1, title)
          bindString(2, territory_id)
          bindString(3, role)
          bindString(4, status)
          bindString(5, created_at)
        }
    notifyQueries(-1_606_936_711) { emit ->
      emit("elections")
    }
  }

  public fun insertCandidacy(
    id: String,
    election_id: String,
    user_id: String,
    status: String,
    created_at: String,
  ) {
    driver.execute(-1_530_627_032, """
        |INSERT INTO candidacies(id, election_id, user_id, "status", created_at)
        |VALUES (?, ?, ?, ?, ?)
        """.trimMargin(), 5) {
          bindString(0, id)
          bindString(1, election_id)
          bindString(2, user_id)
          bindString(3, status)
          bindString(4, created_at)
        }
    notifyQueries(-1_530_627_032) { emit ->
      emit("candidacies")
    }
  }

  private inner class GetElectionsByStatusQuery<out T : Any>(
    public val `value`: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("elections", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("elections", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(204_886_100,
        """SELECT elections.id, elections.title, elections.territory_id, elections."role", elections."status", elections.created_at FROM elections WHERE "status" = ? ORDER BY created_at DESC""",
        mapper, 1) {
      bindString(0, value)
    }

    override fun toString(): String = "governance.sq:getElectionsByStatus"
  }

  private inner class GetElectionByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("elections", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("elections", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(1_940_376_922,
        """SELECT elections.id, elections.title, elections.territory_id, elections."role", elections."status", elections.created_at FROM elections WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "governance.sq:getElectionById"
  }

  private inner class GetCandidaciesByElectionQuery<out T : Any>(
    public val election_id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("candidacies", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("candidacies", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(-1_386_004_633,
        """SELECT candidacies.id, candidacies.election_id, candidacies.user_id, candidacies."status", candidacies.created_at FROM candidacies WHERE election_id = ?""",
        mapper, 1) {
      bindString(0, election_id)
    }

    override fun toString(): String = "governance.sq:getCandidaciesByElection"
  }
}
