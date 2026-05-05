package com.david.eudecido.db

import kotlin.String

public data class Territories(
  public val id: String,
  public val name: String,
  public val type: String,
  public val parent_id: String?,
)
