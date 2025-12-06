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

private val GlassBorder = Color(0x60FFFFFF)

@Composable
fun EditPeriodDialog(
    day: String,
    period: String,
    currentSubject: String,
    onDismiss: () -> Unit,
    onConfirm: (subject: String) -> Unit
) {
    var subject by remember { mutableStateOf(currentSubject) }
    
    // Custom dark glassmorphic dialog (same style as AddEventDialog)
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
                text = "Edit Period",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Info display
            Text(
                text = "$day • $period",
                color = AccentBlue,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Subject Name Field
            StyledTextField(
                value = subject,
                onValueChange = { subject = it },
                label = "Subject Name",
                placeholder = "Enter subject"
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
                
                // Save Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White)
                        .clickable {
                            onConfirm(subject)
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
