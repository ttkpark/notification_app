package com.example.my_notification.domain.model

data class NotificationMessage(
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val senderId: String,
    val data: Map<String, String> = emptyMap(),
    val isRead: Boolean = false
) 