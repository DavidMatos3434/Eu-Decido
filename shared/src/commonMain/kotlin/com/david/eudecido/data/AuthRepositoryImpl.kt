package com.david.eudecido.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class AuthRepositoryImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:8000" // IP padrão para o host no emulador Android
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<AuthResponse> {
        return try {
            val response = httpClient.post("$baseUrl/auth/login") {
                contentType(ContentType.Application.Json)
                setBody(LoginRequest(email, password))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(
        username: String,
        email: String,
        password: String,
        nifHash: String?
    ): Result<AuthResponse> {
        return try {
            val response = httpClient.post("$baseUrl/auth/register") {
                contentType(ContentType.Application.Json)
                setBody(RegisterRequest(username, email, password, nifHash))
            }
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
