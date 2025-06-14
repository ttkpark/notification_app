package com.example.my_notification.data.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.example.my_notification.R
import com.example.my_notification.MainActivity
import com.example.my_notification.data.local.database.NotificationDatabase
import com.example.my_notification.data.local.entity.NotificationEntity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    @Inject
    lateinit var database: NotificationDatabase
    
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var wakeLock: PowerManager.WakeLock? = null
    
    companion object {
        private const val TAG = "FCMService"
        private const val CHANNEL_ID = "notification_channel"
        private const val CHANNEL_NAME = "Push Notifications"
        private const val WAKE_LOCK_TIMEOUT = 10000L // 10초
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeWakeLock()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Wake Lock 획득하여 백그라운드에서도 처리 보장
        acquireWakeLock()
        
        try {
            Log.d(TAG, "From: ${remoteMessage.from}")
            Log.d(TAG, "Message received in background: ${System.currentTimeMillis()}")
            
            // 알림 메시지가 있는 경우
            remoteMessage.notification?.let { notification ->
                val title = notification.title ?: "새 알림"
                val body = notification.body ?: ""
                
                Log.d(TAG, "Message Notification Body: $body")
                
                // 데이터베이스에 알림 저장 (백그라운드에서 안전하게)
                saveNotificationToDatabase(title, body, remoteMessage.data)
                
                // 시스템 알림 표시 (높은 우선순위로)
                showNotification(title, body, true)
            }
            
            // 데이터 메시지만 있는 경우 (notification 없이 data만)
            if (remoteMessage.notification == null && remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "Data-only message received: ${remoteMessage.data}")
                
                val title = remoteMessage.data["title"] ?: "새 알림"
                val body = remoteMessage.data["body"] ?: "새 메시지가 도착했습니다."
                
                // 데이터베이스에 알림 저장
                saveNotificationToDatabase(title, body, remoteMessage.data)
                
                // 시스템 알림 표시 (높은 우선순위로)
                showNotification(title, body, true)
            }
            
            // 모든 데이터 메시지 처리
            if (remoteMessage.data.isNotEmpty()) {
                Log.d(TAG, "Message data payload: ${remoteMessage.data}")
                handleDataMessage(remoteMessage.data)
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "Error processing FCM message", e)
        } finally {
            // Wake Lock 해제 (지연 후)
            releaseWakeLockDelayed()
        }
    }
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed token: $token")
        
        // 새 토큰을 서버에 전송하거나 로컬 저장소에 저장
        sendTokenToServer(token)
    }
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH // 백그라운드에서도 알림이 잘 보이도록 HIGH로 설정
            ).apply {
                description = "Firebase Cloud Messaging 알림을 위한 채널"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
            }
            
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun initializeWakeLock() {
        val powerManager = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "MyApp::FCMWakeLock"
        )
    }
    
    private fun acquireWakeLock() {
        try {
            wakeLock?.let { wl ->
                if (!wl.isHeld) {
                    wl.acquire(WAKE_LOCK_TIMEOUT)
                    Log.d(TAG, "Wake lock acquired")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to acquire wake lock", e)
        }
    }
    
    private fun releaseWakeLock() {
        try {
            wakeLock?.let { wl ->
                if (wl.isHeld) {
                    wl.release()
                    Log.d(TAG, "Wake lock released")
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to release wake lock", e)
        }
    }
    
    private fun releaseWakeLockDelayed() {
        // 3초 후에 Wake Lock 해제 (알림 처리 완료 보장)
        serviceScope.launch {
            kotlinx.coroutines.delay(3000)
            releaseWakeLock()
        }
    }
    
    private fun showNotification(title: String, body: String, isHighPriority: Boolean = false) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("from_notification", true)
        }
        
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val priority = if (isHighPriority) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT
        
        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(priority)
            .setContentIntent(pendingIntent)
            .setDefaults(NotificationCompat.DEFAULT_ALL) // 소리, 진동, LED 모두 활성화
            .setWhen(System.currentTimeMillis())
            .setShowWhen(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 잠금화면에서도 표시
        
        // 백그라운드에서 받은 알림의 경우 추가 설정
        if (isHighPriority) {
            notificationBuilder
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setLights(0xFF0000FF.toInt(), 1000, 1000) // 파란색 LED
        }
        
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = System.currentTimeMillis().toInt()
        
        try {
            notificationManager.notify(notificationId, notificationBuilder.build())
            Log.d(TAG, "Notification displayed with ID: $notificationId")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to show notification", e)
        }
    }
    
    private fun saveNotificationToDatabase(title: String, body: String, data: Map<String, String>) {
        serviceScope.launch {
            try {
                val notification = NotificationEntity(
                    id = generateNotificationId(),
                    title = title,
                    body = body,
                    timestamp = System.currentTimeMillis(),
                    senderId = data["senderId"] ?: "unknown",
                    data = data,
                    isRead = false
                )
                
                // 메인 스레드에서 데이터베이스 작업 수행
                withContext(Dispatchers.IO) {
                    database.notificationDao().insertNotification(notification)
                }
                
                Log.d(TAG, "알림이 데이터베이스에 저장되었습니다: $title (Background: ${isAppInBackground()})")
                
            } catch (e: Exception) {
                Log.e(TAG, "알림 저장 중 오류 발생: ${e.message}", e)
            }
        }
    }
    
    private fun isAppInBackground(): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val runningAppProcesses = activityManager.runningAppProcesses ?: return true
        
        for (processInfo in runningAppProcesses) {
            if (processInfo.processName == packageName) {
                return processInfo.importance != android.app.ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND
            }
        }
        return true
    }
    
    private fun generateNotificationId(): String {
        val timestamp = System.currentTimeMillis()
        val random = Random().nextInt(1000)
        return "fcm_${timestamp}_$random"
    }
    
    private fun handleDataMessage(data: Map<String, String>) {
        // 데이터 메시지 처리 로직
        Log.d(TAG, "Handling data message: $data")
        
        // 추가 데이터 처리 로직 (필요시)
        data["action"]?.let { action ->
            when (action) {
                "open_activity" -> {
                    // 특정 액티비티 열기
                    Log.d(TAG, "Opening specific activity")
                }
                "update_badge" -> {
                    // 배지 업데이트
                    Log.d(TAG, "Updating badge")
                }
                // 다른 액션들...
            }
        }
    }
    
    private fun sendTokenToServer(token: String) {
        // 토큰을 서버로 전송하는 로직 (필요시 구현)
        Log.d(TAG, "Token sent to server: $token")
        
        // TODO: 실제 서버 API 호출하여 토큰 등록
        // CoroutineScope(Dispatchers.IO).launch {
        //     try {
        //         apiService.registerToken(token)
        //     } catch (e: Exception) {
        //         Log.e(TAG, "Failed to send token to server", e)
        //     }
        // }
    }
} 