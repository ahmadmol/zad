package com.example.feature.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
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
private val LightCream = Color(0xFFF7F9F8)
private val DarkSurface = Color(0xFF141D1B)
private val DarkCardBackground = Color(0xFF1F2A28)

private enum class SearchCategory(val title: String, val iconRes: Int) {
    ALL("الكل", R.drawable.ihsan_icon_search),
    QURAN("القرآن", R.drawable.ic_search_quran),
    AZKAR("الأذكار", R.drawable.ic_search_azkar),
    DUA("الأدعية", R.drawable.ic_search_dua),
    HADITH("الأحاديث", R.drawable.ic_search_hadith),
    PRAYER("الصلاة", R.drawable.ihsan_icon_prayer),
    TASBIH("التسبيح", R.drawable.ic_search_tasbih),
    ASMA("أسماء الله", R.drawable.ic_search_asma),
    EHSAN("إحسان", R.drawable.ic_search_ehsan)
}

@Composable
fun GlobalSearchScreen(
    onBack: () -> Unit,
    onNavigateToQuran: (Int, Int?) -> Unit,
    onNavigateToDua: (Long) -> Unit,
    onNavigateToAzkar: () -> Unit = {},
    onNavigateToHadith: () -> Unit = {},
    onNavigateToAsma: () -> Unit = {},
    onNavigateToPrayer: () -> Unit = {},
    onNavigateToQibla: () -> Unit = {},
    onNavigateToDonations: () -> Unit = {},
    quranViewModel: QuranViewModel = koinViewModel(),
    duaViewModel: DuaViewModel = koinViewModel(),
    azkarViewModel: AzkarViewModel = koinViewModel(),
    hadithViewModel: HadithViewModel = koinViewModel(),
    asmaViewModel: AsmaViewModel = koinViewModel(),
    ehsanViewModel: EhsanViewModel = koinViewModel()
) {
    var query by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(SearchCategory.ALL) }

    val quranState by quranViewModel.uiState.collectAsStateWithLifecycle()
    val duaState by duaViewModel.uiState.collectAsStateWithLifecycle()
    val azkarState by azkarViewModel.uiState.collectAsStateWithLifecycle()
    val hadithState by hadithViewModel.uiState.collectAsStateWithLifecycle()
    val asmaState by asmaViewModel.uiState.collectAsStateWithLifecycle()
    val ehsanState by ehsanViewModel.uiState.collectAsStateWithLifecycle()

    val focusManager = LocalFocusManager.current
    val isDark = isSystemInDarkTheme()

    LaunchedEffect(query) {
        if (query.isNotBlank()) {
            quranViewModel.onAction(QuranAction.Search(query))
            duaViewModel.onAction(DuaAction.OnSearchQueryChanged(query))
            azkarViewModel.onAction(AzkarAction.OnSearchQueryChanged(query))
            hadithViewModel.onAction(HadithAction.OnSearchQueryChanged(query))
            asmaViewModel.onAction(AsmaAction.OnSearchQueryChange(query))
            ehsanViewModel.onSearchQueryChange(query)
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            containerColor = if (isDark) DarkSurface else LightCream,
            topBar = {
                // Custom Header with Mosque Skyline Artwork
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(170.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ihsan_search_header),
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )

                    if (isDark) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.35f))
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .statusBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Navigation Back Icon & Title
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "رجوع",
                                    tint = if (isDark) Color.White else DeepTeal
                                )
                            }
                            Column(modifier = Modifier.padding(start = 8.dp)) {
                                Text(
                                    text = "البحث",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isDark) Color.White else DeepTeal
                                )
                                Text(
                                    text = "ابحث في محتوى إحسان",
                                    fontSize = 12.sp,
                                    color = if (isDark) Color.White.copy(alpha = 0.8f) else DeepTeal.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Large Rounded Search Field
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(26.dp),
                            color = if (isDark) DarkCardBackground else Color.White,
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
                                    tint = if (isDark) Color(0xFF80CBC4) else DeepTeal,
                                    modifier = Modifier.size(24.dp)
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                TextField(
                                    value = query,
                                    onValueChange = { query = it },
                                    placeholder = {
                                        Text(
                                            text = "ابحث عن سورة، دعاء، حديث، ذكر...",
                                            fontSize = 14.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.5f) else Color.Gray
                                        )
                                    },
                                    singleLine = true,
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent,
                                        cursorColor = if (isDark) Color(0xFF80CBC4) else DeepTeal,
                                        focusedTextColor = if (isDark) Color.White else DeepTeal,
                                        unfocusedTextColor = if (isDark) Color.White else DeepTeal
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                                    modifier = Modifier.weight(1f)
                                )

                                if (query.isNotEmpty()) {
                                    IconButton(
                                        onClick = { query = "" },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "مسح",
                                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Category Filter Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(SearchCategory.entries.toTypedArray()) { category ->
                        val isSelected = selectedCategory == category
                        Surface(
                            onClick = {
                                selectedCategory = category
                                when (category) {
                                    SearchCategory.PRAYER -> onNavigateToPrayer()
                                    else -> {}
                                }
                            },
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) {
                                if (isDark) Color(0xFF00695C) else DeepTeal
                            } else {
                                if (isDark) DarkCardBackground else Color.White
                            },
                            shadowElevation = if (isSelected) 2.dp else 1.dp,
                            modifier = Modifier.height(38.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = category.iconRes),
                                    contentDescription = category.title,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = category.title,
                                    fontSize = 13.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else (if (isDark) Color.White.copy(alpha = 0.85f) else DeepTeal)
                                )
                            }
                        }
                    }
                }

                // Main Content Body: Suggestions vs Results vs Empty
                if (query.isBlank()) {
                    // Initial State: Suggestions & Tip Card
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        item {
                            Text(
                                text = "اقتراحات للبحث",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else DeepTeal,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }

                        val suggestions = listOf(
                            "سورة الكهف",
                            "دعاء السفر",
                            "أذكار الصباح",
                            "أسماء الله الحسنى",
                            "اتجاه القبلة"
                        )

                        items(suggestions) { suggestion ->
                            Surface(
                                onClick = {
                                    if (suggestion == "اتجاه القبلة") {
                                        onNavigateToQibla()
                                    } else {
                                        query = suggestion
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isDark) DarkCardBackground else Color.White,
                                shadowElevation = 1.dp,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 16.dp, vertical = 14.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = null,
                                        tint = if (isDark) Color(0xFF80CBC4) else DeepTeal.copy(alpha = 0.7f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = suggestion,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = if (isDark) Color.White else Color(0xFF1E2923),
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
                            // Tip Card
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                color = if (isDark) Color(0xFF1B2E2B) else Color(0xFFEFF7F5),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(if (isDark) Color(0xFF0F3832) else Color(0xFFD0EBE5)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lightbulb,
                                            contentDescription = "نصيحة",
                                            tint = if (isDark) Color(0xFFFFD54F) else Color(0xFFE65100),
                                            modifier = Modifier.size(20.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = "نصيحة",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isDark) Color.White else DeepTeal
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "استخدم كلمات بسيطة للحصول على نتائج أفضل مثل: دعاء، سورة، حديث، صلاة...",
                                            fontSize = 12.sp,
                                            color = if (isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF37474F),
                                            lineHeight = 18.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Search Results List
                    val hasQuranResults = (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.QURAN) && quranState.searchResults.isNotEmpty()
                    val hasDuaResults = (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.DUA) && duaState.duas.isNotEmpty()
                    val hasAzkarResults = (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.AZKAR) && azkarState.azkarList.isNotEmpty()
                    val hasHadithResults = (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.HADITH) && hadithState.hadiths.isNotEmpty()
                    val hasAsmaResults = (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.ASMA) && asmaState.searchResults.isNotEmpty()
                    val hasEhsanResults = (selectedCategory == SearchCategory.ALL || selectedCategory == SearchCategory.EHSAN) && ehsanState.donations.isNotEmpty()

                    val hasAnyResult = hasQuranResults || hasDuaResults || hasAzkarResults || hasHadithResults || hasAsmaResults || hasEhsanResults

                    if (!hasAnyResult) {
                        // Empty State
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
                                tint = if (isDark) Color.White.copy(alpha = 0.4f) else Color.Gray.copy(alpha = 0.4f),
                                modifier = Modifier.size(64.dp)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "لم نجد نتائج مطابقة لـ \"$query\"",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isDark) Color.White else DeepTeal
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "جرب البحث بكلمات أخرى أو اختر إحدى الفئات أعلاه.",
                                fontSize = 13.sp,
                                color = if (isDark) Color.White.copy(alpha = 0.6f) else Color.Gray
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Quran Results
                            if (hasQuranResults) {
                                item { SearchCategoryHeader("القرآن الكريم", R.drawable.ic_search_quran, isDark) }
                                items(quranState.searchResults) { verse ->
                                    SearchResultCard(
                                        tag = "آية قرآنية",
                                        title = "سورة ${verse.surahId} — آية ${verse.verseNumber}",
                                        subtitle = verse.text,
                                        iconRes = R.drawable.ic_search_quran,
                                        isDark = isDark,
                                        onClick = { onNavigateToQuran(verse.surahId, verse.verseNumber) }
                                    )
                                }
                            }

                            // Dua Results
                            if (hasDuaResults) {
                                item { SearchCategoryHeader("الأدعية المأثورة", R.drawable.ic_search_dua, isDark) }
                                items(duaState.duas) { dua ->
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

                            // Azkar Results
                            if (hasAzkarResults) {
                                item { SearchCategoryHeader("الأذكار والتسابيح", R.drawable.ic_search_azkar, isDark) }
                                items(azkarState.azkarList) { zikr ->
                                    SearchResultCard(
                                        tag = "ذكر",
                                        title = zikr.category,
                                        subtitle = zikr.text,
                                        iconRes = R.drawable.ic_search_azkar,
                                        isDark = isDark,
                                        onClick = { onNavigateToAzkar() }
                                    )
                                }
                            }

                            // Hadith Results
                            if (hasHadithResults) {
                                item { SearchCategoryHeader("الأحاديث النبوية", R.drawable.ic_search_hadith, isDark) }
                                items(hadithState.hadiths) { hadith ->
                                    SearchResultCard(
                                        tag = "حديث نبوي",
                                        title = hadith.narrator.ifBlank { "حديث مأثور" },
                                        subtitle = hadith.text,
                                        iconRes = R.drawable.ic_search_hadith,
                                        isDark = isDark,
                                        onClick = { onNavigateToHadith() }
                                    )
                                }
                            }

                            // Asma Allah Results
                            if (hasAsmaResults) {
                                item { SearchCategoryHeader("أسماء الله الحسنى", R.drawable.ic_search_asma, isDark) }
                                items(asmaState.searchResults) { allahName ->
                                    SearchResultCard(
                                        tag = "اسم من أسماء الله الحسنى",
                                        title = allahName.name,
                                        subtitle = allahName.meaning,
                                        iconRes = R.drawable.ic_search_asma,
                                        isDark = isDark,
                                        onClick = { onNavigateToAsma() }
                                    )
                                }
                            }

                            // Ehsan Results
                            if (hasEhsanResults) {
                                item { SearchCategoryHeader("مشروعات ومبادرات إحسان", R.drawable.ic_search_ehsan, isDark) }
                                items(ehsanState.donations) { donation ->
                                    SearchResultCard(
                                        tag = "مشروع خيري",
                                        title = donation.title,
                                        subtitle = donation.description,
                                        iconRes = R.drawable.ic_search_ehsan,
                                        isDark = isDark,
                                        onClick = { onNavigateToDonations() }
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
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = if (isDark) Color(0xFF80CBC4) else DeepTeal
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
        color = if (isDark) DarkCardBackground else Color.White,
        shadowElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(if (isDark) Color(0xFF0F3832) else Color(0xFFE8F5E9)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDark) Color(0xFF80CBC4) else DeepTeal.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDark) Color.White else Color(0xFF1E2923)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else Color.Gray,
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
