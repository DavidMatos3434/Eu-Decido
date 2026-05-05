package com.david.eudecido.db

import kotlin.Long
import kotlin.String

public data class Voting_tokens(
  public val id: String,
  public val identity_id: String,
  public val proposal_id: String,
  public val token_hash: String,
  public val used: Long,
  public val created_at: String,
)
