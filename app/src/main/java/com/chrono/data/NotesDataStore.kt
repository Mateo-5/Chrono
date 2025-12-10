package com.chrono.data

import android.content.Context
import com.chrono.security.EncryptedPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class NoteEntry(
    val id: String,
    val title: String,
    val content: String,
    val date: String
)

data class NotesData(
    val notes: List<NoteEntry> = emptyList()
)

class NotesDataStore(private val context: Context) {
    
    companion object {
        private const val KEY_NOTES_DATA = "notes_data"
        private val gson = Gson()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    private val _notesDataFlow = MutableStateFlow(loadNotesData())
    
    val notesData: Flow<NotesData> = _notesDataFlow.asStateFlow()
    
    private fun loadNotesData(): NotesData {
        val json = encryptedPrefs.getString(KEY_NOTES_DATA, null)
        return if (json != null) {
            try {
                gson.fromJson(json, NotesData::class.java) ?: NotesData()
            } catch (e: Exception) {
                e.printStackTrace()
                NotesData()
            }
        } else {
            NotesData()
        }
    }
    
    private suspend fun saveNotesData(data: NotesData) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_NOTES_DATA, gson.toJson(data)).apply()
        _notesDataFlow.value = data
    }
    
    suspend fun addNote(title: String, content: String, date: String) {
        val currentData = _notesDataFlow.value
        val id = System.currentTimeMillis().toString()
        val updatedNotes = currentData.notes.toMutableList()
        updatedNotes.add(0, NoteEntry(id, title, content, date))
        saveNotesData(currentData.copy(notes = updatedNotes))
    }
    
    suspend fun updateNote(id: String, title: String, content: String) {
        val currentData = _notesDataFlow.value
        val updatedNotes = currentData.notes.map { note ->
            if (note.id == id) {
                note.copy(title = title, content = content)
            } else {
                note
            }
        }
        saveNotesData(currentData.copy(notes = updatedNotes))
    }
    
    suspend fun deleteNote(id: String) {
        val currentData = _notesDataFlow.value
        val updatedNotes = currentData.notes.filter { it.id != id }
        saveNotesData(currentData.copy(notes = updatedNotes))
    }
    
    suspend fun restoreData(data: NotesData) {
        saveNotesData(data)
    }
}
