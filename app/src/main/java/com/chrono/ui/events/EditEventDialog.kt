package com.chrono.ui.events

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import com.chrono.ui.theme.AccentBlue
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

private val GlassBorder = Color(0x60FFFFFF)

@Composable
fun EditEventDialog(
    title: String,
    initialTitle: String,
    initialDate: String,
    initialTime: String,
    initialSubtitle: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, date: String, time: String, subtitle: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    val context = LocalContext.current
    var eventTitle by remember { mutableStateOf(initialTitle) }
    var eventSubtitle by remember { mutableStateOf(initialSubtitle) }
    
    val calendar = remember { Calendar.getInstance() }
    // Parse initial date if available
    val parsedInitial = remember {
        try {
            if (initialDate.isNotBlank()) {
                val format = SimpleDateFormat("dd-MMM-yyyy", Locale.getDefault())
                format.parse(initialDate)?.let { calendar.time = it }
            }
            true
        } catch (e: Exception) { false }
    }
    
    var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
    var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
    var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
    var selectedHour by remember { mutableIntStateOf(if (initialTime.isNotBlank()) initialTime.split(":").getOrNull(0)?.toIntOrNull() ?: 12 else 12) }
    var selectedMinute by remember { mutableIntStateOf(if (initialTime.isNotBlank()) initialTime.split(":").getOrNull(1)?.toIntOrNull() ?: 0 else 0) }
    
    var dateSelected by remember { mutableStateOf(initialDate.isNotBlank()) }
    var timeSelected by remember { mutableStateOf(initialTime.isNotBlank()) }
    
    val dateStr = if (dateSelected) {
        com.chrono.ui.components.formatDate(selectedDay, selectedMonth, selectedYear)
    } else "Select date"
    
    val timeStr = if (timeSelected) {
        com.chrono.ui.components.formatTime(selectedHour, selectedMinute)
    } else "Select time"
    
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
                .clickable(enabled = false) {}
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = title,
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Title field
            StyledTextField(
                value = eventTitle,
                onValueChange = { eventTitle = it },
                label = "Title",
                placeholder = "Event title"
            )
            
            // Date Picker
            com.chrono.ui.components.ThemedDatePicker(
                label = "Date",
                selectedDate = dateStr,
                isSelected = dateSelected,
                onDateSelected = { year, month, day ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = day
                    dateSelected = true
                },
                initialYear = selectedYear,
                initialMonth = selectedMonth,
                initialDay = selectedDay
            )
            
            // Time Picker
            com.chrono.ui.components.ThemedTimePicker(
                label = "Time",
                selectedTime = timeStr,
                isSelected = timeSelected,
                onTimeSelected = { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    timeSelected = true
                },
                initialHour = selectedHour,
                initialMinute = selectedMinute
            )
            
            StyledTextField(
                value = eventSubtitle,
                onValueChange = { eventSubtitle = it },
                label = "Description (optional)",
                placeholder = "Additional details"
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (onDelete != null) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFB83A3A))
                            .clickable(onClick = onDelete)
                            .padding(vertical = 14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Delete",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
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
                
                val canSave = eventTitle.isNotBlank() && dateSelected
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (canSave) Color.White else Color.White.copy(alpha = 0.4f))
                        .clickable(enabled = canSave) {
                            onConfirm(eventTitle, dateStr, timeStr.takeIf { timeSelected } ?: "", eventSubtitle)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = if (canSave) Color.Black else Color.Black.copy(alpha = 0.5f),
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
