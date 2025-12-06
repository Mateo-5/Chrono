package com.chrono

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
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
        
        setContent {
            ChronoTheme {
                AppNavigation()
            }
        }
    }
}
