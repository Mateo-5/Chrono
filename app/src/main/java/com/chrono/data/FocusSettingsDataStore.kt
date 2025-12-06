package com.chrono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.focusSettingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "focus_settings")

data class FocusSettings(
    val blockedPackageNames: Set<String> = emptySet(),
    val isStrictDndEnabled: Boolean = true
)

class FocusSettingsDataStore(private val context: Context) {

    companion object {
        private val BLOCKED_PACKAGES = stringSetPreferencesKey("blocked_packages")
        private val STRICT_DND_ENABLED = booleanPreferencesKey("strict_dnd_enabled")
    }

    val focusSettings: Flow<FocusSettings> = context.focusSettingsDataStore.data.map { preferences ->
        FocusSettings(
            blockedPackageNames = preferences[BLOCKED_PACKAGES] ?: emptySet(),
            isStrictDndEnabled = preferences[STRICT_DND_ENABLED] ?: true
        )
    }

    suspend fun updateBlockedPackages(packages: Set<String>) {
        context.focusSettingsDataStore.edit { preferences ->
            preferences[BLOCKED_PACKAGES] = packages
        }
    }

    suspend fun updateStrictDndEnabled(enabled: Boolean) {
        context.focusSettingsDataStore.edit { preferences ->
            preferences[STRICT_DND_ENABLED] = enabled
        }
    }
}
