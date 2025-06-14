package com.example.my_notification.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "notifications")
@TypeConverters(MapConverter::class)
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val body: String,
    val timestamp: Long,
    val senderId: String,
    val data: Map<String, String>,
    val isRead: Boolean
)

class MapConverter {
    @TypeConverter
    fun fromMap(map: Map<String, String>): String {
        return Gson().toJson(map)
    }
    
    @TypeConverter
    fun toMap(json: String): Map<String, String> {
        val type = object : TypeToken<Map<String, String>>() {}.type
        return Gson().fromJson(json, type) ?: emptyMap()
    }
} 