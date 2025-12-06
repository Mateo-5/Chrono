package com.chrono.notification

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import java.util.concurrent.TimeUnit

class WaterBreakWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {
    
    override suspend fun doWork(): Result {
        NotificationHelper.showWaterBreakNotification(applicationContext)
        return Result.success()
    }
    
    companion object {
        private const val WORK_NAME = "water_break_reminder"
        
        fun schedule(context: Context, intervalMinutes: Int) {
            val workRequest = PeriodicWorkRequestBuilder<WaterBreakWorker>(
                intervalMinutes.toLong(), TimeUnit.MINUTES
            ).build()
            
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
