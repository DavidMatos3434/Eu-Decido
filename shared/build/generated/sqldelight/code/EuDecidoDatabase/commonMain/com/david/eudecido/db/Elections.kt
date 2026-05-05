package com.david.eudecido.db

import kotlin.String

public data class Elections(
  public val id: String,
  public val title: String,
  public val territory_id: String,
  public val role: String,
  public val status: String,
  public val created_at: String,
)
