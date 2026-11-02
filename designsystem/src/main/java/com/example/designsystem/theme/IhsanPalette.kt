package com.example.designsystem.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color

/** Stable persisted names. Brand artwork and Quran paper are independent of appearance. */
enum class IhsanPalette(val title: String, val accent: Color, val nightAccent: Color, val gold: Color) {
    EMERALD("زمرد وذهب", Color(0xFF086B56), Color(0xFF6DDBC0), Color(0xFFC4A568)),
    SAND("رمل ونحاس", Color(0xFF854527), Color(0xFFF1B695), Color(0xFFB7855B)),
    MIDNIGHT("أزرق ليلي وذهب", Color(0xFF284E80), Color(0xFFA6CAFF), Color(0xFFC4A568));

    companion object {
        fun fromStorage(value: String?): IhsanPalette = entries.firstOrNull { it.name == value } ?: EMERALD
    }
}

internal fun ColorScheme.withPalette(palette: IhsanPalette, dark: Boolean): ColorScheme {
    val fill = if (dark) when (palette) {
        IhsanPalette.EMERALD -> DarkInteractivePrimary
        IhsanPalette.SAND -> Color(0xFF92512E)
        IhsanPalette.MIDNIGHT -> Color(0xFF365F92)
    } else palette.accent
    val container = if (dark) when (palette) {
        IhsanPalette.EMERALD -> Color(0xFF234239)
        IhsanPalette.SAND -> Color(0xFF4E3427)
        IhsanPalette.MIDNIGHT -> Color(0xFF253B55)
    } else when (palette) {
        IhsanPalette.EMERALD -> Color(0xFFE2F2EB)
        IhsanPalette.SAND -> Color(0xFFF6E8DE)
        IhsanPalette.MIDNIGHT -> Color(0xFFE6EDF7)
    }
    return copy(primary = fill, onPrimary = Color.White,
        primaryContainer = container, onPrimaryContainer = if (dark) DarkHeading else palette.accent,
        secondary = if (dark) palette.nightAccent else palette.accent,
        onSecondary = if (dark) DarkBackground else Color.White,
        secondaryContainer = container, onSecondaryContainer = if (dark) DarkHeading else palette.accent,
        tertiary = if (dark) Color(0xFFE4C78E) else Color(0xFF785B25),
        onTertiary = if (dark) DarkBackground else Color.White,
        tertiaryContainer = if (dark) DarkSurfaceWarm else Color(0xFFF5EBDD),
        onTertiaryContainer = if (dark) DarkHeading else Color(0xFF523D1A))
}

fun ihsanSemanticColors(palette: IhsanPalette, dark: Boolean): IhsanSemanticColors {
    val base = if (dark) DarkIhsanSemanticColors else LightIhsanSemanticColors
    val fill = when (palette) {
        IhsanPalette.EMERALD -> if (dark) DarkInteractivePrimary else palette.accent
        IhsanPalette.SAND -> if (dark) Color(0xFF92512E) else palette.accent
        IhsanPalette.MIDNIGHT -> if (dark) Color(0xFF365F92) else palette.accent
    }
    val accent = if (dark) palette.nightAccent else palette.accent
    val selected = if (dark) when (palette) {
        IhsanPalette.EMERALD -> Color(0xFF234239)
        IhsanPalette.SAND -> Color(0xFF4E3427)
        IhsanPalette.MIDNIGHT -> Color(0xFF253B55)
    } else fill
    return base.copy(surfaceMint = if (dark) base.surfaceMint else when (palette) {
        IhsanPalette.EMERALD -> Color(0xFFF1F8F6)
        IhsanPalette.SAND -> Color(0xFFF7F0E9)
        IhsanPalette.MIDNIGHT -> Color(0xFFF0F3F8)
    }, brand = fill, accentContent = accent,
        brandElevated = androidx.compose.ui.graphics.lerp(palette.accent, Color.Black, .38f),
        selectedContainer = selected, selectedContent = Color.White,
        navigationIndicator = accent, fieldFocusedBorder = accent,
        progressActive = accent, progressTrack = accent.copy(alpha = .16f),
        goldAccent = palette.gold)
}
