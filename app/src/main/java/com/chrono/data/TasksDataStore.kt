package com.chrono.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.tasksDataStore: DataStore<Preferences> by preferencesDataStore(name = "tasks")

enum class TaskType {
    TASK, BREAK
}

data class TaskEntry(
    val id: String,
    val title: String,
    private val _type: TaskType? = null, // Backing field for backward compatibility
    val isCompleted: Boolean = false,
    val isActive: Boolean = false,
    val groupId: String? = null
) {
    // Custom getter to handle nulls from old data
    val type: TaskType
        get() = _type ?: TaskType.TASK
}

data class TasksData(
    val tasks: List<TaskEntry> = emptyList()
)

class TasksDataStore(private val context: Context) {
    
    companion object {
        private val TASKS_DATA = stringPreferencesKey("tasks_data")
        private val gson = Gson()
    }
    
    val tasksData: Flow<TasksData> = context.tasksDataStore.data.map { preferences ->
        val json = preferences[TASKS_DATA]
        if (json != null) {
            try {
                gson.fromJson(json, TasksData::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                TasksData() // Return empty if parsing fails to avoid crash
            }
        } else {
            TasksData()
        }
    }
    
    suspend fun addSingleTask(title: String) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val id = System.currentTimeMillis().toString()
            
            // If there's already an active task, this one is just added to the list.
            // OR, should we make it active? Let's assume new single tasks are just added.
            // User requested: "If single task, just input the task, send a notification..."
            // This implies it becomes active immediately if it's the only one, or maybe we queue it?
            // Let's make it active if no other task is active.
            
            val hasActiveTask = currentData.tasks.any { it.isActive }
            val newTask = TaskEntry(
                id = id,
                title = title,
                _type = TaskType.TASK,
                isActive = !hasActiveTask
            )
            
            val updatedTasks = currentData.tasks.toMutableList()
            updatedTasks.add(0, newTask)
            
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    suspend fun addTaskGroup(tasks: List<Pair<String, TaskType>>) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val groupId = System.currentTimeMillis().toString()
            val hasActiveTask = currentData.tasks.any { it.isActive }
            
            val newTasks = tasks.mapIndexed { index, (title, type) ->
                TaskEntry(
                    id = "${groupId}_$index",
                    title = title,
                    _type = type,
                    isActive = !hasActiveTask && index == 0, // First one active if nothing else is
                    groupId = groupId
                )
            }
            
            val updatedTasks = currentData.tasks.toMutableList()
            updatedTasks.addAll(0, newTasks) // Add to top
            
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    suspend fun completeTask(id: String) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val tasks = currentData.tasks.toMutableList()
            val index = tasks.indexOfFirst { it.id == id }
            
            if (index != -1) {
                val completedTask = tasks[index].copy(isCompleted = true, isActive = false)
                tasks[index] = completedTask
                
                // Check if part of a group and activate next
                if (completedTask.groupId != null) {
                    // Find next task in same group that is not completed
                    // We need to look at the original list order or filter by group
                    // The list might be sorted or filtered in UI, but here it's raw.
                    // Assuming tasks are stored in order.
                    
                    // We need to find the NEXT task in the list that belongs to the same group and is not completed
                    // Since we added them in order, we can look for the next index
                    // But wait, we added them with `addAll(0, newTasks)`, so they are in order: Task 1, Task 2...
                    // But `addAll(0, ...)` reverses order if not careful? No, `addAll(0, [A, B])` results in [A, B, Old...]
                    
                    // Let's find the next task in the group
                    val nextTaskIndex = tasks.indexOfFirst { 
                        it.groupId == completedTask.groupId && !it.isCompleted && it.id != id
                    }
                    
                    if (nextTaskIndex != -1) {
                        tasks[nextTaskIndex] = tasks[nextTaskIndex].copy(isActive = true)
                    }
                }
            }
            
            saveTasksData(preferences, currentData.copy(tasks = tasks))
        }
    }
    
    suspend fun deleteTask(id: String) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val updatedTasks = currentData.tasks.filter { it.id != id }
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    suspend fun completeAll() {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val updatedTasks = currentData.tasks.map { it.copy(isCompleted = true, isActive = false) }
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }

    private fun getTasksData(preferences: Preferences): TasksData {
        val json = preferences[TASKS_DATA]
        return if (json != null) {
            try {
                gson.fromJson(json, TasksData::class.java)
            } catch (e: Exception) {
                e.printStackTrace()
                TasksData()
            }
        } else {
            TasksData()
        }
    }

    private fun saveTasksData(preferences: MutablePreferences, data: TasksData) {
        preferences[TASKS_DATA] = gson.toJson(data)
    }
}

// Helper alias for MutablePreferences since it's not directly imported
typealias MutablePreferences = androidx.datastore.preferences.core.MutablePreferences
