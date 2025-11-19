package com.suraksha.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentBlue,
    secondary = AccentBlue,
    tertiary = AccentBlue,
    background = DarkBackground,
    surface = DarkBackground,
    onPrimary = Black,
    onSecondary = Black,
    onTertiary = Black,
    onBackground = White,
    onSurface = White,
    error = UrgentRed,
    onError = White
)

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

    darkTheme: Boolean = ThemeStore.isDark.value,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        DarkColorScheme
    } else {
        LightColorScheme
    }



    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
