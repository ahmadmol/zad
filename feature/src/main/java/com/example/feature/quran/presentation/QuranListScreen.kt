package com.example.feature.quran.presentation

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.IhsanActionCard
import com.example.designsystem.component.IhsanSearchBar
import com.example.designsystem.component.LastReadCard
import com.example.designsystem.component.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuranListScreen(
    state: QuranUiState,
    onSurahClick: (Int, Int?) -> Unit,
    onSearch: (String) -> Unit,
    onClearSearch: () -> Unit,
    onBack: () -> Unit
) {
    var showGoToSheet by remember { mutableStateOf(false) }
    var selectedTab by remember { mutableIntStateOf(0) }
    var isSearchActive by remember { mutableStateOf(false) }

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
                            Icon(Icons.Default.Close, contentDescription = "Close Search")
                        }
                    }
                )
            } else {
                CenterAlignedTopAppBar(
                    title = { Text("القرآن الكريم", fontWeight = FontWeight.Bold) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
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
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("السور") }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("المحفوظات") }
                    )
                }
            }

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                if (state.isLoading && state.surahs.isEmpty() && !isSearchActive) {
                    QuranShimmerList()
                } else if (state.errorMessage != null && state.surahs.isEmpty() && !isSearchActive) {
                    Text(
                        text = state.errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center).padding(16.dp),
                        textAlign = TextAlign.Center
                    )
                } else if (isSearchActive) {
                    if (state.isLoading) {
                        QuranShimmerList()
                    } else if (state.searchResults.isEmpty() && state.searchQuery.isNotBlank()) {
                        Text(
                            "لا توجد نتائج",
                            modifier = Modifier.align(Alignment.Center),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                                KhatmaProgressCard(progress = state.khatmaProgress)
                            }

                            item {
                                state.lastRead?.let { (surah, ayah) ->
                                    LastReadCard(
                                        surahName = surah.name,
                                        surahNumber = surah.id,
                                        onContinueClick = { onSurahClick(surah.id, ayah) }
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
                            }

                            items(state.surahs) { surah ->
                                IhsanActionCard(
                                    title = "سورة ${surah.name}",
                                    subtitle = "${surah.revelationType} • ${surah.totalVerses} آية",
                                    icon = Icons.AutoMirrored.Filled.MenuBook,
                                    onClick = { onSurahClick(surah.id, null) },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    } else {
                        if (state.bookmarks.isEmpty()) {
                            Text(
                                "لا توجد محفوظات",
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
                    "تقدم الختمة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "${progress.toInt()}%",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
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
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.1f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "استمر في القراءة لتصل لختم القرآن الكريم",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
