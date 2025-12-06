package com.chrono.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.graphics.Color as AndroidColor
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.ui.theme.AccentBlue
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary

/**
 * Themed date picker button that opens a DatePickerDialog
 */
@Composable
fun ThemedDatePicker(
    label: String,
    selectedDate: String,
    isSelected: Boolean,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit,
    initialYear: Int,
    initialMonth: Int,
    initialDay: Int
) {
    val context = LocalContext.current
    
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
                .background(Color(0xFF121212))
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color.White else Color(0xFF3A3A3A),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    showThemedDatePicker(
                        context = context,
                        year = initialYear,
                        month = initialMonth,
                        day = initialDay,
                        onDateSelected = onDateSelected
                    )
                }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate,
                    color = if (isSelected) TextPrimary else TextSecondary.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = "Select date",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Themed time picker button that opens a TimePickerDialog
 */
@Composable
fun ThemedTimePicker(
    label: String,
    selectedTime: String,
    isSelected: Boolean,
    onTimeSelected: (hour: Int, minute: Int) -> Unit,
    initialHour: Int,
    initialMinute: Int,
    is24HourFormat: Boolean = false
) {
    val context = LocalContext.current
    
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
                .background(Color(0xFF121212))
                .border(
                    width = 1.dp,
                    color = if (isSelected) Color.White else Color(0xFF3A3A3A),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable {
                    showThemedTimePicker(
                        context = context,
                        hour = initialHour,
                        minute = initialMinute,
                        is24Hour = is24HourFormat,
                        onTimeSelected = onTimeSelected
                    )
                }
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedTime,
                    color = if (isSelected) TextPrimary else TextSecondary.copy(alpha = 0.5f),
                    fontSize = 16.sp
                )
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = "Select time",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun showThemedDatePicker(
    context: Context,
    year: Int,
    month: Int,
    day: Int,
    onDateSelected: (year: Int, month: Int, day: Int) -> Unit
) {
    val dialog = DatePickerDialog(
        context,
        android.R.style.Theme_DeviceDefault_Dialog,
        { _: DatePicker, selectedYear: Int, selectedMonth: Int, selectedDay: Int ->
            onDateSelected(selectedYear, selectedMonth, selectedDay)
        },
        year,
        month,
        day
    )
    dialog.show()
}

private fun showThemedTimePicker(
    context: Context,
    hour: Int,
    minute: Int,
    is24Hour: Boolean,
    onTimeSelected: (hour: Int, minute: Int) -> Unit
) {
    val dialog = TimePickerDialog(
        context,
        android.R.style.Theme_DeviceDefault_Dialog,
        { _: TimePicker, selectedHour: Int, selectedMinute: Int ->
            onTimeSelected(selectedHour, selectedMinute)
        },
        hour,
        minute,
        is24Hour
    )
    dialog.show()
}

/**
 * Formats a date for display
 */
fun formatDate(day: Int, month: Int, year: Int): String {
    val monthNames = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    return String.format("%02d-%s-%d", day, monthNames[month], year)
}

/**
 * Formats a time for display
 */
fun formatTime(hour: Int, minute: Int, is24Hour: Boolean = false): String {
    return if (is24Hour) {
        String.format("%02d:%02d", hour, minute)
    } else {
        val amPm = if (hour < 12) "AM" else "PM"
        val displayHour = when {
            hour == 0 -> 12
            hour > 12 -> hour - 12
            else -> hour
        }
        String.format("%d:%02d %s", displayHour, minute, amPm)
    }
}
