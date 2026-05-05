package com.david.eudecido.db

import kotlin.String

public data class Candidacies(
  public val id: String,
  public val election_id: String,
  public val user_id: String,
  public val status: String,
  public val created_at: String,
)
