package com.example.my_notification.domain.repository

import com.example.my_notification.domain.model.NotificationMessage
import kotlinx.coroutines.flow.Flow

interface NotificationRepository {
    
    suspend fun saveNotification(notification: NotificationMessage): Result<Unit>
    
    fun getAllNotifications(): Flow<List<NotificationMessage>>
    
    suspend fun markAsRead(notificationId: String): Result<Unit>
    
    suspend fun deleteNotification(notificationId: String): Result<Unit>
    
    suspend fun getFcmToken(): Result<String>
    
    suspend fun refreshFcmToken(): Result<String>
    
    suspend fun subscribeToTopic(topic: String): Result<Unit>
    
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit>
} 