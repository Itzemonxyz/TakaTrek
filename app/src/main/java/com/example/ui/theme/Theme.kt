package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Indigo Color Palette
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF3F51B5), // Indigo 500
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFE8EAF6), // Indigo 50
    onPrimaryContainer = Color(0xFF1A237E), // Indigo 900
    secondary = Color(0xFF5C6BC0), // Indigo 400
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFC5CAE9), // Indigo 100
    onSecondaryContainer = Color(0xFF283593), // Indigo 800
    background = Color(0xFFFFFFFF), // Pure White
    onBackground = Color(0xFF121212),
    surface = Color(0xFFF8F9FA), // Ultra-light gray
    onSurface = Color(0xFF121212),
    surfaceVariant = Color(0xFFE8EAF6),
    onSurfaceVariant = Color(0xFF49454F),
    outline = Color(0xFF79747E),
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFF9DEDC),
    onErrorContainer = Color(0xFF410E0B)
)

private val AmoledDarkColorScheme = darkColorScheme(
    primary = Color(0xFF9FA8DA), // Indigo 200
    onPrimary = Color(0xFF000000), // High Contrast
    primaryContainer = Color(0xFF303F9F), // Indigo 700
    onPrimaryContainer = Color(0xFFFFFFFF),
    secondary = Color(0xFF7986CB), // Indigo 300
    onSecondary = Color(0xFF000000),
    secondaryContainer = Color(0xFF283593), // Indigo 800
    onSecondaryContainer = Color(0xFFFFFFFF),
    background = Color(0xFF000000), // Pitch Black
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF000000), // Pitch Black
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF121212), // Deep grey for cards
    onSurfaceVariant = Color(0xFFCAC4D0),
    outline = Color(0xFF5C6BC0), // High contrast indigo outlines
    error = Color(0xFFF2B8B5),
    onError = Color(0xFF410E0B),
    errorContainer = Color(0xFF8C1D18),
    onErrorContainer = Color(0xFFF9DEDC)
)

@Composable
fun MyApplicationTheme(isAmoledDark: Boolean = false, content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isAmoledDark) AmoledDarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
