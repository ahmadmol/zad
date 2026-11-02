package com.example.feature.quran.presentation

import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset

import com.example.designsystem.theme.IhsanTheme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.IhsanActionCard
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanSearchBar
import com.example.designsystem.component.IhsanTopBar
import com.example.designsystem.component.IhsanTopBarStyle
import com.example.designsystem.component.LastReadCard
import com.example.designsystem.component.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranListScreen(
    state: QuranUiState,
    onSurahClick: (Int, Int?) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onBack: () -> Unit,
    onMemorization: () -> Unit = {},
    onReadingJourney: () -> Unit = {},
    onRetry: () -> Unit = {}
) {
    var showGoToSheet by rememberSaveable { mutableStateOf(false) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var isSearchActive by rememberSaveable { mutableStateOf(false) }
    BackHandler(enabled = isSearchActive && !showGoToSheet) {
        isSearchActive = false
        onClearSearch()
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                TopAppBar(
                    title = {
                        IhsanSearchBar(
                            query = state.searchQuery,
                            onQueryChange = onSearch,
                            placeholder = "بحث في القرآن...",
                            modifier = Modifier.fillMaxWidth()
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { 
                            isSearchActive = false
                            onClearSearch()
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "إغلاق البحث")
                        }
                    }
                )
            } else {
                IhsanTopBar(
                    title = "القرآن الكريم",
                    onNavigateBack = onBack,
                    style = IhsanTopBarStyle.Surface,
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "بحث")
                        }
                        IconButton(onClick = { showGoToSheet = true }) {
                            Icon(Icons.Default.Navigation, contentDescription = "انتقال")
                        }
                    }
                )
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!isSearchActive) {
                TabRow(selectedTabIndex = selectedTab, indicator = { positions ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(positions[selectedTab]),
                        color = IhsanTheme.colors.accentContent)
                }) {
                    Tab(
                    selectedContentColor = if (IhsanTheme.isDark) IhsanTheme.colors.textPrimary else LocalContentColor.current,
                    unselectedContentColor = if (IhsanTheme.isDark) IhsanTheme.colors.textSecondary else LocalContentColor.current.copy(alpha = .7f),
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("السور") }
                    )
                    Tab(
                    selectedContentColor = if (IhsanTheme.isDark) IhsanTheme.colors.textPrimary else LocalContentColor.current,
                    unselectedContentColor = if (IhsanTheme.isDark) IhsanTheme.colors.textSecondary else LocalContentColor.current.copy(alpha = .7f),
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("المحفوظات") }
                    )
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 },
                        selectedContentColor = IhsanTheme.colors.textPrimary,
                        unselectedContentColor = IhsanTheme.colors.textSecondary,
                        text = { Text("الأجزاء") })
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.isLoading && state.surahs.isEmpty() && !isSearchActive) {
                    QuranShimmerList()
                } else if (state.errorMessage != null && state.surahs.isEmpty() && !isSearchActive) {
                    IhsanErrorState(title = "تعذر تحميل القرآن", message = state.errorMessage,
                        modifier = Modifier.align(Alignment.Center), onRetry = onRetry)
                } else if (isSearchActive) {
                    if (state.isLoading) {
                        QuranShimmerList()
                    } else if (state.errorMessage != null) {
                        IhsanErrorState(title = "تعذر البحث", message = state.errorMessage,
                            modifier = Modifier.align(Alignment.Center), retryLabel = "إعادة المحاولة",
                            onRetry = { onSearch(state.searchQuery) })
                    } else if (state.searchResults.isEmpty() && state.searchQuery.isNotBlank()) {
                        IhsanEmptyState(title = "لا توجد نتائج", message = "جرّب كلمة أخرى أو امسح البحث.",
                            modifier = Modifier.align(Alignment.Center), primaryActionLabel = "مسح البحث",
                            onPrimaryAction = onClearSearch)
                    } else if (state.searchQuery.isBlank()) {
                        IhsanEmptyState(title = "البحث في القرآن", message = "اكتب كلمة للبحث في الآيات.",
                            icon = Icons.Default.Search, modifier = Modifier.align(Alignment.Center))
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(state.searchResults) { verse ->
                                val surahName = state.surahs.find { it.id == verse.surahId }?.name ?: ""
                                IhsanActionCard(
                                    title = "$surahName - آية ${verse.verseNumber}",
                                    subtitle = verse.text,
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    onClick = { onSurahClick(verse.surahId, verse.verseNumber) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                } else {
                    if (selectedTab == 0) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                IhsanActionCard(title = "خطة القراءة وملاحظاتي الخاصة",
                                    subtitle = "تأكيد يدوي لإتمام الصفحات • ملاحظات وتصدير اختياري",
                                    icon = Icons.Default.Bookmark, onClick = onReadingJourney)
                            }
                            item {
                                IhsanActionCard(title = "الحفظ والمراجعة", subtitle = "نطاق آيات • تأكيد يدوي • مراجعات مستحقة",
                                    icon = Icons.Default.Bookmark, onClick = onMemorization)
                            }
                            item {
                                KhatmaProgressCard(progress = state.khatmaProgress)
                            }

                            item {
                                state.lastRead?.let { (surah, ayah) ->
                                    LastReadCard(
                                        surahName = surah.name,
                                        surahNumber = surah.id,
                                        ayahNumber = ayah,
                                        onContinueClick = { onSurahClick(surah.id, ayah) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            items(state.surahs) { surah ->
                                val readingProgress = state.surahReadingProgress[surah.id]
                                val lastAyah = state.lastRead
                                    ?.takeIf { it.first.id == surah.id }
                                    ?.second
                                val subtitle = buildString {
                                    append("${surah.revelationType} • ${surah.totalVerses} آية")
                                    if (lastAyah != null) {
                                        append(" • وصلت للآية $lastAyah")
                                    }
                                }
                                Column(modifier = Modifier.fillMaxWidth()) {
                                    IhsanActionCard(
                                        title = "سورة ${surah.name}",
                                        subtitle = subtitle,
                                        icon = Icons.AutoMirrored.Filled.MenuBook,
                                        onClick = { onSurahClick(surah.id, lastAyah) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                    if (readingProgress != null && readingProgress > 0f) {
                                        Spacer(modifier = Modifier.height(6.dp))
                                        LinearProgressIndicator(
                                            progress = { readingProgress.coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(6.dp)
                                                .padding(horizontal = 4.dp),
                                            color = if (IhsanTheme.isDark) IhsanTheme.colors.textBody else MaterialTheme.colorScheme.primary,
                                            trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                    }
                                }
                            }
                        }
                    } else if (selectedTab == 2) {
                        LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            items(com.example.feature.quran.domain.model.QuranNavigation.juzStarts.withIndex().toList()) { (index, start) ->
                                val surah = state.surahs.firstOrNull { it.id == start.first }
                                IhsanActionCard(title = "الجزء ${index + 1}", subtitle = "${surah?.name.orEmpty()} — آية ${start.second}",
                                    icon = Icons.AutoMirrored.Filled.MenuBook, onClick = { onSurahClick(start.first, start.second) }, modifier = Modifier.fillMaxWidth())
                            }
                        }
                    } else {
                        if (state.bookmarks.isEmpty()) {
                            IhsanEmptyState(title = "لا توجد محفوظات", message = "يمكنك حفظ آية من قائمة الآية أثناء القراءة.",
                                icon = Icons.Default.Bookmark, modifier = Modifier.align(Alignment.Center),
                                primaryActionLabel = "تصفح السور", onPrimaryAction = { selectedTab = 0 })
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(state.bookmarks) { bookmark ->
                                    IhsanActionCard(
                                        title = "${bookmark.surahName} - آية ${bookmark.verseNumber}",
                                        subtitle = bookmark.text.take(50) + "...",
                                        icon = Icons.Default.Bookmark,
                                        onClick = { onSurahClick(bookmark.surahId, bookmark.verseNumber) },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (showGoToSheet) {
            QuranGoToSheet(
                surahs = state.surahs,
                onDismiss = { showGoToSheet = false },
                onNavigate = { surahId, ayahNumber ->
                    showGoToSheet = false
                    onSurahClick(surahId, ayahNumber)
                }
            )
        }
    }
}

@Composable
private fun QuranShimmerList() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(8) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .shimmerEffect()
            )
        }
    }
}

@Composable
fun KhatmaProgressCard(progress: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "موضع القراءة في المصحف",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${progress.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (IhsanTheme.isDark) IhsanTheme.colors.textBody else MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { progress / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (IhsanTheme.isDark) IhsanTheme.colors.textBody else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "يمثل آخر موضع فتحته، وليس مقدار ما أتممت قراءته",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
