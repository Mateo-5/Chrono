package com.chrono.data

import android.content.Context
import com.chrono.security.EncryptedPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class ExamEntry(
    val id: String = "",
    val date: String,
    val subject: String,
    val isOver: Boolean = false
)

data class ExamsData(
    val exams: List<ExamEntry> = emptyList()
)

class ExamsDataStore(private val context: Context) {
    
    companion object {
        private const val KEY_EXAMS_DATA = "exams_data"
        private val gson = Gson()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    private val _examsDataFlow = MutableStateFlow(loadExamsData())
    
    val examsData: Flow<ExamsData> = _examsDataFlow.asStateFlow()
    
    private fun loadExamsData(): ExamsData {
        val json = encryptedPrefs.getString(KEY_EXAMS_DATA, null)
        return if (json != null) {
            try {
                gson.fromJson(json, ExamsData::class.java) ?: ExamsData()
            } catch (e: Exception) {
                e.printStackTrace()
                ExamsData()
            }
        } else {
            ExamsData()
        }
    }
    
    private suspend fun saveExamsData(data: ExamsData) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_EXAMS_DATA, gson.toJson(data)).apply()
        _examsDataFlow.value = data
    }
    
    suspend fun addExam(date: String, subject: String) {
        val currentData = _examsDataFlow.value
        val id = System.currentTimeMillis().toString()
        
        val updatedExams = currentData.exams.toMutableList()
        updatedExams.add(ExamEntry(id, date, subject, false))
        updatedExams.sortBy { it.date }
        
        saveExamsData(currentData.copy(exams = updatedExams))
    }
    
    suspend fun updateExam(index: Int, date: String, subject: String) {
        val currentData = _examsDataFlow.value
        val updatedExams = currentData.exams.toMutableList()
        
        if (index < updatedExams.size) {
            val existingId = updatedExams[index].id.ifEmpty { System.currentTimeMillis().toString() }
            updatedExams[index] = ExamEntry(existingId, date, subject, updatedExams[index].isOver)
            updatedExams.sortBy { it.date }
        }
        
        saveExamsData(currentData.copy(exams = updatedExams))
    }
    
    suspend fun markExamOver(index: Int) {
        val currentData = _examsDataFlow.value
        val updatedExams = currentData.exams.toMutableList()
        
        if (index < updatedExams.size) {
            val exam = updatedExams[index]
            updatedExams[index] = exam.copy(isOver = !exam.isOver)
        }
        
        saveExamsData(currentData.copy(exams = updatedExams))
    }
    
    suspend fun deleteExam(index: Int) {
        val currentData = _examsDataFlow.value
        val updatedExams = currentData.exams.toMutableList()
        
        if (index < updatedExams.size) {
            updatedExams.removeAt(index)
        }
        
        saveExamsData(currentData.copy(exams = updatedExams))
    }
    
    suspend fun deleteAllExams() {
        saveExamsData(ExamsData())
    }
    
    suspend fun restoreData(data: ExamsData) {
        saveExamsData(data)
    }
}
