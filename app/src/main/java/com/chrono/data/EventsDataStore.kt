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

private val Context.eventsDataStore: DataStore<Preferences> by preferencesDataStore(name = "events")

data class EventEntry(
    val id: String,
    val title: String,
    val date: String,
    val time: String,
    val subtitle: String
)

data class EventsData(
    val events: List<EventEntry> = emptyList()
)

class EventsDataStore(private val context: Context) {
    
    companion object {
        private val EVENTS_DATA = stringPreferencesKey("events_data")
        private val gson = Gson()
    }
    
    val eventsData: Flow<EventsData> = context.eventsDataStore.data.map { preferences ->
        val json = preferences[EVENTS_DATA]
        if (json != null) {
            gson.fromJson(json, EventsData::class.java)
        } else {
            EventsData()
        }
    }
    
    suspend fun addEvent(title: String, date: String, time: String, subtitle: String) {
        context.eventsDataStore.edit { preferences ->
            val currentJson = preferences[EVENTS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, EventsData::class.java)
            } else {
                EventsData()
            }
            
            val id = System.currentTimeMillis().toString()
            val updatedEvents = currentData.events.toMutableList()
            updatedEvents.add(EventEntry(id, title, date, time, subtitle))
            updatedEvents.sortBy { it.date }
            
            val updatedData = currentData.copy(events = updatedEvents)
            preferences[EVENTS_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun updateEvent(id: String, title: String, time: String, subtitle: String) {
        context.eventsDataStore.edit { preferences ->
            val currentJson = preferences[EVENTS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, EventsData::class.java)
            } else {
                EventsData()
            }
            
            val updatedEvents = currentData.events.map { event ->
                if (event.id == id) {
                    event.copy(title = title, time = time, subtitle = subtitle)
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
    
    fun getEventsForDate(date: String): Flow<List<EventEntry>> {
        return eventsData.map { data ->
            data.events.filter { it.date == date }
        }
    }
}
