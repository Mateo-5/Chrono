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

private val Context.notesDataStore: DataStore<Preferences> by preferencesDataStore(name = "notes")

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
        private val NOTES_DATA = stringPreferencesKey("notes_data")
        private val gson = Gson()
    }
    
    val notesData: Flow<NotesData> = context.notesDataStore.data.map { preferences ->
        val json = preferences[NOTES_DATA]
        if (json != null) {
            gson.fromJson(json, NotesData::class.java)
        } else {
            NotesData()
        }
    }
    
    suspend fun addNote(title: String, content: String, date: String) {
        context.notesDataStore.edit { preferences ->
            val currentJson = preferences[NOTES_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, NotesData::class.java)
            } else {
                NotesData()
            }
            
            val id = System.currentTimeMillis().toString()
            val updatedNotes = currentData.notes.toMutableList()
            updatedNotes.add(0, NoteEntry(id, title, content, date))
            
            val updatedData = currentData.copy(notes = updatedNotes)
            preferences[NOTES_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun updateNote(id: String, title: String, content: String) {
        context.notesDataStore.edit { preferences ->
            val currentJson = preferences[NOTES_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, NotesData::class.java)
            } else {
                NotesData()
            }
            
            val updatedNotes = currentData.notes.map { note ->
                if (note.id == id) {
                    note.copy(title = title, content = content)
                } else {
                    note
                }
            }
            
            val updatedData = currentData.copy(notes = updatedNotes)
            preferences[NOTES_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun deleteNote(id: String) {
        context.notesDataStore.edit { preferences ->
            val currentJson = preferences[NOTES_DATA]
            val currentData = if (currentJson != null) {
                gson.fromJson(currentJson, NotesData::class.java)
            } else {
                NotesData()
            }
            
            val updatedNotes = currentData.notes.filter { it.id != id }
            val updatedData = currentData.copy(notes = updatedNotes)
            preferences[NOTES_DATA] = gson.toJson(updatedData)
        }
    }
    
    suspend fun restoreData(data: NotesData) {
        context.notesDataStore.edit { preferences ->
            preferences[NOTES_DATA] = gson.toJson(data)
        }
    }
}
