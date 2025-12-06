package com.chrono.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.ui.components.ActionButtonGrid
import com.chrono.ui.components.TopHeader
import com.chrono.ui.theme.AccentBlue
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextDark
import com.chrono.ui.theme.TextMuted
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun HomeScreen(
    onNavigate: (String) -> Unit = {}
) {
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    val configuration = LocalConfiguration.current
    val screenHeight = configuration.screenHeightDp.dp
    
    // Dynamic greeting based on time
    val currentHour = LocalTime.now().hour
    val greeting = when {
        currentHour < 5 -> "Good Night"
        currentHour < 12 -> "Good Morning"
        currentHour < 17 -> "Good Afternoon"
        currentHour < 21 -> "Good Evening"
        else -> "Good Night"
    }
    
    // Responsive grid height
    val gridHeight = when {
        screenHeight < 600.dp -> 180.dp
        screenHeight < 800.dp -> 220.dp
        else -> 260.dp
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = statusBarPadding.calculateTopPadding())
        ) {
            // Top Header
            TopHeader(
                greeting = "Chrono",
                onSettingsClick = { onNavigate("settings") },
                onNotificationClick = { onNavigate("notifications") }
            )
            
            // Dynamic Greeting Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = greeting,
                    color = TextPrimary,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "What would you like to do today?",
                    color = TextSecondary,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Action Button Grid
            ActionButtonGrid(
                onActionClick = onNavigate,
                modifier = Modifier.height(gridHeight)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Schedule Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFF8FAFC),
                                Color(0xFFFFFFFF)
                            )
                        )
                    )
                    .padding(top = 24.dp, start = 20.dp, end = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Today's Schedule",
                        color = TextDark,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF1A1A1A))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")),
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(bottom = navBarPadding.calculateBottomPadding()),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF0F0F0)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.CalendarMonth,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No events today",
                            color = TextDark,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap Calendar to add events",
                            color = TextMuted,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
