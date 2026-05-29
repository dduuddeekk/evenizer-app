package com.dudek.evenizer.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80,
    surfaceVariant = Color(0xFF2C2C2C),
    onSurfaceVariant = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40,
    surfaceVariant = Color(0xFFF5F5F5),
    onSurfaceVariant = Color.Black

    /* Other default colors to override
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

@Composable
fun EvenizerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val gradients = if (darkTheme) {
        EvenizerGradients(
            primary = Brush.verticalGradient(listOf(Color(0xFFAB47BC), Color(0xFF6A1B9A))), // Vibrant Purple
            secondary = Brush.verticalGradient(listOf(Color(0xFF66BB6A), Color(0xFF2E7D32))), // Vibrant Green
            tertiary = Brush.verticalGradient(listOf(Color(0xFF42A5F5), Color(0xFF1976D2))), // Vibrant Blue
            background = Brush.verticalGradient(listOf(Color(0xFF0D0D0D), Color(0xFF121212))),
            surface = Brush.verticalGradient(listOf(Color(0xFF1E1E1E), Color(0xFF151515)))
        )
    } else {
        EvenizerGradients(
            primary = Brush.verticalGradient(listOf(Color(0xFF9C27B0), Color(0xFF7B1FA2))),
            secondary = Brush.verticalGradient(listOf(Color(0xFF4CAF50), Color(0xFF388E3C))),
            tertiary = Brush.verticalGradient(listOf(Color(0xFF2196F3), Color(0xFF1976D2))),
            background = Brush.verticalGradient(listOf(Color(0xFFFDFCFE), Color(0xFFF3E5F5))),
            surface = Brush.verticalGradient(listOf(Color.White, Color(0xFFF5F5F5)))
        )
    }

    CompositionLocalProvider(LocalGradients provides gradients) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
