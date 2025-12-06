package com.chrono.data

import android.content.Context
import androidx.datastore.core. DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.remindersDataStore: DataStore<Preferences> by preferencesDataStore(name = "reminders")

data class ReminderEntry(
    val id: String,
    val title: String,
    val dateTime: Long, // Timestamp in millis
    val isActive: Boolean = true
)

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
            gson.fromJson(json, RemindersData::class.java)
        } else {
            RemindersData()
        }
    }
    
    suspend fun addReminder(title: String, dateTime: Long) {
        context.remindersDataStore.edit { preferences ->
            val currentJson = preferences[REMINDERS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, RemindersData::class.java)
            } else {
                RemindersData()
            }
            
            val id = System.currentTimeMillis().toString()
            val updatedReminders = currentData.reminders.toMutableList()
            updatedReminders.add(ReminderEntry(id, title, dateTime, true))
            updatedReminders.sortBy { it.dateTime }
            
            val updatedData = currentData.copy(reminders = updatedReminders)
            preferences[REMINDERS_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun deleteReminder(id: String) {
        context.remindersDataStore.edit { preferences ->
            val currentJson = preferences[REMINDERS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, RemindersData::class.java)
            } else {
                RemindersData()
            }
            
            val updatedReminders = currentData.reminders.filter { it.id != id }
            val updatedData = currentData.copy(reminders = updatedReminders)
            preferences[REMINDERS_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun toggleReminder(id: String) {
        context.remindersDataStore.edit { preferences ->
            val currentJson = preferences[REMINDERS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, RemindersData::class.java)
            } else {
                RemindersData()
            }
            
            val updatedReminders = currentData.reminders.map { reminder ->
                if (reminder.id == id) {
                    reminder.copy(isActive = !reminder.isActive)
                } else {
                    reminder
                }
            }
            
            val updatedData = currentData.copy(reminders = updatedReminders)
            preferences[REMINDERS_DATA] = gson.toJson(updatedData)
        }
    }
}
