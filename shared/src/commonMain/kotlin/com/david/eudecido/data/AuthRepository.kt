package com.david.eudecido.data

interface AuthRepository {
    suspend fun login(email: String, password: String): Result<AuthResponse>
    suspend fun register(username: String, email: String, password: String, nifHash: String? = null): Result<AuthResponse>
}
