package com.example.feature.quran.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.designsystem.theme.PrimaryTeal
import com.example.feature.quran.domain.model.Reader
import com.example.feature.quran.domain.model.Surah
import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.domain.usecase.QuranDownloadStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Redesigned Quran Reader Screen matching the visual reference design.
 * Features:
 * - Clean RTL Top Bar: Back arrow + "رجوع", centered Surah title with "الآية X من Y" pill chip,
 *   action icons (`Aa`, Audio Reciter, Bookmark).
 * - Mushaf Page Frame with ornate borders, cartouche Surah header, and calligraphic Basmala.
 * - Clean Verse presentation with 8-pointed star Ayah badges, removing redundant action buttons.
 * - Bottom Audio Mini Player matching reference mockup with real Media3 audio state.
 */
@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
fun QuranReaderScreen(
    state: QuranUiState,
    onBack: () -> Unit,
    onRetry: () -> Unit,
    onSaveLastRead: (Int, Int) -> Unit,
    onToggleBookmark: (Int, Int) -> Unit,
    onTogglePlay: () -> Unit,
    onPlayNext: () -> Unit,
    onPlayPrevious: () -> Unit,
    onPlayAyah: (Int) -> Unit = {},
    onSelectReader: (Reader) -> Unit = {},
    onDownloadSurah: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onUpdateFontSize: (Float) -> Unit
) {
    val listState = rememberLazyListState()
    var selectedVerseForTafsir by remember { mutableStateOf<Verse?>(null) }
    var activeActionVerse by remember { mutableStateOf<Verse?>(null) }
    var showFontSettings by remember { mutableStateOf(false) }
    var showReaderPicker by remember { mutableStateOf(false) }
    var readyToTrackLastRead by remember { mutableStateOf(false) }
    val ayahs by rememberUpdatedState(state.ayahs)
    val saveLastRead by rememberUpdatedState(onSaveLastRead)

    val selectedSurah = state.selectedSurah
    val hasBasmala = remember(selectedSurah?.id) {
        selectedSurah?.id != null && selectedSurah.id != 9 && selectedSurah.id != 1
    }
    val headerOffset = if (hasBasmala) 2 else 1

    LaunchedEffect(state.ayahs, state.initialAyahScroll) {
        readyToTrackLastRead = false
        if (state.ayahs.isNotEmpty()) {
            if (state.initialAyahScroll != null) {
                val index = state.ayahs.indexOfFirst { it.verseNumber == state.initialAyahScroll }
                if (index != -1) {
                    listState.scrollToItem(index + headerOffset)
                }
            }
            readyToTrackLastRead = true
        }
    }

    LaunchedEffect(state.currentPlayingAyah, state.isPlaying) {
        if (state.isPlaying && state.currentPlayingAyah != null && state.ayahs.isNotEmpty()) {
            val index = state.ayahs.indexOfFirst { it.verseNumber == state.currentPlayingAyah }
            if (index != -1) {
                listState.animateScrollToItem(index + headerOffset)
            }
        }
    }

    LaunchedEffect(listState, readyToTrackLastRead, headerOffset) {
        if (!readyToTrackLastRead) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(350)
            .filter { ayahs.isNotEmpty() }
            .collect { firstVisible ->
                val ayahIndex = firstVisible - headerOffset
                if (ayahIndex in ayahs.indices) {
                    val verse = ayahs[ayahIndex]
                    saveLastRead(verse.surahId, verse.verseNumber)
                }
            }
    }

    if (showReaderPicker) {
        ReaderPickerSheet(
            readers = state.availableReaders,
            selected = state.selectedReader,
            onSelect = {
                onSelectReader(it)
                showReaderPicker = false
            },
            onDismiss = { showReaderPicker = false }
        )
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = Color(0xFFFCF8F5),
            topBar = {
                TopAppBar(
                    modifier = Modifier.statusBarsPadding(),
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = selectedSurah?.let { "سورة ${it.name}" } ?: "تحميل...",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                            val current = state.currentPlayingAyah
                                ?: state.lastRead?.takeIf { it.first.id == selectedSurah?.id }?.second
                                ?: 1
                            val total = selectedSurah?.totalVerses ?: state.ayahs.size
                            if (total > 0) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = Color(0xFFE8F2EC),
                                    border = BorderStroke(0.5.dp, PrimaryTeal.copy(alpha = 0.15f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = PrimaryTeal,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "الآية $current من $total",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.SemiBold,
                                            color = PrimaryTeal
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable(onClick = onBack)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = PrimaryTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "رجوع",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                    },
                    actions = {
                        // Aa Font Icon
                        IconButton(onClick = { showFontSettings = !showFontSettings }) {
                            Text(
                                text = "Aa",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = PrimaryTeal
                            )
                        }
                        // Audio Reciter Icon
                        IconButton(onClick = { showReaderPicker = true }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "اختر القارئ",
                                tint = PrimaryTeal
                            )
                        }
                        // Bookmark Action Icon
                        val currentAyahNum = state.currentPlayingAyah ?: 1
                        val isSurahBookmarked = selectedSurah?.let { surah ->
                            state.bookmarks.any { it.surahId == surah.id && it.verseNumber == currentAyahNum }
                        } ?: false

                        IconButton(onClick = {
                            selectedSurah?.let { surah ->
                                onToggleBookmark(surah.id, currentAyahNum)
                            }
                        }) {
                            Icon(
                                imageVector = if (isSurahBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "علامة مرجعية",
                                tint = PrimaryTeal
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFFFCF8F5)
                    )
                )
            },
            bottomBar = {
                Column {
                    AnimatedVisibility(visible = showFontSettings) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFFFAF4EB),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, Color(0xFFE8E0D5))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "حجم الخط",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                                Slider(
                                    value = state.fontSize,
                                    onValueChange = onUpdateFontSize,
                                    valueRange = 20f..48f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = PrimaryTeal,
                                        activeTrackColor = PrimaryTeal,
                                        inactiveTrackColor = PrimaryTeal.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                                )
                                Text(
                                    text = "${state.fontSize.toInt()}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PrimaryTeal
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = state.ayahs.isNotEmpty(),
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it }
                    ) {
                        AudioBar(
                            isPlaying = state.isPlaying,
                            playbackPosition = state.playbackPosition,
                            playbackDuration = state.playbackDuration,
                            surahName = selectedSurah?.name ?: "",
                            readerName = state.selectedReader?.name ?: "",
                            onTogglePlay = onTogglePlay,
                            onNext = onPlayNext,
                            onPrevious = onPlayPrevious,
                            onSeek = onSeekTo,
                            onExpandReader = { showReaderPicker = true }
                        )
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(Color(0xFFFCF8F5))
            ) {
                QuranDownloadStatusPanel(state = state, onRetry = onDownloadSurah)
                Box(modifier = Modifier.fillMaxWidth().weight(1f)) {
                    when {
                        state.isLoading && state.ayahs.isEmpty() -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = PrimaryTeal
                            )
                        }
                        state.errorMessage != null && state.ayahs.isEmpty() -> {
                            Column(
                                modifier = Modifier.align(Alignment.Center).padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = state.errorMessage.orEmpty(),
                                    color = MaterialTheme.colorScheme.error,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = onRetry) { Text("إعادة المحاولة") }
                            }
                        }
                        state.ayahs.isEmpty() -> {
                            Text("لا توجد آيات", modifier = Modifier.align(Alignment.Center))
                        }
                        else -> {
                            // Mushaf Page Outer Frame
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .border(
                                        border = BorderStroke(1.2.dp, Color(0xFFD8CEB8)),
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .background(Color(0xFFFAF7F0), RoundedCornerShape(16.dp))
                                    .padding(horizontal = 12.dp, vertical = 12.dp)
                            ) {
                                LazyColumn(
                                    state = listState,
                                    modifier = Modifier.fillMaxSize(),
                                    contentPadding = PaddingValues(bottom = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    // Item 0: Cartouche Surah Banner Header
                                    item(key = "surah_header") {
                                        selectedSurah?.let { surah ->
                                            SurahHeaderBanner(surah = surah)
                                        }
                                    }

                                    // Item 1 (Optional): Calligraphic Basmala
                                    if (hasBasmala) {
                                        item(key = "basmala_header") {
                                            BasmalaHeader()
                                        }
                                    }

                                    // Clean Verse List
                                    itemsIndexed(
                                        items = state.ayahs,
                                        key = { _, verse -> "verse_${verse.surahId}_${verse.verseNumber}" }
                                    ) { _, verse ->
                                        VerseItem(
                                            verse = verse,
                                            isPlaying = state.currentPlayingAyah == verse.verseNumber,
                                            fontSize = state.fontSize,
                                            onClick = {
                                                onPlayAyah(verse.verseNumber)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            selectedVerseForTafsir?.let { verse ->
                TafsirBottomSheet(
                    verse = verse,
                    surahName = selectedSurah?.name ?: "",
                    onDismiss = { selectedVerseForTafsir = null }
                )
            }

            activeActionVerse?.let { verse ->
                VerseQuickActionSheet(
                    verse = verse,
                    isBookmarked = state.bookmarks.any { it.surahId == verse.surahId && it.verseNumber == verse.verseNumber },
                    onBookmarkToggle = {
                        onToggleBookmark(verse.surahId, verse.verseNumber)
                        activeActionVerse = null
                    },
                    onTafsirClick = {
                        selectedVerseForTafsir = verse
                        activeActionVerse = null
                    },
                    onPlayClick = {
                        onPlayAyah(verse.verseNumber)
                        activeActionVerse = null
                    },
                    onDismiss = { activeActionVerse = null }
                )
            }
        }
    }
}

@Composable
private fun QuranDownloadStatusPanel(
    state: QuranUiState,
    onRetry: () -> Unit
) {
    val label = when {
        state.isSurahDownloaded -> "تم تنزيل السورة وأصبحت متاحة دون اتصال"
        state.downloadStatus == QuranDownloadStatus.Enqueued -> "التنزيل في الانتظار وسيبدأ عند توفر الشبكة"
        state.downloadStatus == QuranDownloadStatus.Running -> "جارٍ تنزيل السورة: ${state.downloadProgress}%"
        state.downloadStatus == QuranDownloadStatus.Succeeded -> "اكتمل العمل، جارٍ التحقق من الملفات المنزلة"
        state.downloadStatus == QuranDownloadStatus.Failed ->
            state.downloadErrorMessage ?: "تعذر تنزيل السورة"
        state.downloadStatus == QuranDownloadStatus.Cancelled -> "أُلغي تنزيل السورة"
        else -> null
    } ?: return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (state.downloadStatus == QuranDownloadStatus.Failed) {
            MaterialTheme.colorScheme.errorContainer
        } else {
            Color(0xFFE8F2EC)
        }
    ) {
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    color = PrimaryTeal
                )
                if (state.downloadStatus == QuranDownloadStatus.Failed ||
                    state.downloadStatus == QuranDownloadStatus.Cancelled
                ) {
                    TextButton(onClick = onRetry) { Text("إعادة") }
                }
            }
        }
    }
}

/** Ornate Cartouche Header Banner for Surah Name (Matching Reference Design) */
@Composable
fun SurahHeaderBanner(
    surah: Surah,
    modifier: Modifier = Modifier
) {
    val goldColor = Color(0xFFC5A059)
    val tealColor = PrimaryTeal

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFFAF3EA),
        border = BorderStroke(1.2.dp, goldColor.copy(alpha = 0.5f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(2.dp)
                .border(
                    width = 0.8.dp,
                    color = goldColor.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(14.dp)
                )
                .padding(horizontal = 16.dp, vertical = 14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "سُورَةُ ${surah.name}",
                style = IhsanTheme.readingTypography.quran.copy(
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                    color = tealColor
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

/** Calligraphic Basmala Banner Header */
@Composable
fun BasmalaHeader(
    modifier: Modifier = Modifier
) {
    val tealColor = PrimaryTeal

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "بِسْمِ ٱللَّهِ ٱلرَّحْمَـٰنِ ٱلرَّحِيمِ",
            style = IhsanTheme.readingTypography.quran.copy(
                fontSize = 28.sp,
                fontWeight = FontWeight.Normal,
                color = tealColor
            ),
            textAlign = TextAlign.Center
        )
    }
}

/** Traditional Custom Ayah Emblem Badge (Matching Reference Star Medallion) */
@Composable
fun AyahNumberBadge(
    number: Int,
    modifier: Modifier = Modifier,
    isHighlighted: Boolean = false
) {
    val goldColor = Color(0xFFC5A059)
    val tealColor = PrimaryTeal
    val strokeColor = if (isHighlighted) tealColor else goldColor
    val arabicNum = remember(number) { toArabicNumerals(number) }

    Box(
        modifier = modifier.size(38.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val outerRadius = size.minDimension / 2 - 1.5.dp.toPx()
            val innerRadius = outerRadius * 0.76f
            val points = 8
            val path = Path()

            for (i in 0 until points * 2) {
                val radius = if (i % 2 == 0) outerRadius else innerRadius
                val angle = (i * Math.PI / points) - Math.PI / 2
                val x = center.x + (radius * cos(angle)).toFloat()
                val y = center.y + (radius * sin(angle)).toFloat()
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            path.close()

            drawPath(
                path = path,
                color = if (isHighlighted) tealColor.copy(alpha = 0.15f) else Color(0xFFFAF5ED)
            )
            drawPath(
                path = path,
                color = strokeColor,
                style = Stroke(width = 1.5.dp.toPx())
            )
            drawCircle(
                color = strokeColor.copy(alpha = 0.35f),
                radius = innerRadius * 0.82f,
                style = Stroke(width = 1.dp.toPx())
            )
        }
        Text(
            text = arabicNum,
            style = MaterialTheme.typography.labelMedium.copy(
                fontSize = if (number > 99) 10.sp else 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) tealColor else Color(0xFF1B2C2A)
            )
        )
    }
}

/** Clean Mushaf Verse Line (Matching Reference Text Presentation) */
@Composable
fun VerseItem(
    verse: Verse,
    isPlaying: Boolean,
    fontSize: Float,
    onClick: () -> Unit
) {
    val tealColor = PrimaryTeal

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .animateContentSize(),
        shape = RoundedCornerShape(12.dp),
        color = if (isPlaying) tealColor.copy(alpha = 0.08f) else Color.Transparent,
        border = BorderStroke(
            width = if (isPlaying) 1.2.dp else 0.dp,
            color = if (isPlaying) tealColor.copy(alpha = 0.4f) else Color.Transparent
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Verse Text
            Text(
                text = verse.text,
                style = IhsanTheme.readingTypography.quran.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.8).sp,
                    textAlign = TextAlign.Justify,
                    color = if (isPlaying) tealColor else Color(0xFF1B1B1E)
                ),
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Ayah Badge Ornament
            AyahNumberBadge(
                number = verse.verseNumber,
                isHighlighted = isPlaying
            )
        }
    }
}

/** Quick Action Sheet on Verse Interaction */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseQuickActionSheet(
    verse: Verse,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onTafsirClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFFFCF8F5)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "الآية ${toArabicNumerals(verse.verseNumber)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryTeal
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(onClick = onPlayClick) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("تشغيل")
                }
                Button(onClick = onBookmarkToggle) {
                    Icon(if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(if (isBookmarked) "محفوظة" else "حفظ")
                }
                Button(onClick = onTafsirClick) {
                    Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text("التفسير")
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

/** Bottom Media3 Audio Player Bar Matching Reference Design */
@Composable
fun AudioBar(
    isPlaying: Boolean,
    playbackPosition: Long,
    playbackDuration: Long,
    surahName: String,
    readerName: String,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    onExpandReader: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color(0xFFFAFAF7),
        shadowElevation = 12.dp,
        border = BorderStroke(1.dp, Color(0xFFECE4D8)),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Top Info Row: Surah Name & Reciter (Right RTL) + Expand Chevron (Left)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.clickable(onClick = onExpandReader)
                ) {
                    Text(
                        text = if (surahName.isNotBlank()) "سورة $surahName" else "القرآن الكريم",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryTeal
                    )
                    Text(
                        text = readerName.ifBlank { "الشيخ عبد الرحمن السديس" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onExpandReader) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "القارئ",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Player Controls & Progress Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Controls Group (Left side in RTL)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onPrevious,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "السابق",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Surface(
                        onClick = onTogglePlay,
                        shape = CircleShape,
                        color = PrimaryTeal,
                        shadowElevation = 4.dp,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNext,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "التالي",
                            tint = PrimaryTeal,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Time Slider & Duration Text (Right side in RTL)
                Column(modifier = Modifier.weight(1f)) {
                    Slider(
                        value = playbackPosition.toFloat(),
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..(if (playbackDuration > 0) playbackDuration.toFloat() else 1f),
                        colors = SliderDefaults.colors(
                            thumbColor = PrimaryTeal,
                            activeTrackColor = PrimaryTeal,
                            inactiveTrackColor = PrimaryTeal.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.height(20.dp)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = formatTime(playbackPosition),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = formatTime(playbackDuration),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReaderPickerSheet(
    readers: List<Reader>,
    selected: Reader?,
    onSelect: (Reader) -> Unit,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFFFCF8F5)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "اختر القارئ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PrimaryTeal
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(readers) { reader ->
                    val isSelected = reader.id == selected?.id
                    Surface(
                        onClick = { onSelect(reader) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            PrimaryTeal.copy(alpha = 0.12f)
                        } else {
                            Color(0xFFFAF4EB)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) PrimaryTeal else Color(0xFFE8E0D5)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = reader.name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) PrimaryTeal else Color(0xFF1B1B1E)
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "محدد",
                                    tint = PrimaryTeal
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.US, "%d:%02d", minutes, seconds)
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
