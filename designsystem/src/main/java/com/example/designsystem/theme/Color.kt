package com.example.designsystem.theme

import androidx.compose.ui.graphics.Color

// Primary Palette from Global Style Guide
val Primary50 = Color(0xFFF2FBFC)
val Primary100 = Color(0xFFE6F7F9)
val Primary200 = Color(0xFFC2EDF1)
val Primary300 = Color(0xFF9EE3E9)
val Primary400 = Color(0xFF7AD9E1)
val Primary500 = Color(0xFF56CFD9)
val Primary600 = Color(0xFF45A6AE)
val Primary700 = Color(0xFF347C82)
val Primary800 = Color(0xFF225357)
val Primary900 = Color(0xFF11292B)

// Neutral Palette from Global Style Guide
val Neutral50 = Color(0xFFF9FAFB)
val Neutral100 = Color(0xFFF3F4F6)
val Neutral200 = Color(0xFFE5E7EB)
val Neutral300 = Color(0xFFD1D5DB)
val Neutral400 = Color(0xFF9CA3AF)
val Neutral500 = Color(0xFF6B7280)
val Neutral600 = Color(0xFF4B5563)
val Neutral700 = Color(0xFF374151)
val Neutral800 = Color(0xFF1F2937)
val Neutral900 = Color(0xFF111827)

// Brand fill for large green surfaces (heroes, emphasized cards) — same in Light/Dark.
val PrimaryTeal = Color(0xFF073028)
val OnPrimaryWhite = Color(0xFFFFFFFF)
val PrimaryContainerTeal = Primary100
val OnPrimaryContainerTeal = Primary900

val SecondaryTeal = Primary200
val OnSecondaryTeal = Primary800
val SecondaryContainerTeal = Primary50
val OnSecondaryContainerTeal = Primary900

val TertiaryCream = Color(0xFFFCF8F5)
val OnTertiaryTeal = Primary700
val TertiaryContainerWhite = Color(0xFFFFFFFF)

val NeutralGrey = Neutral500
val NeutralVariantGrey = Neutral100

val ErrorRed = Color(0xFFBA1A1A)
val OnErrorWhite = Color(0xFFFFFFFF)
val ErrorContainerLight = Color(0xFFFFDAD6)
val OnErrorContainerLight = Color(0xFF410002)
val ErrorContainerDark = Color(0xFF93000A)
val OnErrorContainerDark = Color(0xFFFFDAD6)

val BackgroundCream = Neutral50
val OnBackgroundDark = Neutral900

val SurfaceWhite = Color(0xFFFFFFFF)
val OnSurfaceDark = Neutral900
val SurfaceVariantLight = Neutral50
val OnSurfaceVariantGrey = Neutral500

val OutlineGrey = Neutral300
val OutlineVariantLight = Neutral200

// Dark Mode — neutral charcoal / near-black foundation.
// The brand green is used as an *Accent* (interactive / decorative) only;
// surfaces, backgrounds and dividers are intentionally desaturated so the
// dark theme does not look like a "darkened green theme".
val DarkBackground = Color(0xFF101012)        // near-black canvas
val DarkSurface = Color(0xFF161618)           // raised sheet on canvas
val DarkSurfaceElevated = Color(0xFF1C1C1F)   // higher card (settings, support)
val DarkSurfaceMuted = Color(0xFF202123)      // tonal fill, icon halos, inputs
val DarkSurfaceWarm = Color(0xFF2A2722)       // soft warm accent surface
val DarkOnSurface = Color(0xFFE9EAEC)         // primary text
val DarkOnSurfaceVariant = Color(0xFFA8ABB1)  // secondary text
val DarkInteractivePrimary = Color(0xFF7DD3B0) // brand mint accent on dark
val DarkInteractivePrimaryContainer = Color(0xFF1F3A30)
val OnDarkInteractivePrimary = Color(0xFF06251F)
val DarkOutline = Color(0xFF414449)
val DarkOutlineVariant = Color(0xFF2A2B2F)    // very subtle dividers
val DarkScrim = Color(0x99000000)             // black 50-60%
val DarkBrandElevated = Color(0xFF1F3A30)     // used for tonal brand fills, not chrome
val DarkGoldAccent = Color(0xFFC5A66A)          // muted gold accent (5-10% visual budget)

// Legacy / Utility Colors
val White = Color(0xFFFFFFFF)
val Dark = Neutral900
val Grey = Neutral300
