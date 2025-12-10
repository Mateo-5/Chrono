package com.chrono.data

import android.content.Context
import com.chrono.security.EncryptedPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class NotificationHistoryEntry(
    val id: String,
    val title: String,
    val message: String,
    val timestamp: Long,
    val type: String = "general"
)

data class NotificationsHistoryData(
    val notifications: List<NotificationHistoryEntry> = emptyList()
)

class NotificationsHistoryDataStore(private val context: Context) {
    
    companion object {
        private const val KEY_NOTIFICATIONS_HISTORY = "notifications_history"
        private val gson = Gson()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    private val _notificationsDataFlow = MutableStateFlow(loadNotificationsData())
    
    val notificationsData: Flow<NotificationsHistoryData> = _notificationsDataFlow.asStateFlow()
    
    private fun loadNotificationsData(): NotificationsHistoryData {
        val json = encryptedPrefs.getString(KEY_NOTIFICATIONS_HISTORY, null)
        return if (json != null) {
            try {
                gson.fromJson(json, NotificationsHistoryData::class.java) ?: NotificationsHistoryData()
            } catch (e: Exception) {
                e.printStackTrace()
                NotificationsHistoryData()
            }
        } else {
            NotificationsHistoryData()
        }
    }
    
    private suspend fun saveNotificationsData(data: NotificationsHistoryData) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_NOTIFICATIONS_HISTORY, gson.toJson(data)).apply()
        _notificationsDataFlow.value = data
    }
    
    suspend fun addNotification(title: String, message: String, type: String = "general") {
        val currentData = _notificationsDataFlow.value
        val newEntry = NotificationHistoryEntry(
            id = System.currentTimeMillis().toString(),
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            type = type
        )
        
        // Keep only last 100 notifications
        val updatedList = (listOf(newEntry) + currentData.notifications).take(100)
        saveNotificationsData(currentData.copy(notifications = updatedList))
    }
    
    suspend fun deleteNotification(id: String) {
        val currentData = _notificationsDataFlow.value
        val updatedList = currentData.notifications.filter { it.id != id }
        saveNotificationsData(currentData.copy(notifications = updatedList))
    }
    
    suspend fun clearAll() {
        saveNotificationsData(NotificationsHistoryData())
    }
}
