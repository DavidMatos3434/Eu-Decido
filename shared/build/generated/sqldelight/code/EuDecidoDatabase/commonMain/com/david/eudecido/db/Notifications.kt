package com.david.eudecido.db

import kotlin.Long
import kotlin.String

public data class Notifications(
  public val id: String,
  public val title: String,
  public val message: String,
  public val is_read: Long,
  public val created_at: String,
)
