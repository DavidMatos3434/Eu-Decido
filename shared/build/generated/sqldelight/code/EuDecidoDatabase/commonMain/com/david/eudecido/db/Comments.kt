package com.david.eudecido.db

import kotlin.String

public data class Comments(
  public val id: String,
  public val proposal_id: String,
  public val user_id: String,
  public val content: String,
  public val created_at: String,
)
