package com.david.eudecido.data

import com.david.eudecido.db.Notifications
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    fun getNotifications(): Flow<List<Notifications>>
    suspend fun insertNotification(id: String, title: String, message: String)
    suspend fun markAsRead(id: String)
}
