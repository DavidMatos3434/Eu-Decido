package com.david.eudecido.data

import kotlinx.serialization.Serializable

@Serializable
data class ProposalResponse(
    val id: String,
    val user_id: String?,
    val territory_id: String?,
    val title: String,
    val description: String,
    val status: String,
    val type: String,
    val created_at: String,
    val updated_at: String,
    val author: String? = null
)

@Serializable
data class CreateProposalRequest(
    val title: String,
    val description: String,
    val territory_id: String? = null,
    val type: String = "IDEIA"
)

@Serializable
data class ResultsResponse(
    val type: String,
    val total: Long,
    val stats: Map<String, Long>,
    val percentage: Map<String, Int>
)
