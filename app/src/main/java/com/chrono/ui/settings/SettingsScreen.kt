package com.chrono.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.chrono.data.SettingsDataStore
import com.chrono.notification.NotificationHelper
import com.chrono.notification.WaterBreakWorker
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val GlassBorder = Color(0x60FFFFFF)

data class IntervalOption(val minutes: Int, val label: String)

private val intervalOptions = listOf(
    IntervalOption(30, "30 min"),
    IntervalOption(60, "1 hour"),
    IntervalOption(120, "2 hours"),
    IntervalOption(180, "3 hours")
)

@Composable
fun SettingsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val settingsDataStore = remember { SettingsDataStore(context) }
    
    val waterBreakEnabled by settingsDataStore.waterBreakEnabled.collectAsState(initial = false)
    val waterBreakInterval by settingsDataStore.waterBreakInterval.collectAsState(initial = 60)
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    
    // Permission launcher for notifications
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            scope.launch {
                settingsDataStore.setWaterBreakEnabled(true)
                WaterBreakWorker.schedule(context, waterBreakInterval)
            }
        }
    }
    
    // Create notification channel on first load
    LaunchedEffect(Unit) {
        NotificationHelper.createNotificationChannel(context)
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(
                    top = statusBarPadding.calculateTopPadding(),
                    bottom = navBarPadding.calculateBottomPadding(),
                    start = 16.dp,
                    end = 16.dp
                )
        ) {
            // Top bar
            Row(
                modifier = Modifier.fillMaxWidth(),
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
                    text = "Settings",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Visibility Assist Section (at top)
            Text(
                text = "VISIBILITY ASSIST",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            val textScale by settingsDataStore.textScale.collectAsState(initial = 1.0f)
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF)
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = "Text & Icon Size",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = "Adjust the size of text and icons throughout the app",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            0.85f to "Small",
                            1.0f to "Normal",
                            1.15f to "Large",
                            1.3f to "XL"
                        ).forEach { (scale, label) ->
                            val isSelected = kotlin.math.abs(textScale - scale) < 0.01f
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        if (isSelected) Color.White
                                        else Color(0xFF1A1A1A)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) Color.White else GlassBorder,
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable {
                                        scope.launch {
                                            settingsDataStore.setTextScale(scale)
                                        }
                                    }
                                    .padding(vertical = 12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    color = if (isSelected) Color.Black else TextSecondary,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                )
                            }
                        }
                    }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1A1A))
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Preview",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Sample text at ${(textScale * 100).toInt()}% size",
                                color = TextPrimary,
                                fontSize = (14 * textScale).sp
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Water Break Section
            Text(
                text = "REMINDERS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            // Water Break Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF)
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // Toggle row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF1A1A1A)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.WaterDrop,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = "Water Break",
                                    color = TextPrimary,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = "Remind me to drink water",
                                    color = TextSecondary,
                                    fontSize = 13.sp
                                )
                            }
                        }
                        
                        Switch(
                            checked = waterBreakEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
                                    // Check notification permission
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        if (ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.POST_NOTIFICATIONS
                                            ) == PackageManager.PERMISSION_GRANTED
                                        ) {
                                            scope.launch {
                                                settingsDataStore.setWaterBreakEnabled(true)
                                                WaterBreakWorker.schedule(context, waterBreakInterval)
                                            }
                                        } else {
                                            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                        }
                                    } else {
                                        scope.launch {
                                            settingsDataStore.setWaterBreakEnabled(true)
                                            WaterBreakWorker.schedule(context, waterBreakInterval)
                                        }
                                    }
                                } else {
                                    scope.launch {
                                        settingsDataStore.setWaterBreakEnabled(false)
                                        WaterBreakWorker.cancel(context)
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0xFF2A2A2A)
                            )
                        )
                    }
                    
                    // Interval selector (only show when enabled)
                    if (waterBreakEnabled) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(
                                text = "Reminder Interval",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                intervalOptions.forEach { option ->
                                    val isSelected = waterBreakInterval == option.minutes
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(
                                                if (isSelected) Color.White
                                                else Color(0xFF1A1A1A)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) Color.White else GlassBorder,
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                            .clickable {
                                                scope.launch {
                                                    settingsDataStore.setWaterBreakInterval(option.minutes)
                                                    if (waterBreakEnabled) {
                                                        WaterBreakWorker.schedule(context, option.minutes)
                                                    }
                                                }
                                            }
                                            .padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = option.label,
                                            color = if (isSelected) Color.Black else TextSecondary,
                                            fontSize = 13.sp,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Focus Timer Section
            Text(
                text = "FOCUS TIMER",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            val focusDuration by settingsDataStore.focusDuration.collectAsState(initial = 25)
            val breakDuration by settingsDataStore.breakDuration.collectAsState(initial = 5)
            val soundEffects by settingsDataStore.soundEffectsEnabled.collectAsState(initial = true)
            
            var showFocusDialog by remember { mutableStateOf(false) }
            var showBreakDialog by remember { mutableStateOf(false) }
            
            if (showFocusDialog) {
                DurationPickerDialog(
                    title = "Focus Duration",
                    currentValue = focusDuration,
                    options = listOf(15, 20, 25, 30, 45, 60),
                    onDismiss = { showFocusDialog = false },
                    onConfirm = { 
                        scope.launch { settingsDataStore.setFocusDuration(it) }
                        showFocusDialog = false
                    }
                )
            }
            
            if (showBreakDialog) {
                DurationPickerDialog(
                    title = "Break Duration",
                    currentValue = breakDuration,
                    options = listOf(5, 10, 15, 20, 30),
                    onDismiss = { showBreakDialog = false },
                    onConfirm = { 
                        scope.launch { settingsDataStore.setBreakDuration(it) }
                        showBreakDialog = false
                    }
                )
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF)
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showFocusDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Focus Duration",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$focusDuration min",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showBreakDialog = true },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Break Duration",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "$breakDuration min",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Sound Effects",
                            color = TextPrimary,
                            fontSize = 14.sp
                        )
                        Switch(
                            checked = soundEffects,
                            onCheckedChange = { 
                                scope.launch { settingsDataStore.setSoundEffectsEnabled(it) }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.Black,
                                checkedTrackColor = Color.White,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = Color(0xFF2A3A4A)
                            )
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Backup & Restore Section
            Text(
                text = "BACKUP & RESTORE",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            val backupManager = remember { com.chrono.data.BackupManager(context) }
            var backupStatus by remember { mutableStateOf<String?>(null) }
            var isExporting by remember { mutableStateOf(false) }
            var isImporting by remember { mutableStateOf(false) }
            
            // File picker launcher for import
            val filePickerLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.OpenDocument()
            ) { uri ->
                if (uri != null) {
                    isImporting = true
                    backupStatus = null
                    scope.launch {
                        try {
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val json = inputStream?.bufferedReader()?.use { it.readText() }
                            inputStream?.close()
                            
                            if (json != null) {
                                val result = backupManager.importFromJson(json)
                                isImporting = false
                                backupStatus = result.fold(
                                    onSuccess = { it },
                                    onFailure = { "Import failed: ${it.message}" }
                                )
                            } else {
                                isImporting = false
                                backupStatus = "Import failed: Could not read file"
                            }
                        } catch (e: Exception) {
                            isImporting = false
                            backupStatus = "Import failed: ${e.message}"
                        }
                    }
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF)
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    
                    Text(
                        text = "Your data is saved locally. Export to back up or restore from a previous backup.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    
                    // Export Button
                    Button(
                        onClick = {
                            isExporting = true
                            backupStatus = null
                            scope.launch {
                                val result = backupManager.exportData()
                                isExporting = false
                                backupStatus = result.fold(
                                    onSuccess = { "Exported to Downloads/Chrono" },
                                    onFailure = { "Export failed: ${it.message}" }
                                )
                            }
                        },
                        enabled = !isExporting && !isImporting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.White,
                            contentColor = Color.Black
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.Black,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isExporting) "Exporting..." else "Export Data",
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Import Button
                    Button(
                        onClick = {
                            // Open file picker to select backup JSON file
                            filePickerLauncher.launch(arrayOf("application/json", "*/*"))
                        },
                        enabled = !isExporting && !isImporting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2A2A2A),
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFF1A1A1A),
                            disabledContentColor = TextSecondary.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isImporting) "Importing..." else "Import Data",
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    // Info text
                    Text(
                        text = "Tap Import and select your backup file from Downloads/Chrono",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                    
                    // Status message
                    if (backupStatus != null) {
                        Text(
                            text = backupStatus!!,
                            color = if (backupStatus!!.contains("failed")) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Permissions Section
            Text(
                text = "PERMISSIONS",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF)
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                val intent = android.content.Intent(
                                    android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                    android.net.Uri.fromParts("package", context.packageName, null)
                                )
                                context.startActivity(intent)
                            },
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Manage Permissions",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Notifications, Alarms, etc.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                        Text(
                            text = ">",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App Info Section
            Text(
                text = "APP INFO",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp, bottom = 12.dp)
            )
            
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF)
                            )
                        )
                    )
                    .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                    .padding(20.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Version",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "1.1.0",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "App Name",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Chrono",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Developer",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Chrono Team",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
