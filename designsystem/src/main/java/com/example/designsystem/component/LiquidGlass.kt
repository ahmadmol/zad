package com.example.designsystem.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme

/**
 * Liquid Glass — restrained Material 3 surface treatment.
 *
 * Three pre-tuned recipes (`subtle`, `tinted`, `elevated`) that combine:
 *   1. a low-alpha vertical sheen (the "light passing through glass" effect),
 *   2. a 1px hairline border in the brand color to keep the surface visible
 *      on top of the green hero,
 *   3. an optional tonal fill for cards on the white body.
 *
 * Intentionally NOT a heavy `BlurEffect`: we want hierarchy and contrast to
 * stay sharp, especially for Arabic/English text on the prayer strip and the
 * Asma card.
 */
enum class LiquidGlassStyle { Subtle, Tinted, Elevated }

/**
 * Apply a Liquid Glass surface to the current modifier.
 *
 * Call AFTER `.clip(shape)` and BEFORE content so the hairline sits on the
 * rounded edge, not inside it. When [cornerRadius] is `0.dp` no clip is
 * applied (use this for list items that already have their own shape).
 */
@Composable
fun Modifier.liquidGlass(
    style: LiquidGlassStyle = LiquidGlassStyle.Subtle,
    shape: RoundedCornerShape = RoundedCornerShape(20.dp),
    cornerRadius: Dp = 20.dp,
    brandTint: Color = MaterialTheme.colorScheme.primary
): Modifier {
    val topAlpha: Float
    val bottomAlpha: Float
    val borderAlpha: Float
    val fillAlpha: Float
    when (style) {
        LiquidGlassStyle.Subtle -> {
            topAlpha = 0.04f
            bottomAlpha = 0.01f
            borderAlpha = 0.08f
            fillAlpha = 0.0f
        }
        LiquidGlassStyle.Tinted -> {
            topAlpha = 0.10f
            bottomAlpha = 0.04f
            borderAlpha = 0.18f
            fillAlpha = 0.06f
        }
        LiquidGlassStyle.Elevated -> {
            topAlpha = 0.06f
            bottomAlpha = 0.0f
            borderAlpha = 0.12f
            fillAlpha = 0.0f
        }
    }

    val sheen = Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = topAlpha),
            Color.White.copy(alpha = bottomAlpha)
        )
    )
    val effectiveShape = if (cornerRadius > 0.dp) RoundedCornerShape(cornerRadius) else shape
    val fill = if (fillAlpha > 0f) {
        Brush.verticalGradient(
            colors = listOf(
                brandTint.copy(alpha = fillAlpha),
                brandTint.copy(alpha = fillAlpha * 0.5f)
            )
        )
    } else null

    return this
        .let { m -> if (cornerRadius > 0.dp) m.clip(effectiveShape) else m }
        .let { m -> if (fill != null) m.background(fill) else m }
        .background(sheen)
        .border(
            BorderStroke(
                width = 1.dp,
                color = if (style == LiquidGlassStyle.Tinted) {
                    Color.White.copy(alpha = borderAlpha)
                } else {
                    IhsanTheme.colors.borderSubtle.copy(alpha = borderAlpha)
                }
            ),
            shape = effectiveShape
        )
}

/**
 * Soft tonal fill used on white-body cards. Replaces a handful of inline
 * `Brush.linearGradient(listOf(surface, primary.copy(alpha = 0.055f)))`
 * blocks so the calm tone is consistent across the Home screen.
 */
@Composable
fun softBrandTonalFill(): Brush = Brush.linearGradient(
    colors = listOf(
        IhsanTheme.colors.surfaceElevated,
        MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
    )
)
