package com.chrono.ui.tasks

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.PriorityHigh
import androidx.compose.material.icons.automirrored.outlined.Assignment
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.TaskType
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary

@Composable
fun TaskTypeSelectionDialog(
    onDismiss: () -> Unit,
    onSingleTaskSelected: () -> Unit,
    onGroupTaskSelected: () -> Unit,
    onPriorityTaskSelected: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "New Task",
                color = TextPrimary,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Single Task
                SelectionCard(
                    title = "Single",
                    icon = Icons.AutoMirrored.Outlined.Assignment,
                    onClick = onSingleTaskSelected,
                    modifier = Modifier.weight(1f)
                )
                
                // Group Task
                SelectionCard(
                    title = "Group",
                    icon = Icons.Filled.FormatListNumbered,
                    onClick = onGroupTaskSelected,
                    modifier = Modifier.weight(1f)
                )
                
                // Priority Task
                SelectionCard(
                    title = "Priority",
                    icon = Icons.Filled.PriorityHigh,
                    onClick = onPriorityTaskSelected,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SelectionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF2A2A2A))
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = title,
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun AddSingleTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add Single Task",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                placeholder = { Text("Enter task name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121212),
                    unfocusedContainerColor = Color(0xFF121212),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0xFF333333),
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonBox(
                    text = "Cancel",
                    onClick = onDismiss,
                    backgroundColor = Color(0xFF2A2A2A),
                    textColor = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                
                ButtonBox(
                    text = "Add Task",
                    onClick = { if (title.isNotBlank()) onConfirm(title) },
                    backgroundColor = if (title.isNotBlank()) Color.White else Color(0xFF333333),
                    textColor = if (title.isNotBlank()) Color.Black else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AddPriorityTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Int) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var minutes by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Priority Task",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "This task will become active immediately",
                color = TextSecondary,
                fontSize = 13.sp
            )
            
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task Title") },
                placeholder = { Text("What needs to be done?") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121212),
                    unfocusedContainerColor = Color(0xFF121212),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0xFF333333),
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            OutlinedTextField(
                value = minutes,
                onValueChange = { if (it.all { c -> c.isDigit() }) minutes = it },
                label = { Text("Time Limit (minutes)") },
                placeholder = { Text("e.g., 30") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121212),
                    unfocusedContainerColor = Color(0xFF121212),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0xFF333333),
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonBox(
                    text = "Cancel",
                    onClick = onDismiss,
                    backgroundColor = Color(0xFF2A2A2A),
                    textColor = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                
                val canSave = title.isNotBlank() && minutes.isNotBlank() && minutes.toIntOrNull() != null
                ButtonBox(
                    text = "Add",
                    onClick = { 
                        if (canSave) onConfirm(title, minutes.toInt())
                    },
                    backgroundColor = if (canSave) Color.White else Color(0xFF333333),
                    textColor = if (canSave) Color.Black else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun AddGroupTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, List<Pair<String, TaskType>>) -> Unit
) {
    var groupName by remember { mutableStateOf("") }
    val tasks = remember { mutableStateListOf<Pair<String, TaskType>>() }
    var currentInput by remember { mutableStateOf("") }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .height(650.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create Task Group",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Group Name Input
            OutlinedTextField(
                value = groupName,
                onValueChange = { groupName = it },
                label = { Text("Group Name") },
                placeholder = { Text("e.g., Morning Routine") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedContainerColor = Color(0xFF121212),
                    unfocusedContainerColor = Color(0xFF121212),
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color(0xFF333333),
                    cursorColor = Color.White,
                    focusedLabelColor = Color.White,
                    unfocusedLabelColor = TextSecondary
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            // Task Input Area
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = currentInput,
                    onValueChange = { currentInput = it },
                    placeholder = { Text("Enter task or break name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF121212),
                        unfocusedContainerColor = Color(0xFF121212),
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color(0xFF333333),
                        cursorColor = Color.White
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ButtonBox(
                        text = "+ Task",
                        onClick = {
                            if (currentInput.isNotBlank()) {
                                tasks.add(currentInput to TaskType.TASK)
                                currentInput = ""
                            }
                        },
                        backgroundColor = Color(0xFF2A2A2A),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                    ButtonBox(
                        text = "+ Break",
                        onClick = {
                            if (currentInput.isNotBlank()) {
                                tasks.add(currentInput to TaskType.BREAK)
                                currentInput = ""
                            }
                        },
                        backgroundColor = Color(0xFF2A2A2A),
                        textColor = Color.White,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            // Tasks List
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(Color(0xFF121212), RoundedCornerShape(12.dp))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(tasks) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF2A2A2A), RoundedCornerShape(8.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (item.second == TaskType.TASK) Icons.AutoMirrored.Outlined.Assignment else Icons.Filled.Coffee,
                                contentDescription = null,
                                tint = if (item.second == TaskType.TASK) Color.White else Color.Gray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = item.first,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                        }
                        IconButton(
                            onClick = { tasks.removeAt(index) },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Remove",
                                tint = TextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
            
            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonBox(
                    text = "Cancel",
                    onClick = onDismiss,
                    backgroundColor = Color(0xFF2A2A2A),
                    textColor = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                
                val canSave = groupName.isNotBlank() && tasks.isNotEmpty()
                ButtonBox(
                    text = "Create",
                    onClick = { if (canSave) onConfirm(groupName, tasks) },
                    backgroundColor = if (canSave) Color.White else Color(0xFF333333),
                    textColor = if (canSave) Color.Black else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun ButtonBox(
    text: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun AddToGroupDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, type: TaskType) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(TaskType.TASK) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Add to $groupName",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            // Type selection
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedType == TaskType.TASK) Color.White else Color(0xFF2A2A2A))
                        .clickable { selectedType = TaskType.TASK }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.Assignment,
                            contentDescription = null,
                            tint = if (selectedType == TaskType.TASK) Color.Black else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Task",
                            color = if (selectedType == TaskType.TASK) Color.Black else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedType == TaskType.BREAK) Color.White else Color(0xFF2A2A2A))
                        .clickable { selectedType = TaskType.BREAK }
                        .padding(vertical = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Coffee,
                            contentDescription = null,
                            tint = if (selectedType == TaskType.BREAK) Color.Black else TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Break",
                            color = if (selectedType == TaskType.BREAK) Color.Black else TextSecondary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
            
            // Title input
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text(if (selectedType == TaskType.TASK) "Task name" else "Break name") },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
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
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ButtonBox(
                    text = "Cancel",
                    onClick = onDismiss,
                    backgroundColor = Color(0xFF2A2A2A),
                    textColor = TextSecondary,
                    modifier = Modifier.weight(1f)
                )
                
                ButtonBox(
                    text = "Add",
                    onClick = { if (title.isNotBlank()) onConfirm(title, selectedType) },
                    backgroundColor = if (title.isNotBlank()) Color.White else Color(0xFF333333),
                    textColor = if (title.isNotBlank()) Color.Black else Color.Gray,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

