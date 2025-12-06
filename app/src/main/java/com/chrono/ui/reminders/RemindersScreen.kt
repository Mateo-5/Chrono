package com.chrono.ui.reminders

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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.chrono.data.RemindersDataStore
import com.chrono.ui.theme.AccentBlue
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
                    items(remindersData.reminders, key = { it.id }) { reminder ->
                        ReminderCard(
                            title = reminder.title,
                            dateTime = reminder.dateTime,
                            isActive = reminder.isActive,
                            onToggle = {
                                scope.launch {
                                    remindersDataStore.toggleReminder(reminder.id)
                                }
                            },
                            onDelete = {
                                scope.launch {
                                    remindersDataStore.deleteReminder(reminder.id)
                                }
                            }
                        )
                    }
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
                onConfirm = { title, dateTime ->
                    scope.launch {
                        remindersDataStore.addReminder(title, dateTime)
                        scheduleAlarm(context, title, dateTime)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

private fun scheduleAlarm(context: android.content.Context, title: String, timeInMillis: Long) {
    val alarmManager = context.getSystemService(android.content.Context.ALARM_SERVICE) as android.app.AlarmManager
    val intent = android.content.Intent(context, com.chrono.notification.AlarmReceiver::class.java).apply {
        putExtra("title", title)
        putExtra("content", "Time for your reminder!")
    }
    
    val pendingIntent = android.app.PendingIntent.getBroadcast(
        context,
        timeInMillis.toInt(), // Unique ID based on time
        intent,
        android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
    )
    
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(
                android.app.AlarmManager.RTC_WAKEUP,
                timeInMillis,
                pendingIntent
            )
        } else {
            // Fallback or request permission (handled in MainActivity)
            alarmManager.setAndAllowWhileIdle(
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
}

@Composable
private fun ReminderCard(
    title: String,
    dateTime: Long,
    isActive: Boolean,
    onToggle: () -> Unit,
    onDelete: () -> Unit
) {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault())
    val dateTimeStr = dateFormat.format(Date(dateTime))
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x40FFFFFF),
                        Color(0x20FFFFFF)
                    )
                )
            )
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon + Content
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1A1A1A)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isActive) Icons.Default.Notifications
                        else Icons.Default.NotificationsOff,
                        contentDescription = null,
                        tint = if (isActive) Color.White else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateTimeStr,
                        color = if (isActive) Color.White else TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
            
            // Toggle switch
            Switch(
                checked = isActive,
                onCheckedChange = { onToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.Black,
                    checkedTrackColor = Color.White,
                    uncheckedThumbColor = TextSecondary,
                    uncheckedTrackColor = Color(0xFF2A3A4A)
                )
            )
            
            // Delete button
            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFEF5350),
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}
