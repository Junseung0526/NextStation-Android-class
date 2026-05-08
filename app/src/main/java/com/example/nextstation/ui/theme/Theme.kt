package com.example.nextstation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Clean Professional Palette
val PrimaryBlue = Color(0xFF1D4ED8)
val SecondaryBlue = Color(0xFF3B82F6)
val BackgroundLight = Color(0xFFF1F5F9)
val BackgroundDark = Color(0xFF0F172A)
val SurfaceLight = Color.White
val SurfaceDark = Color(0xFF1E293B)
val TextPrimaryLight = Color(0xFF0F172A)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryLight = Color(0xFF475569)
val TextSecondaryDark = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = SecondaryBlue,
    secondary = Color(0xFF64748B),
    background = BackgroundDark,
    surface = SurfaceDark,
    onPrimary = Color.White,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,
    onSurfaceVariant = TextSecondaryDark
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryBlue,
    secondary = Color(0xFF64748B),
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onBackground = TextPrimaryLight,
    onSurface = TextPrimaryLight,
    onSurfaceVariant = TextSecondaryLight
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
