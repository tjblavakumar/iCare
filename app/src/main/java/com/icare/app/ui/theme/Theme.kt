package com.icare.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SoothingBlue,
    onPrimary = Color.White,
    primaryContainer = SoothingBlueLight,
    onPrimaryContainer = SoothingBlueDark,
    secondary = SoftSky,
    onSecondary = Color.White,
    secondaryContainer = SoftSkyLight,
    onSecondaryContainer = SoftSkyDark,
    background = CoolWhite,
    onBackground = DarkCharcoal,
    surface = Color.White,
    onSurface = DarkCharcoal,
    surfaceVariant = LightGrey,
    onSurfaceVariant = MediumGrey,
    error = BadRed,
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = SoothingBlueLight,
    onPrimary = DarkCharcoal,
    primaryContainer = SoothingBlueDark,
    onPrimaryContainer = SoothingBlueLight,
    secondary = SoftSkyLight,
    onSecondary = DarkCharcoal,
    secondaryContainer = SoftSkyDark,
    onSecondaryContainer = SoftSkyLight,
    background = DarkBackground,
    onBackground = LightText,
    surface = DarkSurface,
    onSurface = LightText,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = MutedText,
    error = BadRed,
    onError = Color.White
)

@Composable
fun ICareTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    textSizeScale: TextSizeScale = TextSizeScale.NORMAL,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = createTypography(textSizeScale.factor)
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
