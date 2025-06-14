package com.example.my_notification.domain.usecase

import com.example.my_notification.domain.model.NotificationMessage
import com.example.my_notification.domain.repository.NotificationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetNotificationsUseCase @Inject constructor(
    private val notificationRepository: NotificationRepository
) {
    
    operator fun invoke(): Flow<List<NotificationMessage>> {
        return notificationRepository.getAllNotifications()
    }
} 