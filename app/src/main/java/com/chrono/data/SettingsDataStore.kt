package com.chrono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {
    
    companion object {
        private val WATER_BREAK_ENABLED = booleanPreferencesKey("water_break_enabled")
        private val WATER_BREAK_INTERVAL = intPreferencesKey("water_break_interval_minutes")
        
        private val FOCUS_DURATION = intPreferencesKey("focus_duration")
        private val BREAK_DURATION = intPreferencesKey("break_duration")
        private val SOUND_EFFECTS = booleanPreferencesKey("sound_effects")
        
        private val TEXT_SCALE = floatPreferencesKey("text_scale")
        
        const val DEFAULT_INTERVAL = 60 // 1 hour in minutes
    }
    
    val waterBreakEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[WATER_BREAK_ENABLED] ?: false
    }
    
    val waterBreakInterval: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[WATER_BREAK_INTERVAL] ?: DEFAULT_INTERVAL
    }
    
    val focusDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[FOCUS_DURATION] ?: 25
    }
    
    val breakDuration: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[BREAK_DURATION] ?: 5
    }
    
    val soundEffectsEnabled: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[SOUND_EFFECTS] ?: true
    }
    
    val textScale: Flow<Float> = context.dataStore.data.map { preferences ->
        preferences[TEXT_SCALE] ?: 1.0f
    }
    
    suspend fun setWaterBreakEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[WATER_BREAK_ENABLED] = enabled
        }
    }
    
    suspend fun setWaterBreakInterval(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[WATER_BREAK_INTERVAL] = minutes
        }
    }
    
    suspend fun setFocusDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[FOCUS_DURATION] = minutes
        }
    }
    
    suspend fun setBreakDuration(minutes: Int) {
        context.dataStore.edit { preferences ->
            preferences[BREAK_DURATION] = minutes
        }
    }
    
    suspend fun setSoundEffectsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[SOUND_EFFECTS] = enabled
        }
    }
    
    suspend fun setTextScale(scale: Float) {
        context.dataStore.edit { preferences ->
            preferences[TEXT_SCALE] = scale
        }
        // Also write to SharedPreferences for global access
        context.getSharedPreferences("settings", android.content.Context.MODE_PRIVATE)
            .edit()
            .putFloat("text_scale", scale)
            .apply()
    }
}
