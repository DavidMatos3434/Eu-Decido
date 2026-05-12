package com.david.eudecido.data

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val access_token: String,
    val token_type: String,
    val user_id: String,
    val username: String,
    val email: String
)

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class RegisterRequest(
    val username: String,
    val email: String,
    val password: String,
    val nif_hash: String? = null
)
