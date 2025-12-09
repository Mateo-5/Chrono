package com.chrono.ui.reminders

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.ReminderEntry
import com.chrono.data.ReminderType
import com.chrono.data.RemindersDataStore
import com.chrono.data.SingleReminderMode
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val GlassBorder = Color(0x60FFFFFF)

@Composable
fun RemindersScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val remindersDataStore = remember { RemindersDataStore(context) }
    val remindersData by remindersDataStore.remindersData.collectAsState(initial = com.chrono.data.RemindersData())
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var completedExpanded by remember { mutableStateOf(false) }  // Collapsed by default
    
    // Note: Reminders are only marked as completed when user clicks Dismiss/Complete
    
    // Separate active and completed reminders
    // Active includes: all non-completed reminders (including expired single ones that haven't been dismissed)
    val activeReminders = remindersData.reminders.filter { !it.isCompleted }
    val completedReminders = remindersData.reminders.filter { it.isCompleted }
    
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
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
                Text(
                    text = "Reminders",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Reminders List or Empty State
            if (remindersData.reminders.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Alarm,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No reminders yet",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first reminder",
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
                    // Active Reminders
                    if (activeReminders.isNotEmpty()) {
                        items(activeReminders, key = { it.id }) { reminder ->
                            ReminderCard(
                                reminder = reminder,
                                onToggle = {
                                    scope.launch {
                                        remindersDataStore.toggleReminder(reminder.id)
                                    }
                                },
                                onDelete = {
                                    scope.launch {
                                        remindersDataStore.deleteReminder(reminder.id)
                                    }
                                },
                                onMarkCompleted = {
                                    scope.launch {
                                        remindersDataStore.markReminderCompleted(reminder.id)
                                    }
                                }
                            )
                        }
                    }
                    
                    // Completed Section - Collapsible Dropdown
                    if (completedReminders.isNotEmpty()) {
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(0xFF1A1A1A))
                                    .clickable { completedExpanded = !completedExpanded }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4CAF50),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text(
                                        text = "Completed",
                                        color = Color.White,
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(Color(0xFF2A2A2A))
                                            .padding(horizontal = 8.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = completedReminders.size.toString(),
                                            color = TextSecondary,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                Icon(
                                    imageVector = if (completedExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = if (completedExpanded) "Collapse" else "Expand",
                                    tint = TextSecondary
                                )
                            }
                        }
                        
                        item {
                            AnimatedVisibility(
                                visible = completedExpanded,
                                enter = expandVertically(),
                                exit = shrinkVertically()
                            ) {
                                Column(
                                    modifier = Modifier.padding(top = 8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    completedReminders.forEach { reminder ->
                                        ReminderCard(
                                            reminder = reminder,
                                            onToggle = { },
                                            onDelete = {
                                                scope.launch {
                                                    remindersDataStore.deleteReminder(reminder.id)
                                                }
                                            },
                                            onMarkCompleted = { }
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
            onClick = { showAddDialog = true },
            containerColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Reminder",
                tint = Color.Black
            )
        }
        
        // Add Dialog
        if (showAddDialog) {
            EditReminderDialog(
                onDismiss = { showAddDialog = false },
                onConfirmSingle = { title, mode, dateTime ->
                    scope.launch {
                        val reminderId = remindersDataStore.addSingleReminder(title, mode, dateTime)
                        scheduleAlarm(context, title, dateTime, reminderId)
                    }
                    showAddDialog = false
                },
                onConfirmRepeated = { title, hour, minute ->
                    scope.launch {
                        remindersDataStore.addRepeatedReminder(title, hour, minute)
                        scheduleRepeatingAlarm(context, title, hour, minute)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

private fun scheduleAlarm(context: android.content.Context, title: String, timeInMillis: Long, reminderId: String) {
    try {
        val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(context, com.chrono.notification.AlarmReceiver::class.java).apply {
            putExtra("title", title)
            putExtra("request_code", timeInMillis.toInt())
            putExtra("reminder_id", reminderId)
        }
        
        val requestCode = timeInMillis.toInt()
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        // Check for exact alarm permission on Android 12+
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    android.app.AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            } else {
                // Fallback to inexact alarm
                alarmManager.set(
                    android.app.AlarmManager.RTC_WAKEUP,
                    timeInMillis,
                    pendingIntent
                )
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

private fun scheduleRepeatingAlarm(context: android.content.Context, title: String, hour: Int, minute: Int) {
    try {
        val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
        val intent = android.content.Intent(context, com.chrono.notification.AlarmReceiver::class.java).apply {
            putExtra("title", title)
        }
        
        val calendar = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, hour)
            set(java.util.Calendar.MINUTE, minute)
            set(java.util.Calendar.SECOND, 0)
            
            if (before(java.util.Calendar.getInstance())) {
                add(java.util.Calendar.DAY_OF_MONTH, 1)
            }
        }
        
        val requestCode = (hour * 60 + minute)
        
        val pendingIntent = android.app.PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setRepeating(
            android.app.AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            android.app.AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

@Composable
private fun ReminderCard(
    reminder: ReminderEntry,
    onToggle: () -> Unit,
    onDelete: () -> Unit,
    onMarkCompleted: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    
    val displayText = when (reminder.type) {
        ReminderType.SINGLE -> {
            if (reminder.singleMode == SingleReminderMode.START_OF_DAY) {
                val dayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                "${dayFormat.format(Date(reminder.dateTime))} • 6:00 AM"
            } else {
                dateFormat.format(Date(reminder.dateTime))
            }
        }
        ReminderType.REPEATED -> {
            val hour = reminder.repeatTimeHour ?: 0
            val minute = reminder.repeatTimeMinute ?: 0
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, hour)
                set(java.util.Calendar.MINUTE, minute)
            }
            "Daily at ${timeFormat.format(cal.time)}"
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = if (reminder.isCompleted) {
                        listOf(Color(0x10FFFFFF), Color(0x05FFFFFF))
                    } else {
                        listOf(Color(0x40FFFFFF), Color(0x20FFFFFF))
                    }
                )
            )
            .border(
                1.dp,
                if (reminder.isCompleted) Color(0xFF333333) else GlassBorder,
                RoundedCornerShape(16.dp)
            )
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
                        reminder.isCompleted -> Icons.Default.CheckCircle
                        reminder.type == ReminderType.REPEATED -> Icons.Default.Repeat
                        reminder.isActive -> Icons.Default.Notifications
                        else -> Icons.Default.NotificationsOff
                    },
                    contentDescription = null,
                    tint = when {
                        reminder.isCompleted -> Color(0xFF4CAF50)
                        reminder.type == ReminderType.REPEATED -> Color(0xFF42A5F5)
                        reminder.isActive -> Color.White
                        else -> TextSecondary
                    },
                    modifier = Modifier.size(24.dp)
                )
                
                Column {
                    Text(
                        text = reminder.title,
                        color = if (reminder.isCompleted) TextSecondary else Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else null
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = displayText,
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        if (reminder.type == ReminderType.REPEATED && !reminder.isCompleted) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF42A5F5).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Daily",
                                    color = Color(0xFF42A5F5),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                if (!reminder.isCompleted) {
                    Switch(
                        checked = reminder.isActive,
                        onCheckedChange = { onToggle() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFF4CAF50),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF333333)
                        )
                    )
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = TextSecondary.copy(alpha = 0.5f)
                    )
                }
            }
        }
    }
}
