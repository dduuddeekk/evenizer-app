package com.dudek.evenizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dudek.evenizer.ui.theme.LocalGradients

@Composable
fun GradientFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    gradient: Brush = LocalGradients.current.primary,
    contentColor: Color = Color.White,
    content: @Composable () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp),
        containerColor = Color.Transparent,
        contentColor = contentColor,
        elevation = FloatingActionButtonDefaults.elevation(0.dp, 0.dp, 0.dp, 0.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(gradient, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}
