package com.chrono.ui.exams

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
import androidx.compose.material3.TextButton
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import androidx.compose.runtime.mutableIntStateOf

private val GlassBorder = Color(0x60FFFFFF)

@Composable
fun EditExamDialog(
    title: String,
    initialDate: String,
    initialSubject: String,
    onDismiss: () -> Unit,
    onConfirm: (date: String, subject: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var date by remember { mutableStateOf(initialDate) }
    var subject by remember { mutableStateOf(initialSubject) }
    
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Date Picker
            val calendar = remember { Calendar.getInstance() }
            // Parse initial date if available
            val parsedInitial = remember {
                try {
                    if (date.isNotBlank()) {
                        val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        format.parse(date)?.let { calendar.time = it }
                    }
                    true
                } catch (e: Exception) { false }
            }
            
            var selectedYear by remember { mutableIntStateOf(calendar.get(Calendar.YEAR)) }
            var selectedMonth by remember { mutableIntStateOf(calendar.get(Calendar.MONTH)) }
            var selectedDay by remember { mutableIntStateOf(calendar.get(Calendar.DAY_OF_MONTH)) }
            var dateSelected by remember { mutableStateOf(date.isNotBlank()) }
            
            val displayDate = if (dateSelected) {
                com.chrono.ui.components.formatDate(selectedDay, selectedMonth, selectedYear)
            } else "Select date"
            
            com.chrono.ui.components.ThemedDatePicker(
                label = "Date",
                selectedDate = displayDate,
                isSelected = dateSelected,
                onDateSelected = { year, month, day ->
                    selectedYear = year
                    selectedMonth = month
                    selectedDay = day
                    dateSelected = true
                    // Format for storage/callback
                    date = String.format("%d-%02d-%02d", year, month + 1, day)
                },
                initialYear = selectedYear,
                initialMonth = selectedMonth,
                initialDay = selectedDay
            )
            
            StyledTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "Subject",
                placeholder = "Enter subject name"
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
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
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (date.isNotBlank() && subject.isNotBlank()) Color.White
                            else Color.White.copy(alpha = 0.4f)
                        )
                        .clickable(enabled = date.isNotBlank() && subject.isNotBlank()) {
                            onConfirm(date, subject)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = if (date.isNotBlank() && subject.isNotBlank()) Color.Black
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
