package com.example.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.graphics.toArgb
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ColorScrimLight = androidx.compose.ui.graphics.Color(0x52000000)

private val DarkColorScheme = darkColorScheme(
    primary = DarkInteractivePrimary,
    onPrimary = OnDarkInteractivePrimary,
    primaryContainer = DarkInteractivePrimaryContainer,
    onPrimaryContainer = DarkHeading,
    secondary = DarkAccentMint,
    onSecondary = DarkBackground,
    secondaryContainer = DarkInteractivePrimaryContainer,
    onSecondaryContainer = DarkOnSurface,
    tertiary = DarkSurfaceWarm,
    onTertiary = DarkOnSurface,
    tertiaryContainer = DarkSurfaceElevated,
    onTertiaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkHeading,
    surface = DarkSurface,
    surfaceDim = DarkBackground,
    surfaceBright = DarkSurfaceMuted,
    surfaceTint = Color.Transparent,
    inverseSurface = DarkOnSurface,
    inverseOnSurface = DarkBackground,
    inversePrimary = PrimaryTeal,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceMuted,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = DarkError,
    onError = DarkBackground,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    scrim = DarkScrim,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceMuted,
    surfaceContainerHighest = DarkSurfaceMuted
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryTeal,
    onPrimary = OnPrimaryWhite,
    primaryContainer = PrimaryContainerTeal,
    onPrimaryContainer = OnPrimaryContainerTeal,
    secondary = SecondaryTeal,
    onSecondary = OnSecondaryTeal,
    secondaryContainer = SecondaryContainerTeal,
    onSecondaryContainer = OnSecondaryContainerTeal,
    tertiary = TertiaryCream,
    onTertiary = OnTertiaryTeal,
    tertiaryContainer = TertiaryContainerWhite,
    onTertiaryContainer = OnPrimaryContainerTeal,
    background = BackgroundCream,
    onBackground = OnBackgroundDark,
    surface = SurfaceWhite,
    onSurface = OnSurfaceDark,
    surfaceVariant = SurfaceVariantLight,
    onSurfaceVariant = OnSurfaceVariantGrey,
    outline = OutlineGrey,
    outlineVariant = OutlineVariantLight,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerLight,
    onErrorContainer = OnErrorContainerLight,
    scrim = ColorScrimLight,
    surfaceContainerLowest = SurfaceWhite,
    surfaceContainerLow = Neutral50,
    surfaceContainer = SurfaceWhite,
    surfaceContainerHigh = Neutral100,
    surfaceContainerHighest = Neutral200
)

object IhsanTheme {
    val isDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalIhsanDarkTheme.current
    val spacing: Spacing
        @Composable
        @ReadOnlyComposable
        get() = LocalSpacing.current

    val colors: IhsanSemanticColors
        @Composable
        @ReadOnlyComposable
        get() = LocalIhsanColors.current

    val dimens: IhsanDimens
        @Composable
        @ReadOnlyComposable
        get() = LocalIhsanDimens.current

    val readingTypography: ReadingTypography
        @Composable
        @ReadOnlyComposable
        get() = LocalReadingTypography.current
}

private val LocalIhsanDarkTheme = compositionLocalOf { false }

/** Ghost actions need readable foregrounds independently of the emerald filled-button role. */
@Composable
fun ihsanOutlinedButtonColors() = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
    contentColor = if (IhsanTheme.isDark) IhsanTheme.colors.textBody else MaterialTheme.colorScheme.primary,
    disabledContentColor = if (IhsanTheme.isDark) IhsanTheme.colors.textDisabled else MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
)

@Composable
fun ihsanTextButtonColors() = androidx.compose.material3.ButtonDefaults.textButtonColors(
    contentColor = if (IhsanTheme.isDark) IhsanTheme.colors.textBody else MaterialTheme.colorScheme.primary,
    disabledContentColor = if (IhsanTheme.isDark) IhsanTheme.colors.textDisabled else MaterialTheme.colorScheme.onSurface.copy(alpha = .38f)
)

// One window owner prevents the global theme and image headers from competing.
internal val LocalIhsanHeroCoverage = compositionLocalOf<MutableState<Boolean>?> { null }

private val IhsanShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(20.dp),
    extraLarge = RoundedCornerShape(28.dp)
)

@Composable
fun IhsanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && !darkTheme && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val semanticColors = if (darkTheme) DarkIhsanSemanticColors else LightIhsanSemanticColors

    val view = LocalView.current
    val heroCoverage = remember { mutableStateOf(false) }
    val lightStatusBarIcons = !darkTheme // Light skyline headers use dark foreground; dark headers use light foreground.
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            // Transparent status bar for seamless edge-to-edge header blending
            val systemSurface = semanticColors.surfaceBase
            window.statusBarColor = Color.Transparent.toArgb()
            window.navigationBarColor = systemSurface.toArgb()
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = lightStatusBarIcons
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalIhsanColors provides semanticColors,
        LocalIhsanDimens provides IhsanDimens(),
        LocalReadingTypography provides ReadingTypography(),
        LocalIhsanDarkTheme provides darkTheme,
        LocalIhsanHeroCoverage provides heroCoverage
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            shapes = IhsanShapes,
            content = content
        )
    }
}
