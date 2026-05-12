package com.david.eudecido.sync

import com.david.eudecido.data.SessionManager
import com.david.eudecido.data.SyncRepository
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class SyncManager(
    private val syncRepository: SyncRepository,
    private val httpClient: HttpClient,
    private val baseUrl: String = "http://10.0.2.2:8000"
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val json = Json { ignoreUnknownKeys = true }

    init {
        startSyncLoop()
    }

    private fun startSyncLoop() {
        scope.launch {
            syncRepository.getPendingItems().collectLatest { pendingItems ->
                for (item in pendingItems) {
                    processSyncItem(item.id, item.type, item.payload)
                }
            }
        }
    }

    private suspend fun processSyncItem(id: String, type: String, payload: String) {
        try {
            val success = when {
                type == "PROPOSAL" -> sendProposal(payload)
                type == "VOTE" -> sendVote(payload)
                type == "COMMENT" -> sendComment(payload)
                type.startsWith("CANDIDACY:") -> sendCandidacy(type.removePrefix("CANDIDACY:"), payload)
                else -> false
            }

            if (success) {
                syncRepository.updateStatus(id, "SENT")
            }
        } catch (e: Exception) {
            println("Sync Error processing $id ($type): ${e.message}")
        }
    }

    private suspend fun sendProposal(payload: String): Boolean {
        val token = SessionManager.token ?: return false
        return try {
            val response = httpClient.post("$baseUrl/proposals") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(payload)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun sendVote(payload: String): Boolean {
        return try {
            val response = httpClient.post("$baseUrl/vote") {
                contentType(ContentType.Application.Json)
                setBody(payload)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun sendComment(payload: String): Boolean {
        val token = SessionManager.token ?: return false
        return try {
            val jsonObject = json.parseToJsonElement(payload).jsonObject
            val proposalId = jsonObject["proposal_id"]?.jsonPrimitive?.content ?: return false
            val content = jsonObject["content"]?.jsonPrimitive?.content ?: return false

            val response = httpClient.post("$baseUrl/proposals/$proposalId/comments") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody("""{"content":"$content"}""")
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }

    private suspend fun sendCandidacy(electionId: String, payload: String): Boolean {
        val token = SessionManager.token ?: return false
        return try {
            val response = httpClient.post("$baseUrl/elections/$electionId/candidacies") {
                contentType(ContentType.Application.Json)
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(payload)
            }
            response.status.value in 200..299
        } catch (e: Exception) {
            false
        }
    }
}
