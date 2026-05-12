package com.david.eudecido.data

import kotlinx.serialization.Serializable

@Serializable
data class ElectionResponse(
    val id: String,
    val proposal_id: String?,
    val title: String,
    val territory_id: String?,
    val role: String,
    val status: String,
    val created_at: String,
    val updated_at: String
)

@Serializable
data class CandidacyResponse(
    val id: String,
    val election_id: String,
    val user_id: String,
    val username: String?,
    val status: String,
    val manifesto: String?,
    val created_at: String
)

@Serializable
data class ApplyCandidacyRequest(
    val manifesto: String?
)
