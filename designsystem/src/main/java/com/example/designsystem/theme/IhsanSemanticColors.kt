package com.example.designsystem.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Product-specific semantic colors layered on Material roles.
 * Prefer [androidx.compose.material3.MaterialTheme.colorScheme] when a Material role already fits.
 */
@Immutable
data class IhsanSemanticColors(
    val brand: Color,
    val onBrand: Color,
    val surfaceMuted: Color,
    val surfaceMint: Color,
    val textSecondaryMuted: Color,
    val borderSubtle: Color,
    val success: Color,
    val onSuccess: Color,
    val warning: Color,
    val onWarning: Color,
    val charityOffer: Color,
    val charityOfferContainer: Color,
    val charityRequest: Color,
    val charityRequestContainer: Color,
    val progressActive: Color,
    val progressTrack: Color,
    val accentWarm: Color,
    /** Soft circle behind quick-action icons on light surfaces. */
    val quickActionSurface: Color,
    /** Slightly lifted brand surface for active prayer chips on dark headers. */
    val brandElevated: Color,
    /** Third-party WhatsApp brand — intentional exception, do not use as app primary. */
    val whatsapp: Color,
    val favorite: Color
)

val LightIhsanSemanticColors = IhsanSemanticColors(
    brand = PrimaryTeal,
    onBrand = OnPrimaryWhite,
    surfaceMuted = Color(0xFFF9F9F9),
    surfaceMint = Color(0xFFF1F8F6),
    textSecondaryMuted = Neutral500,
    borderSubtle = Neutral200,
    success = Color(0xFF2E7D32),
    onSuccess = OnPrimaryWhite,
    warning = Color(0xFFE65100),
    onWarning = OnPrimaryWhite,
    charityOffer = Color(0xFF2E7D32),
    charityOfferContainer = Color(0xFFE8F5E9),
    charityRequest = Color(0xFFE65100),
    charityRequestContainer = Color(0xFFFFF3E0),
    progressActive = PrimaryTeal,
    progressTrack = PrimaryTeal.copy(alpha = 0.12f),
    accentWarm = Color(0xFFC66927),
    quickActionSurface = Color(0xFFF3F6F5),
    brandElevated = Color(0xFF0C4036),
    whatsapp = Color(0xFF25D366),
    favorite = Color(0xFFE91E63)
)

val DarkIhsanSemanticColors = IhsanSemanticColors(
    brand = PrimaryTeal,
    onBrand = OnPrimaryWhite,
    surfaceMuted = Neutral800,
    surfaceMint = PrimaryTeal.copy(alpha = 0.25f),
    textSecondaryMuted = Neutral400,
    borderSubtle = Neutral700,
    success = Color(0xFF81C784),
    onSuccess = Neutral900,
    warning = Color(0xFFFFB74D),
    onWarning = Neutral900,
    charityOffer = Color(0xFF81C784),
    charityOfferContainer = Color(0xFF1B5E20).copy(alpha = 0.45f),
    charityRequest = Color(0xFFFFB74D),
    charityRequestContainer = Color(0xFFE65100).copy(alpha = 0.35f),
    progressActive = Primary200,
    progressTrack = PrimaryTeal.copy(alpha = 0.35f),
    accentWarm = Color(0xFFFFB74D),
    quickActionSurface = Neutral800,
    brandElevated = Color(0xFF0C4036),
    whatsapp = Color(0xFF25D366),
    favorite = Color(0xFFF48FB1)
)

val LocalIhsanColors = staticCompositionLocalOf { LightIhsanSemanticColors }
