package com.david.eudecido.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.http.HttpHeaders

class IdentityRepositoryImpl(
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:8000"
) : IdentityRepository {

    override fun hashNif(nif: String): String {
        // Numa app real, usaríamos SHA-256. 
        // Para este protótipo, vamos usar uma simulação consistente.
        return "hash_" + nif.trim().hashCode().toString()
    }

    override suspend fun verifyAndRegisterIdentity(nifHash: String): String? {
        // Na nossa nova lógica, a identidade é criada no /auth/register.
        // Este método pode ser usado para verificar o status atual.
        return try {
            val token = SessionManager.token ?: return null
            val response: Map<String, String> = httpClient.get("$baseUrl/identity/status") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
            
            if (response["verified"] == "true") "verified" else null
        } catch (e: Exception) {
            null
        }
    }
}
