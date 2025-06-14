package com.example.my_notification.presentation.state

import com.example.my_notification.domain.model.NotificationMessage

data class NotificationUiState(
    val notifications: List<NotificationMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val fcmToken: String = "",
    val isTokenLoading: Boolean = false
)

sealed class NotificationUiEvent {
    object LoadNotifications : NotificationUiEvent()
    object RefreshToken : NotificationUiEvent()
    data class MarkAsRead(val notificationId: String) : NotificationUiEvent()
    data class DeleteNotification(val notificationId: String) : NotificationUiEvent()
    data class SubscribeToTopic(val topic: String) : NotificationUiEvent()
    data class UnsubscribeFromTopic(val topic: String) : NotificationUiEvent()
    object ClearError : NotificationUiEvent()
} 