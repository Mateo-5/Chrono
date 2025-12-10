package com.chrono.ui.notes

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.NotesDataStore
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun NoteDetailScreen(
    noteId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val notesDataStore = remember { NotesDataStore(context) }
    val notesData by notesDataStore.notesData.collectAsState(initial = com.chrono.data.NotesData())
    
    val note = notesData.notes.find { it.id == noteId }
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    
    // Editable state
    var editableTitle by remember(note?.title) { mutableStateOf(note?.title ?: "") }
    var editableContent by remember(note?.content) { mutableStateOf(note?.content ?: "") }
    var hasChanges by remember { mutableStateOf(false) }
    
    // Update local state when note changes
    LaunchedEffect(note) {
        if (note != null) {
            editableTitle = note.title
            editableContent = note.content
            hasChanges = false
        }
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundGradient)
    ) {
        if (note == null) {
            // Note not found
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Note not found",
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = statusBarPadding.calculateTopPadding(),
                        bottom = navBarPadding.calculateBottomPadding()
                    )
            ) {
                // Header with back, save, and delete
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(onClick = {
                        // Auto-save on back if there are changes
                        if (hasChanges && editableTitle.isNotBlank()) {
                            scope.launch {
                                notesDataStore.updateNote(noteId, editableTitle, editableContent)
                            }
                        }
                        onBack()
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                    
                    Row {
                        // Save button (only visible when there are changes)
                        if (hasChanges && editableTitle.isNotBlank()) {
                            IconButton(onClick = {
                                scope.launch {
                                    notesDataStore.updateNote(noteId, editableTitle, editableContent)
                                    hasChanges = false
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save",
                                    tint = Color(0xFF4CAF50)
                                )
                            }
                        }
                        
                        IconButton(onClick = {
                            scope.launch {
                                notesDataStore.deleteNote(noteId)
                                onBack()
                            }
                        }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = Color(0xFFEF5350)
                            )
                        }
                    }
                }
                
                // Note content - inline editable
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                ) {
                    // Editable Title
                    BasicTextField(
                        value = editableTitle,
                        onValueChange = { 
                            editableTitle = it
                            hasChanges = true
                        },
                        textStyle = TextStyle(
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            imeAction = ImeAction.Next
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        decorationBox = { innerTextField ->
                            Box {
                                if (editableTitle.isEmpty()) {
                                    Text(
                                        text = "Title",
                                        color = TextSecondary,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Date (read-only)
                    Text(
                        text = note.date,
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    // Editable Content
                    BasicTextField(
                        value = editableContent,
                        onValueChange = { 
                            editableContent = it
                            hasChanges = true
                        },
                        textStyle = TextStyle(
                            color = Color.White.copy(alpha = 0.9f),
                            fontSize = 16.sp,
                            lineHeight = 26.sp
                        ),
                        cursorBrush = SolidColor(Color.White),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Sentences,
                            keyboardType = KeyboardType.Text
                            // No imeAction - allows natural multiline and IME composition
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        decorationBox = { innerTextField ->
                            Box {
                                if (editableContent.isEmpty()) {
                                    Text(
                                        text = "Start writing...",
                                        color = TextSecondary,
                                        fontSize = 16.sp
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
