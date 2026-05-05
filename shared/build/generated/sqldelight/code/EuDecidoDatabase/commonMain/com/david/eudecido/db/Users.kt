package com.david.eudecido.db

import kotlin.Long
import kotlin.String

public data class Users(
  public val id: String,
  public val identity_id: String?,
  public val username: String,
  public val email: String?,
  public val is_candidate: Long,
  public val created_at: String,
)
