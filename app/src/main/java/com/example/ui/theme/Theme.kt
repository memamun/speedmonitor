package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.model.ThemeMode

private val DarkColorScheme =
    darkColorScheme(
        primary = DarkAccentPurple,
        onPrimary = OnAccentPurple,
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = PurpleGrey80,
        onSecondary = DarkBackground,
        surface = DarkSurface,
        onSurface = DarkTextPrimary,
        surfaceVariant = DarkSurfaceVariant,
        onSurfaceVariant = DarkTextSecondary,
        background = DarkBackground,
        onBackground = DarkTextPrimary,
        outline = Color(0xFF4A4458),
        outlineVariant = Color(0xFF383545),
        error = StatusRed
    )

private val LightColorScheme =
    lightColorScheme(
        primary = LightAccentPurple,
        onPrimary = OnLightAccentPurple,
        primaryContainer = Color(0xFFEADDFF),
        onPrimaryContainer = Color(0xFF21005D),
        secondary = Color(0xFF625B71),
        onSecondary = Color(0xFFFFFFFF),
        surface = LightSurface,
        onSurface = LightTextPrimary,
        surfaceVariant = LightSurfaceVariant,
        onSurfaceVariant = LightTextSecondary,
        background = LightBackground,
        onBackground = LightTextPrimary,
        outline = Color(0xFF79747E),
        outlineVariant = Color(0xFFD3D0DC),
        error = Color(0xFFB3261E)
    )

@Composable
fun MyApplicationTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit,
) {
    val isDark = when (themeMode) {
        ThemeMode.SYSTEM -> isSystemInDarkTheme()
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
    }

    val colorScheme = if (isDark) DarkColorScheme else LightColorScheme

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}

