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

private val Context.remindersDataStore: DataStore<Preferences> by preferencesDataStore(name = "reminders")

enum class ReminderType {
    SINGLE, REPEATED
}

enum class SingleReminderMode {
    START_OF_DAY, TIME_SPECIFIC
}

data class ReminderEntry(
    val id: String,
    val title: String,
    private val _type: ReminderType? = null,  // Backing field for backwards compatibility
    private val _singleMode: SingleReminderMode? = null,
    val dateTime: Long = 0L,
    val repeatTimeHour: Int? = null,
    val repeatTimeMinute: Int? = null,
    val isActive: Boolean = true,
    val isCompleted: Boolean = false,
    // Legacy fields for old data
    val date: String? = null,
    val time: String? = null
) {
    // Safe accessor with default
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
        private val REMINDERS_DATA = stringPreferencesKey("reminders_data")
        private val gson = Gson()
    }
    
    val remindersData: Flow<RemindersData> = context.remindersDataStore.data.map { preferences ->
        val json = preferences[REMINDERS_DATA]
        if (json != null) {
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
    
    suspend fun addSingleReminder(title: String, mode: SingleReminderMode, dateTime: Long): String {
        val id = System.currentTimeMillis().toString()
        context.remindersDataStore.edit { preferences ->
            val currentData = getData(preferences)
            
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
            
            preferences[REMINDERS_DATA] = gson.toJson(currentData.copy(reminders = updatedReminders))
        }
        return id
    }
    
    suspend fun addRepeatedReminder(title: String, hour: Int, minute: Int) {
        context.remindersDataStore.edit { preferences ->
            val currentData = getData(preferences)
            
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
            
            preferences[REMINDERS_DATA] = gson.toJson(currentData.copy(reminders = updatedReminders))
        }
    }
    
    suspend fun addReminder(title: String, dateTime: Long) {
        addSingleReminder(title, SingleReminderMode.TIME_SPECIFIC, dateTime)
    }
    
    suspend fun deleteReminder(id: String) {
        context.remindersDataStore.edit { preferences ->
            val currentData = getData(preferences)
            val updatedReminders = currentData.reminders.filter { it.id != id }
            preferences[REMINDERS_DATA] = gson.toJson(currentData.copy(reminders = updatedReminders))
        }
    }
    
    suspend fun toggleReminder(id: String) {
        context.remindersDataStore.edit { preferences ->
            val currentData = getData(preferences)
            val updatedReminders = currentData.reminders.map { reminder ->
                if (reminder.id == id) {
                    reminder.copy(isActive = !reminder.isActive)
                } else {
                    reminder
                }
            }
            preferences[REMINDERS_DATA] = gson.toJson(currentData.copy(reminders = updatedReminders))
        }
    }
    
    suspend fun markReminderCompleted(id: String) {
        context.remindersDataStore.edit { preferences ->
            val currentData = getData(preferences)
            val updatedReminders = currentData.reminders.map { reminder ->
                if (reminder.id == id) {
                    reminder.copy(isCompleted = true, isActive = false)
                } else {
                    reminder
                }
            }
            preferences[REMINDERS_DATA] = gson.toJson(currentData.copy(reminders = updatedReminders))
        }
    }
    
    private fun getData(preferences: Preferences): RemindersData {
        val json = preferences[REMINDERS_DATA]
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
    
    suspend fun restoreData(data: RemindersData) {
        context.remindersDataStore.edit { preferences ->
            preferences[REMINDERS_DATA] = gson.toJson(data)
        }
    }
}
