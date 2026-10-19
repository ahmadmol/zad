package com.example.feature.dashboard

import com.example.feature.dashboard.presentation.SearchResultKind
import com.example.feature.dashboard.presentation.GlobalLearningSearchViewModel

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import com.example.designsystem.theme.IhsanTheme
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.R
import com.example.feature.asma.presentation.AsmaAction
import com.example.feature.asma.presentation.AsmaViewModel
import com.example.feature.azkar.presentation.AzkarAction
import com.example.feature.azkar.presentation.AzkarViewModel
import com.example.feature.duas.presentation.DuaAction
import com.example.feature.duas.presentation.DuaViewModel
import com.example.feature.ehsan.presentation.EhsanViewModel
import com.example.feature.hadith.presentation.HadithAction
import com.example.feature.hadith.presentation.HadithViewModel
import com.example.feature.quran.presentation.QuranAction
import com.example.feature.quran.presentation.QuranViewModel
import org.koin.androidx.compose.koinViewModel

private val DeepTeal = Color(0xFF0F5247)
private val SoftMint = Color(0xFFEFF7F5)
private val SoftMintBorder = Color(0xFFE2EFEA)

private enum class SearchCategory(val title: String, val iconRes: Int) {
    HADITH("الأحاديث", R.drawable.ic_search_hadith),
    DUA("الأدعية", R.drawable.ic_search_dua),
    AZKAR("الأذكار", R.drawable.ic_search_azkar),
    QURAN("القرآن", R.drawable.ic_search_quran),
    EHSAN("مجتمع إحسان", R.drawable.ic_search_ehsan),
    ASMA("أسماء الله الحسنى", R.drawable.ic_search_asma),
    TASBIH("التسبيح", R.drawable.ic_search_tasbih),
    VIDEOS("الدروس والفيديو", android.R.drawable.ic_media_play),
    PRAYER("الصلاة", R.drawable.ihsan_icon_prayer)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun GlobalSearchScreen(
    onBack: () -> Unit,
    onNavigateToQuran: (Int, Int?) -> Unit,
    onNavigateToDua: (Long) -> Unit,
    onNavigateToDuaLibrary: () -> Unit,
    onOpenResult: (SearchResultKind, String) -> Unit,
    onNavigateToAzkar: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToAsma: () -> Unit = {},
    onNavigateToPrayer: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToDonations: () -> Unit = {},
    onNavigateToLive: () -> Unit = {},
    quranViewModel: QuranViewModel = koinViewModel(),
    duaViewModel: DuaViewModel = koinViewModel(),
    azkarViewModel: AzkarViewModel = koinViewModel(),
    hadithViewModel: HadithViewModel = koinViewModel(),
    asmaViewModel: AsmaViewModel = koinViewModel(),
    ehsanViewModel: EhsanViewModel = koinViewModel(),
    learningViewModel: GlobalLearningSearchViewModel = koinViewModel()
) {
    var query by remember { mutableStateOf("") }
    var selectedCategoryFilter by remember { mutableStateOf<SearchCategory?>(null) }

    val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
    val duaState by duaViewModel.uiState.collectAsStateWithLifecycle()
    val azkarState by azkarViewModel.uiState.collectAsStateWithLifecycle()
    val hadithState by hadithViewModel.uiState.collectAsStateWithLifecycle()
    val asmaState by asmaViewModel.uiState.collectAsStateWithLifecycle()
    val ehsanState by ehsanViewModel.uiState.collectAsStateWithLifecycle()
    val learningHits by learningViewModel.results.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val isDark = IhsanTheme.isDark

    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            quranViewModel.onAction(QuranAction.Search(query))
            duaViewModel.onAction(DuaAction.OnSearchQueryChanged(query))
            azkarViewModel.onAction(AzkarAction.OnSearchQueryChanged(query))
            hadithViewModel.onAction(HadithAction.OnSearchQueryChanged(query))
            asmaViewModel.onAction(AsmaAction.OnSearchQueryChange(query))
            ehsanViewModel.onTypeChange("ALL")
            ehsanViewModel.onSearchQueryChange(query)
            learningViewModel.search(query)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = if (isDark) IhsanTheme.colors.surfaceBase else Color(0xFFF9FBFB),
            topBar = {
                // Header Area with background image and search input
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                        .background(if (isDark) IhsanTheme.colors.surfaceBase else SoftMint)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_search_header),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    )

