package com.chrono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.notificationsHistoryDataStore: DataStore<Preferences> by preferencesDataStore(name = "notifications_history")

data class NotificationHistoryEntry(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String = "general" // "reminder", "task", "water", "general"
)

data class NotificationsHistoryData(
    val notifications: List<NotificationHistoryEntry> = emptyList()
)

class NotificationsHistoryDataStore(private val context: Context) {
    
    companion object {
        private val NOTIFICATIONS_HISTORY_DATA = stringPreferencesKey("notifications_history_data")
        private val gson = Gson()
    }
    
    val notificationsData: Flow<NotificationsHistoryData> = context.notificationsHistoryDataStore.data.map { preferences ->
        val json = preferences[NOTIFICATIONS_HISTORY_DATA]
        if (json != null) {
            try {
                gson.fromJson(json, NotificationsHistoryData::class.java) ?: NotificationsHistoryData()
            } catch (e: Exception) {
                NotificationsHistoryData()
            }
        } else {
            NotificationsHistoryData()
        }
    }
    
    suspend fun addNotification(title: String, message: String, type: String = "general") {
        context.notificationsHistoryDataStore.edit { preferences ->
            val currentData = getNotificationsData(preferences)
            val newEntry = NotificationHistoryEntry(
                id = System.currentTimeMillis().toString(),
                title = title,
                message = message,
                timestamp = System.currentTimeMillis(),
                type = type
            )
            
            // Keep only last 100 notifications
            val updatedList = (listOf(newEntry) + currentData.notifications).take(100)
            saveNotificationsData(preferences, currentData.copy(notifications = updatedList))
        }
    }
    
    suspend fun deleteNotification(id: String) {
        context.notificationsHistoryDataStore.edit { preferences ->
            val currentData = getNotificationsData(preferences)
            val updatedList = currentData.notifications.filter { it.id != id }
            saveNotificationsData(preferences, currentData.copy(notifications = updatedList))
        }
    }
    
    suspend fun clearAll() {
        context.notificationsHistoryDataStore.edit { preferences ->
            saveNotificationsData(preferences, NotificationsHistoryData())
        }
    }
    
    private fun getNotificationsData(preferences: Preferences): NotificationsHistoryData {
        val json = preferences[NOTIFICATIONS_HISTORY_DATA]
        return if (json != null) {
            try {
                gson.fromJson(json, NotificationsHistoryData::class.java) ?: NotificationsHistoryData()
            } catch (e: Exception) {
                NotificationsHistoryData()
            }
        } else {
            NotificationsHistoryData()
        }
    }
    
    private fun saveNotificationsData(preferences: MutablePreferences, data: NotificationsHistoryData) {
        preferences[NOTIFICATIONS_HISTORY_DATA] = gson.toJson(data)
    }
}
