package com.suraksha.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color


// Define the dark color scheme using the colors from Color.kt
private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentBlue,
    tertiary = AccentBlue,
    background = DarkBackground,
    surface = DarkBackground, // The main canvas background
    onPrimary = Black,        // Text on blue buttons
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,     // Text on the main background
    onSurface = White,        // Text on surfaces
    error = UrgentRed,
    onError = White
)

// A standard light theme (not used by default, but good to have)
private val LightColorScheme = lightColorScheme(
    primary = FluorescentBlue,
    secondary = AccentBlue,
    tertiary = AccentBlue,
    background = LightBackground,
    surface = LightSurface,
    onPrimary = Black,
    onSecondary = White,
    onTertiary = White,
    onBackground = Color(0xFF1B1B1B),
    onSurface = Color(0xFF1B1B1B),
    error = UrgentRed,
    onError = White
)

@Composable
fun SurakshaTheme(
    // Force dark theme as it's our app's design
    darkTheme: Boolean = ThemeStore.isDark.value,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }

    // The enableEdgeToEdge() call in MainActivity now handles the system bars.
    // This theme composable can be simplified.

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography, // This comes from Type.kt
        content = content
    )
}
