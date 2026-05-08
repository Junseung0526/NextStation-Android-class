package com.example.nextstation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Trendy Colors
val GlassWhite = Color(0x33FFFFFF)
val GlassBorder = Color(0x66FFFFFF)
val DeepPurple = Color(0xFF6200EE)
val SoftPink = Color(0xFFE91E63)
val DarkBackground = Color(0xFF121212)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    secondary = Color(0xFF03DAC6),
    tertiary = Color(0xFFCF6679),
    background = DarkBackground,
    surface = Color(0xFF1E1E1E)
)

private val LightColorScheme = lightColorScheme(
    primary = DeepPurple,
    secondary = SoftPink,
    tertiary = Color(0xFF018786),
    background = Color(0xFFF5F5F5),
    surface = Color.White
)

@Composable
fun NextStationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
