package com.example.feature.quran.presentation

import android.content.Context
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.AbsoluteAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.TextUnit
import com.example.designsystem.theme.QuranInk
import com.example.designsystem.theme.QuranDecorativeGreen
import com.example.feature.R
import com.example.feature.quran.domain.model.Verse
import org.json.JSONArray
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer

/** Presentation assets only. IDs are matched against the application's existing Quran text. */
internal data class MushafPage(val number: Int, val verses: List<Verse>, val names: Map<Int, String>)

internal fun loadMushafPages(context: Context): List<MushafPage> {
    val full = JSONArray(context.assets.open("quran_full.json").bufferedReader().use { it.readText() })
    val starts = JSONArray(context.assets.open("mushaf_page_starts.json").bufferedReader().use { it.readText() })
    val verses = mutableListOf<Verse>()
    val names = mutableMapOf<Int, String>()
    for (s in 0 until full.length()) {
        val surah = full.getJSONObject(s)
        val id = surah.getInt("id")
        names[id] = surah.getString("name")
        val ayahs = surah.getJSONArray("verses")
        for (v in 0 until ayahs.length()) {
            val ayah = ayahs.getJSONObject(v)
            verses += Verse(verses.size + 1, id, ayah.getInt("id"), ayah.getString("text"))
        }
    }
    val offsets = (0 until starts.length()).map { p ->
        val key = starts.getJSONArray(p)
        verses.indexOfFirst { it.surahId == key.getInt(0) && it.verseNumber == key.getInt(1) }.also { require(it >= 0) }
    } + verses.size
    require(starts.length() == 604 && verses.size == 6236 && offsets.zipWithNext().all { it.first < it.second })
    return (0 until 604).map { MushafPage(it + 1, verses.subList(offsets[it], offsets[it + 1]).toList(), names) }
}

internal fun List<MushafPage>.pageFor(surahId: Int, verseNumber: Int): Int =
    indexOfFirst { page -> page.verses.any { it.surahId == surahId && it.verseNumber == verseNumber } }.coerceAtLeast(0)

