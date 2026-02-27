package org.shadow.project.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = AccentGreen,
    onPrimary = BotBackground,
    secondary = AccentBlue,
    tertiary = AccentYellow,
    background = BotBackground,
    surface = BotSurface,
    surfaceVariant = BotSurfaceVariant,
    error = AccentRed,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onSurfaceVariant = TextSecondary
)
@Composable
fun KDrainTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = BotTypography,
        content = content
    )
}
