package com.chrono.data

import android.content.Context
import com.chrono.security.EncryptedPreferencesManager
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

data class FocusSettings(
    val blockedPackageNames: Set<String> = emptySet(),
    val isStrictDndEnabled: Boolean = true
)

class FocusSettingsDataStore(private val context: Context) {
    
    companion object {
        private const val KEY_FOCUS_SETTINGS = "focus_settings"
        private val gson = Gson()
    }
    
    private val encryptedPrefs by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    private val _focusSettingsFlow = MutableStateFlow(loadFocusSettings())
    
    val focusSettings: Flow<FocusSettings> = _focusSettingsFlow.asStateFlow()
    
    private fun loadFocusSettings(): FocusSettings {
        val json = encryptedPrefs.getString(KEY_FOCUS_SETTINGS, null)
        return if (json != null) {
            try {
                gson.fromJson(json, FocusSettings::class.java) ?: FocusSettings()
            } catch (e: Exception) {
                e.printStackTrace()
                FocusSettings()
            }
        } else {
            FocusSettings()
        }
    }
    
    private suspend fun saveFocusSettings(settings: FocusSettings) = withContext(Dispatchers.IO) {
        encryptedPrefs.edit().putString(KEY_FOCUS_SETTINGS, gson.toJson(settings)).apply()
        _focusSettingsFlow.value = settings
    }
    
    suspend fun updateBlockedPackages(packages: Set<String>) {
        val current = _focusSettingsFlow.value
        saveFocusSettings(current.copy(blockedPackageNames = packages))
    }
    
    suspend fun updateStrictDndEnabled(enabled: Boolean) {
        val current = _focusSettingsFlow.value
        saveFocusSettings(current.copy(isStrictDndEnabled = enabled))
    }
    
    suspend fun restoreData(settings: FocusSettings) {
        saveFocusSettings(settings)
    }
}
