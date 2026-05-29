package com.dudek.evenizer.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Immutable
data class EvenizerGradients(
    val primary: Brush = Brush.verticalGradient(listOf(GradientStart, GradientEnd)),
    val secondary: Brush = Brush.verticalGradient(listOf(SecondaryGradientStart, SecondaryGradientEnd)),
    val tertiary: Brush = Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF1976D2))),
    val quaternary: Brush = Brush.verticalGradient(listOf(Color(0xFFFF9800), Color(0xFFF57C00))), // Orange
    val quinary: Brush = Brush.verticalGradient(listOf(Color(0xFFF44336), Color(0xFFD32F2F))),    // Red
    val background: Brush = Brush.verticalGradient(listOf(BackgroundGradientStart, BackgroundGradientEnd)),
    val surface: Brush = Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F5F5)))
)

val LocalGradients = staticCompositionLocalOf { EvenizerGradients() }
