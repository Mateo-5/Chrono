package com.chrono.data

import android.content.Context
import com.chrono.security.EncryptedPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

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
        private const val KEY_TIMETABLE_DATA = "timetable_data"
        private val gson = Gson()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    private val _timetableDataFlow = MutableStateFlow(loadTimetableData())
    
    val timetableData: Flow<TimetableData> = _timetableDataFlow.asStateFlow()
    
    private fun loadTimetableData(): TimetableData {
        val json = encryptedPrefs.getString(KEY_TIMETABLE_DATA, null)
        return if (json != null) {
            try {
                gson.fromJson(json, TimetableData::class.java) ?: TimetableData()
            } catch (e: Exception) {
                e.printStackTrace()
                TimetableData()
            }
        } else {
            TimetableData()
        }
    }
    
    private suspend fun saveTimetableData(data: TimetableData) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_TIMETABLE_DATA, gson.toJson(data)).apply()
        _timetableDataFlow.value = data
    }
    
    suspend fun updateSchedule(day: String, periodIndex: Int, subject: String) {
        val currentData = _timetableDataFlow.value
        val updatedSchedule = currentData.schedule.toMutableMap()
        val daySchedule = updatedSchedule[day]?.toMutableList() ?: MutableList(6) { "" }
        daySchedule[periodIndex] = subject
        updatedSchedule[day] = daySchedule
        
        saveTimetableData(currentData.copy(schedule = updatedSchedule))
    }
    
    suspend fun updateTimePeriod(periodIndex: Int, timeLabel: String) {
        val currentData = _timetableDataFlow.value
        val updatedPeriods = currentData.timePeriods.toMutableList()
        updatedPeriods[periodIndex] = timeLabel
        
        saveTimetableData(currentData.copy(timePeriods = updatedPeriods))
    }
    
    suspend fun restoreData(data: TimetableData) {
        saveTimetableData(data)
    }
}
