package com.david.eudecido.data

import kotlinx.serialization.Serializable

@Serializable
data class CommentResponse(
    val id: String,
    val proposal_id: String,
    val user_id: String?,
    val author: String,
    val content: String,
    val created_at: String
)
