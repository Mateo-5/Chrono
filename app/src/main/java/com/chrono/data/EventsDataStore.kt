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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private val Context.eventsDataStore: DataStore<Preferences> by preferencesDataStore(name = "events")

data class EventEntry(
    val id: String,
    val title: String,
    val date: String,
    val time: String = "",  // Kept for backward compatibility but not used for new events
    val subtitle: String,
    val isYearly: Boolean = false
)

data class EventsData(
    val events: List<EventEntry> = emptyList()
)

class EventsDataStore(private val context: Context) {
    
    companion object {
        private val EVENTS_DATA = stringPreferencesKey("events_data")
        private val gson = Gson()
        private val dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy", Locale.getDefault())
    }
    
    val eventsData: Flow<EventsData> = context.eventsDataStore.data.map { preferences ->
        val json = preferences[EVENTS_DATA]
        if (json != null) {
            gson.fromJson(json, EventsData::class.java)
        } else {
            EventsData()
        }
    }
    
    suspend fun addEvent(title: String, date: String, subtitle: String, isYearly: Boolean = false) {
        context.eventsDataStore.edit { preferences ->
            val currentJson = preferences[EVENTS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, EventsData::class.java)
            } else {
                EventsData()
            }
            
            val id = System.currentTimeMillis().toString()
            val updatedEvents = currentData.events.toMutableList()
            updatedEvents.add(EventEntry(id, title, date, "", subtitle, isYearly))
            updatedEvents.sortBy { it.date }
            
            val updatedData = currentData.copy(events = updatedEvents)
            preferences[EVENTS_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun updateEvent(id: String, title: String, subtitle: String, isYearly: Boolean) {
        context.eventsDataStore.edit { preferences ->
            val currentJson = preferences[EVENTS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, EventsData::class.java)
            } else {
                EventsData()
            }
            
            val updatedEvents = currentData.events.map { event ->
                if (event.id == id) {
                    event.copy(title = title, subtitle = subtitle, isYearly = isYearly)
                } else {
                    event
                }
            }
            
            val updatedData = currentData.copy(events = updatedEvents)
            preferences[EVENTS_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun deleteEvent(id: String) {
        context.eventsDataStore.edit { preferences ->
            val currentJson = preferences[EVENTS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, EventsData::class.java)
            } else {
                EventsData()
            }
            
            val updatedEvents = currentData.events.filter { it.id != id }
            val updatedData = currentData.copy(events = updatedEvents)
            preferences[EVENTS_DATA] = gson.toJson(updatedData)
        }
    }
    
    fun getEventsForDate(targetDate: String): Flow<List<EventEntry>> {
        return eventsData.map { data ->
            data.events.filter { event ->
                if (event.isYearly) {
                    // For yearly events, match only day and month
                    val eventDayMonth = event.date.take(6) // "dd-MMM"
                    val targetDayMonth = targetDate.take(6)
                    eventDayMonth == targetDayMonth
                } else {
                    event.date == targetDate
                }
            }
        }
    }
    
    suspend fun restoreData(data: EventsData) {
        context.eventsDataStore.edit { preferences ->
            preferences[EVENTS_DATA] = gson.toJson(data)
        }
    }
}
