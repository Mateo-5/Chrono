package com.chrono.ui.events

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.EventsDataStore
import com.chrono.ui.theme.AccentBlue
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.launch

private val GlassBorder = Color(0x60FFFFFF)

@Composable
fun EventsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val eventsDataStore = remember { EventsDataStore(context) }
    val eventsData by eventsDataStore.eventsData.collectAsState(initial = com.chrono.data.EventsData())
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editEventId by remember { mutableStateOf("") }
    var editTitle by remember { mutableStateOf("") }
    var editDate by remember { mutableStateOf("") }
    var editTime by remember { mutableStateOf("") }
    var editSubtitle by remember { mutableStateOf("") }
    
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
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 16.dp),
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
                    text = "Events",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Events Timeline or Empty State
            if (eventsData.events.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.CalendarMonth,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No events yet",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first event",
                            color = TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(eventsData.events, key = { it.id }) { event ->
                        EventCard(
                            title = event.title,
                            date = event.date,
                            time = event.time,
                            subtitle = event.subtitle,
                            onClick = {
                                editEventId = event.id
                                editTitle = event.title
                                editDate = event.date
                                editTime = event.time
                                editSubtitle = event.subtitle
                                showEditDialog = true
                            }
                        )
                    }
                }
            }
        }
        
        // FAB
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = Color.White,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Event",
                tint = Color.Black
            )
        }
        
        // Dialogs
        if (showAddDialog) {
            EditEventDialog(
                title = "Add Event",
                initialTitle = "",
                initialDate = "",
                initialTime = "",
                initialSubtitle = "",
                onDismiss = { showAddDialog = false },
                onConfirm = { title, date, time, subtitle ->
                    scope.launch {
                        eventsDataStore.addEvent(title, date, time, subtitle)
                    }
                    showAddDialog = false
                }
            )
        }
        
        if (showEditDialog) {
            EditEventDialog(
                title = "Edit Event",
                initialTitle = editTitle,
                initialDate = editDate,
                initialTime = editTime,
                initialSubtitle = editSubtitle,
                onDismiss = { showEditDialog = false },
                onConfirm = { title, date, time, subtitle ->
                    scope.launch {
                        eventsDataStore.updateEvent(editEventId, title, time, subtitle)
                    }
                    showEditDialog = false
                },
                onDelete = {
                    scope.launch {
                        eventsDataStore.deleteEvent(editEventId)
                    }
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
private fun EventCard(
    title: String,
    date: String,
    time: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0x40FFFFFF),
                        Color(0x20FFFFFF)
                    )
                )
            )
            .border(1.dp, GlassBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date indicator
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = date.substringBefore("-"),
                        color = Color.White,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = date.substringAfter("-").take(3),
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Event details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = time,
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )
                if (subtitle.isNotEmpty()) {
                    Text(
                        text = subtitle,
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
