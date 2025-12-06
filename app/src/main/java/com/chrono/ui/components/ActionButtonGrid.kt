package com.chrono.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.EventNote
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

data class ActionItem(
    val icon: ImageVector,
    val label: String,
    val route: String
)

val defaultActions = listOf(
    ActionItem(Icons.Outlined.CalendarMonth, "Calendar", "calendar"),
    ActionItem(Icons.Outlined.Schedule, "Timetable", "timetable"),
    ActionItem(Icons.Outlined.School, "Exams", "exams"),
    ActionItem(Icons.Outlined.Alarm, "Reminders", "reminders"),
    ActionItem(Icons.Outlined.CheckCircle, "Tasks", "tasks"),
    ActionItem(Icons.Outlined.EditNote, "Notes", "notes"),
    ActionItem(Icons.Outlined.EventNote, "Events", "events"),
    ActionItem(Icons.Outlined.LightMode, "Focus", "focus")
)

@Composable
fun ActionButtonGrid(
    actions: List<ActionItem> = defaultActions,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(4),
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(actions) { action ->
            ActionButton(
                icon = action.icon,
                label = action.label,
                onClick = { onActionClick(action.route) }
            )
        }
    }
}
