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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.outlined.Assignment
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.TaskType
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary

@Composable
fun TaskTypeSelectionDialog(
    onDismiss: () -> Unit,
    onSingleTaskSelected: () -> Unit,
    onGroupTaskSelected: () -> Unit
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
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Single Task Option
                SelectionCard(
                    title = "Single Task",
                    icon = Icons.Outlined.Assignment,
                    onClick = onSingleTaskSelected,
                    modifier = Modifier.weight(1f)
                )
                
                // Group Task Option
                SelectionCard(
                    title = "Group Task",
                    icon = Icons.Filled.FormatListBulleted,
                    onClick = onGroupTaskSelected,
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
fun AddGroupTaskDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<Pair<String, TaskType>>) -> Unit
) {
    // List of (Title, Type)
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
                .height(600.dp) // Fixed height for list
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
            
            // Input Area
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
            
            // List
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
                                imageVector = if (item.second == TaskType.TASK) Icons.Outlined.Assignment else Icons.Filled.Coffee,
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
                
                ButtonBox(
                    text = "Create Group",
                    onClick = { if (tasks.isNotEmpty()) onConfirm(tasks) },
                    backgroundColor = if (tasks.isNotEmpty()) Color.White else Color(0xFF333333),
                    textColor = if (tasks.isNotEmpty()) Color.Black else Color.Gray,
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
