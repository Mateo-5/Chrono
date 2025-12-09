package com.chrono.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.Calendar
import java.util.concurrent.TimeUnit

class WaterBreakWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        
        // Only show notification between 6 AM and 11 PM
        if (currentHour in START_HOUR until END_HOUR) {
            NotificationHelper.showWaterBreakNotification(applicationContext)
        }
        
        return Result.success()
    }
    
    companion object {
        private const val WORK_NAME = "water_break_reminder"
        private const val START_HOUR = 6   // 6 AM
        private const val END_HOUR = 23    // 11 PM
        
        fun schedule(context: Context, intervalMinutes: Int) {
            // Calculate initial delay to next valid time slot
            val calendar = Calendar.getInstance()
            val currentHour = calendar.get(Calendar.HOUR_OF_DAY)
            
            var initialDelayMinutes = 0L
            
            if (currentHour < START_HOUR) {
                // Before 6 AM - delay until 6 AM
                val until6am = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, START_HOUR)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                initialDelayMinutes = (until6am.timeInMillis - calendar.timeInMillis) / 60000
            } else if (currentHour >= END_HOUR) {
                // After 11 PM - delay until 6 AM next day
                val until6amTomorrow = Calendar.getInstance().apply {
                    add(Calendar.DAY_OF_MONTH, 1)
                    set(Calendar.HOUR_OF_DAY, START_HOUR)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                }
                initialDelayMinutes = (until6amTomorrow.timeInMillis - calendar.timeInMillis) / 60000
            }
            // Otherwise, within valid hours - schedule starts from next interval
            
            val workRequest = PeriodicWorkRequestBuilder<WaterBreakWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            )
                .setInitialDelay(initialDelayMinutes.coerceAtLeast(intervalMinutes.toLong()), TimeUnit.MINUTES)
                .build()
            
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                workRequest
            )
        }
        
        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }
}
