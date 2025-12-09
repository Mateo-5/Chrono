package com.chrono.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary

@Composable
fun EditNoteDialog(
    title: String,
    initialTitle: String,
    initialContent: String,
    onDismiss: () -> Unit,
    onConfirm: (title: String, content: String) -> Unit,
    onDelete: (() -> Unit)? = null
) {
    var noteTitle by remember { mutableStateOf(initialTitle) }
    var noteContent by remember { mutableStateOf(initialContent) }
    
    // Use interactionSource to prevent click events from propagating
    val dismissInteractionSource = remember { MutableInteractionSource() }
    val blockInteractionSource = remember { MutableInteractionSource() }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.6f))
            .clickable(
                interactionSource = dismissInteractionSource,
                indication = null,
                onClick = onDismiss
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF3A3A3A), RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = blockInteractionSource,
                    indication = null,
                    onClick = { /* Block click propagation */ }
                )
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
            
            // Title field - single line, Next goes to content
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Title",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                androidx.compose.material3.OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    singleLine = true,
                    placeholder = { Text("Note title") },
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Next
                    ),
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
            
            // Content field - multi-line, Enter key MUST insert newlines
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = "Content",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
                androidx.compose.material3.OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    singleLine = false,  // CRITICAL: Must be false for multiline
                    minLines = 5,
                    maxLines = 10,
                    placeholder = { Text("Write your note...") },
                    // Do NOT specify imeAction for multiline - let system handle it naturally
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                        // No imeAction specified = natural multiline behavior
                    ),
                    // Explicitly block all keyboard actions from doing anything
                    keyboardActions = KeyboardActions(
                        onDone = { /* Block - do nothing */ },
                        onGo = { /* Block - do nothing */ },
                        onNext = { /* Block - do nothing */ },
                        onPrevious = { /* Block - do nothing */ },
                        onSearch = { /* Block - do nothing */ },
                        onSend = { /* Block - do nothing */ }
                    ),
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
                            if (noteTitle.isNotBlank()) Color.White
                            else Color.White.copy(alpha = 0.4f)
                        )
                        .clickable(enabled = noteTitle.isNotBlank()) {
                            onConfirm(noteTitle, noteContent)
                        }
                        .padding(vertical = 14.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Save",
                        color = if (noteTitle.isNotBlank()) Color.Black
                        else Color.Black.copy(alpha = 0.5f),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
