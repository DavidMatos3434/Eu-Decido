package com.david.eudecido.data

import com.david.eudecido.db.Elections
import com.david.eudecido.db.Candidacies
import kotlinx.coroutines.flow.Flow

interface ElectionRepository {
    fun getActiveElections(): Flow<List<Elections>>
    fun getElectionById(id: String): Flow<Elections?>
    fun getCandidacies(electionId: String): Flow<List<Candidacies>>
    
    suspend fun createElection(
        id: String,
        title: String,
        territoryId: String,
        role: String,
        status: String
    )
    
    suspend fun applyForCandidacy(
        id: String,
        electionId: String,
        userId: String,
        manifesto: String
    )

    // Sincronização com o backend
    suspend fun syncElections(): Result<Unit>
    suspend fun syncCandidacies(electionId: String): Result<Unit>
}
