package com.chrono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.timetableDataStore: DataStore<Preferences> by preferencesDataStore(name = "timetable")

data class TimetableData(
    val timePeriods: List<String> = listOf("9:00\nAM", "10:00\nAM", "11:00\nAM", "12:00\nPM", "1:00\nPM", "2:00\nPM"),
    val schedule: Map<String, List<String>> = mapOf(
        "Mon" to List(6) { "" },
        "Tue" to List(6) { "" },
        "Wed" to List(6) { "" },
        "Thu" to List(6) { "" },
        "Fri" to List(6) { "" }
    )
)

class TimetableDataStore(private val context: Context) {
    
    companion object {
        private val TIMETABLE_DATA = stringPreferencesKey("timetable_data")
        private val gson = Gson()
    }
    
    val timetableData: Flow<TimetableData> = context.timetableDataStore.data.map { preferences ->
        val json = preferences[TIMETABLE_DATA]
        if (json != null) {
            gson.fromJson(json, TimetableData::class.java)
        } else {
            TimetableData()
        }
    }
    
    suspend fun updateSchedule(day: String, periodIndex: Int, subject: String) {
        context.timetableDataStore.edit { preferences ->
            val currentJson = preferences[TIMETABLE_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, TimetableData::class.java)
            } else {
                TimetableData()
            }
            
            val updatedSchedule = currentData.schedule.toMutableMap()
            val daySchedule = updatedSchedule[day]?.toMutableList() ?: MutableList(6) { "" }
            daySchedule[periodIndex] = subject
            updatedSchedule[day] = daySchedule
            
            val updatedData = currentData.copy(schedule = updatedSchedule)
            preferences[TIMETABLE_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun updateTimePeriod(periodIndex: Int, timeLabel: String) {
        context.timetableDataStore.edit { preferences ->
            val currentJson = preferences[TIMETABLE_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, TimetableData::class.java)
            } else {
                TimetableData()
            }
            
            val updatedPeriods = currentData.timePeriods.toMutableList()
            updatedPeriods[periodIndex] = timeLabel
            
            val updatedData = currentData.copy(timePeriods = updatedPeriods)
            preferences[TIMETABLE_DATA] = gson.toJson(updatedData)
        }
    }
}
