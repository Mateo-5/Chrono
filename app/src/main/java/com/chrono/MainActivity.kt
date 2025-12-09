package com.chrono

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.view.WindowCompat
import com.chrono.data.SettingsDataStore
import com.chrono.ui.navigation.AppNavigation
import com.chrono.ui.theme.ChronoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display (handles notch/cutouts)
        enableEdgeToEdge()
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Request permissions
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.POST_NOTIFICATIONS,
                    android.Manifest.permission.SCHEDULE_EXACT_ALARM
                ),
                101
            )
        }
        
        // Initialize Notification Channels
        com.chrono.notification.NotificationHelper.createNotificationChannel(this)
        
        // Get saved text scale and apply font scaling
        val prefs = getSharedPreferences("settings", MODE_PRIVATE)
        val textScale = prefs.getFloat("text_scale", 1.0f)
        
        // Apply font scale to configuration
        val config = Configuration(resources.configuration)
        config.fontScale = textScale
        resources.updateConfiguration(config, resources.displayMetrics)
        
        setContent {
            val settingsDataStore = remember { SettingsDataStore(this) }
            val currentScale by settingsDataStore.textScale.collectAsState(initial = textScale)
            
            // Recreate activity when scale changes to apply new font size
            if (kotlin.math.abs(currentScale - textScale) > 0.01f) {
                // Save and recreate
                prefs.edit().putFloat("text_scale", currentScale).apply()
                recreate()
            }
            
            ChronoTheme(textScale = currentScale) {
                AppNavigation()
            }
        }
    }
}
