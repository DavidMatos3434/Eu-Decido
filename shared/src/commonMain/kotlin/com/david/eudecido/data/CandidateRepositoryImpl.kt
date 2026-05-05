package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Candidate_profiles
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class CandidateRepositoryImpl(database: EuDecidoDatabase) : CandidateRepository {
    private val queries = database.governanceQueries

    override fun getCandidates(): Flow<List<Candidate_profiles>> {
        return queries.getAllCandidates().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun addCandidate(
        id: String,
        userId: String,
        displayName: String?,
        bio: String?,
        photoUrl: String?,
        verified: Boolean
    ) {
        queries.insertCandidate(
            id = id,
            user_id = userId,
            display_name = displayName,
            bio = bio,
            photo_url = photoUrl,
            verified = if (verified) 1L else 0L,
            created_at = Clock.System.now().toString() // Campo adicionado
        )
    }
}
