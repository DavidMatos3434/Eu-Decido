package com.david.eudecido.db

import app.cash.sqldelight.Transacter
import app.cash.sqldelight.db.QueryResult
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.db.SqlSchema
import com.david.eudecido.db.shared.newInstance
import com.david.eudecido.db.shared.schema
import kotlin.Unit

public interface EuDecidoDatabase : Transacter {
  public val governanceQueries: GovernanceQueries

  public val notificationsQueries: NotificationsQueries

  public val proposalsQueries: ProposalsQueries

  public val usersQueries: UsersQueries

  public companion object {
    public val Schema: SqlSchema<QueryResult.Value<Unit>>
      get() = EuDecidoDatabase::class.schema

    public operator fun invoke(driver: SqlDriver): EuDecidoDatabase =
        EuDecidoDatabase::class.newInstance(driver)
  }
}
