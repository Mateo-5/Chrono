package com.chrono.security

import android.content.Context
import android.content.SharedPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Handles one-time migration of unencrypted DataStore data to encrypted SharedPreferences.
 * This preserves existing user data when upgrading to the secured version.
 */
object DataMigrationManager {
    
    private const val MIGRATION_COMPLETED_KEY = "migration_v1_completed"
    private val gson = Gson()
    
    // Keys for encrypted storage (matching DataStore keys)
    const val KEY_TASKS = "tasks_data"
    const val KEY_NOTES = "notes_data"
    const val KEY_REMINDERS = "reminders_data" 
    const val KEY_EVENTS = "events_data"
    const val KEY_EXAMS = "exams_data"
    const val KEY_TIMETABLE = "timetable_data"
    const val KEY_SETTINGS = "settings_data"
    const val KEY_FOCUS_SETTINGS = "focus_settings"
    const val KEY_NOTIFICATIONS_HISTORY = "notifications_history"
    
    /**
     * Checks if migration has already been completed.
     */
    fun isMigrationCompleted(context: Context): Boolean {
        val prefs = EncryptedPreferencesManager.getEncryptedPrefs(context)
        return prefs.getBoolean(MIGRATION_COMPLETED_KEY, false)
    }
    
    /**
     * Performs the migration from unencrypted DataStore to encrypted SharedPreferences.
     * This is idempotent - can be called multiple times safely.
     */
    suspend fun migrateIfNeeded(context: Context): MigrationResult = withContext(Dispatchers.IO) {
        if (isMigrationCompleted(context)) {
            return@withContext MigrationResult.AlreadyCompleted
        }
        
        val encryptedPrefs = EncryptedPreferencesManager.getEncryptedPrefs(context)
        val editor = encryptedPrefs.edit()
        var migratedCount = 0
        
        try {
            // Migrate each DataStore file
            migratedCount += migrateDataStore(context, "tasks", KEY_TASKS, editor)
            migratedCount += migrateDataStore(context, "notes", KEY_NOTES, editor)
            migratedCount += migrateDataStore(context, "reminders", KEY_REMINDERS, editor)
            migratedCount += migrateDataStore(context, "events", KEY_EVENTS, editor)
            migratedCount += migrateDataStore(context, "exams", KEY_EXAMS, editor)
            migratedCount += migrateDataStore(context, "timetable", KEY_TIMETABLE, editor)
            migratedCount += migrateDataStore(context, "settings", KEY_SETTINGS, editor)
            migratedCount += migrateDataStore(context, "focus_settings", KEY_FOCUS_SETTINGS, editor)
            migratedCount += migrateDataStore(context, "notifications_history", KEY_NOTIFICATIONS_HISTORY, editor)
            
            // Mark migration as completed
            editor.putBoolean(MIGRATION_COMPLETED_KEY, true)
            editor.apply()
            
            // Delete old unencrypted files
            deleteOldDataStoreFiles(context)
            
            MigrationResult.Success(migratedCount)
        } catch (e: Exception) {
            e.printStackTrace()
            MigrationResult.Failed(e.message ?: "Unknown error")
        }
    }
    
    /**
     * Migrates a single DataStore file to encrypted storage.
     * Returns 1 if data was migrated, 0 if no data found.
     */
    private fun migrateDataStore(
        context: Context,
        datastoreName: String,
        encryptedKey: String,
        editor: SharedPreferences.Editor
    ): Int {
        val datastoreDir = File(context.filesDir, "datastore")
        val datastoreFile = File(datastoreDir, "$datastoreName.preferences_pb")
        
        if (!datastoreFile.exists()) {
            return 0
        }
        
        try {
            // Read the protobuf file and extract the JSON value
            // DataStore preferences format: key-value pairs in protobuf
            val content = datastoreFile.readBytes()
            val jsonValue = extractJsonFromDataStore(content, "${datastoreName}_data")
            
            if (jsonValue != null) {
                editor.putString(encryptedKey, jsonValue)
                return 1
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        return 0
    }
    
    /**
     * Extracts JSON string from DataStore protobuf content.
     * DataStore uses a simple key-value format that we can parse.
     */
    private fun extractJsonFromDataStore(content: ByteArray, key: String): String? {
        try {
            // Convert to string and look for JSON content
            val contentStr = String(content, Charsets.UTF_8)
            
            // DataStore embeds JSON as string values - look for JSON patterns
            val jsonStart = contentStr.indexOf('{')
            val jsonEnd = contentStr.lastIndexOf('}')
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                val potentialJson = contentStr.substring(jsonStart, jsonEnd + 1)
                // Validate it's actually JSON by trying to parse it
                gson.fromJson(potentialJson, Any::class.java)
                return potentialJson
            }
        } catch (e: Exception) {
            // Not valid JSON, ignore
        }
        return null
    }
    
    /**
     * Deletes old unencrypted DataStore files after successful migration.
     */
    private fun deleteOldDataStoreFiles(context: Context) {
        val datastoreDir = File(context.filesDir, "datastore")
        if (datastoreDir.exists() && datastoreDir.isDirectory) {
            datastoreDir.listFiles()?.forEach { file ->
                if (file.name.endsWith(".preferences_pb")) {
                    file.delete()
                }
            }
        }
    }
    
    sealed class MigrationResult {
        data object AlreadyCompleted : MigrationResult()
        data class Success(val count: Int) : MigrationResult()
        data class Failed(val error: String) : MigrationResult()
    }
}
