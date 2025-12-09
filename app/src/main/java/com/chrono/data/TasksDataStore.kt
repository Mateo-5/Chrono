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

enum class TaskStatus {
    PENDING, ACTIVE, COMPLETED, FAILED
}

data class TaskEntry(
    val id: String,
    val title: String,
    private val _type: TaskType? = null,
    val isCompleted: Boolean = false,
    val isActive: Boolean = false,
    val groupId: String? = null,
    val groupName: String? = null,
    val order: Int = 0,
    val isPriority: Boolean = false,
    val priorityDeadlineMinutes: Int? = null,
    val priorityCreatedAt: Long? = null,
    val isFailed: Boolean = false,
    val previousActiveTaskId: String? = null
) {
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
                gson.fromJson(json, TasksData::class.java) ?: TasksData()
            } catch (e: Exception) {
                e.printStackTrace()
                TasksData()
            }
        } else {
            TasksData()
        }
    }
    
    suspend fun addSingleTask(title: String) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val id = System.currentTimeMillis().toString()
            val maxOrder = currentData.tasks.maxOfOrNull { it.order } ?: -1
            
            val hasActiveTask = currentData.tasks.any { it.isActive }
            val newTask = TaskEntry(
                id = id,
                title = title,
                _type = TaskType.TASK,
                isActive = !hasActiveTask,
                order = maxOrder + 1
            )
            
            val updatedTasks = currentData.tasks.toMutableList()
            updatedTasks.add(0, newTask)
            
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    suspend fun addPriorityTask(title: String, deadlineMinutes: Int) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val id = System.currentTimeMillis().toString()
            
            val currentActiveTaskId = currentData.tasks.find { it.isActive && !it.isPriority }?.id
            val updatedExisting = currentData.tasks.map { it.copy(isActive = false) }
            
            val newTask = TaskEntry(
                id = id,
                title = title,
                _type = TaskType.TASK,
                isActive = true,
                order = -1,
                isPriority = true,
                priorityDeadlineMinutes = deadlineMinutes,
                priorityCreatedAt = System.currentTimeMillis(),
                previousActiveTaskId = currentActiveTaskId
            )
            
            val updatedTasks = updatedExisting.toMutableList()
            updatedTasks.add(0, newTask)
            
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    suspend fun markPriorityTaskFailed(id: String) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val tasks = currentData.tasks.toMutableList()
            val failedTaskIndex = tasks.indexOfFirst { it.id == id }
            
            if (failedTaskIndex != -1) {
                val failedTask = tasks[failedTaskIndex]
                val previousActiveId = failedTask.previousActiveTaskId
                
                tasks[failedTaskIndex] = failedTask.copy(
                    isActive = false,
                    isFailed = true
                )
                
                if (previousActiveId != null) {
                    val prevIndex = tasks.indexOfFirst { it.id == previousActiveId }
                    if (prevIndex != -1 && !tasks[prevIndex].isCompleted) {
                        tasks[prevIndex] = tasks[prevIndex].copy(isActive = true)
                    }
                }
                
                saveTasksData(preferences, currentData.copy(tasks = tasks))
            }
        }
    }
    
    suspend fun addTaskGroup(groupName: String, tasks: List<Pair<String, TaskType>>) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val groupId = System.currentTimeMillis().toString()
            val hasActiveTask = currentData.tasks.any { it.isActive }
            val maxOrder = currentData.tasks.maxOfOrNull { it.order } ?: -1
            
            val newTasks = tasks.mapIndexed { index, (title, type) ->
                TaskEntry(
                    id = "${groupId}_$index",
                    title = title,
                    _type = type,
                    isActive = !hasActiveTask && index == 0,
                    groupId = groupId,
                    groupName = if (index == 0) groupName else null,
                    order = maxOrder + 1 + index
                )
            }
            
            val updatedTasks = currentData.tasks.toMutableList()
            updatedTasks.addAll(0, newTasks)
            
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    // Add a task or break to an existing group
    suspend fun addToGroup(groupId: String, title: String, type: TaskType) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val groupTasks = currentData.tasks.filter { it.groupId == groupId }
            
            if (groupTasks.isEmpty()) return@edit
            
            val maxGroupOrder = groupTasks.maxOfOrNull { it.order } ?: 0
            val newTaskId = "${groupId}_${System.currentTimeMillis()}"
            
            val newTask = TaskEntry(
                id = newTaskId,
                title = title,
                _type = type,
                isActive = false,
                groupId = groupId,
                groupName = null,
                order = maxGroupOrder + 1
            )
            
            // Insert after the last task in the group
            val updatedTasks = currentData.tasks.toMutableList()
            val lastGroupIndex = updatedTasks.indexOfLast { it.groupId == groupId }
            if (lastGroupIndex != -1) {
                updatedTasks.add(lastGroupIndex + 1, newTask)
            } else {
                updatedTasks.add(newTask)
            }
            
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    // Complete all tasks in a group
    suspend fun completeGroup(groupId: String) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val updatedTasks = currentData.tasks.map { task ->
                if (task.groupId == groupId) {
                    task.copy(isCompleted = true, isActive = false)
                } else {
                    task
                }
            }
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    // Delete an entire group
    suspend fun deleteGroup(groupId: String) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val updatedTasks = currentData.tasks.filter { it.groupId != groupId }
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    suspend fun setActiveTask(id: String) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val updatedTasks = currentData.tasks.map { task ->
                if (task.id == id && !task.isCompleted && !task.isFailed) {
                    task.copy(isActive = true)
                } else {
                    task.copy(isActive = false)
                }
            }
            saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
        }
    }
    
    suspend fun reorderTasks(fromIndex: Int, toIndex: Int) {
        context.tasksDataStore.edit { preferences ->
            val currentData = getTasksData(preferences)
            val tasks = currentData.tasks.toMutableList()
            
            if (fromIndex in tasks.indices && toIndex in tasks.indices) {
                val item = tasks.removeAt(fromIndex)
                tasks.add(toIndex, item)
                
                val reordered = tasks.mapIndexed { index, task ->
                    task.copy(order = index)
                }
                
                saveTasksData(preferences, currentData.copy(tasks = reordered))
            }
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
                
                if (completedTask.groupId != null) {
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
            val taskToDelete = currentData.tasks.find { it.id == id }
            
            // If deleting a task that has the group name, transfer it to another task in the group
            if (taskToDelete?.groupId != null && taskToDelete.groupName != null) {
                val nextInGroup = currentData.tasks.find { 
                    it.groupId == taskToDelete.groupId && it.id != id 
                }
                if (nextInGroup != null) {
                    val updatedTasks = currentData.tasks.map { task ->
                        if (task.id == nextInGroup.id) {
                            task.copy(groupName = taskToDelete.groupName)
                        } else {
                            task
                        }
                    }.filter { it.id != id }
                    saveTasksData(preferences, currentData.copy(tasks = updatedTasks))
                    return@edit
                }
            }
            
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
                gson.fromJson(json, TasksData::class.java) ?: TasksData()
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
    
    suspend fun restoreData(data: TasksData) {
        context.tasksDataStore.edit { preferences ->
            preferences[TASKS_DATA] = gson.toJson(data)
        }
    }
}

typealias MutablePreferences = androidx.datastore.preferences.core.MutablePreferences
