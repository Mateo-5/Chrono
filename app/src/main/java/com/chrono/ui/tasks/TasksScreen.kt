package com.chrono.ui.tasks

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.outlined.Assignment
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.TaskEntry
import com.chrono.data.TaskType
import com.chrono.data.TasksDataStore
import com.chrono.notification.NotificationHelper
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.launch

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
    
    // Effect to update notification when active task changes
    LaunchedEffect(tasksData.tasks) {
        val activeTask = tasksData.tasks.find { it.isActive }
        if (activeTask != null) {
            NotificationHelper.showActiveTaskNotification(context, activeTask)
        } else {
            NotificationHelper.cancelTaskNotification(context)
        }
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
                
                // Complete All button
                if (tasksData.tasks.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF2A2A2A))
                            .clickable {
                                scope.launch {
                                    tasksDataStore.completeAll()
                                }
                            }
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
            
            // Tasks List or Empty State
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
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(tasksData.tasks, key = { it.id }) { task ->
                        TaskCard(
                            task = task,
                            onComplete = {
                                scope.launch {
                                    tasksDataStore.completeTask(task.id)
                                    // Notification for completion is handled by UI/Receiver logic or we can trigger here
                                    NotificationHelper.showTaskCompletedNotification(context, task.title)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    tasksDataStore.deleteTask(task.id)
                                }
                            }
                        )
                    }
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
                onSingleTaskSelected = {
                    showTypeSelection = false
                    showSingleTaskDialog = true
                },
                onGroupTaskSelected = {
                    showTypeSelection = false
                    showGroupTaskDialog = true
                }
            )
        }
        
        if (showSingleTaskDialog) {
            AddSingleTaskDialog(
                onDismiss = { showSingleTaskDialog = false },
                onConfirm = { title ->
                    scope.launch {
                        tasksDataStore.addSingleTask(title)
                    }
                    showSingleTaskDialog = false
                }
            )
        }
        
        if (showGroupTaskDialog) {
            AddGroupTaskDialog(
                onDismiss = { showGroupTaskDialog = false },
                onConfirm = { tasks ->
                    scope.launch {
                        tasksDataStore.addTaskGroup(tasks)
                    }
                    showGroupTaskDialog = false
                }
            )
        }
    }
}

@Composable
private fun TaskCard(
    task: TaskEntry,
    onComplete: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (task.isActive) {
                        listOf(Color(0xFF2A2A2A), Color(0xFF1A1A1A)) // Highlight active
                    } else if (task.isCompleted) {
                        listOf(Color(0x10FFFFFF), Color(0x05FFFFFF)) // Dimmed completed
                    } else {
                        listOf(Color(0xFF1A1A1A), Color(0xFF121212)) // Normal pending
                    }
                )
            )
            .border(
                1.dp,
                if (task.isActive) Color.White else if (task.isCompleted) Color(0xFF333333) else GlassBorder,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side - Icon + Title + Status
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Status Icon
                Icon(
                    imageVector = if (task.isCompleted) Icons.Default.CheckCircle 
                                 else if (task.type == TaskType.BREAK) Icons.Filled.Coffee
                                 else Icons.AutoMirrored.Outlined.Assignment,
                    contentDescription = null,
                    tint = if (task.isActive) Color.White 
                           else if (task.isCompleted) Color(0xFF4CAF50) 
                           else TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = task.title,
                        color = if (task.isCompleted) TextSecondary else Color.White,
                        fontSize = 16.sp,
                        fontWeight = if (task.isActive) FontWeight.Bold else FontWeight.Medium
                    )
                    
                    // Status Text
                    val statusText = when {
                        task.isActive -> if (task.type == TaskType.BREAK) "☕ On Break" else "⚡ Active Now"
                        task.isCompleted -> "Completed"
                        else -> "Pending"
                    }
                    
                    Text(
                        text = statusText,
                        color = if (task.isActive) Color.White else TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            // Right side - Actions
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (!task.isCompleted) {
                    IconButton(onClick = onComplete) {
                        Icon(
                            imageVector = Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Mark Complete",
                            tint = if (task.isActive) Color.White else TextSecondary
                        )
                    }
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = if (task.isActive) Color.White.copy(alpha = 0.5f) else TextSecondary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
