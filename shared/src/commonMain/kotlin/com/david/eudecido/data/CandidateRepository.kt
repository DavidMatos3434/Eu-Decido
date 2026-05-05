package com.david.eudecido.data

import com.david.eudecido.db.Candidate_profiles
import kotlinx.coroutines.flow.Flow

interface CandidateRepository {
    fun getCandidates(): Flow<List<Candidate_profiles>>
    suspend fun addCandidate(
        id: String,
        userId: String,
        displayName: String?,
        bio: String?,
        photoUrl: String?,
        verified: Boolean
    )
}
