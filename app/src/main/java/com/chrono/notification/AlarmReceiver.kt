package com.chrono.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import java.util.Calendar

class AlarmReceiver : BroadcastReceiver() {
    
    companion object {
        private const val WAKE_LOCK_TAG = "chrono:ReminderWakeLock"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        // Acquire wake lock to ensure device stays awake
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
        val wakeLock = powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            WAKE_LOCK_TAG
        )
        wakeLock.acquire(10 * 1000L) // 10 seconds max
        
        try {
            val title = intent.getStringExtra("title") ?: "Reminder"
            val requestCode = intent.getIntExtra("request_code", System.currentTimeMillis().toInt())
            val reminderId = intent.getStringExtra("reminder_id")
            val isRepeating = intent.getBooleanExtra("is_repeating", false)
            
            // Show notification first (always works)
            NotificationHelper.showReminderNotification(context, title, requestCode, reminderId)
            
            // Start full-screen activity
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
            }
            
            // Reschedule if this is a repeating alarm
            if (isRepeating) {
                val hour = intent.getIntExtra("repeat_hour", 0)
                val minute = intent.getIntExtra("repeat_minute", 0)
                rescheduleRepeatingAlarm(context, title, hour, minute)
            }
        } finally {
            if (wakeLock.isHeld) {
                wakeLock.release()
            }
        }
    }
    
    private fun rescheduleRepeatingAlarm(context: Context, title: String, hour: Int, minute: Int) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val intent = Intent(context, AlarmReceiver::class.java).apply {
                putExtra("title", title)
                putExtra("is_repeating", true)
                putExtra("repeat_hour", hour)
                putExtra("repeat_minute", minute)
            }
            
            // Schedule for tomorrow at the same time
            val calendar = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_MONTH, 1)
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            
            val requestCode = (hour * 60 + minute) + 10000
            
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        calendar.timeInMillis,
                        pendingIntent
                    )
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    calendar.timeInMillis,
                    pendingIntent
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
