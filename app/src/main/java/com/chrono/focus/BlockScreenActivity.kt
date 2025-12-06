package com.chrono.focus

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.chrono.ui.theme.ChronoTheme

class BlockScreenActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ChronoTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Block,
                            contentDescription = "Blocked",
                            tint = Color.White,
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Text(
                            text = "Stay Focused!",
                            color = Color.White,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "This app is blocked during your focus session.",
                            color = Color.Gray,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(48.dp))
                        Button(
                            onClick = { 
                                // Go back home
                                val startMain = android.content.Intent(android.content.Intent.ACTION_MAIN)
                                startMain.addCategory(android.content.Intent.CATEGORY_HOME)
                                startMain.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(startMain)
                                finish()
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "Go Back",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
    
    override fun onBackPressed() {
        // Prevent going back to the blocked app
        val startMain = android.content.Intent(android.content.Intent.ACTION_MAIN)
        startMain.addCategory(android.content.Intent.CATEGORY_HOME)
        startMain.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(startMain)
        super.onBackPressed()
    }
}
