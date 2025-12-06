package com.chrono.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import androidx.core.app.NotificationCompat
import com.chrono.MainActivity
import com.chrono.R
import com.chrono.data.TaskEntry
import com.chrono.data.TaskType

object NotificationHelper {
    
    const val WATER_BREAK_CHANNEL_ID = "water_break_channel"
    const val TASK_CHANNEL_ID = "task_channel"
    
    private const val WATER_BREAK_NOTIFICATION_ID = 1001
    private const val ACTIVE_TASK_NOTIFICATION_ID = 1002
    
    fun createNotificationChannel(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Water Break Channel
        val waterChannel = NotificationChannel(
            WATER_BREAK_CHANNEL_ID,
            "Water Break Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Reminds you to drink water"
            enableLights(true)
            lightColor = Color.parseColor("#5FA8D3")
            enableVibration(true)
        }
        notificationManager.createNotificationChannel(waterChannel)
        
        // Task Channel
        val taskChannel = NotificationChannel(
            TASK_CHANNEL_ID,
            "Active Tasks",
            NotificationManager.IMPORTANCE_LOW // Low importance for persistent notification to avoid constant popping
        ).apply {
            description = "Shows your currently active task"
            enableLights(false)
            enableVibration(false)
        }
        notificationManager.createNotificationChannel(taskChannel)
    }
    
    fun showWaterBreakNotification(context: Context) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(context, WATER_BREAK_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_compass)
            .setContentTitle("Water Break")
            .setContentText("Time to drink some water and stay hydrated!")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("Time to drink some water and stay hydrated! Taking regular water breaks helps maintain focus and energy."))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(Color.parseColor("#5FA8D3"))
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
        
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(WATER_BREAK_NOTIFICATION_ID, notification)
    }
    
    fun showActiveTaskNotification(context: Context, task: TaskEntry) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Complete Action
        val completeIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
            action = TaskNotificationReceiver.ACTION_COMPLETE_TASK
            putExtra(TaskNotificationReceiver.EXTRA_TASK_ID, task.id)
            putExtra(TaskNotificationReceiver.EXTRA_TASK_TITLE, task.title)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context, task.id.hashCode(), completeIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val title = if (task.type == TaskType.BREAK) "Break Time" else "Current Task"
        val icon = if (task.type == TaskType.BREAK) android.R.drawable.ic_lock_idle_alarm else android.R.drawable.ic_menu_edit
        
        val notification = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(task.title)
            .setOngoing(true) // Persistent
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .addAction(R.drawable.ic_launcher_foreground, "Complete", completePendingIntent) // Using launcher icon as placeholder if check icon not avail
            .build()
            
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(ACTIVE_TASK_NOTIFICATION_ID, notification)
    }
    
    fun showTaskCompletedNotification(context: Context, taskTitle: String) {
        val notification = NotificationCompat.Builder(context, TASK_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_input_add)
            .setContentTitle("Task Completed")
            .setContentText("Completed: $taskTitle")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
            
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Use a unique ID so they stack if multiple happen quickly, or same to replace?
        // Let's stack them.
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
    
    fun cancelTaskNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(ACTIVE_TASK_NOTIFICATION_ID)
    }
}
