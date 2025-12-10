package com.chrono.security

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext

/**
 * Centralized encrypted data manager for all app data.
 * Provides reactive data flows similar to DataStore but backed by EncryptedSharedPreferences.
 */
class EncryptedDataManager(private val context: Context) {
    
    @PublishedApi
    internal val gson = Gson()
    private val prefs: SharedPreferences by lazy {
        EncryptedPreferencesManager.getEncryptedPrefs(context)
    }
    
    // In-memory caches with reactive flows
    private val dataFlows = mutableMapOf<String, MutableStateFlow<String?>>()
    
    /**
     * Gets a reactive flow for a specific data key.
     * Emits updates whenever the data changes.
     */
    fun getDataFlow(key: String): Flow<String?> {
        return getOrCreateFlow(key).asStateFlow()
    }
    
    private fun getOrCreateFlow(key: String): MutableStateFlow<String?> {
        return dataFlows.getOrPut(key) {
            MutableStateFlow(prefs.getString(key, null))
        }
    }
    
    /**
     * Saves data and notifies observers.
     */
    suspend fun saveData(key: String, json: String) = withContext(Dispatchers.IO) {
        prefs.edit().putString(key, json).apply()
        getOrCreateFlow(key).value = json
    }
    
    /**
     * Reads data synchronously.
     */
    fun getData(key: String): String? {
        return prefs.getString(key, null)
    }
    
    /**
     * Serializes and saves an object.
     */
    suspend fun <T> saveObject(key: String, obj: T) {
        saveData(key, gson.toJson(obj))
    }
    
    /**
     * Reads and deserializes an object.
     */
    inline fun <reified T> getObject(key: String): T? {
        val json = getData(key) ?: return null
        return try {
            gson.fromJson(json, T::class.java)
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * Deletes data for a specific key.
     */
    suspend fun deleteData(key: String) = withContext(Dispatchers.IO) {
        prefs.edit().remove(key).apply()
        getOrCreateFlow(key).value = null
    }
    
    companion object {
        @Volatile
        private var instance: EncryptedDataManager? = null
        
        fun getInstance(context: Context): EncryptedDataManager {
            return instance ?: synchronized(this) {
                instance ?: EncryptedDataManager(context.applicationContext).also {
                    instance = it
                }
            }
        }
    }
}
