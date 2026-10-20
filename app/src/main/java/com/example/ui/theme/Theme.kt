package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BCutDarkColorScheme = darkColorScheme(
    primary = UltraPurple,
    onPrimary = Color.White,
    primaryContainer = CardDark2,
    onPrimaryContainer = UltraCyan,
    secondary = UltraCyan,
    onSecondary = Color(0xFF002025),
    secondaryContainer = CardDark2,
    onSecondaryContainer = UltraCyan,
    tertiary = UltraPink,
    onTertiary = Color.White,
    background = BgDark,
    onBackground = TextPrimary,
    surface = CardDark,
    onSurface = TextPrimary,
    surfaceVariant = CardDark2,
    onSurfaceVariant = TextSecondary,
    outline = CardBorder
)

@Composable
fun BCutUltraTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = BCutDarkColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    BCutUltraTheme(content = content)
}
