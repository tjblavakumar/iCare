package com.icare.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Text size scale factors
enum class TextSizeScale(val factor: Float, val label: String) {
    NORMAL(1.0f, "Normal"),
    LARGE(1.2f, "Large"),
    EXTRA_LARGE(1.4f, "Extra Large")
}

// Create typography with scale factor
fun createTypography(scale: Float = 1.0f) = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = (34 * scale).sp,
        lineHeight = (42 * scale).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = (30 * scale).sp,
        lineHeight = (38 * scale).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = (26 * scale).sp,
        lineHeight = (34 * scale).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.SemiBold,
        fontSize = (22 * scale).sp,
        lineHeight = (30 * scale).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = (18 * scale).sp,
        lineHeight = (26 * scale).sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = (18 * scale).sp,
        lineHeight = (26 * scale).sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = (16 * scale).sp,
        lineHeight = (24 * scale).sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = (16 * scale).sp,
        lineHeight = (22 * scale).sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = (14 * scale).sp,
        lineHeight = (20 * scale).sp
    )
)

// Default typography (larger than before)
val Typography = createTypography(1.0f)
