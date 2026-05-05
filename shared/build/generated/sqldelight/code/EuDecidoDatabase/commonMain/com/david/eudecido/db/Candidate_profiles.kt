package com.david.eudecido.db

import kotlin.Long
import kotlin.String

public data class Candidate_profiles(
  public val id: String,
  public val user_id: String,
  public val display_name: String?,
  public val bio: String?,
  public val photo_url: String?,
  public val verified: Long,
  public val created_at: String,
)
