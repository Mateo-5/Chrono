package com.chrono.ui.calendar

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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FloatingActionButton
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
import com.chrono.data.EventsData
import com.chrono.data.EventsDataStore
import com.chrono.ui.components.ScheduleCard
import com.chrono.ui.components.ScheduleItem
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.GlowBlue
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

// Glassmorphism colors
private val GlassBorder = Color(0x60FFFFFF)

@Composable
fun CalendarScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val eventsDataStore = remember { EventsDataStore(context) }
    val eventsData by eventsDataStore.eventsData.collectAsState(initial = EventsData())
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Responsive calendar padding
    val calendarPadding = when {
        screenHeight < 700.dp -> 12.dp
        else -> 20.dp
    }
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Format selected date as string for comparison (dd-MMM-yyyy)
    val selectedDateString = "${selectedDate.dayOfMonth}-${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}-${selectedDate.year}"
    
    // Get events for selected date
    val eventsForDate = eventsData.events.filter { it.date == selectedDateString }
    
    // Get all dates with events for indicators
    val datesWithEvents = eventsData.events.mapNotNull { event ->
        try {
            // Parse date string back to LocalDate for comparison
            val parts = event.date.split("-")
            if (parts.size == 3) {
                val day = parts[0].toInt()
                val monthStr = parts[1]
                val year = parts[2].toInt()
                val month = java.time.Month.values().find { 
                    it.getDisplayName(TextStyle.SHORT, Locale.getDefault()).equals(monthStr, ignoreCase = true) 
                }
                if (month != null) LocalDate.of(year, month, day) else null
            } else null
        } catch (e: Exception) { null }
    }.toSet()
    
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
                    text = "Calendar",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Glassmorphic Calendar Card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0x30FFFFFF),
                                Color(0x10FFFFFF)
                            )
                        )
                    )
                    .border(
                        width = 1.dp,
                        color = GlassBorder,
                        shape = RoundedCornerShape(24.dp)
                    )
                    .padding(calendarPadding)
            ) {
                Column {
                    // Month navigation
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                            Icon(
                                imageVector = Icons.Filled.ChevronLeft,
                                contentDescription = "Previous month",
                                tint = TextPrimary
                            )
                        }
                        
                        Text(
                            text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${currentMonth.year}",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                            Icon(
                                imageVector = Icons.Filled.ChevronRight,
                                contentDescription = "Next month",
                                tint = TextPrimary
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Day of week headers
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        listOf("S", "M", "T", "W", "T", "F", "S").forEach { day ->
                            Text(
                                text = day,
                                color = TextSecondary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Calendar grid
                    CalendarGrid(
                        yearMonth = currentMonth,
                        selectedDate = selectedDate,
                        eventsOnDates = datesWithEvents,
                        onDateSelected = { selectedDate = it }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Selected date events header
            Text(
                text = "Events for ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())}",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Events list or empty state
            if (eventsForDate.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = navBarPadding.calculateBottomPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No events for this date.\nTap + to add one.",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .padding(bottom = navBarPadding.calculateBottomPadding()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(eventsForDate) { event ->
                        ScheduleCard(
                            item = ScheduleItem(
                                time = event.time,
                                title = event.title,
                                subtitle = event.subtitle,
                                accentColor = Color.White
                            )
                        )
                    }
                }
            }
        }
        
        // FAB to add event - respects nav bar
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(
                    end = 24.dp,
                    bottom = 24.dp + navBarPadding.calculateBottomPadding()
                ),
            containerColor = Color.White
        ) {
            Icon(
                imageVector = Icons.Filled.Add,
                contentDescription = "Add Event",
                tint = Color.Black
            )
        }
        
        // Add Event Dialog
        if (showAddDialog) {
            AddEventDialog(
                selectedDate = selectedDate,
                onDismiss = { showAddDialog = false },
                onConfirm = { title, time, subtitle ->
                    scope.launch {
                        eventsDataStore.addEvent(title, selectedDateString, time, subtitle)
                    }
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
private fun AddEventDialog(
    selectedDate: LocalDate,
    onDismiss: () -> Unit,
    onConfirm: (title: String, time: String, subtitle: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var subtitle by remember { mutableStateOf("") }
    
    // Custom dark glassmorphic dialog
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF3A3A3A), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {} // Prevent click-through
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                text = "Add Event",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Date display
            Text(
                text = "${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${selectedDate.year}",
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Event Title Field
            StyledTextField(
                value = title,
                onValueChange = { title = it },
                label = "Event Title",
                placeholder = "Enter event name"
            )
            
            // Time Field
            StyledTextField(
                value = time,
                onValueChange = { time = it },
                label = "Time",
                placeholder = "e.g., 9:00 AM"
            )
            
            // Description Field
            StyledTextField(
                value = subtitle,
                onValueChange = { subtitle = it },
                label = "Description",
                placeholder = "Optional details"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Cancel Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF2A2A2A))
                        .clickable(onClick = onDismiss)
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Cancel",
                        color = TextSecondary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                // Add Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (title.isNotBlank() && time.isNotBlank()) Color.White
                            else Color.White.copy(alpha = 0.4f)
                        )
                        .clickable(enabled = title.isNotBlank() && time.isNotBlank()) {
                            onConfirm(title, time, subtitle)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Add Event",
                        color = if (title.isNotBlank() && time.isNotBlank()) Color.Black
                               else Color.Black.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun StyledTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = label,
            color = TextSecondary,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium
        )
        androidx.compose.material3.OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            placeholder = { Text(placeholder) },
            colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = Color(0xFF121212),
                unfocusedContainerColor = Color(0xFF121212),
                focusedBorderColor = Color.White,
                unfocusedBorderColor = Color(0xFF3A3A3A),
                focusedPlaceholderColor = TextSecondary.copy(alpha = 0.5f),
                unfocusedPlaceholderColor = TextSecondary.copy(alpha = 0.5f),
                cursorColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun CalendarGrid(
    yearMonth: YearMonth,
    selectedDate: LocalDate,
    eventsOnDates: Set<LocalDate>,
    onDateSelected: (LocalDate) -> Unit
) {
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7 // Sunday = 0
    val daysInMonth = yearMonth.lengthOfMonth()
    
    val totalCells = ((firstDayOfWeek + daysInMonth + 6) / 7) * 7
    val today = LocalDate.now()
    
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        var dayCounter = 1
        for (week in 0 until (totalCells / 7)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                for (dayOfWeek in 0..6) {
                    val cellIndex = week * 7 + dayOfWeek
                    if (cellIndex >= firstDayOfWeek && dayCounter <= daysInMonth) {
                        val date = yearMonth.atDay(dayCounter)
                        val isSelected = date == selectedDate
                        val isToday = date == today
                        val hasEvent = date in eventsOnDates
                        
                        DayCell(
                            day = dayCounter,
                            isSelected = isSelected,
                            isToday = isToday,
                            hasEvent = hasEvent,
                            onClick = { onDateSelected(date) },
                            modifier = Modifier.weight(1f)
                        )
                        dayCounter++
                    } else {
                        // Empty cell
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvent: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = when {
        isSelected -> Color.White
        isToday -> GlowBlue.copy(alpha = 0.3f)
        else -> Color.Transparent
    }
    
    val textColor = when {
        isSelected -> Color.Black
        else -> TextPrimary
    }
    
    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(backgroundColor)
            .then(
                if (isToday && !isSelected) {
                    Modifier.border(1.dp, GlowBlue, CircleShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = day.toString(),
                color = textColor,
                fontSize = 14.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
            )
            // Event indicator dot
            if (hasEvent && !isSelected) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}
