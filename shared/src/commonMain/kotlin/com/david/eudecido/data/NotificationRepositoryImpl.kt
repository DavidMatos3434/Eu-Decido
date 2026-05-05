package com.david.eudecido.data

import com.david.eudecido.db.EuDecidoDatabase
import com.david.eudecido.db.Notifications
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Clock

class NotificationRepositoryImpl(database: EuDecidoDatabase) : NotificationRepository {
    private val queries = database.notificationsQueries

    override fun getNotifications(): Flow<List<Notifications>> {
        return queries.selectAllNotifications().asFlow().mapToList(Dispatchers.IO)
    }

    override suspend fun insertNotification(id: String, title: String, message: String) {
        queries.insertNotification(
            id = id,
            title = title,
            message = message,
            is_read = 0L, // Atualizado de 'read' para 'is_read'
            created_at = Clock.System.now().toString()
        )
    }

    override suspend fun markAsRead(id: String) {
        queries.markAsRead(id)
    }
}
