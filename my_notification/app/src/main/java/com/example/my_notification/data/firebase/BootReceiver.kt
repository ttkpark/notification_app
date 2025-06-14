package com.example.my_notification.data.firebase

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging

class BootReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Log.d("BootReceiver", "Device booted, re-initializing FCM")
            
            // FCM 토큰 갱신
            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    Log.w("BootReceiver", "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }
                
                val token = task.result
                Log.d("BootReceiver", "FCM Registration Token after boot: $token")
            }
        }
    }
} 