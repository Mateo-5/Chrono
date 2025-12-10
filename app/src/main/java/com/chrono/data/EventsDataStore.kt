package com.chrono.data

import android.content.Context
import com.chrono.security.EncryptedPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

data class EventEntry(
    val id: String,
    val title: String,
    val date: String,
    val time: String = "",
    val subtitle: String,
    val isYearly: Boolean = false
)

data class EventsData(
    val events: List<EventEntry> = emptyList()
)

class EventsDataStore(private val context: Context) {
    
    companion object {
        private const val KEY_EVENTS_DATA = "events_data"
        private val gson = Gson()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    private val _eventsDataFlow = MutableStateFlow(loadEventsData())
    
    val eventsData: Flow<EventsData> = _eventsDataFlow.asStateFlow()
    
    private fun loadEventsData(): EventsData {
        val json = encryptedPrefs.getString(KEY_EVENTS_DATA, null)
        return if (json != null) {
            try {
                gson.fromJson(json, EventsData::class.java) ?: EventsData()
            } catch (e: Exception) {
                e.printStackTrace()
                EventsData()
            }
        } else {
            EventsData()
        }
    }
    
    private suspend fun saveEventsData(data: EventsData) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_EVENTS_DATA, gson.toJson(data)).apply()
        _eventsDataFlow.value = data
    }
    
    suspend fun addEvent(title: String, date: String, subtitle: String, isYearly: Boolean = false) {
        val currentData = _eventsDataFlow.value
        val id = System.currentTimeMillis().toString()
        
        val updatedEvents = currentData.events.toMutableList()
        updatedEvents.add(EventEntry(id, title, date, "", subtitle, isYearly))
        updatedEvents.sortBy { it.date }
        
        saveEventsData(currentData.copy(events = updatedEvents))
    }
    
    suspend fun updateEvent(id: String, title: String, subtitle: String, isYearly: Boolean) {
        val currentData = _eventsDataFlow.value
        val updatedEvents = currentData.events.map { event ->
            if (event.id == id) {
                event.copy(title = title, subtitle = subtitle, isYearly = isYearly)
            } else {
                event
            }
        }
        saveEventsData(currentData.copy(events = updatedEvents))
    }
    
    suspend fun deleteEvent(id: String) {
        val currentData = _eventsDataFlow.value
        val updatedEvents = currentData.events.filter { it.id != id }
        saveEventsData(currentData.copy(events = updatedEvents))
    }
    
    fun getEventsForDate(targetDate: String): Flow<List<EventEntry>> {
        return eventsData.map { data ->
            data.events.filter { event ->
                if (event.isYearly) {
                    val eventDayMonth = event.date.take(6)
                    val targetDayMonth = targetDate.take(6)
                    eventDayMonth == targetDayMonth
                } else {
                    event.date == targetDate
                }
            }
        }
    }
    
    suspend fun restoreData(data: EventsData) {
        saveEventsData(data)
    }
}
