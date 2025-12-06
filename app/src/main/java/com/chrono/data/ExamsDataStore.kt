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

private val Context.examsDataStore: DataStore<Preferences> by preferencesDataStore(name = "exams")

data class ExamEntry(
    val date: String,
    val subject: String
)

data class ExamsData(
    val exams: List<ExamEntry> = emptyList()
)

class ExamsDataStore(private val context: Context) {
    
    companion object {
        private val EXAMS_DATA = stringPreferencesKey("exams_data")
        private val gson = Gson()
    }
    
    val examsData: Flow<ExamsData> = context.examsDataStore.data.map { preferences ->
        val json = preferences[EXAMS_DATA]
        if (json != null) {
            gson.fromJson(json, ExamsData::class.java)
        } else {
            ExamsData()
        }
    }
    
    suspend fun addExam(date: String, subject: String) {
        context.examsDataStore.edit { preferences ->
            val currentJson = preferences[EXAMS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, ExamsData::class.java)
            } else {
                ExamsData()
            }
            
            val updatedExams = currentData.exams.toMutableList()
            updatedExams.add(ExamEntry(date, subject))
            updatedExams.sortBy { it.date }
            
            val updatedData = currentData.copy(exams = updatedExams)
            preferences[EXAMS_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun updateExam(index: Int, date: String, subject: String) {
        context.examsDataStore.edit { preferences ->
            val currentJson = preferences[EXAMS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, ExamsData::class.java)
            } else {
                ExamsData()
            }
            
            val updatedExams = currentData.exams.toMutableList()
            if (index < updatedExams.size) {
                updatedExams[index] = ExamEntry(date, subject)
                updatedExams.sortBy { it.date }
            }
            
            val updatedData = currentData.copy(exams = updatedExams)
            preferences[EXAMS_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun deleteExam(index: Int) {
        context.examsDataStore.edit { preferences ->
            val currentJson = preferences[EXAMS_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, ExamsData::class.java)
            } else {
                ExamsData()
            }
            
            val updatedExams = currentData.exams.toMutableList()
            if (index < updatedExams.size) {
                updatedExams.removeAt(index)
            }
            
            val updatedData = currentData.copy(exams = updatedExams)
            preferences[EXAMS_DATA] = gson.toJson(updatedData)
        }
    }
}
