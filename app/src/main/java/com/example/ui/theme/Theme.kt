package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = CyanPrimaryDark,
    onPrimary = Color(0xFF00363F),
    primaryContainer = CyanPrimaryContainer,
    onPrimaryContainer = OnCyanPrimaryContainer,
    secondary = VioletSecondaryDark,
    onSecondary = Color(0xFF1E0A4E),
    secondaryContainer = VioletSecondaryContainer,
    onSecondaryContainer = Color(0xFFEDE9FE),
    tertiary = AmberTertiaryDark,
    onTertiary = Color(0xFF451A03),
    tertiaryContainer = AmberTertiaryContainer,
    onTertiaryContainer = Color(0xFFFEF3C7),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant
)

private val LightColorScheme = lightColorScheme(
    primary = CyanPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFCFFAFE),
    onPrimaryContainer = Color(0xFF083344),
    secondary = VioletSecondary,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFEDE9FE),
    onSecondaryContainer = Color(0xFF2E1065),
    tertiary = AmberTertiary,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFEF3C7),
    onTertiaryContainer = Color(0xFF451A03),
    background = LightBackground,
    onBackground = LightOnSurface,
    surface = LightSurface,
    onSurface = LightOnSurface,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightOnSurfaceVariant
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to sleek media dark theme for media master experience
    dynamicColor: Boolean = false, // Keep consistent stylish branding
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
