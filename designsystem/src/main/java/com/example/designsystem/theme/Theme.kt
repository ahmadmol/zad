package com.example.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ColorScrimLight = androidx.compose.ui.graphics.Color(0x52000000)

private val DarkColorScheme = darkColorScheme(
    primary = DarkInteractivePrimary,
    onPrimary = OnDarkInteractivePrimary,
    primaryContainer = DarkInteractivePrimaryContainer,
    onPrimaryContainer = DarkInteractivePrimary,
    secondary = DarkInteractivePrimary.copy(alpha = 0.85f),
    onSecondary = OnDarkInteractivePrimary,
    secondaryContainer = DarkSurfaceMuted,
    onSecondaryContainer = DarkOnSurface,
    tertiary = DarkSurfaceWarm,
    onTertiary = DarkOnSurface,
    tertiaryContainer = DarkSurfaceElevated,
    onTertiaryContainer = DarkOnSurface,
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceMuted,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = ErrorRed,
    onError = OnErrorWhite,
    errorContainer = ErrorContainerDark,
    onErrorContainer = OnErrorContainerDark,
    scrim = DarkScrim,
    surfaceContainerLowest = DarkBackground,
    surfaceContainerLow = DarkSurface,
    surfaceContainer = DarkSurfaceElevated,
    surfaceContainerHigh = DarkSurfaceMuted,
    surfaceContainerHighest = DarkOutlineVariant
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
}

@Composable
fun IhsanTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val semanticColors = if (darkTheme) DarkIhsanSemanticColors else LightIhsanSemanticColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window ?: return@SideEffect
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    CompositionLocalProvider(
        LocalSpacing provides Spacing(),
        LocalIhsanColors provides semanticColors,
        LocalIhsanDimens provides IhsanDimens()
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}
