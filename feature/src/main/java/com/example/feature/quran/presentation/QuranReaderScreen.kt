package com.example.feature.quran.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.produceState
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.quran.domain.model.Reader
import com.example.feature.quran.domain.model.Verse
import com.example.feature.quran.domain.usecase.QuranDownloadStatus
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale

/**
 * Redesigned Quran Reader Screen matching the visual reference design.
 * Features:
 * - Clean Top Bar with Left Back Arrow + "رجوع", Centered Surah Title + "الآية X من Y" pill chip, Right action icons (Bookmark, Speaker, Aa).
 * - Complete original ornamental page artwork with native Quran text.
 * - Surah title and page number aligned with the artwork cartouches.
 * - Centered Calligraphic Bismillah.
 * - Readable native Quran text with inline ayah numbers inside the page frame.
 * - Bottom Audio Mini-Player matching reference mockup with real Media3 playback state.
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
    onUpdateFontSize: (Float) -> Unit,
    onSelectPageSurah: (Int, Int) -> Unit = { _, _ -> }
) {
    val context = LocalContext.current
    var pageLoadError by remember { mutableStateOf<String?>(null) }
    // Lint misses the explicit producer assignment with this Compose/Kotlin version.
    @Suppress("ProduceStateDoesNotAssignValue")
    val pages by produceState<List<MushafPage>>(emptyList(), context) {
        val result = withContext(Dispatchers.IO) { runCatching { loadMushafPages(context) } }
        value = result.getOrElse {
            pageLoadError = "تعذر تحميل صفحات المصحف"
            emptyList()
        }
    }
    val pager = rememberPagerState(pageCount = { pages.size.coerceAtLeast(1) })
    var selectedVerseForTafsir by remember { mutableStateOf<Verse?>(null) }
    var activeActionVerse by remember { mutableStateOf<Verse?>(null) }
    var showFontSettings by remember { mutableStateOf(false) }
    var showReaderPicker by remember { mutableStateOf(false) }
    var playerExpanded by androidx.compose.runtime.saveable.rememberSaveable { mutableStateOf(true) }
    var readyToTrackLastRead by remember { mutableStateOf(false) }
    val selectedSurah = state.selectedSurah
    val currentPage = pages.getOrNull(pager.currentPage)
    val visibleVerse = currentPage?.verses?.firstOrNull()
    val saveLastRead by rememberUpdatedState(onSaveLastRead)
    val selectPageSurah by rememberUpdatedState(onSelectPageSurah)
    val selectedId by rememberUpdatedState(selectedSurah?.id)
    val requestedAyah by rememberUpdatedState(state.initialAyahScroll)
    LaunchedEffect(pages, selectedSurah?.id, state.initialAyahScroll) {
        if (pages.isNotEmpty() && selectedSurah != null) {
            pager.scrollToPage(pages.pageFor(selectedSurah?.id ?: 1, state.initialAyahScroll ?: 1))
            readyToTrackLastRead = true
        }
    }
    LaunchedEffect(state.currentPlayingSurahId, state.currentPlayingAyah, state.isPlaying, pages) {
        if (state.isPlaying && state.currentPlayingAyah != null && pages.isNotEmpty())
            pager.animateScrollToPage(pages.pageFor(state.currentPlayingSurahId ?: selectedSurah?.id ?: 1, state.currentPlayingAyah))
    }
    LaunchedEffect(pager, pages, readyToTrackLastRead) {
        if (!readyToTrackLastRead) return@LaunchedEffect
        snapshotFlow { pager.settledPage }.distinctUntilChanged().debounce(350).collect { index ->
            pages.getOrNull(index)?.let { page ->
                page.verses.firstOrNull { it.surahId == selectedId && it.verseNumber == requestedAyah } ?: page.verses.firstOrNull()
            }?.let { verse ->
                saveLastRead(verse.surahId, verse.verseNumber)
                if (verse.surahId != selectedId) selectPageSurah(verse.surahId, verse.verseNumber)
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

    val isDark = IhsanTheme.isDark
    val bgCanvas = if (isDark) IhsanTheme.colors.surfaceBase else Color(0xFFFAF8F5)
    val topBarTitleColor = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF1C5242)
    val topBarChipBg = if (isDark) IhsanTheme.colors.selectedContainer else Color(0xFFE5F0EB)
    val topBarChipText = if (isDark) IhsanTheme.colors.selectedContent else Color(0xFF1C5242)
    val topBarIconTint = if (isDark) IhsanTheme.colors.selectedContent else Color(0xFF1C5242)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = bgCanvas,
            topBar = {
                // Top Navigation Bar (matching reference mockup)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(bgCanvas)
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(onBack, Modifier.size(48.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "رجوع", tint = topBarIconTint)
                        }

                        // Center Slot: Surah Title + Ayah Pill Chip
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "سورة ${currentPage?.names?.get(visibleVerse?.surahId) ?: selectedSurah?.name ?: "…"}",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 20.sp),
                                fontWeight = FontWeight.Bold,
                                color = topBarTitleColor
                            )
                            val current = state.currentPlayingAyah
                                ?: state.lastRead?.takeIf { it.first.id == selectedSurah?.id }?.second
                                ?: 1
                            val total = selectedSurah?.totalVerses ?: state.ayahs.size
                            if (total > 0) {
                                Surface(
                                    shape = RoundedCornerShape(14.dp),
                                    color = topBarChipBg,
                                    border = BorderStroke(0.5.dp, topBarChipText.copy(alpha = 0.25f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                            contentDescription = null,
                                            tint = topBarChipText,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text(
                                            text = "الصفحة ${pager.currentPage + 1} من 604",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = topBarChipText
                                        )
                                    }
                                }
                            }
                        }

                        // Right Slot: Bookmark, Audio Reciter, Font Size
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(0.dp)
                        ) {
                            val currentAyahNum = visibleVerse?.verseNumber ?: state.currentPlayingAyah ?: 1
                            val isSurahBookmarked = visibleVerse?.let { verse ->
                                state.bookmarks.any { it.surahId == verse.surahId && it.verseNumber == currentAyahNum }
                            } ?: false

                            IconButton(onClick = {
                                visibleVerse?.let { verse ->
                                    onToggleBookmark(verse.surahId, currentAyahNum)
                                }
                            }) {
                                Icon(
                                    imageVector = if (isSurahBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = "علامة مرجعية",
                                    tint = topBarIconTint
                                )
                            }
                            IconButton(onClick = { showReaderPicker = true }) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "اختر القارئ",
                                    tint = topBarIconTint
                                )
                            }
                            IconButton(onClick = { showFontSettings = !showFontSettings }) {
                                Text(
                                    text = "Aa",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = topBarIconTint
                                )
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Column {
                    AnimatedVisibility(visible = showFontSettings) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFFAF4EB),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE8E0D5))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "تكبير الصفحة",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = topBarIconTint
                                )
                                Slider(
                                    value = state.fontSize,
                                    onValueChange = onUpdateFontSize,
                                    valueRange = 20f..48f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = topBarIconTint,
                                        activeTrackColor = topBarIconTint,
                                        inactiveTrackColor = topBarIconTint.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 16.dp)
                                )
                                Text(
                                    text = "${(state.fontSize / 28f * 100f).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = topBarIconTint
                                )
                            }
                        }
                    }
                    AnimatedVisibility(
                        visible = state.ayahs.isNotEmpty(),
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it }
                    ) {
                        if (playerExpanded) AudioBar(
                            isPlaying = state.isPlaying,
                            isBuffering = state.isBuffering,
                            playbackPosition = state.playbackPosition,
                            playbackDuration = state.playbackDuration,
                            surahName = state.surahs.firstOrNull { it.id == state.currentPlayingSurahId }?.name ?: selectedSurah?.name ?: "الفاتحة",
                            readerName = state.selectedReader?.name ?: "قارئ غير محدد",
                            onTogglePlay = onTogglePlay,
                            onNext = onPlayNext,
                            onPrevious = onPlayPrevious,
                            onSeek = onSeekTo,
                            onExpandReader = { playerExpanded = false }
                        )
                        else Surface(color = IhsanTheme.colors.surfaceElevated) {
                            Row(Modifier.fillMaxWidth().navigationBarsPadding(), verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = onTogglePlay) { Icon(if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    if (state.isPlaying) "إيقاف مؤقت" else "تشغيل", tint = IhsanTheme.colors.textPrimary) }
                                Text("مشغل التلاوة مطوي", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textBody)
                                TextButton(onClick = { playerExpanded = true }, colors = com.example.designsystem.theme.ihsanTextButtonColors()) { Text("إظهار المشغل") }
                            }
                        }
                    }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
                    .background(bgCanvas)
            ) {
                QuranDownloadStatusPanel(state = state, onRetry = onDownloadSurah)
                if (state.errorMessage != null && state.ayahs.isNotEmpty()) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(state.errorMessage, Modifier.weight(1f), color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = onTogglePlay, colors = com.example.designsystem.theme.ihsanTextButtonColors()) { Text("إعادة التشغيل") }
                    }
                }
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)) {
                    when {
                        state.isLoading && state.ayahs.isEmpty() -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = topBarIconTint
                            )
                        }
                        state.errorMessage != null && state.ayahs.isEmpty() -> {
                            Column(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .padding(16.dp),
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
                            if (pageLoadError != null) Text(pageLoadError.orEmpty(), modifier = Modifier.align(Alignment.Center))
                            else if (pages.isEmpty()) CircularProgressIndicator(Modifier.align(Alignment.Center))
                            else HorizontalPager(state = pager, modifier = Modifier.fillMaxSize().padding(horizontal = 8.dp, vertical = 4.dp),
                                userScrollEnabled = state.fontSize <= 28f, key = { it }, beyondViewportPageCount = 1) { index ->
                                MushafPageSurface(pages[index], state.fontSize,
                                    state.currentPlayingSurahId, state.currentPlayingAyah,
                                    onVerse = { verse ->
                                        if (verse.surahId != selectedSurah?.id) onSelectPageSurah(verse.surahId, verse.verseNumber)
                                        onSaveLastRead(verse.surahId, verse.verseNumber)
                                        activeActionVerse = state.ayahs.firstOrNull {
                                            it.surahId == verse.surahId && it.verseNumber == verse.verseNumber
                                        } ?: verse
                                    }, modifier = Modifier.fillMaxSize())
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
                val loadedVerse = state.ayahs.firstOrNull { it.surahId == verse.surahId && it.verseNumber == verse.verseNumber }
                val actionVerse = loadedVerse ?: verse
                VerseQuickActionSheet(
                    verse = actionVerse,
                    surahName = currentPage?.names?.get(verse.surahId).orEmpty(),
                    contentReady = selectedSurah?.id == verse.surahId && loadedVerse != null,
                    isBookmarked = state.bookmarks.any { it.surahId == verse.surahId && it.verseNumber == verse.verseNumber },
                    onBookmarkToggle = {
                        onToggleBookmark(verse.surahId, verse.verseNumber)
                        activeActionVerse = null
                    },
                    onTafsirClick = {
                        selectedVerseForTafsir = actionVerse
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
            if (IhsanTheme.isDark) IhsanTheme.colors.surfaceMuted else Color(0xFFE8F2EC)
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
                    color = if (state.downloadStatus == QuranDownloadStatus.Failed) MaterialTheme.colorScheme.onErrorContainer else if (IhsanTheme.isDark) IhsanTheme.colors.textBody else Color(0xFF1C5242)
                )
                if (state.downloadStatus == QuranDownloadStatus.Failed ||
                    state.downloadStatus == QuranDownloadStatus.Cancelled
                ) {
                    TextButton(onClick = onRetry, colors = com.example.designsystem.theme.ihsanTextButtonColors()) { Text("إعادة") }
                }
            }
        }
    }
}

/** Quick Action Sheet on Verse Interaction */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VerseQuickActionSheet(
    verse: Verse,
    surahName: String,
    contentReady: Boolean,
    isBookmarked: Boolean,
    onBookmarkToggle: () -> Unit,
    onTafsirClick: () -> Unit,
    onPlayClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val shareText = "${verse.text}\n\nسورة $surahName (${verse.surahId}:${verse.verseNumber})\nالمصدر: Tanzil Project — Uthmani 1.1، CC BY 3.0، النص دون تعديل\nhttps://tanzil.net"
    val isDark = IhsanTheme.isDark
    val sheetBg = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFFCF8F5)
    val titleColor = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF1C5242)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = sheetBg
    ) {
        Column(modifier = Modifier.heightIn(max = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp * .85f).verticalScroll(rememberScrollState()).padding(20.dp)) {
            Text(
                text = "الآية ${toArabicNumerals(verse.verseNumber)}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Button(onClick = onPlayClick, enabled = contentReady && com.example.feature.quran.util.RecitationAvailability.RECORDINGS_APPROVED, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.Default.PlayArrow, contentDescription = null); Text("تشغيل", fontSize = 12.sp) }
                }
                Button(onClick = onBookmarkToggle, modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null); Text(if (isBookmarked) "محفوظة" else "حفظ", fontSize = 12.sp) }
                }
                Button(onClick = onTafsirClick, enabled = contentReady && !verse.tafsir.isNullOrBlank(), modifier = Modifier.weight(1f), contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) { Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null); Text("التفسير", fontSize = 12.sp) }
                }
            }
            if (verse.tafsir.isNullOrBlank()) Text("التفسير غير متاح في هذه النسخة؛ يلزم مصدر موثق وترخيص.",
                style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary, modifier = Modifier.padding(top = 8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = {
                    (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                        .setPrimaryClip(android.content.ClipData.newPlainText("آية قرآنية", shareText))
                    com.example.feature.core.notification.UserMessageNotifier.notify(context, "نُسخت الآية مع مرجعها")
                }) { Text("نسخ الآية") }
                TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = {
                    runCatching { context.startActivity(android.content.Intent.createChooser(android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"; putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                    }, "مشاركة الآية")) }.onFailure { com.example.feature.core.notification.UserMessageNotifier.notify(context, "تعذر فتح المشاركة") }
                }) { Text("مشاركة الآية") }
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
    isBuffering: Boolean = false,
    onExpandReader: () -> Unit
) {
    val isDark = IhsanTheme.isDark
    val isApproved = com.example.feature.quran.util.RecitationAvailability.RECORDINGS_APPROVED
    val barBg = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFFAF9F6)
    val barBorder = if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFEFE8DD)
    val primaryText = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF1C5242)
    val subtext = if (isDark) IhsanTheme.colors.textSecondary else Color(0xFF72807A)
    val accentColor = if (isDark) IhsanTheme.colors.accentContent else Color(0xFF1C5242)
    val playContainer = if (!isApproved) {
        if (isDark) IhsanTheme.colors.surfaceMuted else Color(0xFFE0E0E0)
    } else if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF1C5242)
    val playIconTint = if (!isApproved) {
        if (isDark) IhsanTheme.colors.textDisabled else Color(0xFF8E8E93)
    } else if (isDark) IhsanTheme.colors.selectedContent else Color.White
    val controlTint = if (isApproved) primaryText else subtext.copy(alpha = 0.4f)

    val durationMs = if (playbackDuration > 0) playbackDuration else 45000L

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = barBg,
        shadowElevation = 1.dp,
        border = BorderStroke(1.dp, barBorder),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            // Top Info Row (Surah Name & Reciter on Right in RTL, Controls on Left in RTL)
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left Side: Media Controls
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(0.dp)
                    ) {
                        IconButton(
                            onClick = onPrevious,
                            enabled = isApproved,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipPrevious,
                                contentDescription = "السابق",
                                tint = controlTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Surface(
                            onClick = onTogglePlay,
                            enabled = isApproved,
                            shape = CircleShape,
                            color = playContainer,
                            shadowElevation = if (isDark || !isApproved) 0.dp else 4.dp,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                if (isBuffering) CircularProgressIndicator(Modifier.size(28.dp), color = playIconTint, strokeWidth = 2.dp)
                                else Icon(
                                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                    tint = playIconTint,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onNext,
                            enabled = isApproved,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SkipNext,
                                contentDescription = "التالي",
                                tint = controlTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    // Right Side: Surah Name, Reciter & Expand Chevron
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f).clickable(onClick = onExpandReader).padding(start = 8.dp)
                    ) {
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                            Text(
                                text = if (surahName.isNotBlank()) "سورة $surahName" else "سورة الفاتحة",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = primaryText,
                                fontSize = 16.sp
                            )
                            Text(
                                text = if (com.example.feature.quran.util.RecitationAvailability.RECORDINGS_APPROVED) readerName.ifBlank { "اختر القارئ" } else "التلاوات تنتظر الترخيص",
                                style = MaterialTheme.typography.bodySmall,
                                color = subtext,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowUp,
                            contentDescription = "طي المشغل",
                            tint = subtext,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Time Slider & Duration Text
            Column(modifier = Modifier.fillMaxWidth()) {
                Slider(
                    value = playbackPosition.coerceAtMost(durationMs).toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..durationMs.toFloat(),
                    enabled = com.example.feature.quran.util.RecitationAvailability.RECORDINGS_APPROVED && playbackDuration > 0,
                    colors = SliderDefaults.colors(
                        thumbColor = accentColor,
                        activeTrackColor = accentColor,
                        inactiveTrackColor = if (isDark) IhsanTheme.colors.progressTrack else accentColor.copy(alpha = 0.2f)
                    ),
                    modifier = Modifier.height(18.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = formatTime(playbackPosition),
                        style = MaterialTheme.typography.labelSmall,
                        color = subtext,
                        fontSize = 11.sp
                    )
                    Text(
                        text = if (playbackDuration > 0) formatTime(playbackDuration) else "--:--",
                        style = MaterialTheme.typography.labelSmall,
                        color = subtext,
                        fontSize = 11.sp
                    )
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
    val isDark = IhsanTheme.isDark
    val sheetBg = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFFCF8F5)
    val primaryText = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF1C5242)
    val accentColor = if (isDark) IhsanTheme.colors.accentContent else Color(0xFF1C5242)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = sheetBg
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "اختر القارئ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = primaryText
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text("القارئ المفضل يُحفظ على جهازك. تشغيل التسجيلات وتنزيلها معلّقان لحين اعتماد الترخيص (PENDING_LICENSE).",
                style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary, modifier = Modifier.padding(bottom = 10.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(readers) { reader ->
                    val isSelected = reader.id == selected?.id
                    Surface(
                        onClick = { onSelect(reader) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            accentColor.copy(alpha = 0.15f)
                        } else {
                            if (isDark) IhsanTheme.colors.surfaceMuted else Color(0xFFFAF4EB)
                        },
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (isSelected) accentColor else (if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE8E0D5))
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
                                color = if (isSelected) accentColor else (if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF1B1B1E))
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "محدد",
                                    tint = accentColor
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
