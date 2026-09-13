package com.example.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp

private val UiFamily = FontFamily.Default

private fun uiStyle(weight: FontWeight, size: Int, lineHeight: Int) = TextStyle(
    fontFamily = UiFamily,
    fontWeight = weight,
    fontSize = size.sp,
    lineHeight = lineHeight.sp,
    letterSpacing = 0.sp
)

/** Arabic-first type scale. Arabic UI text should not inherit Latin letter spacing. */
val Typography = Typography(
    displayLarge = uiStyle(FontWeight.Bold, 48, 56),
    displayMedium = uiStyle(FontWeight.Bold, 40, 48),
    displaySmall = uiStyle(FontWeight.Bold, 34, 42),
    headlineLarge = uiStyle(FontWeight.Bold, 30, 38),
    headlineMedium = uiStyle(FontWeight.Bold, 26, 34),
    headlineSmall = uiStyle(FontWeight.SemiBold, 23, 30),
    titleLarge = uiStyle(FontWeight.SemiBold, 21, 28),
    titleMedium = uiStyle(FontWeight.SemiBold, 17, 24),
    titleSmall = uiStyle(FontWeight.Medium, 15, 22),
    bodyLarge = uiStyle(FontWeight.Normal, 17, 27),
    bodyMedium = uiStyle(FontWeight.Normal, 15, 23),
    bodySmall = uiStyle(FontWeight.Normal, 13, 20),
    labelLarge = uiStyle(FontWeight.SemiBold, 14, 20),
    labelMedium = uiStyle(FontWeight.Medium, 12, 18),
    labelSmall = uiStyle(FontWeight.Medium, 11, 16)
)

@Immutable
data class ReadingTypography(
    val quran: TextStyle = TextStyle(
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 52.sp,
        letterSpacing = 0.sp,
        textAlign = TextAlign.Center
    ),
    val devotional: TextStyle = Typography.bodyLarge.copy(lineHeight = 30.sp),
    val source: TextStyle = Typography.bodySmall
)

val LocalReadingTypography = staticCompositionLocalOf { ReadingTypography() }
