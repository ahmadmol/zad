package com.example.feature.quran.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feature.quran.domain.model.Verse
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
    onDownloadSurah: () -> Unit,
    onSeekTo: (Long) -> Unit,
    onUpdateFontSize: (Float) -> Unit
) {
    val listState = rememberLazyListState()
    var selectedVerseForTafsir by remember { mutableStateOf<Verse?>(null) }
    var showFontSettings by remember { mutableStateOf(false) }

    LaunchedEffect(state.ayahs, state.initialAyahScroll) {
        if (state.ayahs.isNotEmpty() && state.initialAyahScroll != null) {
            val index = state.ayahs.indexOfFirst { it.verseNumber == state.initialAyahScroll }
            if (index != -1) {
                listState.scrollToItem(index)
            }
        }
    }
    LaunchedEffect(state.currentPlayingAyah) {
        if (state.currentPlayingAyah != null && state.isPlaying) {
            val index = state.ayahs.indexOfFirst { it.verseNumber == state.currentPlayingAyah }
            if (index != -1) {
                listState.animateScrollToItem(index)
            }
        }
    }

    // Save last read on scroll
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex }
            .distinctUntilChanged()
            .filter { state.ayahs.isNotEmpty() }
            .collect { index ->
                if (index < state.ayahs.size) {
                    val verse = state.ayahs[index]
                    onSaveLastRead(verse.surahId, verse.verseNumber)
                }
            }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            text = state.selectedSurah?.name ?: "تحميل...",
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onDownloadSurah) {
                            Icon(Icons.Default.Download, contentDescription = "Download Surah")
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
                            onTogglePlay = onTogglePlay,
                            onNext = onPlayNext,
                            onPrevious = onPlayPrevious,
                            onSeek = onSeekTo,
                            readerName = state.selectedReader?.name ?: ""
                        )
                    }
                }
            }
        ) { padding ->
            Box(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (state.isLoading && state.ayahs.isEmpty()) {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                } else if (state.errorMessage != null) {
                    Column(
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.errorMessage,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = onRetry) {
                            Text("إعادة المحاولة")
                        }
                    }
                } else if (state.ayahs.isEmpty()) {
                    Text(
                        text = "لا توجد آيات",
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        itemsIndexed(state.ayahs) { _, verse ->
                            val isBookmarked = state.bookmarks.any { it.surahId == verse.surahId && it.verseNumber == verse.verseNumber }
                            val isPlaying = state.currentPlayingAyah == verse.verseNumber
                            val isDownloaded = state.downloadedAyahs.contains(verse.verseNumber)
                            VerseItem(
                                verse = verse,
                                isBookmarked = isBookmarked,
                                isPlaying = isPlaying,
                                isDownloaded = isDownloaded,
                                fontSize = state.fontSize,
                                onBookmarkClick = { onToggleBookmark(verse.surahId, verse.verseNumber) },
                                onTafsirClick = { selectedVerseForTafsir = verse }
                            )
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
    onTogglePlay: () -> Unit,
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    onSeek: (Long) -> Unit,
    readerName: String
) {
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
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "القارئ الحالي",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = readerName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onPrevious) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous", modifier = Modifier.size(32.dp))
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
                        Icon(Icons.Default.SkipNext, contentDescription = "Next", modifier = Modifier.size(32.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

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
    onTafsirClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .border(
                width = if (isPlaying) 1.dp else 0.dp,
                color = if (isPlaying) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Transparent,
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
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = verse.verseNumber.toString(),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isPlaying) Color.White else MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
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
        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    }
}