/** A full native text page, fitted as a single paragraph. No per-verse cards or vertical list. */
@Composable
internal fun MushafPageSurface(page: MushafPage, requestedFontSize: Float,
    activeSurahId: Int?, activeVerseNumber: Int?, onVerse: (Verse) -> Unit, modifier: Modifier = Modifier) {
    val ink = QuranInk
    val font = remember { FontFamily(Font(R.font.amiri_quran)) }
    val zoom = (requestedFontSize / 28f).coerceIn(.7f, 2f)
    var pan by remember(page.number, zoom) { mutableStateOf(Offset.Zero) }
    // Use the original 1024 × 1536 artwork unchanged. All content coordinates
    // are relative to this fitted page, so the frame never stretches or crops.
    BoxWithConstraints(modifier.clipToBounds(), contentAlignment = Alignment.Center) {
        val pageWidth = minOf(maxWidth, maxHeight * (2f / 3f))
        val pageHeight = pageWidth * 1.5f
        val panLimitX = with(LocalDensity.current) { pageWidth.toPx() } * (zoom - 1f).coerceAtLeast(0f) / 2f
        val panLimitY = with(LocalDensity.current) { pageHeight.toPx() } * (zoom - 1f).coerceAtLeast(0f) / 2f
        val panGesture = if (zoom > 1f) Modifier.pointerInput(page.number, zoom) {
            detectDragGestures { change, delta ->
                change.consume()
                pan = Offset((pan.x + delta.x).coerceIn(-panLimitX, panLimitX), (pan.y + delta.y).coerceIn(-panLimitY, panLimitY))
            }
        } else Modifier
        Box(Modifier.size(pageWidth, pageHeight).then(panGesture).graphicsLayer {
            scaleX = zoom; scaleY = zoom; translationX = pan.x; translationY = pan.y
        }, contentAlignment = AbsoluteAlignment.TopLeft) {
            Image(
                painter = painterResource(R.drawable.quran_page_background),
                contentDescription = null,
                contentScale = ContentScale.Fit,
                modifier = Modifier.matchParentSize()
            )
            Box(
                Modifier.absoluteOffset(x = pageWidth * .28f, y = pageHeight * .080f)
                    .size(pageWidth * .44f, pageHeight * .098f),
                contentAlignment = Alignment.Center
            ) {
                val titleSize = with(LocalDensity.current) { (pageWidth * .067f).toSp() }
                PageCartoucheText("سُورَةُ ${page.names[page.verses.first().surahId].orEmpty()}", font, titleSize, ink)
            }
            BoxWithConstraints(
                Modifier.absoluteOffset(x = pageWidth * .125f, y = pageHeight * .212f)
                    .size(pageWidth * .75f, pageHeight * .662f).clipToBounds(),
                contentAlignment = Alignment.Center
            ) {
                val density = LocalDensity.current
                val measurer = rememberTextMeasurer()
                val text = remember(page, activeSurahId, activeVerseNumber, ink) {
                    buildAnnotatedString {
                        page.verses.forEachIndexed { index, verse ->
                            if (verse.verseNumber == 1 && verse.surahId > 1) {
                                if (index > 0) {
                                    append("\n")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append("سورة ${page.names[verse.surahId]}\n") }
                                }
                                if (verse.surahId != 9) append("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ\n")
                            }
                            pushStringAnnotation("verse", index.toString())
                            withStyle(SpanStyle(color = if (verse.surahId == activeSurahId && verse.verseNumber == activeVerseNumber)
                                QuranDecorativeGreen else ink)) {
                                append(verse.text)
                                append(" \u06DD${verse.verseNumber.toString().map { ('٠'.code + it.digitToInt()).toChar() }.joinToString("")} ")
                            }
                            pop()
                            if (page.number == 1 && index < page.verses.lastIndex) append("\n")
                        }
                    }
                }
                val width = with(density) { maxWidth.roundToPx() }
                val height = with(density) { maxHeight.roundToPx() }
                val fitted = remember(text, width, height, density.fontScale) {
                    val pageScale = (width / density.density / 320f).coerceAtLeast(1f)
                    // Fit once at the approved 100% layout; magnification scales the complete
                    // page instead of silently clamping a font slider to the same fitted size.
                    var size = (28f * pageScale).coerceIn(20f, 64f)
                    while (size > 6f) {
                        val result = measurer.measure(text, style = TextStyle(fontFamily = font, fontSize = size.sp,
                            lineHeight = (size * 1.60f).sp, textDirection = TextDirection.Rtl,
                            textAlign = if (page.number <= 2) TextAlign.Center else TextAlign.Justify),
                            constraints = Constraints(maxWidth = width))
                        if (result.size.height <= height) break
                        size -= .5f
                    }
                    size
                }
                var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
                Text(text, color = ink, fontFamily = font, fontSize = fitted.sp, lineHeight = (fitted * 1.60f).sp,
                    textAlign = if (page.number <= 2) TextAlign.Center else TextAlign.Justify,
                    style = TextStyle(textDirection = TextDirection.Rtl), onTextLayout = { layout = it },
                    modifier = Modifier.fillMaxWidth().pointerInput(page, text) {
                        detectTapGestures(onTap = { point ->
                            layout?.getOffsetForPosition(point)?.let { offset ->
                                text.getStringAnnotations("verse", offset, offset).firstOrNull()?.item?.toIntOrNull()?.let {
                                    onVerse(page.verses[it])
                                }
                            }
                        })
                    })
            }
            Box(
                Modifier.absoluteOffset(x = pageWidth * .447f, y = pageHeight * .904f)
                    .size(pageWidth * .106f, pageHeight * .050f),
                contentAlignment = Alignment.Center
            ) {
                val numberSize = with(LocalDensity.current) { (pageWidth * .043f).toSp() }
                PageCartoucheText(toArabicNumerals(page.number), font, numberSize, ink)
            }
        }
    }
}

/** Fit Amiri's full font metrics as well as longer surah names within the artwork. */
@Composable
private fun PageCartoucheText(text: String, font: FontFamily, nominalSize: TextUnit, ink: Color) {
    BoxWithConstraints(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        val density = LocalDensity.current
        val measurer = rememberTextMeasurer()
        val width = with(density) { maxWidth.roundToPx() }
        val height = with(density) { maxHeight.roundToPx() }
        val fitted = remember(text, width, height, nominalSize, density.fontScale) {
            var size = nominalSize.value
            while (size > 3f) {
                val layout = measurer.measure(text, style = TextStyle(fontFamily = font,
                    fontSize = size.sp, lineHeight = (size * 1.6f).sp, textAlign = TextAlign.Center),
                    constraints = Constraints(maxWidth = width))
                if (layout.size.height <= height && !layout.hasVisualOverflow) break
                size -= .5f
            }
            size.sp
        }
        Text(text, modifier = Modifier.fillMaxWidth(), style = TextStyle(
            fontFamily = font, fontSize = fitted, lineHeight = fitted * 1.6f,
            color = ink, textAlign = TextAlign.Center))
    }
}
