package com.example.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Shared dimensional rules. Prefer [IhsanTheme.spacing] for padding rhythm.
 */
@Immutable
data class IhsanDimens(
    val minTouchTarget: Dp = 48.dp,
    val controlHeight: Dp = 56.dp,
    val controlHeightCompact: Dp = 48.dp,
    val iconSmall: Dp = 16.dp,
    val iconMedium: Dp = 24.dp,
    val iconLarge: Dp = 48.dp,
    val radiusSmall: Dp = 8.dp,
    val radiusMedium: Dp = 12.dp,
    val radiusLarge: Dp = 16.dp,
    val radiusPill: Dp = 24.dp,
    val screenHorizontal: Dp = 16.dp,
    val sectionSpacing: Dp = 24.dp
)

val LocalIhsanDimens = staticCompositionLocalOf { IhsanDimens() }
