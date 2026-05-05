package com.david.eudecido.db.shared

import app.cash.sqldelight.TransacterImpl
import app.cash.sqldelight.db.AfterVersion
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.GovernanceQueries
import com.david.eudecido.db.NotificationsQueries
import com.david.eudecido.db.ProposalsQueries
import com.david.eudecido.db.UsersQueries
import kotlin.Long
import kotlin.Unit
import kotlin.reflect.KClass

internal val KClass<EuDecidoDatabase>.schema: SqlSchema<QueryResult.Value<Unit>>
  get() = EuDecidoDatabaseImpl.Schema

internal fun KClass<EuDecidoDatabase>.newInstance(driver: SqlDriver): EuDecidoDatabase =
    EuDecidoDatabaseImpl(driver)

private class EuDecidoDatabaseImpl(
  driver: SqlDriver,
) : TransacterImpl(driver), EuDecidoDatabase {
  override val governanceQueries: GovernanceQueries = GovernanceQueries(driver)

  override val notificationsQueries: NotificationsQueries = NotificationsQueries(driver)

  override val proposalsQueries: ProposalsQueries = ProposalsQueries(driver)

  override val usersQueries: UsersQueries = UsersQueries(driver)

  public object Schema : SqlSchema<QueryResult.Value<Unit>> {
    override val version: Long
      get() = 1

    override fun create(driver: SqlDriver): QueryResult.Value<Unit> {
      driver.execute(null, """
          |CREATE TABLE candidate_profiles (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    user_id TEXT NOT NULL,
          |    display_name TEXT,
          |    bio TEXT,
          |    photo_url TEXT,
          |    verified INTEGER NOT NULL DEFAULT 0,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE representatives (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    user_id TEXT NOT NULL,
          |    territory_id TEXT NOT NULL,
          |    "role" TEXT NOT NULL,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE elections (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    territory_id TEXT NOT NULL,
          |    "role" TEXT NOT NULL,
          |    "status" TEXT NOT NULL,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE candidacies (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    election_id TEXT NOT NULL,
          |    user_id TEXT NOT NULL,
          |    "status" TEXT NOT NULL,
          |    created_at TEXT NOT NULL,
          |    FOREIGN KEY (election_id) REFERENCES elections(id)
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE notifications (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    title TEXT NOT NULL,
          |    message TEXT NOT NULL,
          |    is_read INTEGER NOT NULL DEFAULT 0, -- 0: False, 1: True
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE proposals (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    user_id TEXT NOT NULL,
          |    territory_id TEXT NOT NULL,
          |    title TEXT NOT NULL,
          |    description TEXT NOT NULL,
          |    "status" TEXT NOT NULL,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE comments (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    proposal_id TEXT NOT NULL,
          |    user_id TEXT NOT NULL,
          |    content TEXT NOT NULL,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE votes (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    proposal_id TEXT NOT NULL,
          |    vote_value TEXT NOT NULL,
          |    voting_token TEXT NOT NULL UNIQUE,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE voting_tokens (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    identity_id TEXT NOT NULL,
          |    proposal_id TEXT NOT NULL,
          |    token_hash TEXT NOT NULL UNIQUE,
          |    "used" INTEGER NOT NULL DEFAULT 0,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE sync_queue (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    "type" TEXT NOT NULL,
          |    payload TEXT NOT NULL,
          |    "status" TEXT NOT NULL,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE users (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    identity_id TEXT,
          |    username TEXT NOT NULL UNIQUE,
          |    email TEXT,
          |    is_candidate INTEGER NOT NULL DEFAULT 0,
          |    created_at TEXT NOT NULL
          |)
          """.trimMargin(), 0)
      driver.execute(null, """
          |CREATE TABLE territories (
          |    id TEXT NOT NULL PRIMARY KEY,
          |    name TEXT NOT NULL,
          |    "type" TEXT NOT NULL,
          |    parent_id TEXT
          |)
          """.trimMargin(), 0)
      return QueryResult.Unit
    }

    override fun migrate(
      driver: SqlDriver,
      oldVersion: Long,
      newVersion: Long,
      vararg callbacks: AfterVersion,
    ): QueryResult.Value<Unit> = QueryResult.Unit
  }
}
