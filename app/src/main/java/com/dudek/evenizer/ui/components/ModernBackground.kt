package com.dudek.evenizer.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dudek.evenizer.ui.theme.LocalGradients

@Composable
fun ModernBackground(
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LocalGradients.current.background)
    ) {
        content()
    }
}
