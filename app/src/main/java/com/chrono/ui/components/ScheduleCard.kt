package com.chrono.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.ui.theme.AccentBlue
import com.chrono.ui.theme.SurfaceCardLight
import com.chrono.ui.theme.TextDark
import com.chrono.ui.theme.TextMuted

data class ScheduleItem(
    val time: String,
    val title: String,
    val subtitle: String = "",
    val accentColor: Color = Color.White
)

@Composable
fun ScheduleCard(
    item: ScheduleItem,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCardLight),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Accent indicator
            Icon(
                imageVector = Icons.Filled.Circle,
                contentDescription = null,
                tint = item.accentColor,
                modifier = Modifier.size(10.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Content
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = TextDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
                if (item.subtitle.isNotEmpty()) {
                    Text(
                        text = item.subtitle,
                        color = TextMuted,
                        fontSize = 13.sp
                    )
                }
            }
            
            // Time badge
            Text(
                text = item.time,
                color = TextMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
