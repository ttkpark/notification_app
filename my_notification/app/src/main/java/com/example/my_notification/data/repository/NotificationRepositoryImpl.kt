package com.example.my_notification.data.repository

import com.example.my_notification.data.local.dao.NotificationDao
import com.example.my_notification.data.local.entity.NotificationEntity
import com.example.my_notification.domain.model.NotificationMessage
import com.example.my_notification.domain.repository.NotificationRepository
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val notificationDao: NotificationDao,
    private val firebaseMessaging: FirebaseMessaging
) : NotificationRepository {
    
    override suspend fun saveNotification(notification: NotificationMessage): Result<Unit> {
        return try {
            val entity = notification.toEntity()
            notificationDao.insertNotification(entity)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override fun getAllNotifications(): Flow<List<NotificationMessage>> {
        return notificationDao.getAllNotifications().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }
    
    override suspend fun markAsRead(notificationId: String): Result<Unit> {
        return try {
            notificationDao.markAsRead(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun deleteNotification(notificationId: String): Result<Unit> {
        return try {
            notificationDao.deleteNotification(notificationId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun getFcmToken(): Result<String> {
        return try {
            val token = firebaseMessaging.token.await()
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun refreshFcmToken(): Result<String> {
        return try {
            firebaseMessaging.deleteToken().await()
            val newToken = firebaseMessaging.token.await()
            Result.success(newToken)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun subscribeToTopic(topic: String): Result<Unit> {
        return try {
            firebaseMessaging.subscribeToTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    override suspend fun unsubscribeFromTopic(topic: String): Result<Unit> {
        return try {
            firebaseMessaging.unsubscribeFromTopic(topic).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

// Extension functions for mapping
private fun NotificationMessage.toEntity(): NotificationEntity {
    return NotificationEntity(
        id = this.id,
        title = this.title,
        body = this.body,
        timestamp = this.timestamp,
        senderId = this.senderId,
        data = this.data,
        isRead = this.isRead
    )
}

private fun NotificationEntity.toDomainModel(): NotificationMessage {
    return NotificationMessage(
        id = this.id,
        title = this.title,
        body = this.body,
        timestamp = this.timestamp,
        senderId = this.senderId,
        data = this.data,
        isRead = this.isRead
    )
} 