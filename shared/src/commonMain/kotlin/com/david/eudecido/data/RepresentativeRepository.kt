package com.david.eudecido.data

import com.david.eudecido.db.Representatives
import kotlinx.coroutines.flow.Flow

interface RepresentativeRepository {
    fun getRepresentatives(): Flow<List<Representatives>>
    suspend fun addRepresentative(id: String, userId: String, territoryId: String, role: String)
}
