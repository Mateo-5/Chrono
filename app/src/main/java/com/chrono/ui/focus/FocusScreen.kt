package com.chrono.ui.focus

import android.app.AppOpsManager
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.Process
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.FocusSettingsDataStore
import com.chrono.focus.FocusService
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.CharcoalGray
import com.chrono.ui.theme.DarkGray
import com.chrono.ui.theme.MediumGray
import com.chrono.ui.theme.PureWhite
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val GlassBorder = Color(0x40FFFFFF)
private val FocusColor = PureWhite
private val BreakColor = MediumGray

@Composable
fun FocusScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    
    val focusSettingsDataStore = remember { FocusSettingsDataStore(context) }
    val focusSettings by focusSettingsDataStore.focusSettings.collectAsState(initial = com.chrono.data.FocusSettings())
    
    // Service State
    var focusService by remember { mutableStateOf<FocusService?>(null) }
    var isBound by remember { mutableStateOf(false) }
    
    val serviceConnection = remember {
        object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                val binder = service as FocusService.LocalBinder
                focusService = binder.getService()
                isBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                focusService = null
                isBound = false
            }
        }
    }
    
    // Bind to service
    DisposableEffect(Unit) {
        val intent = Intent(context, FocusService::class.java)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        onDispose {
            if (isBound) {
                context.unbindService(serviceConnection)
                isBound = false
            }
        }
    }
    
    // Observe Service State
    val timeRemaining by focusService?.timeRemaining?.collectAsState(initial = 25 * 60) ?: remember { mutableStateOf(25 * 60) }
    val isRunning by focusService?.isRunning?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    val isBreak by focusService?.isBreak?.collectAsState(initial = false) ?: remember { mutableStateOf(false) }
    val sessionsCompleted by focusService?.sessionsCompleted?.collectAsState(initial = 0) ?: remember { mutableStateOf(0) }
    
    // Modes: Normal vs Strict
    var isStrictMode by remember { mutableStateOf(false) }
    var showAppSelection by remember { mutableStateOf(false) }
    
    val totalTime = if (isBreak) 5 * 60 else 25 * 60
    val progress by animateFloatAsState(
        targetValue = timeRemaining.toFloat() / totalTime.toFloat(),
        animationSpec = tween(1000, easing = LinearEasing),
        label = "progress"
    )
    
    val currentColor = if (isBreak) BreakColor else FocusColor
    
    // Entrance animation
    var showContent by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { showContent = true }
    
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
                ),
            horizontalAlignment = Alignment.CenterHorizontally
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
                        text = "Focus",
                        color = TextPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Settings / Mode Toggle
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isStrictMode) "Strict" else "Normal",
                        color = if (isStrictMode) Color(0xFFFF6B6B) else TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Switch(
                        checked = isStrictMode,
                        onCheckedChange = { isStrictMode = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = Color(0xFFFF6B6B),
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = Color(0xFF2A2A2A)
                        ),
                        modifier = Modifier.scale(0.8f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(0.5f))
            
            // Mode pill indicator
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + scaleIn()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(currentColor.copy(alpha = 0.15f))
                            .border(1.dp, currentColor.copy(alpha = 0.3f), RoundedCornerShape(20.dp))
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isBreak) "BREAK TIME" else "FOCUS TIME",
                            color = currentColor,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    if (isStrictMode && !isBreak) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { showAppSelection = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = null,
                                tint = Color(0xFFFF6B6B),
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = " ${focusSettings.blockedPackageNames.size} Apps Blocked",
                                color = Color(0xFFFF6B6B),
                                fontSize = 12.sp
                            )
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Edit",
                                tint = TextSecondary,
                                modifier = Modifier.size(14.dp).padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer Circle with animated progress ring
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn() + scaleIn(spring(Spring.DampingRatioMediumBouncy))
            ) {
                Box(
                    modifier = Modifier.size(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Background ring
                    Canvas(modifier = Modifier.size(280.dp)) {
                        drawCircle(
                            color = Color.White.copy(alpha = 0.1f),
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    
                    // Progress ring
                    Canvas(modifier = Modifier.size(280.dp)) {
                        drawArc(
                            color = currentColor,
                            startAngle = -90f,
                            sweepAngle = 360f * progress,
                            useCenter = false,
                            style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                    
                    // Inner content
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        val minutes = timeRemaining / 60
                        val seconds = timeRemaining % 60
                        
                        Text(
                            text = String.format("%02d:%02d", minutes, seconds),
                            color = Color.White,
                            fontSize = 56.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isBreak) "Relax & Recharge" else "Stay Focused",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Controls
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn()
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Reset button
                    ControlButton(
                        icon = Icons.Default.Refresh,
                        contentDescription = "Reset",
                        onClick = {
                            focusService?.resetTimer()
                        },
                        backgroundColor = DarkGray,
                        size = 56
                    )
                    
                    // Play/Pause button
                    ControlButton(
                        icon = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isRunning) "Pause" else "Play",
                        onClick = {
                            if (!isRunning && isStrictMode && !isBreak) {
                                if (!checkPermissions(context)) {
                                    requestPermissions(context)
                                    return@ControlButton
                                }
                            }
                            
                            if (isRunning) {
                                focusService?.toggleTimer()
                            } else {
                                // Start Service
                                val intent = Intent(context, FocusService::class.java).apply {
                                    putExtra(FocusService.EXTRA_STRICT_MODE, isStrictMode)
                                }
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    context.startForegroundService(intent)
                                } else {
                                    context.startService(intent)
                                }
                            }
                            vibrate(context, light = true)
                        },
                        backgroundColor = currentColor,
                        size = 80,
                        isPrimary = true
                    )
                    
                    // Skip button
                    ControlButton(
                        icon = Icons.Default.SkipNext,
                        contentDescription = "Skip",
                        onClick = {
                            focusService?.skipSession()
                        },
                        backgroundColor = DarkGray,
                        size = 56
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Sessions counter
            AnimatedVisibility(
                visible = showContent,
                enter = fadeIn()
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.White.copy(alpha = 0.05f))
                        .border(1.dp, GlassBorder, RoundedCornerShape(20.dp))
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Sessions Completed",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                        Text(
                            text = "$sessionsCompleted",
                            color = PureWhite,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
        }
        
        if (showAppSelection) {
            AppSelectionDialog(
                initialSelection = focusSettings.blockedPackageNames,
                onDismiss = { showAppSelection = false },
                onConfirm = { selected ->
                    scope.launch {
                        focusSettingsDataStore.updateBlockedPackages(selected)
                    }
                    showAppSelection = false
                }
            )
        }
    }
}

@Composable
private fun ControlButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    size: Int,
    isPrimary: Boolean = false
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy),
        label = "scale"
    )
    
    Box(
        modifier = Modifier
            .size(size.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(backgroundColor)
            .border(1.dp, if (isPressed) MediumGray else DarkGray, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (isPrimary) CharcoalGray else PureWhite,
            modifier = Modifier.size((size * 0.45f).dp)
        )
    }
}

private fun vibrate(context: Context, light: Boolean = false) {
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val effect = if (light) {
            VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
        } else {
            VibrationEffect.createWaveform(longArrayOf(0, 200, 100, 200), -1)
        }
        vibrator.vibrate(effect)
    } else {
        @Suppress("DEPRECATION")
        if (light) vibrator.vibrate(50) else vibrator.vibrate(longArrayOf(0, 200, 100, 200), -1)
    }
}

private fun checkPermissions(context: Context): Boolean {
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    } else {
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    val hasUsageStats = mode == AppOpsManager.MODE_ALLOWED
    val hasOverlay = Settings.canDrawOverlays(context)
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    val hasDnd = notificationManager.isNotificationPolicyAccessGranted
    
    return hasUsageStats && hasOverlay && hasDnd
}

private fun requestPermissions(context: Context) {
    if (!Settings.canDrawOverlays(context)) {
        context.startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
    }
    val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
    val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        appOps.unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    } else {
        appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), context.packageName)
    }
    if (mode != AppOpsManager.MODE_ALLOWED) {
        context.startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
    }
    
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (!notificationManager.isNotificationPolicyAccessGranted) {
        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS))
    }
}

private fun setDnd(context: Context, enable: Boolean) {
    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    if (notificationManager.isNotificationPolicyAccessGranted) {
        if (enable) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        } else {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}
