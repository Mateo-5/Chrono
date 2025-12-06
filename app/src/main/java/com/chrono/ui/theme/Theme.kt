package com.chrono.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MinimalistDarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = PureBlack,
    primaryContainer = DarkGray,
    onPrimaryContainer = PureWhite,
    secondary = MediumGray,
    onSecondary = PureBlack,
    secondaryContainer = CharcoalGray,
    onSecondaryContainer = LightGray,
    tertiary = LightGray,
    onTertiary = PureBlack,
    background = PureBlack,
    onBackground = PureWhite,
    surface = CharcoalGray,
    onSurface = PureWhite,
    surfaceVariant = DarkGray,
    onSurfaceVariant = LightGray,
    outline = MediumGray,
    outlineVariant = DarkGray
)

@Composable
fun ChronoTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = MinimalistDarkColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = PureBlack.toArgb()
            @Suppress("DEPRECATION")
            window.navigationBarColor = PureBlack.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = com.chrono.ui.theme.Typography,
        content = content
    )
}
