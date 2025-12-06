package com.chrono.ui.timetable

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.ui.theme.AccentBlue
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import java.util.Calendar
import androidx.compose.runtime.mutableIntStateOf

private val GlassBorder = Color(0x60FFFFFF)

@Composable
fun EditTimeDialog(
    periodIndex: Int,
    currentTime: String,
    onDismiss: () -> Unit,
    onConfirm: (time: String) -> Unit
) {
    var time by remember { mutableStateOf(currentTime) }
    
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
                text = "Edit Time Period",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Period ${periodIndex + 1}",
                color = AccentBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Time Picker
            val calendar = remember { Calendar.getInstance() }
            // Parse initial time if available (assuming format HH:mm or HH:mm AM/PM)
            val parsedInitial = remember {
                try {
                    if (time.isNotBlank()) {
                        // Simple parsing logic
                        val parts = time.split(":", " ")
                        if (parts.size >= 2) {
                            var h = parts[0].toInt()
                            val m = parts[1].toInt()
                            if (time.contains("PM", ignoreCase = true) && h < 12) h += 12
                            if (time.contains("AM", ignoreCase = true) && h == 12) h = 0
                            calendar.set(Calendar.HOUR_OF_DAY, h)
                            calendar.set(Calendar.MINUTE, m)
                        }
                    }
                    true
                } catch (e: Exception) { false }
            }
            
            var selectedHour by remember { mutableIntStateOf(calendar.get(Calendar.HOUR_OF_DAY)) }
            var selectedMinute by remember { mutableIntStateOf(calendar.get(Calendar.MINUTE)) }
            var timeSelected by remember { mutableStateOf(time.isNotBlank()) }
            
            val displayTime = if (timeSelected) {
                com.chrono.ui.components.formatTime(selectedHour, selectedMinute, false)
            } else "Select time"
            
            com.chrono.ui.components.ThemedTimePicker(
                label = "Time",
                selectedTime = displayTime,
                isSelected = timeSelected,
                onTimeSelected = { hour, minute ->
                    selectedHour = hour
                    selectedMinute = minute
                    timeSelected = true
                    time = com.chrono.ui.components.formatTime(hour, minute, false)
                },
                initialHour = selectedHour,
                initialMinute = selectedMinute,
                is24HourFormat = false
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
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
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable {
                            onConfirm(time)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = Color.Black,
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF0D1B2A))
                .border(
                    width = 1.dp,
                    color = if (value.isNotEmpty()) AccentBlue.copy(alpha = 0.5f)
                    else Color(0xFF3A4A5A),
                    shape = RoundedCornerShape(12.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = false,
                maxLines = 2,
                textStyle = androidx.compose.ui.text.TextStyle(
                    color = TextPrimary,
                    fontSize = 16.sp
                ),
                decorationBox = { innerTextField ->
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            color = TextSecondary.copy(alpha = 0.5f),
                            fontSize = 16.sp
                        )
                    }
                    innerTextField()
                }
            )
        }
    }
}
