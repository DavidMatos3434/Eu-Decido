package com.david.eudecido.db

import kotlin.String

public data class Sync_queue(
  public val id: String,
  public val type: String,
  public val payload: String,
  public val status: String,
  public val created_at: String,
)
