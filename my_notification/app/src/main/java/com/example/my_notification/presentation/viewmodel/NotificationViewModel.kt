package com.example.my_notification.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.my_notification.domain.usecase.GetNotificationsUseCase
import com.example.my_notification.domain.usecase.ManageTokenUseCase
import com.example.my_notification.domain.repository.NotificationRepository
import com.example.my_notification.presentation.state.NotificationUiEvent
import com.example.my_notification.presentation.state.NotificationUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val getNotificationsUseCase: GetNotificationsUseCase,
    private val manageTokenUseCase: ManageTokenUseCase,
    private val notificationRepository: NotificationRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()
    
    init {
        loadNotifications()
        loadFcmToken()
    }
    
    fun handleEvent(event: NotificationUiEvent) {
        when (event) {
            is NotificationUiEvent.LoadNotifications -> loadNotifications()
            is NotificationUiEvent.RefreshToken -> refreshToken()
            is NotificationUiEvent.MarkAsRead -> markAsRead(event.notificationId)
            is NotificationUiEvent.DeleteNotification -> deleteNotification(event.notificationId)
            is NotificationUiEvent.SubscribeToTopic -> subscribeToTopic(event.topic)
            is NotificationUiEvent.UnsubscribeFromTopic -> unsubscribeFromTopic(event.topic)
            is NotificationUiEvent.ClearError -> clearError()
        }
    }
    
    private fun loadNotifications() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            getNotificationsUseCase()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = exception.message ?: "알 수 없는 오류가 발생했습니다."
                    )
                }
                .collect { notifications ->
                    _uiState.value = _uiState.value.copy(
                        notifications = notifications,
                        isLoading = false
                    )
                }
        }
    }
    
    private fun loadFcmToken() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTokenLoading = true)
            
            manageTokenUseCase.getFcmToken()
                .onSuccess { token ->
                    _uiState.value = _uiState.value.copy(
                        fcmToken = token,
                        isTokenLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isTokenLoading = false,
                        errorMessage = exception.message ?: "토큰을 가져올 수 없습니다."
                    )
                }
        }
    }
    
    private fun refreshToken() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isTokenLoading = true)
            
            manageTokenUseCase.refreshFcmToken()
                .onSuccess { token ->
                    _uiState.value = _uiState.value.copy(
                        fcmToken = token,
                        isTokenLoading = false
                    )
                }
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        isTokenLoading = false,
                        errorMessage = exception.message ?: "토큰을 새로고침할 수 없습니다."
                    )
                }
        }
    }
    
    private fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.markAsRead(notificationId)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "알림을 읽음 처리할 수 없습니다."
                    )
                }
        }
    }
    
    private fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            notificationRepository.deleteNotification(notificationId)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "알림을 삭제할 수 없습니다."
                    )
                }
        }
    }
    
    private fun subscribeToTopic(topic: String) {
        viewModelScope.launch {
            manageTokenUseCase.subscribeToTopic(topic)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "토픽 구독에 실패했습니다."
                    )
                }
        }
    }
    
    private fun unsubscribeFromTopic(topic: String) {
        viewModelScope.launch {
            manageTokenUseCase.unsubscribeFromTopic(topic)
                .onFailure { exception ->
                    _uiState.value = _uiState.value.copy(
                        errorMessage = exception.message ?: "토픽 구독 해제에 실패했습니다."
                    )
                }
        }
    }
    
    private fun clearError() {
        _uiState.value = _uiState.value.copy(errorMessage = null)
    }
} 