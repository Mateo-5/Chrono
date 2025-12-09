package com.chrono.ui.tasks

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.chrono.data.TaskEntry
import com.chrono.data.TaskType
import com.chrono.data.TasksDataStore
import com.chrono.notification.NotificationHelper
import com.chrono.notification.PriorityTaskFailedActivity
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

private val GlassBorder = Color(0x60FFFFFF)

@Composable
fun TasksScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val tasksDataStore = remember { TasksDataStore(context) }
    val tasksData by tasksDataStore.tasksData.collectAsState(initial = com.chrono.data.TasksData())
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    
    // Dialog States
    var showTypeSelection by remember { mutableStateOf(false) }
    var showSingleTaskDialog by remember { mutableStateOf(false) }
    var showGroupTaskDialog by remember { mutableStateOf(false) }
    var showPriorityTaskDialog by remember { mutableStateOf(false) }
    var showAddToGroupDialog by remember { mutableStateOf(false) }
    var addToGroupId by remember { mutableStateOf("") }
    var addToGroupName by remember { mutableStateOf("") }
    
    // Collapsed groups state
    val collapsedGroups = remember { mutableStateMapOf<String, Boolean>() }
    
    // Drag state
    var draggedTaskId by remember { mutableStateOf<String?>(null) }
    var dragOffset by remember { mutableFloatStateOf(0f) }
    
    // Update notification when active task changes
    LaunchedEffect(tasksData.tasks) {
        val activeTask = tasksData.tasks.find { it.isActive }
        if (activeTask != null) {
            NotificationHelper.showActiveTaskNotification(context, activeTask)
        } else {
            NotificationHelper.cancelTaskNotification(context)
        }
    }
    
    // Build display structure
    data class GroupInfo(
        val groupId: String,
        val groupName: String,
        val tasks: List<TaskEntry>,
        val completedCount: Int,
        val totalCount: Int
    )
    
    val groups = remember(tasksData.tasks) {
        val groupMap = mutableMapOf<String, MutableList<TaskEntry>>()
        val groupNames = mutableMapOf<String, String>()
        
        tasksData.tasks.forEach { task ->
            if (task.groupId != null) {
                groupMap.getOrPut(task.groupId) { mutableListOf() }.add(task)
                if (task.groupName != null) {
                    groupNames[task.groupId] = task.groupName
                }
            }
        }
        
        groupMap.map { (id, tasks) ->
            GroupInfo(
                groupId = id,
                groupName = groupNames[id] ?: "Group",
                tasks = tasks,
                completedCount = tasks.count { it.isCompleted },
                totalCount = tasks.size
            )
        }
    }
    
    val singleTasks = remember(tasksData.tasks) {
        tasksData.tasks.filter { it.groupId == null }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = statusBarPadding.calculateTopPadding(),
                    bottom = navBarPadding.calculateBottomPadding()
                )
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    Text(
                        text = "Tasks",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                if (tasksData.tasks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A2A2A))
                            .clickable { scope.launch { tasksDataStore.completeAll() } }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "Complete All",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            
            // Empty State
            if (tasksData.tasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No tasks yet",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to start working",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Single Tasks
                    items(singleTasks, key = { it.id }) { task ->
                        val isDragging = draggedTaskId == task.id
                        val taskIndex = tasksData.tasks.indexOfFirst { it.id == task.id }
                        
                        Box(
                            modifier = Modifier
                                .zIndex(if (isDragging) 1f else 0f)
                                .offset { IntOffset(0, if (isDragging) dragOffset.roundToInt() else 0) }
                                .scale(if (isDragging) 1.02f else 1f)
                                .shadow(if (isDragging) 16.dp else 0.dp, RoundedCornerShape(16.dp))
                                .then(
                                    if (!task.isCompleted && !task.isFailed) {
                                        Modifier.pointerInput(task.id) {
                                            detectDragGesturesAfterLongPress(
                                                onDragStart = { draggedTaskId = task.id },
                                                onDrag = { change, offset ->
                                                    change.consume()
                                                    dragOffset += offset.y
                                                },
                                                onDragEnd = {
                                                    if (draggedTaskId != null && taskIndex >= 0) {
                                                        val moveBy = (dragOffset / 100f).roundToInt()
                                                        val target = (taskIndex + moveBy).coerceIn(0, tasksData.tasks.size - 1)
                                                        if (target != taskIndex) {
                                                            scope.launch { tasksDataStore.reorderTasks(taskIndex, target) }
                                                        }
                                                    }
                                                    draggedTaskId = null
                                                    dragOffset = 0f
                                                },
                                                onDragCancel = { draggedTaskId = null; dragOffset = 0f }
                                            )
                                        }
                                    } else Modifier
                                )
                        ) {
                            TaskCard(
                                task = task,
                                onComplete = { scope.launch { tasksDataStore.completeTask(task.id); NotificationHelper.showTaskCompletedNotification(context, task.title) } },
                                onDelete = { scope.launch { tasksDataStore.deleteTask(task.id) } },
                                onSetActive = { scope.launch { tasksDataStore.setActiveTask(task.id) } },
                                onPriorityFailed = {
                                    scope.launch { tasksDataStore.markPriorityTaskFailed(task.id) }
                                    context.startActivity(Intent(context, PriorityTaskFailedActivity::class.java).apply {
                                        putExtra("task_title", task.title)
                                        putExtra("task_id", task.id)
                                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    })
                                }
                            )
                        }
                    }
                    
                    // Groups
                    items(groups, key = { it.groupId }) { group ->
                        val isCollapsed = collapsedGroups.getOrPut(group.groupId) { true }
                        
                        Column {
                            // Group Header (Collapsible)
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1A1A1A))
                                    .clickable { collapsedGroups[group.groupId] = !isCollapsed }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(4.dp, 24.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(Color.White)
                                    )
                                    Column {
                                        Text(
                                            text = group.groupName,
                                            color = Color.White,
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = "${group.completedCount}/${group.totalCount} completed",
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    // Add to group
                                    IconButton(
                                        onClick = {
                                            addToGroupId = group.groupId
                                            addToGroupName = group.groupName
                                            showAddToGroupDialog = true
                                        },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add to group",
                                            tint = Color.White,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    // Complete group
                                    IconButton(
                                        onClick = { scope.launch { tasksDataStore.completeGroup(group.groupId) } },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = "Complete group",
                                            tint = Color(0xFF4CAF50),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    // Delete group
                                    IconButton(
                                        onClick = { scope.launch { tasksDataStore.deleteGroup(group.groupId) } },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete group",
                                            tint = Color(0xFFEF5350),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    
                                    // Expand/Collapse
                                    Icon(
                                        imageVector = if (isCollapsed) Icons.Default.ExpandMore else Icons.Default.ExpandLess,
                                        contentDescription = if (isCollapsed) "Expand" else "Collapse",
                                        tint = TextSecondary
                                    )
                                }
                            }
                            
                            // Group Tasks
                            AnimatedVisibility(
                                visible = !isCollapsed,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    group.tasks.forEach { task ->
                                        TaskCard(
                                            task = task,
                                            onComplete = { scope.launch { tasksDataStore.completeTask(task.id); NotificationHelper.showTaskCompletedNotification(context, task.title) } },
                                            onDelete = { scope.launch { tasksDataStore.deleteTask(task.id) } },
                                            onSetActive = { scope.launch { tasksDataStore.setActiveTask(task.id) } },
                                            onPriorityFailed = {}
                                        )
                                    }
                                }
                            }
                        }
                    }
                    
                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = { showTypeSelection = true },
            containerColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Task",
                tint = Color.Black
            )
        }
        
        // Dialogs
        if (showTypeSelection) {
            TaskTypeSelectionDialog(
                onDismiss = { showTypeSelection = false },
                onSingleTaskSelected = { showTypeSelection = false; showSingleTaskDialog = true },
                onGroupTaskSelected = { showTypeSelection = false; showGroupTaskDialog = true },
                onPriorityTaskSelected = { showTypeSelection = false; showPriorityTaskDialog = true }
            )
        }
        
        if (showSingleTaskDialog) {
            AddSingleTaskDialog(
                onDismiss = { showSingleTaskDialog = false },
                onConfirm = { title -> scope.launch { tasksDataStore.addSingleTask(title) }; showSingleTaskDialog = false }
            )
        }
        
        if (showGroupTaskDialog) {
            AddGroupTaskDialog(
                onDismiss = { showGroupTaskDialog = false },
                onConfirm = { groupName, tasks -> scope.launch { tasksDataStore.addTaskGroup(groupName, tasks) }; showGroupTaskDialog = false }
            )
        }
        
        if (showPriorityTaskDialog) {
            AddPriorityTaskDialog(
                onDismiss = { showPriorityTaskDialog = false },
                onConfirm = { title, minutes -> scope.launch { tasksDataStore.addPriorityTask(title, minutes) }; showPriorityTaskDialog = false }
            )
        }
        
        if (showAddToGroupDialog) {
            AddToGroupDialog(
                groupName = addToGroupName,
                onDismiss = { showAddToGroupDialog = false },
                onConfirm = { title, type ->
                    scope.launch { tasksDataStore.addToGroup(addToGroupId, title, type) }
                    showAddToGroupDialog = false
                }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntry,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onSetActive: () -> Unit,
    onPriorityFailed: () -> Unit
) {
    var remainingSeconds by remember(task.priorityCreatedAt, task.priorityDeadlineMinutes) { 
        mutableStateOf<Long?>(
            if (task.isPriority && task.priorityDeadlineMinutes != null && task.priorityCreatedAt != null && !task.isFailed && !task.isCompleted) {
                val elapsedMs = System.currentTimeMillis() - task.priorityCreatedAt
                val totalMs = task.priorityDeadlineMinutes * 60 * 1000L
                ((totalMs - elapsedMs) / 1000).coerceAtLeast(0)
            } else null
        )
    }
    
    LaunchedEffect(task.isPriority, task.isCompleted, task.isFailed) {
        if (task.isPriority && task.priorityDeadlineMinutes != null && task.priorityCreatedAt != null && !task.isCompleted && !task.isFailed) {
            while (true) {
                delay(1000)
                val elapsedMs = System.currentTimeMillis() - task.priorityCreatedAt
                val totalMs = task.priorityDeadlineMinutes * 60 * 1000L
                val remaining = ((totalMs - elapsedMs) / 1000).coerceAtLeast(0)
                remainingSeconds = remaining
                if (remaining <= 0) { onPriorityFailed(); break }
            }
        }
    }
    
    val countdownText = remainingSeconds?.let { String.format("%02d:%02d", it / 60, it % 60) }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = when {
                        task.isFailed -> listOf(Color(0xFF3A1A1A), Color(0xFF2A1010))
                        task.isPriority && task.isActive -> listOf(Color(0xFF3A2A2A), Color(0xFF2A1A1A))
                        task.isActive -> listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A))
                        task.isCompleted -> listOf(Color(0x10FFFFFF), Color(0x05FFFFFF))
                        else -> listOf(Color(0xFF1A1A1A), Color(0xFF121212))
                    }
                )
            )
            .border(
                1.dp,
                when {
                    task.isFailed -> Color(0xFFFF4444)
                    task.isPriority && task.isActive -> Color(0xFFFF6B6B)
                    task.isActive -> Color.White
                    task.isCompleted -> Color(0xFF333333)
                    else -> GlassBorder
                },
                RoundedCornerShape(16.dp)
            )
            .clickable(enabled = !task.isCompleted && !task.isActive && !task.isFailed) { onSetActive() }
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = when {
                        task.isFailed -> Icons.Filled.Warning
                        task.isCompleted -> Icons.Default.CheckCircle
                        task.isPriority -> Icons.Filled.PriorityHigh
                        task.type == TaskType.BREAK -> Icons.Filled.Coffee
                        else -> Icons.AutoMirrored.Outlined.Assignment
                    },
                    contentDescription = null,
                    tint = when {
                        task.isFailed -> Color(0xFFFF4444)
                        task.isPriority && task.isActive -> Color(0xFFFF6B6B)
                        task.isActive -> Color.White
                        task.isCompleted -> Color(0xFF4CAF50)
                        else -> TextSecondary
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = task.title,
                        color = when {
                            task.isFailed -> Color(0xFFFF4444)
                            task.isCompleted -> TextSecondary
                            else -> Color.White
                        },
                        fontSize = 16.sp,
                        fontWeight = if (task.isActive) FontWeight.Bold else FontWeight.Medium,
                        textDecoration = if (task.isCompleted || task.isFailed) TextDecoration.LineThrough else null
                    )
                    
                    Text(
                        text = when {
                            task.isFailed -> "Failed"
                            task.isPriority && countdownText != null -> "$countdownText remaining"
                            task.isActive -> if (task.type == TaskType.BREAK) "On Break" else "Active"
                            task.isCompleted -> "Completed"
                            else -> "Tap to activate"
                        },
                        color = when {
                            task.isFailed -> Color(0xFFFF4444)
                            task.isPriority && task.isActive -> Color(0xFFFF6B6B)
                            task.isActive -> Color.White
                            else -> TextSecondary
                        },
                        fontSize = 12.sp
                    )
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!task.isCompleted && !task.isFailed) {
                    IconButton(onClick = onComplete) {
                        Icon(Icons.Default.RadioButtonUnchecked, "Complete", tint = if (task.isActive) Color.White else TextSecondary)
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, "Delete", tint = TextSecondary.copy(0.5f))
                }
            }
        }
    }
}
