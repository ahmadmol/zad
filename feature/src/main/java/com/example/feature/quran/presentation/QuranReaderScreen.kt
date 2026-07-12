package com.example.feature.quran.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.example.feature.quran.domain.model.Reader
import com.example.feature.quran.domain.model.Verse
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
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
    var showFontSettings by remember { mutableStateOf(false) }
    var showReaderPicker by remember { mutableStateOf(false) }
    var readyToTrackLastRead by remember { mutableStateOf(false) }
    val ayahs by rememberUpdatedState(state.ayahs)
    val saveLastRead by rememberUpdatedState(onSaveLastRead)

    LaunchedEffect(state.ayahs, state.initialAyahScroll) {
        readyToTrackLastRead = false
        if (state.ayahs.isNotEmpty()) {
            if (state.initialAyahScroll != null) {
                val index = state.ayahs.indexOfFirst { it.verseNumber == state.initialAyahScroll }
                if (index != -1) listState.scrollToItem(index)
            }
            readyToTrackLastRead = true
        }
    }

    LaunchedEffect(state.currentPlayingAyah, state.isPlaying) {
        if (state.isPlaying && state.currentPlayingAyah != null && state.ayahs.isNotEmpty()) {
            val index = state.ayahs.indexOfFirst { it.verseNumber == state.currentPlayingAyah }
            if (index != -1) listState.animateScrollToItem(index)
        }
    }

    LaunchedEffect(listState, readyToTrackLastRead) {
        if (!readyToTrackLastRead) return@LaunchedEffect
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .debounce(350)
            .filter { ayahs.isNotEmpty() }
            .collect { index ->
                if (index in ayahs.indices) {
                    val verse = ayahs[index]
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
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = state.selectedSurah?.name ?: "تحميل...",
                                fontWeight = FontWeight.Bold
                            )
                            val current = state.currentPlayingAyah
                                ?: state.lastRead?.takeIf { it.first.id == state.selectedSurah?.id }?.second
                            val total = state.selectedSurah?.totalVerses ?: state.ayahs.size
                            if (current != null && total > 0) {
                                Text(
                                    text = "الآية $current من $total",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onDownloadSurah) {
                            Icon(
                                if (state.isDownloading) Icons.Default.HourglassTop else Icons.Default.Download,
                                contentDescription = "Download Surah"
                            )
                        }
                        IconButton(onClick = { showFontSettings = !showFontSettings }) {
                            Icon(Icons.Default.TextFields, contentDescription = "Font Settings")
                        }
                    }
                )
            },
            bottomBar = {
                Column {
                    AnimatedVisibility(visible = showFontSettings) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.surface,
                            shadowElevation = 8.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("حجم الخط", style = MaterialTheme.typography.bodySmall)
                                Slider(
                                    value = state.fontSize,
                                    onValueChange = onUpdateFontSize,
                                    valueRange = 20f..48f,
                                    modifier = Modifier.weight(1f).padding(horizontal = 16.dp)
                                )
                                Text("${state.fontSize.toInt()}", style = MaterialTheme.typography.bodyMedium)
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
                            currentAyah = state.currentPlayingAyah,
                            totalAyahs = state.selectedSurah?.totalVerses ?: state.ayahs.size,
                            onTogglePlay = onTogglePlay,
                            onNext = onPlayNext,
                            onPrevious = onPlayPrevious,
                            onSeek = onSeekTo,
                            readerName = state.selectedReader?.name ?: "",
                            onReaderClick = { showReaderPicker = true }
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                when {
                    state.isLoading && state.ayahs.isEmpty() -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
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
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            itemsIndexed(state.ayahs) { _, verse ->
                                val isBookmarked = state.bookmarks.any {
                                    it.surahId == verse.surahId && it.verseNumber == verse.verseNumber
                                }
                                VerseItem(
                                    verse = verse,
                                    isBookmarked = isBookmarked,
                                    isPlaying = state.currentPlayingAyah == verse.verseNumber,
                                    isDownloaded = state.downloadedAyahs.contains(verse.verseNumber),
                                    fontSize = state.fontSize,
                                    onBookmarkClick = {
                                        onToggleBookmark(verse.surahId, verse.verseNumber)
                                    },
                                    onTafsirClick = { selectedVerseForTafsir = verse },
                                    onPlayClick = { onPlayAyah(verse.verseNumber) }
                                )
                            }
                        }
                    }
                }
            }

            selectedVerseForTafsir?.let { verse ->
                TafsirBottomSheet(
                    verse = verse,
                    surahName = state.selectedSurah?.name ?: "",
                    onDismiss = { selectedVerseForTafsir = null }
                )
            }
        }
    }
}

@Composable
fun AudioBar(
    isPlaying: Boolean,
    playbackPosition: Long,
    playbackDuration: Long,
    currentAyah: Int?,
    totalAyahs: Int,
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    readerName: String,
    onReaderClick: () -> Unit
) {
    val surahProgress = if (currentAyah != null && totalAyahs > 0) {
        currentAyah.toFloat() / totalAyahs.toFloat()
    } else 0f

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(onClick = onReaderClick)
                ) {
                    Text(
                        text = "القارئ الحالي",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = readerName.ifBlank { "اختر قارئًا" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    if (currentAyah != null && totalAyahs > 0) {
                        Text(
                            text = "تقدم السورة: $currentAyah / $totalAyahs",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) {
                        Icon(
                            Icons.Default.SkipPrevious,
                            contentDescription = "Previous",
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Surface(
                        onClick = onTogglePlay,
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = "Play/Pause",
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }

                    IconButton(onClick = onNext) {
                        Icon(
                            Icons.Default.SkipNext,
                            contentDescription = "Next",
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            if (totalAyahs > 0) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { surahProgress.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column {
                Slider(
                    value = playbackPosition.toFloat(),
                    onValueChange = { onSeek(it.toLong()) },
                    valueRange = 0f..(if (playbackDuration > 0) playbackDuration.toFloat() else 1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                    )
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
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
            Text(
                text = "اختر القارئ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(readers) { reader ->
                    val isSelected = reader.id == selected?.id
                    Surface(
                        onClick = { onSelect(reader) },
                        shape = RoundedCornerShape(16.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = reader.name,
                            modifier = Modifier.padding(16.dp),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
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
    return String.format(Locale.US, "%02d:%02d", minutes, seconds)
}

@Composable
fun VerseItem(
    verse: Verse,
    isBookmarked: Boolean,
    isPlaying: Boolean,
    isDownloaded: Boolean,
    fontSize: Float,
    onBookmarkClick: () -> Unit,
    onTafsirClick: () -> Unit,
    onPlayClick: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlayClick)
            .background(
                if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isPlaying) 1.dp else 0.dp,
                color = if (isPlaying) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                } else {
                    Color.Transparent
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .background(
                            if (isPlaying) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            CircleShape
                        )
                        .clickable(onClick = onPlayClick),
                    contentAlignment = Alignment.Center
                ) {
                    if (isPlaying) {
                        Icon(
                            Icons.Default.Pause,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    } else {
                        Text(
                            text = verse.verseNumber.toString(),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                IconButton(onClick = onPlayClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "تشغيل الآية",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                IconButton(onClick = onBookmarkClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = "Bookmark",
                        tint = if (isBookmarked) Color.Red else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
                if (isDownloaded) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = "Downloaded",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp).padding(top = 4.dp)
                    )
                }
                IconButton(onClick = onTafsirClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = "Tafsir",
                        tint = Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = verse.text,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.6).sp,
                    textAlign = TextAlign.Justify,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else Color.Black
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}
