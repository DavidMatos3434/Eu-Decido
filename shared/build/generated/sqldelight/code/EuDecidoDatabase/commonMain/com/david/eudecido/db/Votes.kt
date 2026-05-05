package com.david.eudecido.db

import kotlin.String

public data class Votes(
  public val id: String,
  public val proposal_id: String,
  public val vote_value: String,
  public val voting_token: String,
  public val created_at: String,
)
