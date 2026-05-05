package com.david.eudecido.db

import kotlin.Long
import kotlin.String

public data class CountVotesByValue(
  public val vote_value: String,
  public val n_votes: Long,
)
