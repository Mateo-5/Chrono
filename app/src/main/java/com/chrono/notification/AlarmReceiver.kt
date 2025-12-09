package com.chrono.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("title") ?: "Reminder"
        val requestCode = intent.getIntExtra("request_code", System.currentTimeMillis().toInt())
        val reminderId = intent.getStringExtra("reminder_id")
        
        // Use the notification helper which will both show notification AND attempt full-screen
        NotificationHelper.showReminderNotification(context, title, requestCode, reminderId)
        
        // Also directly start the full-screen activity for reliability
        val fullScreenIntent = Intent(context, FullScreenReminderActivity::class.java).apply {
            putExtra("title", title)
            putExtra("reminder_id", reminderId)
            putExtra("notification_id", requestCode)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or 
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or 
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        
        try {
            context.startActivity(fullScreenIntent)
        } catch (e: Exception) {
            e.printStackTrace()
            // Notification fallback already sent via showReminderNotification
        }
    }
}
