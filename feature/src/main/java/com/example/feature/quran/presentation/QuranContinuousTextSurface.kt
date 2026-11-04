package com.example.feature.quran.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.designsystem.theme.QuranInk
import com.example.feature.R
import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.model.Verse
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filterNotNull
import kotlin.math.abs

/**
 * Continuous, highly readable Quran text surface with scalable typography.
 * Allows effortless reading with dynamic font sizing, pinch-to-zoom font scaling,
 * inline ayah rosettes, surah header cartouche, calligraphic Bismillah, and interactive verse selection.
 */
@OptIn(FlowPreview::class)
@Composable
fun QuranContinuousTextSurface(
    surah: Surah,
    ayahs: List<Verse>,
    fontSizeSp: Float,
    activeSurahId: Int?,
    activeVerseNumber: Int?,
    onVerseClick: (Verse) -> Unit,
    onVisibleAyahChanged: (surahId: Int, ayahNumber: Int) -> Unit,
    modifier: Modifier = Modifier,
    initialAyah: Int = 1,
    requestKey: String = "${surah.id}:$initialAyah",
    onZoomFontSize: (Float) -> Unit = {},
    onToggleChrome: () -> Unit = {}
) {
    val isDark = IhsanTheme.isDark
    val font = remember { FontFamily(Font(R.font.amiri_quran)) }
    val textColor = if (isDark) IhsanTheme.colors.textPrimary else QuranInk
    val cardBg = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFF7F3EB)
    val cardBorder = if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE5DDD0)

    val listState = rememberLazyListState()
    val currentFontSize by rememberUpdatedState(fontSizeSp)
    val updateFontSize by rememberUpdatedState(onZoomFontSize)
    val toggleChrome by rememberUpdatedState(onToggleChrome)
    val visibleAyahCallback by rememberUpdatedState(onVisibleAyahChanged)

    // Jump to initial ayah once per requestKey + surah.id
    val scrollAnchorKey = "$requestKey:${surah.id}"
    var scrolledKey by rememberSaveable { mutableStateOf("") }
    val initialScrollDone = scrolledKey == scrollAnchorKey

    LaunchedEffect(ayahs.isNotEmpty(), scrollAnchorKey, initialAyah) {
        if (ayahs.isNotEmpty() && scrolledKey != scrollAnchorKey) {
            val targetIndex = (initialAyah - 1).coerceIn(0, ayahs.lastIndex)
            val offset = if (surah.id != 9 && surah.id != 1) 2 else 1
            val scrollIndex = targetIndex + offset
            listState.scrollToItem(scrollIndex)
            scrolledKey = scrollAnchorKey
        }
    }

    // Track visible ayah ONLY after initial scroll is completed and when the user scrolls
    LaunchedEffect(listState, ayahs, initialScrollDone, surah.id) {
        if (!initialScrollDone || ayahs.isEmpty()) return@LaunchedEffect
        var initialEmissionSkipped = false
        snapshotFlow {
            val visibleIndex = listState.firstVisibleItemIndex
            val offset = if (surah.id != 9 && surah.id != 1) 2 else 1
            val verseIndex = (visibleIndex - offset).coerceIn(0, ayahs.lastIndex)
            ayahs.getOrNull(verseIndex)
        }
            .filterNotNull()
            .distinctUntilChanged { a, b -> a.surahId == b.surahId && a.verseNumber == b.verseNumber }
            .debounce(350)
            .collect { verse ->
                if (!initialEmissionSkipped) {
                    initialEmissionSkipped = true
                    if (verse.verseNumber == initialAyah) return@collect
                }
                visibleAyahCallback(verse.surahId, verse.verseNumber)
            }
    }

    // Multi-touch pinch-to-zoom font scaling without interfering with vertical single-finger scroll
    val pinchZoomModifier = Modifier.pointerInput(surah.id) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            do {
                val event = awaitPointerEvent()
                if (event.changes.count { it.pressed } >= 2) {
                    val zoomChange = event.calculateZoom()
                    if (abs(zoomChange - 1f) > 0.02f) {
                        val nextSize = (currentFontSize * zoomChange).coerceIn(20f, 48f)
                        updateFontSize(nextSize)
                        event.changes.forEach { if (it.positionChanged()) it.consume() }
                    }
                }
            } while (event.changes.any { it.pressed })
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier
            .fillMaxSize()
            .then(pinchZoomModifier)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Surah Header Cartouche
        item(key = "surah_header_${surah.id}") {
            Surface(
                onClick = toggleChrome,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = cardBg,
                border = BorderStroke(1.dp, cardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "سُورَةُ ${surah.name}",
                        fontFamily = font,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDark) IhsanTheme.colors.accentContent else Color(0xFF1C5242),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${surah.revelationType} • ${surah.totalVerses} آية",
                        style = MaterialTheme.typography.bodyMedium,
                        color = IhsanTheme.colors.textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 2. Bismillah Header (except Surah At-Tawbah and Al-Fatihah where ayah 1 is Bismillah)
        if (surah.id != 9 && surah.id != 1) {
            item(key = "bismillah_${surah.id}") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ",
                        fontFamily = font,
                        fontSize = (fontSizeSp * 1.05f).sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // 3. Flowing verses with interactive highlighting and tap gestures
        items(
            count = ayahs.size,
            key = { index -> "${ayahs[index].surahId}:${ayahs[index].verseNumber}" }
        ) { index ->
            val verse = ayahs[index]
            val isSelected = verse.surahId == activeSurahId && verse.verseNumber == activeVerseNumber
            val highlightBg = if (isSelected) {
                if (isDark) IhsanTheme.colors.selectedContainer else Color(0xFFE8F3EE)
            } else Color.Transparent

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onVerseClick(verse) }
                    .semantics {
                        customActions = listOf(
                            CustomAccessibilityAction("خيارات الآية ${verse.verseNumber}") {
                                onVerseClick(verse)
                                true
                            }
                        )
                    },
                shape = RoundedCornerShape(12.dp),
                color = highlightBg,
                border = if (isSelected) BorderStroke(1.dp, IhsanTheme.colors.accentContent.copy(alpha = 0.5f)) else null
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append(verse.text)
                            append(" ")
                            withStyle(
                                SpanStyle(
                                    color = if (isDark) IhsanTheme.colors.accentContent else Color(0xFF1C5242),
                                    fontWeight = FontWeight.Bold
                                )
                            ) {
                                append("\u06DD${toArabicNumerals(verse.verseNumber)}")
                            }
                        },
                        fontFamily = font,
                        fontSize = fontSizeSp.sp,
                        lineHeight = (fontSizeSp * 1.65f).sp,
                        color = textColor,
                        textAlign = TextAlign.Justify,
                        style = TextStyle(textDirection = TextDirection.Rtl),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

/** Utility to convert Digits to Arabic Numerals */
fun toArabicNumerals(number: Int): String {
    val arabicDigits = charArrayOf('٠', '١', '٢', '٣', '٤', '٥', '٦', '٧', '٨', '٩')
    val str = number.toString()
    val builder = StringBuilder()
    for (ch in str) {
        if (ch in '0'..'9') {
            builder.append(arabicDigits[ch - '0'])
        } else {
            builder.append(ch)
        }
    }
    return builder.toString()
}
