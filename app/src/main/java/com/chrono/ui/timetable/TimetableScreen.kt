package com.chrono.ui.timetable

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.TimetableDataStore

import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextPrimary
import kotlinx.coroutines.launch

private val GlassBorder = Color(0x60FFFFFF)
private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri")

@Composable
fun TimetableScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val timetableDataStore = remember { TimetableDataStore(context) }
    val timetableData by timetableDataStore.timetableData.collectAsState(
        initial = com.chrono.data.TimetableData()
    )
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    
    var showEditDialog by remember { mutableStateOf(false) }
    var editDay by remember { mutableStateOf("") }
    var editPeriodIndex by remember { mutableStateOf(0) }
    var editCurrentSubject by remember { mutableStateOf("") }
    
    var showTimeDialog by remember { mutableStateOf(false) }
    var editTimeIndex by remember { mutableStateOf(0) }
    var editCurrentTime by remember { mutableStateOf("") }
    
    // Lock orientation to landscape
    androidx.compose.runtime.DisposableEffect(Unit) {
        val activity = context as? android.app.Activity
        val originalOrientation = activity?.requestedOrientation
        activity?.requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        
        onDispose {
            activity?.requestedOrientation = originalOrientation ?: android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        }
    }
    
    // Calculate responsive dimensions
    val headerHeight = 56.dp
    val availableHeight = screenHeight - statusBarPadding.calculateTopPadding() - 
                         navBarPadding.calculateBottomPadding() - headerHeight - 32.dp
    val timeHeaderHeight = 50.dp
    val rowHeight = (availableHeight - timeHeaderHeight) / 5  // 5 days
    
    val dayColumnWidth = 70.dp
    val availableWidth = screenWidth - dayColumnWidth - 32.dp - 48.dp // padding + back button
    val periodWidth = availableWidth / 6  // 6 periods
    
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
            // Compact header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
                    .padding(horizontal = 8.dp),
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
                    text = "Timetable",
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Timetable Grid - direct on background
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Empty corner
                    Box(modifier = Modifier.width(dayColumnWidth).height(timeHeaderHeight))
                    
                    // Time period headers
                    Row(
                        modifier = Modifier.weight(1f),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        timetableData.timePeriods.forEachIndexed { index, time ->
                            TimeHeaderCell(
                                time = time,
                                width = periodWidth,
                                height = timeHeaderHeight,
                                onClick = {
                                    editTimeIndex = index
                                    editCurrentTime = time
                                    showTimeDialog = true
                                }
                            )
                        }
                    }
                }
                
                // Day rows
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.SpaceEvenly
                ) {
                    days.forEach { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Day header
                            DayHeaderCell(
                                day = day,
                                width = dayColumnWidth,
                                height = rowHeight
                            )
                            
                            // Subject cells
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                timetableData.schedule[day]?.forEachIndexed { periodIndex, subject ->
                                    SubjectCell(
                                        subject = subject,
                                        width = periodWidth,
                                        height = rowHeight,
                                        onClick = {
                                            editDay = day
                                            editPeriodIndex = periodIndex
                                            editCurrentSubject = subject
                                            showEditDialog = true
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Dialogs
        if (showEditDialog) {
            EditPeriodDialog(
                day = editDay,
                period = timetableData.timePeriods.getOrNull(editPeriodIndex) ?: "",
                currentSubject = editCurrentSubject,
                onDismiss = { showEditDialog = false },
                onConfirm = { newSubject ->
                    scope.launch {
                        timetableDataStore.updateSchedule(editDay, editPeriodIndex, newSubject)
                    }
                    showEditDialog = false
                }
            )
        }
        
        if (showTimeDialog) {
            EditTimeDialog(
                periodIndex = editTimeIndex,
                currentTime = editCurrentTime,
                onDismiss = { showTimeDialog = false },
                onConfirm = { newTime ->
                    scope.launch {
                        timetableDataStore.updateTimePeriod(editTimeIndex, newTime)
                    }
                    showTimeDialog = false
                }
            )
        }
    }
}

@Composable
private fun TimeHeaderCell(
    time: String,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = time,
            color = Color.White,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            lineHeight = 13.sp
        )
    }
}

@Composable
private fun DayHeaderCell(
    day: String,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            color = Color.White,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SubjectCell(
    subject: String,
    width: androidx.compose.ui.unit.Dp,
    height: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(width)
            .height(height)
            .clickable(onClick = onClick)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = subject.ifEmpty { "+" },
            color = if (subject.isNotEmpty()) Color.White else Color.White.copy(alpha = 0.4f),
            fontSize = 12.sp,
            fontWeight = if (subject.isNotEmpty()) FontWeight.Medium else FontWeight.Light,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}
