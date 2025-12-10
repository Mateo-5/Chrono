package com.chrono.data

import android.content.Context
import com.chrono.security.EncryptedPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

class SettingsDataStore(private val context: Context) {
    
    companion object {
        private const val KEY_WATER_BREAK_ENABLED = "water_break_enabled"
        private const val KEY_WATER_BREAK_INTERVAL = "water_break_interval_minutes"
        private const val KEY_FOCUS_DURATION = "focus_duration"
        private const val KEY_BREAK_DURATION = "break_duration"
        private const val KEY_SOUND_EFFECTS = "sound_effects"
        private const val KEY_TEXT_SCALE = "text_scale"
        
        const val DEFAULT_INTERVAL = 60
    }
    
    private val encryptedPrefs by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    private val _waterBreakEnabled = MutableStateFlow(encryptedPrefs.getBoolean(KEY_WATER_BREAK_ENABLED, false))
    private val _waterBreakInterval = MutableStateFlow(encryptedPrefs.getInt(KEY_WATER_BREAK_INTERVAL, DEFAULT_INTERVAL))
    private val _focusDuration = MutableStateFlow(encryptedPrefs.getInt(KEY_FOCUS_DURATION, 25))
    private val _breakDuration = MutableStateFlow(encryptedPrefs.getInt(KEY_BREAK_DURATION, 5))
    private val _soundEffectsEnabled = MutableStateFlow(encryptedPrefs.getBoolean(KEY_SOUND_EFFECTS, true))
    private val _textScale = MutableStateFlow(encryptedPrefs.getFloat(KEY_TEXT_SCALE, 1.0f))
    
    val waterBreakEnabled: Flow<Boolean> = _waterBreakEnabled.asStateFlow()
    val waterBreakInterval: Flow<Int> = _waterBreakInterval.asStateFlow()
    val focusDuration: Flow<Int> = _focusDuration.asStateFlow()
    val breakDuration: Flow<Int> = _breakDuration.asStateFlow()
    val soundEffectsEnabled: Flow<Boolean> = _soundEffectsEnabled.asStateFlow()
    val textScale: Flow<Float> = _textScale.asStateFlow()
    
    suspend fun setWaterBreakEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putBoolean(KEY_WATER_BREAK_ENABLED, enabled).apply()
        _waterBreakEnabled.value = enabled
    }
    
    suspend fun setWaterBreakInterval(minutes: Int) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putInt(KEY_WATER_BREAK_INTERVAL, minutes).apply()
        _waterBreakInterval.value = minutes
    }
    
    suspend fun setFocusDuration(minutes: Int) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putInt(KEY_FOCUS_DURATION, minutes).apply()
        _focusDuration.value = minutes
    }
    
    suspend fun setBreakDuration(minutes: Int) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putInt(KEY_BREAK_DURATION, minutes).apply()
        _breakDuration.value = minutes
    }
    
    suspend fun setSoundEffectsEnabled(enabled: Boolean) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putBoolean(KEY_SOUND_EFFECTS, enabled).apply()
        _soundEffectsEnabled.value = enabled
    }
    
    suspend fun setTextScale(scale: Float) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putFloat(KEY_TEXT_SCALE, scale).apply()
        _textScale.value = scale
    }
}
