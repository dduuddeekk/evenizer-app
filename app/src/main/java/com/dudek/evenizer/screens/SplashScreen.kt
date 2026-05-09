package com.dudek.evenizer.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.NetworkModule
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val step = remember { mutableIntStateOf(0) }

    val backgroundColor by animateColorAsState(
        targetValue = when (step.intValue) {
            0 -> Color(0xFF9C27B0) // Home Color
            1 -> Color(0xFF4CAF50) // Event Color
            2 -> Color(0xFF2196F3) // Organizer Color
            3 -> Color(0xFFFF9800) // Ticket Color
            else -> Color(0xFFF44336) // Profile/Main Color
        },
        animationSpec = tween(durationMillis = 800),
        label = "backgroundColor"
    )

    val loadingText = when (step.intValue) {
        0 -> stringResource(R.string.splash_init)
        1 -> stringResource(R.string.splash_events)
        2 -> stringResource(R.string.splash_organizers)
        3 -> stringResource(R.string.splash_tickets)
        else -> stringResource(R.string.splash_welcome)
    }

    LaunchedEffect(Unit) {
        try {
            // Step 0: Initializing (Wait for health check)
            val response = NetworkModule.apiService.checkHealth()
            
            if (response.success) {
                // If health check is successful, proceed with steps quickly
                delay(600)
                step.intValue = 1
                delay(600)
                step.intValue = 2
                delay(600)
                step.intValue = 3
                delay(600)
                step.intValue = 4
                delay(600)
            } else {
                // Fallback to original timing if success is false
                delay(1000)
                step.intValue = 1
                delay(1200)
                step.intValue = 2
                delay(1200)
                step.intValue = 3
                delay(1200)
                step.intValue = 4
                delay(1000)
            }
        } catch (e: Exception) {
            // Fallback to original timing on network error
            delay(1000)
            step.intValue = 1
            delay(1200)
            step.intValue = 2
            delay(1200)
            step.intValue = 3
            delay(1200)
            step.intValue = 4
            delay(1000)
        }
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Evenizer",
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            CircularProgressIndicator(
                color = Color.White,
                strokeWidth = 3.dp
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = loadingText,
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}
