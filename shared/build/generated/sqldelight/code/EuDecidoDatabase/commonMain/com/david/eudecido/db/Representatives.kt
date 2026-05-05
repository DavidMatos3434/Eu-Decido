package com.david.eudecido.db

import kotlin.String

public data class Representatives(
  public val id: String,
  public val user_id: String,
  public val territory_id: String,
  public val role: String,
  public val created_at: String,
)
