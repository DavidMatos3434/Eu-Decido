package com.david.eudecido.data

import com.david.eudecido.db.Users
import com.david.eudecido.db.Territories
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(id: String): Flow<Users?>
    suspend fun insertUser(
        id: String, 
        identityId: String?, 
        username: String, 
        email: String?, 
        isCandidate: Boolean
    )
    fun getTerritories(): Flow<List<Territories>>
    suspend fun addTerritory(id: String, name: String, type: String, parentId: String?)
}
