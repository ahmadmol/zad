package com.example.designsystem.component

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import android.graphics.BitmapFactory
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.res.imageResource
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.R

private const val KAABA_LOGO_INLINE_ID = "ihsan_kaaba_logo_inline"

/**
 * Standardized Header Title / Subtitle component that renders the word "Ihsan" / "إحسان" / "الإحسان"
 * using the official Kaaba icon version of the brand wordmark.
 *
 * Ensures high contrast with the header background (dark text/tint on light background, light on dark).
 */
@Composable
fun IhsanTextWithKaabaIcon(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    style: TextStyle = TextStyle.Default,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    textAlign: TextAlign? = null,
    logoHeight: Dp? = null
) {
    val trimmed = text.trim()
    val isExactBrandName = trimmed == "إحسان" || trimmed == "Ihsan" || trimmed == "Ehsan"

    if (isExactBrandName) {
        val targetHeight = logoHeight ?: if (style.fontSize != TextUnit.Unspecified && style.fontSize.value > 0) {
            (style.fontSize.value * 1.5f).dp
        } else {
            32.dp
        }
        Image(
            painter = ihsanWordmarkPainter(color),
            contentDescription = text,
            modifier = modifier.height(targetHeight),
            contentScale = ContentScale.Fit
        )
    } else {
        val fontSizeSp = if (style.fontSize != TextUnit.Unspecified && style.fontSize.value > 0) {
            style.fontSize.value
        } else {
            16f
        }

        val (annotatedString, hasLogoReplacement) = buildAnnotatedHeaderString(text)

        if (hasLogoReplacement) {
            val inlineContent = mapOf(
                KAABA_LOGO_INLINE_ID to InlineTextContent(
                    Placeholder(
                        width = (fontSizeSp * 2.2f).sp,
                        height = (fontSizeSp * 1.15f).sp,
                        placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                    )
                ) {
                    Image(
                        painter = ihsanWordmarkPainter(color),
                        contentDescription = "إحسان",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
            )

            Text(
                text = annotatedString,
                modifier = modifier,
                color = color,
                style = style,
                maxLines = maxLines,
                overflow = overflow,
                textAlign = textAlign,
                inlineContent = inlineContent
            )
        } else {
            Text(
                text = text,
                modifier = modifier,
                color = color,
                style = style,
                maxLines = maxLines,
                overflow = overflow,
                textAlign = textAlign
            )
        }
    }
}

private fun buildAnnotatedHeaderString(text: String): Pair<AnnotatedString, Boolean> {
    val targets = listOf("الإحسان", "إحسان", "Ihsan", "Ehsan")
    var foundIndex = -1
    var matchedTarget = ""

    for (target in targets) {
        val idx = text.indexOf(target)
        if (idx >= 0) {
            foundIndex = idx
            matchedTarget = target
            break
        }
    }

    if (foundIndex < 0) {
        return Pair(AnnotatedString(text), false)
    }

    val annotated = buildAnnotatedString {
        append(text.substring(0, foundIndex))
        appendInlineContent(KAABA_LOGO_INLINE_ID, alternateText = matchedTarget)
        append(text.substring(foundIndex + matchedTarget.length))
    }

    return Pair(annotated, true)
}

private val wordmarkCache = mutableMapOf<Boolean, ImageBitmap>()

@Composable
private fun ihsanWordmarkPainter(@Suppress("UNUSED_PARAMETER") color: Color): BitmapPainter {
    val context = LocalContext.current
    val wordmarkColor = if (com.example.designsystem.theme.IhsanTheme.isDark) Color(0xFFE9F5EF) else Color(0xFF086B56)
    val goldColor = Color(0xFFC7A35C)
    val dark = com.example.designsystem.theme.IhsanTheme.isDark
    val bitmap = remember(context, dark) {
        wordmarkCache[dark] ?: run {
        val source = BitmapFactory.decodeResource(context.resources, R.drawable.splash_ihsan_logo_transparent,
            BitmapFactory.Options().apply { inScaled = false })
        val output = source.copy(android.graphics.Bitmap.Config.ARGB_8888, true)
        val pixels = IntArray(output.width * output.height)
        output.getPixels(pixels, 0, output.width, 0, 0, output.width, output.height)
        val ink = android.graphics.Color.rgb((wordmarkColor.red * 255).toInt(), (wordmarkColor.green * 255).toInt(), (wordmarkColor.blue * 255).toInt())
        for (i in pixels.indices) {
            val pixel = pixels[i]
            val r = android.graphics.Color.red(pixel)
            val g = android.graphics.Color.green(pixel)
            val b = android.graphics.Color.blue(pixel)
            // Neutral wordmark pixels follow the theme; the original gold symbol stays gold.
            val isGold = r > g && g > b && r - b > 45
            val x = i % output.width
            val y = i / output.width
            val isKaaba = x in (output.width * 733 / 1254)..(output.width * 949 / 1254) &&
                y in (output.height * 574 / 1254)..(output.height * 745 / 1254) && r < 145 && g < 155 && b < 145
            if (isKaaba && !isGold && android.graphics.Color.alpha(pixel) > 0) {
                val shade = ((r + g + b) / 3 + if (dark) 24 else 0).coerceIn(0, 110)
                pixels[i] = (pixel and 0xff000000.toInt()) or android.graphics.Color.rgb(shade, shade, shade)
            } else if (!isGold && !isKaaba && android.graphics.Color.alpha(pixel) > 0)
                pixels[i] = (pixel and 0xff000000.toInt()) or (ink and 0x00ffffff)
            else if (dark && isGold && android.graphics.Color.alpha(pixel) > 0)
                pixels[i] = (pixel and 0xff000000.toInt()) or android.graphics.Color.rgb(
                    (goldColor.red * 255).toInt(), (goldColor.green * 255).toInt(), (goldColor.blue * 255).toInt())
        }
        output.setPixels(pixels, 0, output.width, 0, 0, output.width, output.height)
        source.recycle()
        val cropped = android.graphics.Bitmap.createBitmap(output, output.width * 183 / 1254, output.height * 475 / 1254,
            output.width * 863 / 1254, output.height * 308 / 1254)
        output.recycle()
        cropped.asImageBitmap().also { wordmarkCache[dark] = it }
        }
    }
    return remember(bitmap) { BitmapPainter(bitmap) }
}
