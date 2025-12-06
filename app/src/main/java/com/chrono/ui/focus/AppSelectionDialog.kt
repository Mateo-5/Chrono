package com.chrono.ui.focus

import android.content.pm.PackageManager
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.chrono.ui.theme.TextPrimary
import com.chrono.ui.theme.TextSecondary
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppInfo(
    val name: String,
    val packageName: String,
    val icon: android.graphics.Bitmap?
)

@Composable
fun AppSelectionDialog(
    initialSelection: Set<String>,
    onDismiss: () -> Unit,
    onConfirm: (Set<String>) -> Unit
) {
    val context = LocalContext.current
    val selectedPackages = remember { mutableStateListOf<String>().apply { addAll(initialSelection) } }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val pm = context.packageManager
            val apps = pm.getInstalledPackages(PackageManager.GET_META_DATA)
                .filter { 
                    // Filter out system apps roughly, or just show launchable ones
                    pm.getLaunchIntentForPackage(it.packageName) != null 
                }
                .mapNotNull { 
                    val appInfo = it.applicationInfo ?: return@mapNotNull null
                    AppInfo(
                        name = appInfo.loadLabel(pm).toString(),
                        packageName = it.packageName,
                        icon = appInfo.loadIcon(pm).toBitmap()
                    )
                }
                .sortedBy { it.name }
            
            withContext(Dispatchers.Main) {
                installedApps = apps
                isLoading = false
            }
        }
    }

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
                .height(600.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF1A1A1A))
                .border(1.dp, Color(0xFF333333), RoundedCornerShape(24.dp))
                .clickable(enabled = false) {}
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Select Apps to Block",
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            
            if (isLoading) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF121212), RoundedCornerShape(12.dp))
                        .padding(8.dp)
                ) {
                    items(installedApps) { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (selectedPackages.contains(app.packageName)) {
                                        selectedPackages.remove(app.packageName)
                                    } else {
                                        selectedPackages.add(app.packageName)
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (app.icon != null) {
                                Image(
                                    bitmap = app.icon.asImageBitmap(),
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp)
                                )
                            } else {
                                Box(modifier = Modifier.size(32.dp).background(Color.Gray))
                            }
                            
                            Spacer(modifier = Modifier.width(12.dp))
                            
                            Text(
                                text = app.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                modifier = Modifier.weight(1f)
                            )
                            
                            Checkbox(
                                checked = selectedPackages.contains(app.packageName),
                                onCheckedChange = { checked ->
                                    if (checked) selectedPackages.add(app.packageName)
                                    else selectedPackages.remove(app.packageName)
                                },
                                colors = CheckboxDefaults.colors(
                                    checkedColor = Color.White,
                                    uncheckedColor = Color.Gray,
                                    checkmarkColor = Color.Black
                                )
                            )
                        }
                    }
                }
            }
            
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
                    text = "Save",
                    onClick = { onConfirm(selectedPackages.toSet()) },
                    backgroundColor = Color.White,
                    textColor = Color.Black,
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
