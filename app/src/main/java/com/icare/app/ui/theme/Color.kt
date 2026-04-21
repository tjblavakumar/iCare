package com.icare.app.ui.theme

import androidx.compose.ui.graphics.Color

// Primary - Soothing Blue
val SoothingBlue = Color(0xFF5B9BD5)
val SoothingBlueLight = Color(0xFF8BB8E8)
val SoothingBlueDark = Color(0xFF3D7AB8)

// Secondary - Soft Sky Blue
val SoftSky = Color(0xFF87CEEB)
val SoftSkyLight = Color(0xFFB0E0F0)
val SoftSkyDark = Color(0xFF5FAFD7)

// Clickable text color - darker blue for better contrast
val ClickableBlue = Color(0xFF1E6BB8)

// Legacy aliases for compatibility
val WarmCoral = SoothingBlue
val WarmCoralLight = SoothingBlueLight
val WarmCoralDark = SoothingBlueDark
val SoftTeal = SoftSky
val SoftTealLight = SoftSkyLight
val SoftTealDark = SoftSkyDark

// Status colors
val HappyGreen = Color(0xFF2ECC71)
val LowAmber = Color(0xFFF39C12)
val BadRed = Color(0xFFE74C3C)
val InactiveGrey = Color(0xFFBDC3C7)

// Backgrounds - Light Mode
val CoolWhite = Color(0xFFF8FBFF)
val WarmWhite = CoolWhite  // Legacy alias
val LightGrey = Color(0xFFF0F4F8)
val MediumGrey = Color(0xFF7F8C9A)

// Backgrounds - Dark Mode
val DarkBackground = Color(0xFF121820)
val DarkSurface = Color(0xFF1E2630)
val DarkSurfaceVariant = Color(0xFF2A3440)
val LightText = Color(0xFFFFFFFF)  // Pure white for better contrast
val MutedText = Color(0xFFB0BEC5)  // Lighter muted text for readability

// Shared
val DarkCharcoal = Color(0xFF2C3E50)

// Universal Card Colors (work on both light and dark themes)
val CardBackground = Color(0xFFFFFFFF)  // White cards always
val CardTextPrimary = Color(0xFF1A1A1A)  // Near black for primary text
val CardTextSecondary = Color(0xFF666666)  // Dark gray for secondary text
