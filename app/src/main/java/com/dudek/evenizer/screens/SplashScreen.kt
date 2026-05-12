package com.dudek.evenizer.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dudek.evenizer.R
import com.dudek.evenizer.data.network.di.NetworkModule
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(5f) } // Start big
    val step = remember { mutableIntStateOf(0) }
    val showLoading = remember { mutableStateOf(false) }

    // Colors for transition
    val colors = listOf(
        Color(0xFF9C27B0), // Purple
        Color(0xFF4CAF50), // Green
        Color(0xFF2196F3), // Blue
        Color(0xFFFF9800), // Orange
        Color(0xFFF44336)  // Red
    )

    val backgroundColor by animateColorAsState(
        targetValue = if (!showLoading.value) Color.White else colors[step.intValue % colors.size],
        animationSpec = tween(durationMillis = 800),
        label = "backgroundColor"
    )

    val contentColor by animateColorAsState(
        targetValue = if (!showLoading.value) Color.Black else Color.White,
        animationSpec = tween(durationMillis = 800),
        label = "contentColor"
    )

    val loadingText = when (step.intValue) {
        0 -> stringResource(R.string.splash_init)
        1 -> stringResource(R.string.splash_events)
        2 -> stringResource(R.string.splash_organizers)
        3 -> stringResource(R.string.splash_tickets)
        else -> stringResource(R.string.splash_welcome)
    }

    LaunchedEffect(Unit) {
        // Phase 1: Shrink Logo
        delay(500) // Initial wait on white screen
        logoScale.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
        )
        
        // Phase 2: Start Loading & Color Transitions
        showLoading.value = true
        
        try {
            val response = NetworkModule.getApiService().checkHealth()
            if (response.success) {
                delay(600)
                step.intValue = 1
                delay(600)
                step.intValue = 2
                delay(600)
                step.intValue = 3
                delay(600)
                step.intValue = 4
                delay(800)
            } else {
                delay(1000)
                step.intValue = 1
                delay(1000)
                step.intValue = 2
                delay(1000)
                step.intValue = 3
                delay(1000)
                step.intValue = 4
                delay(1000)
            }
        } catch (_: Exception) {
            delay(1000)
            step.intValue = 1
            delay(1000)
            step.intValue = 2
            delay(1000)
            step.intValue = 3
            delay(1000)
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
            // Logo Image using ic_launcher components (painterResource doesn't support adaptive icons)
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(logoScale.value)
                    .clip(CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_background),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
                Image(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            if (showLoading.value) {
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = loadingText,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = contentColor,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
