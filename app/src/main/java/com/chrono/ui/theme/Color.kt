package com.chrono.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary colors - minimalistic black and white
val PureBlack = Color(0xFF000000)
val PureWhite = Color(0xFFFFFFFF)
val OffWhite = Color(0xFFF5F5F5)
val LightGray = Color(0xFFE0E0E0)
val MediumGray = Color(0xFF9E9E9E)
val DarkGray = Color(0xFF424242)
val CharcoalGray = Color(0xFF212121)

// Text colors
val TextPrimary = PureWhite
val TextSecondary = MediumGray
val TextDark = CharcoalGray
val TextMuted = MediumGray

// Surface colors
val SurfaceCard = CharcoalGray
val SurfaceCardLight = DarkGray

// Accent - subtle gray instead of blue
val AccentBlue = Color(0xFFBDBDBD)  // Light gray accent
val GlowBlue = Color(0xFF757575)    // Medium gray for subtle highlights

// Background gradient - solid black for minimalism
val BackgroundGradient = Brush.verticalGradient(
    colors = listOf(
        PureBlack,
        CharcoalGray
    )
)
