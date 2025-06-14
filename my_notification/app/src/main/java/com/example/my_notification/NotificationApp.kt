package com.example.my_notification

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NotificationApp : Application() {
    
    override fun onCreate() {
        super.onCreate()
    }
} 