                    if (isDark) {
                        Box(
                            modifier = Modifier
                                .matchParentSize()
                                .background(Brush.verticalGradient(listOf(IhsanTheme.colors.surfaceBase.copy(alpha = .20f), IhsanTheme.colors.surfaceBase.copy(alpha = .82f))))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Navigation Row: Back button on left, "البحث" Title centered
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier
                                    .align(Alignment.CenterStart)
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(if (isDark) IhsanTheme.colors.surfaceMuted else Color.White.copy(alpha = 0.7f))
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Text(
                                text = "البحث",
                                fontSize = 24.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal,
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        com.example.designsystem.component.IhsanSearchBar(
                            query = query, onQueryChange = { query = it }, placeholder = "ابحث في كل محتويات إحسان",
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }))

                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (query.isBlank()) {
                    // Default Landing View (Categories Grid + Popular Searches + Tip)
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        // Categories Title
                        item {
                            Text(
                                text = "تصفّح المحتوى",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            )
                        }

                        // Keep complete category labels readable with larger accessibility fonts.
                        item {
                            val categories = SearchCategory.entries.toTypedArray()
                            val columns = if (androidx.compose.ui.platform.LocalDensity.current.fontScale > 1.15f &&
                                androidx.compose.ui.platform.LocalConfiguration.current.screenWidthDp < 500) 3 else 4
                            Column(
                                verticalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                for (rowIndex in 0 until (categories.size + columns - 1) / columns) {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        for (colIndex in 0 until columns) {
                                            val catIndex = rowIndex * columns + colIndex
                                            if (catIndex < categories.size) {
                                                val category = categories[catIndex]
                                                Surface(
                                                    onClick = {
                                                        when (category) {
                                                            SearchCategory.QURAN -> onNavigateToQuran(1, null)
                                                            SearchCategory.DUA -> onNavigateToDuaLibrary()
                                                            SearchCategory.AZKAR -> onNavigateToAzkar()
                                                            SearchCategory.HADITH -> onNavigateToHadith()
                                                            SearchCategory.EHSAN -> onNavigateToDonations()
                                                            SearchCategory.ASMA -> onNavigateToAsma()
                                                            SearchCategory.TASBIH -> onOpenResult(SearchResultKind.TASBIH, "")
                                                            SearchCategory.VIDEOS -> { selectedCategoryFilter = SearchCategory.VIDEOS; query = "حلقة" }
                                                            SearchCategory.PRAYER -> onNavigateToPrayer()
                                                        }
                                                    },
                                                    shape = RoundedCornerShape(16.dp),
                                                    color = if (isDark) IhsanTheme.colors.surfaceElevated else SoftMint,
                                                    border = androidx.compose.foundation.BorderStroke(
                                                        width = 1.dp,
                                                        color = if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE4EFEA)
                                                    ),
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .heightIn(min = 36.dp)
                                                ) {
                                                    Column(
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.Center,
                                                        modifier = Modifier
                                                            .fillMaxSize()
                                                            .padding(horizontal = 4.dp, vertical = 8.dp)
                                                    ) {

                                                        Text(
                                                            text = category.title,
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal,
                                                            maxLines = 2,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Popular Searches List Items
                        val popularSearches = listOf(
                            "سورة الكهف",
                            "دعاء السفر",
                            "أذكار الصباح",
                            "أسماء الله الحسنى",
                            "المسجد الحرام بث مباشر"
                        )

                        item {
                            Surface(shape = RoundedCornerShape(16.dp), color = if (isDark) IhsanTheme.colors.surfaceElevated else Color.White,
                                border = BorderStroke(.5.dp, IhsanTheme.colors.borderSubtle), modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
                                Column(Modifier.padding(12.dp)) {
                                    Text("عمليات البحث الشائعة", fontSize = 16.sp, fontWeight = FontWeight.Bold,
                                        color = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal, modifier = Modifier.padding(bottom = 8.dp))
                                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        popularSearches.forEach { suggestion ->
                                            Surface(onClick = { if (suggestion == "المسجد الحرام بث مباشر") onNavigateToLive() else query = suggestion },
                                                shape = RoundedCornerShape(14.dp), color = if (isDark) IhsanTheme.colors.surfaceBase else SoftMint) {
                                                Row(Modifier.padding(horizontal = 10.dp, vertical = 7.dp), verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(Icons.Default.Search, null, tint = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal, modifier = Modifier.size(14.dp))
                                                    Text(suggestion, Modifier.padding(start = 5.dp), fontSize = 11.sp,
                                                        color = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal)
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Tip Card
                        item {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = if (isDark) IhsanTheme.colors.surfaceElevated else SoftMint,
                                border = androidx.compose.foundation.BorderStroke(
                                    width = 1.dp,
                                    color = if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE0ECE8)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) IhsanTheme.colors.surfaceMuted else Color(0xFFFEF3C7)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "نصيحة",
                                            tint = if (IhsanTheme.isDark) IhsanTheme.colors.warning else Color(0xFFF59E0B),
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "نصيحة",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "استخدم كلمات بسيطة للحصول على نتائج أفضل\nمثل: دعاء، سورة، حديث، صلاة ...",
                                            fontSize = 12.sp,
                                            color = if (isDark) IhsanTheme.colors.textBody else Color(0xFF37474F),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Category Filter Row
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            val isAllSelected = selectedCategoryFilter == null
                            FilterChip(
                                selected = isAllSelected,
                                onClick = { selectedCategoryFilter = null },
                                label = { Text("الكل") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isDark) IhsanTheme.colors.selectedContainer else DeepTeal,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                        items(SearchCategory.entries.filter { it != SearchCategory.PRAYER }) { category ->
                            val isSelected = selectedCategoryFilter == category
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategoryFilter = if (isSelected) null else category
                                },
                                leadingIcon = {
                                    SearchSymbol(category.iconRes, Modifier.size(16.dp))
                                },
                                label = { Text(category.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (isDark) IhsanTheme.colors.selectedContainer else DeepTeal,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }

                    // Active Search Results List
                    val filter = selectedCategoryFilter
                    // Each repository emits independently. Never count an unfiltered or stale
                    // library as a result while its new query is still being processed.
                    val quranResults = quranState.searchResults.takeIf { quranState.searchQuery == query && !quranState.isSearching }.orEmpty()
                    val duaResults = duaState.duas.takeIf { duaState.searchQuery == query }.orEmpty()
                    val hadithResults = hadithState.hadiths.takeIf { hadithState.searchQuery == query }.orEmpty()
                    val asmaResults = asmaState.searchResults.takeIf { asmaState.searchQuery == query }.orEmpty()
                    val communityResults = ehsanState.filteredDonations.filter {
                        com.example.feature.core.search.ArabicSearch.matches(it.title + " " + it.description, query)
                    }
                    val visibleAzkar = azkarState.azkarList.filter {
                        (filter != SearchCategory.TASBIH || it.category == "تسبيح") &&
                            com.example.feature.core.search.ArabicSearch.matches(it.category + " " + it.text, query)
                    }
                    val learningResults = learningHits.filter { it.query == query.trim() }
                    val showLearning = (filter == null || filter == SearchCategory.VIDEOS) && learningResults.isNotEmpty()
                    val showQuran = (filter == null || filter == SearchCategory.QURAN) && quranResults.isNotEmpty()
                    val showDua = (filter == null || filter == SearchCategory.DUA) && duaResults.isNotEmpty()
                    val showAzkar = (filter == null || filter == SearchCategory.AZKAR || filter == SearchCategory.TASBIH) && visibleAzkar.isNotEmpty()
                    val showHadith = (filter == null || filter == SearchCategory.HADITH) && hadithResults.isNotEmpty()
                    val showAsma = (filter == null || filter == SearchCategory.ASMA) && asmaResults.isNotEmpty()
                    val showEhsan = (filter == null || filter == SearchCategory.EHSAN) && communityResults.isNotEmpty()

                    val hasAnyResult = showQuran || showDua || showAzkar || showHadith || showAsma || showEhsan || showLearning

                    val resultCount = (if (showQuran) quranResults.size else 0) +
                        (if (showDua) duaResults.size else 0) + (if (showAzkar) visibleAzkar.size else 0) +
                        (if (showHadith) hadithResults.size else 0) + (if (showAsma) asmaResults.size else 0) +
                        (if (showEhsan) communityResults.size else 0) + (if (showLearning) learningResults.size else 0)
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("نتائج البحث", Modifier.weight(1f), fontSize = 18.sp, fontWeight = FontWeight.Bold,
                            color = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal)
                        Text("($resultCount)", fontSize = 12.sp, color = IhsanTheme.colors.textSecondary)
                    }

                    if (!hasAnyResult) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = if (isDark) IhsanTheme.colors.textDisabled else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "لم نجد نتائج مطابقة لـ \"$query\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) IhsanTheme.colors.textPrimary else DeepTeal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "جرب البحث بكلمات أخرى أو اختر إحدى الفئات أعلاه.",
                                fontSize = 13.sp,
                                color = if (isDark) IhsanTheme.colors.textSecondary else Color.Gray,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            if (showQuran) {
                                item { SearchCategoryHeader("القرآن الكريم", R.drawable.ic_search_quran, isDark) }
                                items(quranResults) { verse ->
                                    val chapter = quranState.surahs.firstOrNull { it.id == verse.surahId }
                                    val isChapterHit = verse.verseNumber == 1 && chapter != null &&
                                        com.example.feature.quran.domain.model.QuranSearch.matchesSurah(chapter.name, query)
                                    SearchResultCard(
                                        tag = if (isChapterHit) "سورة" else "آية قرآنية",
                                        title = if (isChapterHit) "سورة ${chapter!!.name}" else verse.text,
                                        subtitle = "سورة ${quranState.surahs.firstOrNull { it.id == verse.surahId }?.name ?: verse.surahId} - الآية ${verse.verseNumber}",
                                        iconRes = R.drawable.ic_search_quran,
                                        isDark = isDark,
                                        onClick = { onNavigateToQuran(verse.surahId, verse.verseNumber) }
                                    )
                                }
                            }

                            if (showDua) {
                                item { SearchCategoryHeader("الأدعية المأثورة", R.drawable.ic_search_dua, isDark) }
                                items(duaResults) { dua ->
                                    SearchResultCard(
                                        tag = "دعاء",
                                        title = dua.title,
                                        subtitle = dua.text,
                                        iconRes = R.drawable.ic_search_dua,
                                        isDark = isDark,
                                        onClick = { onNavigateToDua(dua.id) }
                                    )
                                }
                            }

                            if (showAzkar) {
                                item { SearchCategoryHeader("الأذكار والتسابيح", R.drawable.ic_search_azkar, isDark) }
                                items(visibleAzkar) { zikr ->
                                    SearchResultCard(
                                        tag = "ذكر",
                                        title = zikr.category,
                                        subtitle = zikr.text,
                                        iconRes = R.drawable.ic_search_azkar,
                                        isDark = isDark,
                                        onClick = { onOpenResult(SearchResultKind.ZIKR, zikr.id.toString()) }
                                    )
                                }
                            }

                            if (showHadith) {
                                item { SearchCategoryHeader("الأحاديث النبوية", R.drawable.ic_search_hadith, isDark) }
                                items(hadithResults) { hadith ->
                                    SearchResultCard(
                                        tag = "حديث نبوي",
                                        title = hadith.text,
                                        subtitle = hadith.narrator.ifBlank { "حديث مأثور" },
                                        iconRes = R.drawable.ic_search_hadith,
                                        isDark = isDark,
                                        onClick = { onOpenResult(SearchResultKind.HADITH, hadith.id.toString()) }
                                    )
                                }
                            }

                            if (showAsma) {
                                item { SearchCategoryHeader("أسماء الله الحسنى", R.drawable.ic_search_asma, isDark) }
                                items(asmaResults) { allahName ->
                                    SearchResultCard(
                                        tag = "اسم من أسماء الله الحسنى",
                                        title = allahName.name,
                                        subtitle = allahName.explanation,
                                        iconRes = R.drawable.ic_search_asma,
                                        isDark = isDark,
                                        onClick = { onOpenResult(SearchResultKind.ASMA, allahName.id.toString()) }
                                    )
                                }
                            }

                            if (showLearning) {
                                item { SearchCategoryHeader("الدروس والفيديو", android.R.drawable.ic_media_play, isDark) }
                                items(learningResults) { hit -> SearchResultCard(tag = hit.source, title = hit.title,
                                    subtitle = "محتوى من الناشر — يتطلب الإنترنت للتشغيل", iconRes = android.R.drawable.ic_media_play,
                                    isDark = isDark, onClick = { onOpenResult(hit.kind, hit.id) }) }
                            }

                            if (showEhsan) {
                                item { SearchCategoryHeader("مشروعات ومبادرات إحسان", R.drawable.ic_search_ehsan, isDark) }
                                items(communityResults) { donation ->
                                    SearchResultCard(
                                        tag = if (donation.origin == "DEMO") "مثال DEMO" else "سجل محلي",
                                        title = donation.title,
                                        subtitle = donation.description,
                                        iconRes = R.drawable.ic_search_ehsan,
                                        isDark = isDark,
                                        onClick = { onOpenResult(SearchResultKind.COMMUNITY, donation.id.toString()) }
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

@Composable
private fun SearchCategoryHeader(title: String, iconRes: Int, isDark: Boolean) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)
    ) {
        SearchSymbol(iconRes, Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) IhsanTheme.colors.accentContent else DeepTeal
        )
    }
}

@Composable
private fun SearchResultCard(
    tag: String,
    title: String,
    subtitle: String,
    iconRes: Int,
    isDark: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) IhsanTheme.colors.surfaceElevated else Color.White,
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFE5EBE8)
        ),
        shadowElevation = 0.5.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDark) IhsanTheme.colors.surfaceMuted else SoftMint),
                contentAlignment = Alignment.Center
            ) {
                SearchSymbol(iconRes, Modifier.size(22.dp))
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) IhsanTheme.colors.accentContent else DeepTeal.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    lineHeight = 22.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) IhsanTheme.colors.textPrimary else Color(0xFF1E2923)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isDark) IhsanTheme.colors.textSecondary else Color.Gray,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = null,
                tint = Color.Gray.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun GlobalSearchScreenPreview() {
    IhsanTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                containerColor = Color(0xFFF9FBFB),
                topBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp))
                            .background(SoftMint)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ihsan_search_header),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                IconButton(
                                    onClick = {},
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.7f))
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "رجوع",
                                        tint = DeepTeal,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Text(
                                    text = "البحث",
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = DeepTeal,
                                    textAlign = TextAlign.Center
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(26.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SoftMintBorder),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = DeepTeal,
                                        modifier = Modifier.size(22.dp)
                                    )

                                    Spacer(modifier = Modifier.width(10.dp))

                                    Text(
                                        text = "... ابحث في كل محتويات إحسان",
                                        fontSize = 14.sp,
                                        color = Color(0xFF8E9BA0),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            ) { padding ->
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 16.dp),
                    modifier = Modifier.padding(padding)
                ) {
                    item {
                        Text(
                            text = "تصفّح المحتوى",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    item {
                        val categories = SearchCategory.entries.toTypedArray()
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            for (r in 0 until 2) {
                                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    for (c in 0 until 4) {
                                        val idx = r * 4 + c
                                        if (idx < categories.size) {
                                            val cat = categories[idx]
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = SoftMint,
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4EFEA)),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .heightIn(min = 36.dp)
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.padding(4.dp)
                                                ) {
                                                    Image(
                                                        painter = painterResource(id = cat.iconRes),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(28.dp)
                                                    )
                                                    Spacer(modifier = Modifier.height(6.dp))
                                                    Text(
                                                        text = cat.title,
                                                        fontSize = 12.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = DeepTeal,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(36.dp)
                                    .height(4.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFD1E3DF))
                            )
                        }
                    }

                    item {
                        Text(
                            text = "عمليات البحث الشائعة",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = DeepTeal,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }

                    val popular = listOf(
                        "سورة الكهف",
                        "دعاء السفر",
                        "أذكار الصباح",
                        "أسماء الله الحسنى",
                        "المسجد الحرام بث مباشر"
                    )

                    items(popular) { item ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE5EBE8)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(SoftMint),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = DeepTeal,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = item,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1E2923),
                                    modifier = Modifier.weight(1f)
                                )
                                Icon(
                                    imageVector = Icons.Default.ChevronLeft,
                                    contentDescription = null,
                                    tint = Color.Gray.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = SoftMint,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0ECE8)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(44.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFEF3C7)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Lightbulb,
                                        contentDescription = null,
                                        tint = if (IhsanTheme.isDark) IhsanTheme.colors.warning else Color(0xFFF59E0B),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "نصيحة",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = DeepTeal
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "استخدم كلمات بسيطة للحصول على نتائج أفضل\nمثل: دعاء، سورة، حديث، صلاة ...",
                                        fontSize = 12.sp,
                                        color = Color(0xFF37474F),
                                        lineHeight = 18.sp
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

@Composable
private fun SearchSymbol(iconRes: Int, modifier: Modifier = Modifier) {
    val symbol = when (iconRes) {
        R.drawable.ic_search_quran -> Icons.Default.MenuBook
        R.drawable.ic_search_dua -> Icons.Default.VolunteerActivism
        R.drawable.ic_search_azkar -> Icons.Default.WbSunny
        R.drawable.ic_search_hadith -> Icons.Default.AutoStories
        R.drawable.ic_search_ehsan -> Icons.Default.FavoriteBorder
        R.drawable.ic_search_asma -> Icons.Default.TextFields
        R.drawable.ic_search_tasbih -> Icons.Default.TouchApp
        android.R.drawable.ic_media_play -> Icons.Default.PlayCircleOutline
        else -> Icons.Default.Mosque
    }
    Icon(symbol, null, modifier = modifier, tint = IhsanTheme.colors.accentContent)
}
