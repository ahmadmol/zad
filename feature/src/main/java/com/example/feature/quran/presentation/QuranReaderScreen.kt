package com.example.feature.quran.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.draw.alpha
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.quran.domain.model.Reader
import com.example.feature.quran.domain.model.Verse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.util.Locale

/**
 * Enhanced, stable Quran Reader Screen.
 *
 * Core Guarantees:
 * 1. Strict Opening Priority: Explicit target always wins and lands deterministically.
 * 2. Request-Scoped Landing Guard: `initialLandingDone` is tied to the identity of the current
 *    reading request (`activeRequestKey`), preventing stale state restoration or blocked re-entry
 *    while still preserving user page flips across configuration changes of the same request.
 * 3. Two High-Quality Paradigms:
 *    - Mushaf Mode: Classic 604-page Madani Mushaf with pinch-to-zoom, slider zoom, and reset badge.
 *    - Text Mode: Flowing scalable continuous typography for maximum readability.
 * 4. Preserves approved visual language, religious respect, and audio integration without
 *    redundant saves or page jumps during audio playback.
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
    onSelectPageSurah: (Int, Int) -> Unit = { _, _ -> },
    onPrivateNote: (Int, Int) -> Unit = { _, _ -> },
    onUpdateActivePosition: (Int, Int) -> Unit = { _, _ -> },
    onSetReadingMode: (QuranReadingMode) -> Unit = {}
) {
    val context = LocalContext.current
    var pageLoadError by remember { mutableStateOf<String?>(null) }

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
    var playerExpanded by rememberSaveable { mutableStateOf(false) }
    var chromeVisible by rememberSaveable { mutableStateOf(false) }

    val targetSurahId = state.currentReadingSurahId ?: state.selectedSurah?.id ?: 1
    val targetAyahNumber = state.currentReadingAyah ?: state.initialAyahScroll ?: 1

    // Request identity ties `initialLandingDone` to the specific reading request so a new
    // target always initializes cleanly while configuration changes on the same request stay locked.
    val requestIdentity = state.activeRequestKey.ifBlank {
        "fallback:${state.selectedSurah?.id ?: targetSurahId}:${state.initialAyahScroll ?: targetAyahNumber}"
    }
    var landedRequestKey by rememberSaveable { mutableStateOf("") }
    val initialLandingDone = landedRequestKey == requestIdentity &&
        (state.isTargetLocked || state.selectedSurah != null)

    val targetPageIndex = remember(pages, targetSurahId, targetAyahNumber) {
        if (pages.isNotEmpty()) pages.pageFor(targetSurahId, targetAyahNumber) else 0
    }
    val isWaitingForInitialLanding = pages.isNotEmpty() && !initialLandingDone && targetPageIndex != pager.currentPage

    BackHandler(
        enabled = showFontSettings || showReaderPicker || activeActionVerse != null || selectedVerseForTafsir != null || playerExpanded
    ) {
        when {
            showFontSettings -> showFontSettings = false
            showReaderPicker -> showReaderPicker = false
            selectedVerseForTafsir != null -> selectedVerseForTafsir = null
            activeActionVerse != null -> activeActionVerse = null
            playerExpanded -> playerExpanded = false
        }
    }

    val selectedSurah = state.selectedSurah
    val currentPage = pages.getOrNull(pager.currentPage)
    val visibleVerse = currentPage?.verses?.firstOrNull()

    val saveLastRead by rememberUpdatedState(onSaveLastRead)
    val updateActivePosition by rememberUpdatedState(onUpdateActivePosition)
    val selectPageSurah by rememberUpdatedState(onSelectPageSurah)
    val latestState by rememberUpdatedState(state)

    LaunchedEffect(chromeVisible, showFontSettings, showReaderPicker, activeActionVerse, selectedVerseForTafsir) {
        if (chromeVisible && !showFontSettings && !showReaderPicker && activeActionVerse == null && selectedVerseForTafsir == null) {
            delay(4_500)
            chromeVisible = false
        }
    }
    val toggleChrome = {
        chromeVisible = !chromeVisible
    }

    // Initial landing execution: scroll to resolved target page once per reading request.
    LaunchedEffect(pages.isNotEmpty(), requestIdentity, state.isTargetLocked, state.selectedSurah?.id) {
        val readyToLand = state.isTargetLocked || state.selectedSurah != null
        if (pages.isNotEmpty() && readyToLand && landedRequestKey != requestIdentity) {
            val targetPageIndex = pages.pageFor(targetSurahId, targetAyahNumber)
            if (pager.currentPage != targetPageIndex) {
                pager.scrollToPage(targetPageIndex)
            }
            snapshotFlow { pager.currentPage == targetPageIndex && !pager.isScrollInProgress }
                .filter { it }
                .first()
            delay(150)
            landedRequestKey = requestIdentity
        }
    }

    // Mode switch synchronization: if user switches from ContinuousText back to Mushaf,
    // ensure the Mushaf pager aligns with the active reading position without re-saving.
    LaunchedEffect(state.readingMode) {
        if (state.readingMode == QuranReadingMode.Mushaf && initialLandingDone && pages.isNotEmpty()) {
            val activePage = pages.pageFor(targetSurahId, targetAyahNumber)
            if (pager.currentPage != activePage) {
                pager.scrollToPage(activePage)
            }
        }
    }

    // Audio tracking: automatically flip page when audio advances to an ayah on another page
    LaunchedEffect(state.currentPlayingSurahId, state.currentPlayingAyah, state.isPlaying, pages.isNotEmpty(), initialLandingDone) {
        if (initialLandingDone && state.isPlaying && state.currentPlayingAyah != null && pages.isNotEmpty()) {
            val playingSurah = state.currentPlayingSurahId ?: targetSurahId
            val playingPage = pages.pageFor(playingSurah, state.currentPlayingAyah)
            if (pager.currentPage != playingPage && !pager.isScrollInProgress) {
                pager.animateScrollToPage(playingPage)
            }
        }
    }

    // Track user page flips ONLY after initial landing for the current request is settled.
    LaunchedEffect(pager, pages, initialLandingDone, requestIdentity) {
        if (!initialLandingDone || pages.isEmpty()) return@LaunchedEffect
        var lastDispatchedPage = pager.settledPage
        snapshotFlow { pager.settledPage }
            .distinctUntilChanged()
            .debounce(350)
            .collect { index ->
                if (index == lastDispatchedPage) return@collect
                lastDispatchedPage = index
                val current = latestState
                // If audio playback just drove this page flip, update active position to the playing ayah
                // rather than snapping back to the first verse of the page.
                if (current.isPlaying && current.currentPlayingAyah != null) {
                    val playingSurah = current.currentPlayingSurahId ?: current.currentReadingSurahId ?: 1
                    val playingPage = pages.pageFor(playingSurah, current.currentPlayingAyah)
                    if (playingPage == index) {
                        updateActivePosition(playingSurah, current.currentPlayingAyah)
                        return@collect
                    }
                }
                pages.getOrNull(index)?.let { page ->
                    val currentSurah = current.currentReadingSurahId ?: current.selectedSurah?.id ?: 1
                    val currentAyah = current.currentReadingAyah ?: current.initialAyahScroll ?: 1
                    // If the newly settled page already contains the current reading position, do not overwrite it.
                    val alreadyOnThisPage = page.verses.any {
                        it.surahId == currentSurah && it.verseNumber == currentAyah
                    }
                    if (!alreadyOnThisPage) {
                        val verse = page.verses.firstOrNull { it.surahId == currentSurah }
                            ?: page.verses.firstOrNull()
                        if (verse != null) {
                            saveLastRead(verse.surahId, verse.verseNumber)
                            updateActivePosition(verse.surahId, verse.verseNumber)
                            selectPageSurah(verse.surahId, verse.verseNumber)
                        }
                    }
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

    val activeSurahOnPage = currentPage?.verses?.firstOrNull {
        it.surahId == (state.currentReadingSurahId ?: selectedSurah?.id)
    } ?: visibleVerse

    val displayedSurahName = when {
        state.readingMode == QuranReadingMode.ContinuousText -> selectedSurah?.name ?: "…"
        currentPage != null && activeSurahOnPage != null -> currentPage.names[activeSurahOnPage.surahId] ?: selectedSurah?.name ?: "…"
        else -> selectedSurah?.name ?: "…"
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = bgCanvas,
            topBar = {
                AnimatedVisibility(visible = chromeVisible, enter = slideInVertically { -it }, exit = slideOutVertically { -it }) {
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
                        // Back Navigation (48dp touch target)
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFF0EAE1))
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع",
                                tint = topBarIconTint,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Center: Surah Title + Page / Ayah Badge
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "سورة $displayedSurahName",
                                style = MaterialTheme.typography.titleLarge.copy(fontSize = 19.sp),
                                fontWeight = FontWeight.Bold,
                                color = topBarTitleColor
                            )
                            Spacer(modifier = Modifier.height(2.dp))
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
                                    val pageText = if (state.readingMode == QuranReadingMode.Mushaf) {
                                        "الصفحة ${pager.currentPage + 1} من 604"
                                    } else {
                                        "الآية ${toArabicNumerals(state.currentReadingAyah ?: 1)} من ${toArabicNumerals(selectedSurah?.totalVerses ?: 1)}"
                                    }
                                    Text(
                                        text = pageText,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = topBarChipText
                                    )
                                }
                            }
                        }

                        // Right: Reading Mode Toggle, Bookmark, Reciter, Font Sizing
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // Toggle Reading Mode (Mushaf vs Continuous Text)
                            IconButton(
                                onClick = {
                                    val nextMode = if (state.readingMode == QuranReadingMode.Mushaf) {
                                        QuranReadingMode.ContinuousText
                                    } else {
                                        QuranReadingMode.Mushaf
                                    }
                                    onSetReadingMode(nextMode)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (state.readingMode == QuranReadingMode.Mushaf) {
                                        Icons.AutoMirrored.Filled.MenuBook
                                    } else {
                                        Icons.Default.AutoStories
                                    },
                                    contentDescription = if (state.readingMode == QuranReadingMode.Mushaf) {
                                        "التبديل إلى القراءة المتصلة"
                                    } else {
                                        "التبديل إلى عرض المصحف"
                                    },
                                    tint = topBarIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Bookmark Toggle
                            val currentAyahNum = state.currentReadingAyah
                                ?: activeSurahOnPage?.verseNumber
                                ?: state.currentPlayingAyah
                                ?: 1
                            val activeSurahForBookmark = state.currentReadingSurahId
                                ?: activeSurahOnPage?.surahId
                                ?: selectedSurah?.id
                                ?: 1

                            val isBookmarked = state.bookmarks.any {
                                it.surahId == activeSurahForBookmark && it.verseNumber == currentAyahNum
                            }

                            IconButton(
                                onClick = {
                                    onToggleBookmark(activeSurahForBookmark, currentAyahNum)
                                },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                    contentDescription = if (isBookmarked) "إزالة العلامة" else "حفظ علامة مرجعية",
                                    tint = topBarIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Audio Reciter Selection
                            IconButton(
                                onClick = { showReaderPicker = true },
                                modifier = Modifier.size(48.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "اختر القارئ",
                                    tint = topBarIconTint,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Text Size / Zoom Controls
                            IconButton(
                                onClick = { showFontSettings = !showFontSettings },
                                modifier = Modifier.size(48.dp)
                            ) {
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
                }
            },
            bottomBar = {
                AnimatedVisibility(visible = chromeVisible, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                Column {
                    AnimatedVisibility(visible = showFontSettings) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFFAF4EB),
                            shadowElevation = 8.dp,
                            border = BorderStroke(1.dp, if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE8E0D5))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = if (state.readingMode == QuranReadingMode.Mushaf) "تكبير المصحف" else "حجم الخط",
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
                                        .padding(horizontal = 14.dp)
                                )
                                Text(
                                    text = "${(state.fontSize / 28f * 100f).toInt()}%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = topBarIconTint
                                )
                                if (state.fontSize != 28f) {
                                    TextButton(
                                        onClick = { onUpdateFontSize(28f) },
                                        colors = com.example.designsystem.theme.ihsanTextButtonColors()
                                    ) {
                                        Text("إعادة ضبط")
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible = state.ayahs.isNotEmpty(),
                        enter = slideInVertically { it },
                        exit = slideOutVertically { it }
                    ) {
                        if (playerExpanded) {
                            AudioBar(
                                isPlaying = state.isPlaying,
                                isBuffering = state.isBuffering,
                                playbackPosition = state.playbackPosition,
                                playbackDuration = state.playbackDuration,
                                surahName = state.surahs.firstOrNull { it.id == state.currentPlayingSurahId }?.name
                                    ?: selectedSurah?.name
                                    ?: "الفاتحة",
                                readerName = state.selectedReader?.name ?: "قارئ غير محدد",
                                onTogglePlay = onTogglePlay,
                                onNext = onPlayNext,
                                onPrevious = onPlayPrevious,
                                onSeek = onSeekTo,
                                onExpandReader = { playerExpanded = false }
                            )
                        } else {
                            Surface(
                                color = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFF7F3EB),
                                border = BorderStroke(1.dp, if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFEFE8DD))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .navigationBarsPadding()
                                        .padding(horizontal = 16.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = onTogglePlay) {
                                        Icon(
                                            imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (state.isPlaying) "إيقاف مؤقت" else "تشغيل",
                                            tint = topBarIconTint
                                        )
                                    }
                                    Text(
                                        text = "مشغل التلاوة",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = IhsanTheme.colors.textBody
                                    )
                                    TextButton(
                                        onClick = { playerExpanded = true },
                                        colors = com.example.designsystem.theme.ihsanTextButtonColors()
                                    ) {
                                        Text("إظهار المشغل")
                                    }
                                }
                            }
                        }
                    }
                }
                }
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .then(if (chromeVisible) Modifier else Modifier.statusBarsPadding().navigationBarsPadding())
                    .fillMaxSize()
                    .background(bgCanvas)
            ) {
                QuranDownloadStatusPanel(state = state, onRetry = onDownloadSurah)

                if (state.errorMessage != null && state.ayahs.isNotEmpty() &&
                    state.errorMessage != com.example.feature.quran.util.RecitationAvailability.MESSAGE
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = state.errorMessage,
                            modifier = Modifier.weight(1f),
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                        TextButton(
                            onClick = onTogglePlay,
                            colors = com.example.designsystem.theme.ihsanTextButtonColors()
                        ) {
                            Text("إعادة التشغيل")
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
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
                        else -> {
                            if (pageLoadError != null) {
                                Text(pageLoadError.orEmpty(), modifier = Modifier.align(Alignment.Center))
                            } else if (pages.isEmpty()) {
                                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center), color = topBarIconTint)
                            } else if (state.readingMode == QuranReadingMode.ContinuousText && selectedSurah != null) {
                                // Mode 2: Continuous Scalable Reading Mode
                                QuranContinuousTextSurface(
                                    surah = selectedSurah,
                                    ayahs = state.ayahs,
                                    fontSizeSp = state.fontSize,
                                    activeSurahId = state.currentPlayingSurahId ?: state.currentReadingSurahId ?: selectedSurah.id,
                                    activeVerseNumber = state.currentPlayingAyah ?: state.currentReadingAyah ?: 1,
                                    onVerseClick = { verse ->
                                        activeActionVerse = verse
                                    },
                                    onVisibleAyahChanged = { sId, aNum ->
                                        saveLastRead(sId, aNum)
                                        updateActivePosition(sId, aNum)
                                    },
                                    initialAyah = targetAyahNumber,
                                    requestKey = requestIdentity,
                                    onZoomFontSize = onUpdateFontSize,
                                    onToggleChrome = toggleChrome,
                                    modifier = Modifier.fillMaxSize()
                                )
                            } else {
                                // Mode 1: Classic Mushaf Page Mode
                                Box(modifier = Modifier.fillMaxSize()) {
                                    HorizontalPager(
                                        state = pager,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                            .alpha(if (isWaitingForInitialLanding) 0f else 1f),
                                        userScrollEnabled = state.fontSize <= 28.5f,
                                        key = { it },
                                        beyondViewportPageCount = 1
                                    ) { index ->
                                        MushafPageSurface(
                                            page = pages[index],
                                            requestedFontSize = state.fontSize,
                                            activeSurahId = state.currentPlayingSurahId ?: state.currentReadingSurahId,
                                            activeVerseNumber = state.currentPlayingAyah ?: state.currentReadingAyah,
                                            onVerse = { verse ->
                                                updateActivePosition(verse.surahId, verse.verseNumber)
                                                saveLastRead(verse.surahId, verse.verseNumber)
                                                activeActionVerse = state.ayahs.firstOrNull {
                                                    it.surahId == verse.surahId && it.verseNumber == verse.verseNumber
                                                } ?: verse
                                            },
                                            onZoomFontSize = onUpdateFontSize,
                                            onToggleChrome = toggleChrome,
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    }

                                    if (isWaitingForInitialLanding) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.align(Alignment.Center),
                                            color = topBarIconTint
                                        )
                                    }

                                    // Floating Quick Reset Zoom Pill when zoomed in or out from 100%
                                    if (chromeVisible && kotlin.math.abs(state.fontSize - 28f) > 1f) {
                                        Surface(
                                            onClick = { onUpdateFontSize(28f) },
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFF7F3EB),
                                            border = BorderStroke(1.dp, topBarIconTint.copy(alpha = 0.3f)),
                                            shadowElevation = 6.dp,
                                            modifier = Modifier
                                                .align(Alignment.TopCenter)
                                                .padding(top = 10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .heightIn(min = 40.dp)
                                                    .padding(horizontal = 14.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Refresh,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(15.dp),
                                                    tint = topBarIconTint
                                                )
                                                Text(
                                                    text = "إعادة ضبط التكبير (١٠٠٪)",
                                                    style = MaterialTheme.typography.labelMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = topBarIconTint
                                                )
                                            }
                                        }
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
                    surahName = displayedSurahName,
                    onDismiss = { selectedVerseForTafsir = null }
                )
            }

            activeActionVerse?.let { verse ->
                val loadedVerse = state.ayahs.firstOrNull { it.surahId == verse.surahId && it.verseNumber == verse.verseNumber }
                val actionVerse = loadedVerse ?: verse
                VerseQuickActionSheet(
                    verse = actionVerse,
                    surahName = currentPage?.names?.get(verse.surahId) ?: displayedSurahName,
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
                    onPrivateNote = {
                        onPrivateNote(actionVerse.surahId, actionVerse.verseNumber)
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
        state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Enqueued -> "التنزيل في الانتظار وسيبدأ عند توفر الشبكة"
        state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Running -> "جارٍ تنزيل السورة: ${state.downloadProgress}%"
        state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Succeeded -> "اكتمل العمل، جارٍ التحقق من الملفات المنزلة"
        state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Failed ->
            state.downloadErrorMessage ?: "تعذر تنزيل السورة"
        state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Cancelled -> "أُلغي تنزيل السورة"
        else -> null
    } ?: return

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = if (state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Failed) {
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
                    color = if (state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Failed) {
                        MaterialTheme.colorScheme.onErrorContainer
                    } else if (IhsanTheme.isDark) {
                        IhsanTheme.colors.textBody
                    } else {
                        Color(0xFF1C5242)
                    }
                )
                if (state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Failed ||
                    state.downloadStatus == com.example.feature.quran.domain.usecase.QuranDownloadStatus.Cancelled
                ) {
                    TextButton(
                        onClick = onRetry,
                        colors = com.example.designsystem.theme.ihsanTextButtonColors()
                    ) {
                        Text("إعادة")
                    }
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
    onDismiss: () -> Unit,
    onPrivateNote: () -> Unit
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
        Column(
            modifier = Modifier
                .heightIn(max = androidx.compose.ui.platform.LocalConfiguration.current.screenHeightDp.dp * .85f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
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
                Button(
                    onClick = onPlayClick,
                    enabled = contentReady && com.example.feature.quran.util.RecitationAvailability.RECORDINGS_APPROVED,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Text("تشغيل", fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = onBookmarkToggle,
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = null)
                        Text(if (isBookmarked) "محفوظة" else "حفظ", fontSize = 12.sp)
                    }
                }
                Button(
                    onClick = onTafsirClick,
                    enabled = contentReady && !verse.tafsir.isNullOrBlank(),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                        Text("التفسير", fontSize = 12.sp)
                    }
                }
            }
            com.example.designsystem.component.IhsanSecondaryButton(
                onClick = onPrivateNote,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("إضافة ملاحظة شخصية للآية")
            }
            if (!com.example.feature.quran.util.RecitationAvailability.RECORDINGS_APPROVED) {
                val officialUrl = com.example.feature.quran.util.OfficialRecitationLink.quranComVerse(verse.surahId, verse.verseNumber)
                Text(
                    text = "التلاوة داخل التطبيق غير مفعّلة حتى اعتماد ترخيص التسجيلات. يمكن فتح الآية نفسها في Quran.com.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
                if (officialUrl != null) {
                    TextButton(
                        onClick = {
                            runCatching {
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(officialUrl)))
                            }.onFailure {
                                com.example.feature.core.notification.UserMessageNotifier.notify(context, "تعذر فتح صفحة التلاوة")
                            }
                        },
                        colors = com.example.designsystem.theme.ihsanTextButtonColors()
                    ) { Text("فتح الآية في Quran.com") }
                }
            }
            if (verse.tafsir.isNullOrBlank()) {
                Text(
                    text = "التفسير غير متاح في هذه النسخة؛ يلزم مصدر موثق وترخيص.",
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(
                    colors = com.example.designsystem.theme.ihsanTextButtonColors(),
                    onClick = {
                        (context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager)
                            .setPrimaryClip(android.content.ClipData.newPlainText("آية قرآنية", shareText))
                        com.example.feature.core.notification.UserMessageNotifier.notify(context, "نُسخت الآية مع مرجعها")
                    }
                ) {
                    Text("نسخ الآية")
                }
                TextButton(
                    colors = com.example.designsystem.theme.ihsanTextButtonColors(),
                    onClick = {
                        runCatching {
                            context.startActivity(
                                android.content.Intent.createChooser(
                                    android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(android.content.Intent.EXTRA_TEXT, shareText)
                                    },
                                    "مشاركة الآية"
                                )
                            )
                        }.onFailure {
                            com.example.feature.core.notification.UserMessageNotifier.notify(context, "تعذر فتح المشاركة")
                        }
                    }
                ) {
                    Text("مشاركة الآية")
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
    isBuffering: Boolean = false,
    onExpandReader: () -> Unit
) {
    val isDark = IhsanTheme.isDark
    val isApproved = com.example.feature.quran.util.RecitationAvailability.RECORDINGS_APPROVED
    val barBg = if (isDark) IhsanTheme.colors.surfaceElevated else Color(0xFFFAF9F6)
    val barBorder = if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFEFE8DD)
    val primaryText = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF1C5242)
    val subtext = IhsanTheme.colors.textSecondary
    val accentColor = if (isDark) IhsanTheme.colors.accentContent else Color(0xFF1C5242)
    val playContainer = if (!isApproved) {
        if (isDark) IhsanTheme.colors.surfaceMuted else Color(0xFFE0E0E0)
    } else if (isDark) MaterialTheme.colorScheme.primary else Color(0xFF1C5242)
    val playIconTint = if (!isApproved) {
        if (isDark) IhsanTheme.colors.textDisabled else Color(0xFF8E8E93)
    } else if (isDark) IhsanTheme.colors.selectedContent else Color.White
    val controlTint = if (isApproved) primaryText else subtext.copy(alpha = 0.4f)

    val durationMs = playbackDuration.coerceAtLeast(1L)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = barBg,
        shadowElevation = 8.dp,
        border = BorderStroke(1.dp, barBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Controls Group
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onPrevious,
                        enabled = isApproved,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "السابق",
                            tint = controlTint
                        )
                    }

                    IconButton(
                        onClick = onTogglePlay,
                        modifier = Modifier
                            .size(44.dp)
                            .background(playContainer, CircleShape)
                    ) {
                        if (isBuffering) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = playIconTint,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "إيقاف مؤقت" else "تشغيل",
                                tint = playIconTint,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }

                    IconButton(
                        onClick = onNext,
                        enabled = isApproved,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "التالي",
                            tint = controlTint
                        )
                    }
                }

                // Surah Name and Reciter Info
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onExpandReader)
                        .padding(start = 12.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = if (surahName.isNotBlank()) "سورة $surahName" else "التلاوة",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = primaryText,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isApproved) readerName.ifBlank { "اختر القارئ" } else "التلاوات تنتظر الترخيص",
                            style = MaterialTheme.typography.bodySmall,
                            color = subtext,
                            fontSize = 12.sp
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

            Spacer(modifier = Modifier.height(6.dp))

            // Time Slider & Duration Text
            if (!isApproved) {
                Text(
                    text = "التشغيل والتنزيل غير متاحين حتى اعتماد ترخيص التسجيلات.",
                    style = MaterialTheme.typography.bodySmall,
                    color = subtext,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    textAlign = TextAlign.Start
                )
            } else {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Slider(
                        value = playbackPosition.coerceIn(0L, durationMs).toFloat(),
                        onValueChange = { onSeek(it.toLong()) },
                        valueRange = 0f..durationMs.toFloat(),
                        enabled = isApproved && playbackDuration > 0,
                        colors = SliderDefaults.colors(
                            thumbColor = accentColor,
                            activeTrackColor = accentColor,
                            inactiveTrackColor = if (isDark) IhsanTheme.colors.progressTrack else accentColor.copy(alpha = 0.2f)
                        ),
                        modifier = Modifier.heightIn(min = 40.dp)
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
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "القارئ المفضل يُحفظ على جهازك. تشغيل التسجيلات وتنزيلها معلّقان لحين اعتماد الترخيص (PENDING_LICENSE).",
                style = MaterialTheme.typography.bodySmall,
                color = IhsanTheme.colors.textSecondary,
                modifier = Modifier.padding(bottom = 10.dp)
            )
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
