package com.david.eudecido.db

import kotlin.String

public data class Proposals(
  public val id: String,
  public val user_id: String,
  public val territory_id: String,
  public val title: String,
  public val description: String,
  public val status: String,
  public val created_at: String,
)
