package com.chrono.ui.exams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.School
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.data.ExamsDataStore
import com.chrono.ui.theme.AccentBlue
import com.chrono.ui.theme.BackgroundGradient
import com.chrono.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun ExamsScreen(
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val examsDataStore = remember { ExamsDataStore(context) }
    val examsData by examsDataStore.examsData.collectAsState(initial = com.chrono.data.ExamsData())
    
    val statusBarPadding = WindowInsets.statusBars.asPaddingValues()
    val navBarPadding = WindowInsets.navigationBars.asPaddingValues()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var editIndex by remember { mutableStateOf(0) }
    var editDate by remember { mutableStateOf("") }
    var editSubject by remember { mutableStateOf("") }
    
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
                    text = "Exams",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            // Table Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 12.dp)
            ) {
                Text(
                    text = "Date",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Subject",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            
            // Exam list or Empty State
            if (examsData.exams.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.layout.Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.School,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No exams yet",
                            color = TextPrimary,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap + to add your first exam",
                            color = com.chrono.ui.theme.TextSecondary,
                            fontSize = 14.sp
                        )
                    }
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 32.dp)
                ) {
                    examsData.exams.forEachIndexed { index, exam ->
                        ExamRow(
                            date = exam.date,
                            subject = exam.subject,
                            onClick = {
                                editIndex = index
                                editDate = exam.date
                                editSubject = exam.subject
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
                contentDescription = "Add Exam",
                tint = Color.Black
            )
        }
        
        // Dialogs
        if (showAddDialog) {
            EditExamDialog(
                title = "Add Exam",
                initialDate = "",
                initialSubject = "",
                onDismiss = { showAddDialog = false },
                onConfirm = { date, subject ->
                    scope.launch {
                        examsDataStore.addExam(date, subject)
                    }
                    showAddDialog = false
                }
            )
        }
        
        if (showEditDialog) {
            EditExamDialog(
                title = "Edit Exam",
                initialDate = editDate,
                initialSubject = editSubject,
                onDismiss = { showEditDialog = false },
                onConfirm = { date, subject ->
                    scope.launch {
                        examsDataStore.updateExam(editIndex, date, subject)
                    }
                    showEditDialog = false
                },
                onDelete = {
                    scope.launch {
                        examsDataStore.deleteExam(editIndex)
                    }
                    showEditDialog = false
                }
            )
        }
    }
}

@Composable
private fun ExamRow(
    date: String,
    subject: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = date,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = subject,
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.weight(1f)
        )
    }
}
