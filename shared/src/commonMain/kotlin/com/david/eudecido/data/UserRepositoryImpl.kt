package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Users
import com.david.eudecido.db.Territories
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOneOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class UserRepositoryImpl(database: EuDecidoDatabase) : UserRepository {
    private val queries = database.usersQueries

    override fun getUser(id: String): Flow<Users?> {
        return queries.getUserById(id).asFlow().mapToOneOrNull(Dispatchers.IO)
    }

    override suspend fun insertUser(
        id: String, 
        identityId: String?, 
        username: String, 
        email: String?, 
        isCandidate: Boolean
    ) {
        queries.insertUser(
            id = id,
            identity_id = identityId,
            username = username,
            email = email,
            is_candidate = if (isCandidate) 1L else 0L,
            created_at = Clock.System.now().toString()
        )
    }

    override fun getTerritories(): Flow<List<Territories>> {
        return queries.getAllTerritories().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun addTerritory(id: String, name: String, type: String, parentId: String?) {
        queries.insertTerritory(id, name, type, parentId)
    }
}
