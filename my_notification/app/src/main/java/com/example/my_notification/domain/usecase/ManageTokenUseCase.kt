package com.example.my_notification.domain.usecase

import com.example.my_notification.domain.repository.NotificationRepository
import javax.inject.Inject

class ManageTokenUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    
    suspend fun getFcmToken(): Result<String> {
        return notificationRepository.getFcmToken()
    }
    
    suspend fun refreshFcmToken(): Result<String> {
        return notificationRepository.refreshFcmToken()
    }
    
    suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return notificationRepository.subscribeToTopic(topic)
    }
    
    suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return notificationRepository.unsubscribeFromTopic(topic)
    }
} 