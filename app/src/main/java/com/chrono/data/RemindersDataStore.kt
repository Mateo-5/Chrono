package com.chrono.data

import android.content.Context
import com.chrono.security.EncryptedPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

enum class ReminderType {
    SINGLE, REPEATED
}

enum class SingleReminderMode {
    START_OF_DAY, TIME_SPECIFIC
}

data class ReminderEntry(
    val id: String,
    val title: String,
    private val _type: ReminderType? = null,
    private val _singleMode: SingleReminderMode? = null,
    val dateTime: Long = 0L,
    val repeatTimeHour: Int? = null,
    val repeatTimeMinute: Int? = null,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    val date: String? = null,
    val time: String? = null
) {
    val type: ReminderType
        get() = _type ?: ReminderType.SINGLE
    
    val singleMode: SingleReminderMode
        get() = _singleMode ?: SingleReminderMode.TIME_SPECIFIC
}

data class RemindersData(
    val reminders: List<ReminderEntry> = emptyList()
)

class RemindersDataStore(private val context: Context) {
    
    companion object {
        private const val KEY_REMINDERS_DATA = "reminders_data"
        private val gson = Gson()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    private val _remindersDataFlow = MutableStateFlow(loadRemindersData())
    
    val remindersData: Flow<RemindersData> = _remindersDataFlow.asStateFlow()
    
    private fun loadRemindersData(): RemindersData {
        val json = encryptedPrefs.getString(KEY_REMINDERS_DATA, null)
        return if (json != null) {
            try {
                gson.fromJson(json, RemindersData::class.java) ?: RemindersData()
            } catch (e: Exception) {
                e.printStackTrace()
                RemindersData()
            }
        } else {
            RemindersData()
        }
    }
    
    private suspend fun saveRemindersData(data: RemindersData) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_REMINDERS_DATA, gson.toJson(data)).apply()
        _remindersDataFlow.value = data
    }
    
    suspend fun addSingleReminder(title: String, mode: SingleReminderMode, dateTime: Long): String {
        val id = System.currentTimeMillis().toString()
        val currentData = _remindersDataFlow.value
        
        val updatedReminders = currentData.reminders.toMutableList()
        updatedReminders.add(ReminderEntry(
            id = id,
            title = title,
            _type = ReminderType.SINGLE,
            _singleMode = mode,
            dateTime = dateTime,
            isActive = true,
            isCompleted = false
        ))
        updatedReminders.sortBy { it.dateTime }
        
        saveRemindersData(currentData.copy(reminders = updatedReminders))
        return id
    }
    
    suspend fun addRepeatedReminder(title: String, hour: Int, minute: Int) {
        val currentData = _remindersDataFlow.value
        val id = System.currentTimeMillis().toString()
        
        val updatedReminders = currentData.reminders.toMutableList()
        updatedReminders.add(ReminderEntry(
            id = id,
            title = title,
            _type = ReminderType.REPEATED,
            _singleMode = null,
            dateTime = 0L,
            repeatTimeHour = hour,
            repeatTimeMinute = minute,
            isActive = true,
            isCompleted = false
        ))
        
        saveRemindersData(currentData.copy(reminders = updatedReminders))
    }
    
    suspend fun addReminder(title: String, dateTime: Long) {
        addSingleReminder(title, SingleReminderMode.TIME_SPECIFIC, dateTime)
    }
    
    suspend fun deleteReminder(id: String) {
        val currentData = _remindersDataFlow.value
        val updatedReminders = currentData.reminders.filter { it.id != id }
        saveRemindersData(currentData.copy(reminders = updatedReminders))
    }
    
    suspend fun toggleReminder(id: String) {
        val currentData = _remindersDataFlow.value
        val updatedReminders = currentData.reminders.map { reminder ->
            if (reminder.id == id) {
                reminder.copy(isActive = !reminder.isActive)
            } else {
                reminder
            }
        }
        saveRemindersData(currentData.copy(reminders = updatedReminders))
    }
    
    suspend fun markReminderCompleted(id: String) {
        val currentData = _remindersDataFlow.value
        val updatedReminders = currentData.reminders.map { reminder ->
            if (reminder.id == id) {
                reminder.copy(isCompleted = true, isActive = false)
            } else {
                reminder
            }
        }
        saveRemindersData(currentData.copy(reminders = updatedReminders))
    }
    
    suspend fun restoreData(data: RemindersData) {
        saveRemindersData(data)
    }
}
