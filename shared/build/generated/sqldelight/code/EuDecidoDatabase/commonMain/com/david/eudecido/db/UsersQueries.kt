package com.david.eudecido.db

import app.cash.sqldelight.Query
import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlCursor
import app.cash.sqldelight.db.SqlDriver
import kotlin.Any
import kotlin.Long
import kotlin.String

public class UsersQueries(
  driver: SqlDriver,
) : TransacterImpl(driver) {
  public fun <T : Any> getUserById(id: String, mapper: (
    id: String,
    identity_id: String?,
    username: String,
    email: String?,
    is_candidate: Long,
    created_at: String,
  ) -> T): Query<T> = GetUserByIdQuery(id) { cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1),
      cursor.getString(2)!!,
      cursor.getString(3),
      cursor.getLong(4)!!,
      cursor.getString(5)!!
    )
  }

  public fun getUserById(id: String): Query<Users> = getUserById(id) { id_, identity_id, username,
      email, is_candidate, created_at ->
    Users(
      id_,
      identity_id,
      username,
      email,
      is_candidate,
      created_at
    )
  }

  public fun <T : Any> getAllTerritories(mapper: (
    id: String,
    name: String,
    type: String,
    parent_id: String?,
  ) -> T): Query<T> = Query(-1_078_849_394, arrayOf("territories"), driver, "users.sq",
      "getAllTerritories",
      "SELECT territories.id, territories.name, territories.\"type\", territories.parent_id FROM territories") {
      cursor ->
    mapper(
      cursor.getString(0)!!,
      cursor.getString(1)!!,
      cursor.getString(2)!!,
      cursor.getString(3)
    )
  }

  public fun getAllTerritories(): Query<Territories> = getAllTerritories { id, name, type,
      parent_id ->
    Territories(
      id,
      name,
      type,
      parent_id
    )
  }

  public fun insertUser(
    id: String,
    identity_id: String?,
    username: String,
    email: String?,
    is_candidate: Long,
    created_at: String,
  ) {
    driver.execute(-891_319_421, """
        |INSERT OR REPLACE INTO users(id, identity_id, username, email, is_candidate, created_at)
        |VALUES (?, ?, ?, ?, ?, ?)
        """.trimMargin(), 6) {
          bindString(0, id)
          bindString(1, identity_id)
          bindString(2, username)
          bindString(3, email)
          bindLong(4, is_candidate)
          bindString(5, created_at)
        }
    notifyQueries(-891_319_421) { emit ->
      emit("users")
    }
  }

  public fun insertTerritory(
    id: String,
    name: String,
    type: String,
    parent_id: String?,
  ) {
    driver.execute(-1_396_417_694, """
        |INSERT OR REPLACE INTO territories(id, name, "type", parent_id)
        |VALUES (?, ?, ?, ?)
        """.trimMargin(), 4) {
          bindString(0, id)
          bindString(1, name)
          bindString(2, type)
          bindString(3, parent_id)
        }
    notifyQueries(-1_396_417_694) { emit ->
      emit("territories")
    }
  }

  private inner class GetUserByIdQuery<out T : Any>(
    public val id: String,
    mapper: (SqlCursor) -> T,
  ) : Query<T>(mapper) {
    override fun addListener(listener: Query.Listener) {
      driver.addListener("users", listener = listener)
    }

    override fun removeListener(listener: Query.Listener) {
      driver.removeListener("users", listener = listener)
    }

    override fun <R> execute(mapper: (SqlCursor) -> QueryResult<R>): QueryResult<R> =
        driver.executeQuery(62_216_756,
        """SELECT users.id, users.identity_id, users.username, users.email, users.is_candidate, users.created_at FROM users WHERE id = ?""",
        mapper, 1) {
      bindString(0, id)
    }

    override fun toString(): String = "users.sq:getUserById"
  }
}
