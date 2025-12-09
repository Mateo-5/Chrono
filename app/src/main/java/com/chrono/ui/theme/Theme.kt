package com.chrono.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
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
    textScale: Float = 1.0f,
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
    
    // Create scaled typography
    val scaledTypography = Typography(
        displayLarge = TextStyle(fontSize = (57 * textScale).sp, fontWeight = FontWeight.Normal),
        displayMedium = TextStyle(fontSize = (45 * textScale).sp, fontWeight = FontWeight.Normal),
        displaySmall = TextStyle(fontSize = (36 * textScale).sp, fontWeight = FontWeight.Normal),
        headlineLarge = TextStyle(fontSize = (32 * textScale).sp, fontWeight = FontWeight.Normal),
        headlineMedium = TextStyle(fontSize = (28 * textScale).sp, fontWeight = FontWeight.Normal),
        headlineSmall = TextStyle(fontSize = (24 * textScale).sp, fontWeight = FontWeight.Normal),
        titleLarge = TextStyle(fontSize = (22 * textScale).sp, fontWeight = FontWeight.Normal),
        titleMedium = TextStyle(fontSize = (16 * textScale).sp, fontWeight = FontWeight.Medium),
        titleSmall = TextStyle(fontSize = (14 * textScale).sp, fontWeight = FontWeight.Medium),
        bodyLarge = TextStyle(fontSize = (16 * textScale).sp, fontWeight = FontWeight.Normal),
        bodyMedium = TextStyle(fontSize = (14 * textScale).sp, fontWeight = FontWeight.Normal),
        bodySmall = TextStyle(fontSize = (12 * textScale).sp, fontWeight = FontWeight.Normal),
        labelLarge = TextStyle(fontSize = (14 * textScale).sp, fontWeight = FontWeight.Medium),
        labelMedium = TextStyle(fontSize = (12 * textScale).sp, fontWeight = FontWeight.Medium),
        labelSmall = TextStyle(fontSize = (11 * textScale).sp, fontWeight = FontWeight.Medium)
    )

    MaterialTheme(
        colorScheme = colorScheme,
        typography = scaledTypography,
        content = content
    )
}
