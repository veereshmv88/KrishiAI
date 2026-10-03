package com.krishiai.app.ui.screens.notifications

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.krishiai.app.core.notifications.NotificationManagerImpl
import com.krishiai.app.data.local.entity.NotificationEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationManager: NotificationManagerImpl
) : ViewModel() {

    val notifications: StateFlow<List<NotificationEntity>> = notificationManager.notifications
    val unreadCount: StateFlow<Int> = notificationManager.unreadCount

    fun markAsRead(id: String) {
        notificationManager.markAsRead(id)
    }

    fun markAllAsRead() {
        notificationManager.markAllAsRead()
    }

    fun deleteNotification(id: String) {
        notificationManager.deleteNotification(id)
    }
}
