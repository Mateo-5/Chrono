package com.chrono.security

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Provides encrypted SharedPreferences using Android's EncryptedSharedPreferences.
 * Uses AES-256 for encryption with keys stored in the Android Keystore.
 */
object EncryptedPreferencesManager {
    
    private const val ENCRYPTED_PREFS_NAME = "chrono_encrypted_prefs"
    
    @Volatile
    private var encryptedPrefs: SharedPreferences? = null
    
    /**
     * Gets the encrypted SharedPreferences instance.
     * Thread-safe singleton pattern.
     */
    fun getEncryptedPrefs(context: Context): SharedPreferences {
        return encryptedPrefs ?: synchronized(this) {
            encryptedPrefs ?: createEncryptedPrefs(context).also {
                encryptedPrefs = it
            }
        }
    }
    
    private fun createEncryptedPrefs(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        
        return EncryptedSharedPreferences.create(
            context,
            ENCRYPTED_PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }
    
    /**
     * Checks if encrypted preferences have any data.
     */
    fun hasData(context: Context): Boolean {
        return getEncryptedPrefs(context).all.isNotEmpty()
    }
    
    /**
     * Clears all encrypted data. Use with caution.
     */
    fun clearAll(context: Context) {
        getEncryptedPrefs(context).edit().clear().apply()
    }
}
