package com.chrono.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.chrono.data.TasksDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskNotificationReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_COMPLETE_TASK) {
            val taskId = intent.getStringExtra(EXTRA_TASK_ID) ?: return
            val tasksDataStore = TasksDataStore(context)
            
            CoroutineScope(Dispatchers.IO).launch {
                tasksDataStore.completeTask(taskId)
                // NotificationHelper will be triggered by UI observation or we can trigger it here?
                // Ideally, the UI observes the data and updates. 
                // But if the app is in background, we might need to manually trigger the next notification.
                // However, TasksDataStore doesn't broadcast changes to non-observers.
                // For now, let's rely on the fact that if the app is open, it updates.
                // If the app is closed, we might need a Service or WorkManager to handle the "Next Task" logic reliably.
                // Given the scope, let's assume the user might be interacting with the app or we can just update the data.
                // But wait, if we complete a task, we need to show the NEXT task notification if it's a group.
                // The `completeTask` logic in DataStore updates the `isActive` flag.
                // We need a way to react to that.
                
                // Let's manually trigger a check or update notification here.
                // Since we don't have easy access to the "Next Active Task" without querying data store again.
                // We can query it.
                
                tasksDataStore.tasksData.collect { data ->
                    val activeTask = data.tasks.find { it.isActive }
                    if (activeTask != null) {
                        NotificationHelper.showActiveTaskNotification(context, activeTask)
                    } else {
                        NotificationHelper.cancelTaskNotification(context)
                    }
                    // We only need to do this once, so cancel collection
                    return@collect
                }
            }
            
            // Show completion message
            val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task"
            NotificationHelper.showTaskCompletedNotification(context, taskTitle)
        }
    }
    
    companion object {
        const val ACTION_COMPLETE_TASK = "com.chrono.ACTION_COMPLETE_TASK"
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
    }
}
