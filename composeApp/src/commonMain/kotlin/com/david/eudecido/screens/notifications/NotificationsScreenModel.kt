package com.david.eudecido.screens.notifications

import cafe.adriel.voyager.core.model.ScreenModel
import cafe.adriel.voyager.core.model.screenModelScope
import com.david.eudecido.data.NotificationRepository
import com.david.eudecido.models.NotificationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class NotificationsScreenModel(
    private val notificationRepository: NotificationRepository
) : ScreenModel {
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        screenModelScope.launch {
            notificationRepository.getNotifications().collectLatest { dbNotifications ->
                _notifications.value = dbNotifications.map {
                    NotificationItem(
                        id = it.id,
                        title = it.title,
                        message = it.message,
                        isRead = it.is_read == 1L
                    )
                }
            }
        }
    }

    fun markAsRead(id: String) {
        screenModelScope.launch {
            notificationRepository.markAsRead(id)
        }
    }
}
