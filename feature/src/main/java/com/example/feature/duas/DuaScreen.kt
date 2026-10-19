package com.example.feature.duas

import androidx.compose.foundation.Image
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.NavigateBefore
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.component.IhsanSearchBar
import com.example.designsystem.component.IhsanTopBar
import com.example.designsystem.component.IhsanTopBarStyle
import com.example.designsystem.theme.IhsanTheme
import com.example.designsystem.theme.DarkGoldAccent
import com.example.designsystem.theme.DarkWarning
import com.example.designsystem.theme.DarkSurfaceWarm
import com.example.feature.R
import com.example.feature.duas.domain.model.Dua
import com.example.feature.duas.presentation.DuaSections
import com.example.feature.duas.presentation.DuaAction
import com.example.feature.duas.presentation.DuaViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun DuaScreen(
    viewModel: DuaViewModel = koinViewModel(),
    onBack: () -> Unit = {},
    onDuaClick: (Long) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
            containerColor = colors.surfaceBase
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                // 1. Header with Mosque Skyline Artwork
                IhsanTopBar(
                    title = "الأدعية",
                    subtitle = "أدعية من الكتاب والسنة",
                    onNavigateBack = {
                        if (uiState.selectedCategory != null || uiState.searchQuery.isNotBlank() || uiState.showFavoritesOnly) {
                            viewModel.onAction(DuaAction.OnCategorySelected(null))
                            viewModel.onAction(DuaAction.OnSearchQueryChanged(""))
                            viewModel.onAction(DuaAction.OnToggleFavoritesOnly(false))
                        } else onBack()
                    },
                    style = IhsanTopBarStyle.Hero, backgroundImageRes = com.example.feature.R.drawable.ihsan_home_hero_background
                )

                // 2. Search Bar
                IhsanSearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = { viewModel.onAction(DuaAction.OnSearchQueryChanged(it)) },
                    placeholder = "ابحث في الأدعية...",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                )

                // 3. Category Chips
                DuaCategoryChips(
                    allDuas = uiState.allDuas,
                    selectedCategory = uiState.selectedCategory,
                    showFavoritesOnly = uiState.showFavoritesOnly,
                    onCategorySelected = { category ->
                        viewModel.onAction(DuaAction.OnToggleFavoritesOnly(false))
                        viewModel.onAction(DuaAction.OnCategorySelected(category))
                    },
                    onFavoritesSelected = {
                        viewModel.onAction(DuaAction.OnCategorySelected(null))
                        viewModel.onAction(DuaAction.OnToggleFavoritesOnly(true))
                    }
                )

                Spacer(modifier = Modifier.height(6.dp))

                // 4. Content Area: Loading / Error / Empty / List
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    when {
                        uiState.isLoading -> {
                            IhsanLoadingState(
                                message = "جارٍ تحميل الأدعية...",
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        uiState.errorMessage != null -> {
                            IhsanErrorState(
                                title = "تعذر تحميل الأدعية",
                                message = uiState.errorMessage,
                                retryLabel = stringResource(R.string.common_retry),
                                onRetry = { viewModel.onAction(DuaAction.Refresh) },
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        uiState.selectedCategory == null && uiState.searchQuery.isBlank() && !uiState.showFavoritesOnly -> {
                            LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                items(DuaSections.titles) { section ->
                                    DuaSectionCard(section, onClick = { viewModel.onAction(DuaAction.OnCategorySelected(section)) })
                                }
                                item { WordOfTheDayCard() }
                            }
                        }

                        uiState.duas.isEmpty() -> {
                            IhsanEmptyState(
                                title = "لا توجد نتائج",
                                message = if (uiState.searchQuery.isNotBlank()) {
                                    "لم نجد دعاءً مطابقًا لبحثك \"${uiState.searchQuery}\"\nجرّب كلمات أخرى"
                                } else if (uiState.showFavoritesOnly) {
                                    "لا توجد أدعية مضافة للمفضلة بعد"
                                } else {
                                    "لا يتوفر محتوى لهذا القسم في البيانات الحالية"
                                },
                                icon = Icons.Outlined.SearchOff,
                                primaryActionLabel = if (uiState.searchQuery.isNotBlank() || uiState.showFavoritesOnly || uiState.selectedCategory != null) {
                                    "إعادة ضبط الفلاتر"
                                } else null,
                                onPrimaryAction = if (uiState.searchQuery.isNotBlank() || uiState.showFavoritesOnly || uiState.selectedCategory != null) {
                                    {
                                        viewModel.onAction(DuaAction.OnSearchQueryChanged(""))
                                        viewModel.onAction(DuaAction.OnCategorySelected(null))
                                        viewModel.onAction(DuaAction.OnToggleFavoritesOnly(false))
                                    }
                                } else null,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(uiState.duas, key = { it.id }) { dua ->
                                    DuaCardItem(
                                        dua = dua,
                                        isDark = isDark,
                                        surfaceMint = colors.surfaceMint,
                                        primaryColor = IhsanTheme.colors.accentContent,
                                        onClick = { onDuaClick(dua.id) },
                                        onToggleFavorite = {
                                            viewModel.onAction(
                                                DuaAction.OnToggleFavorite(dua.id, dua.isFavorite)
                                            )
                                        }
                                    )
                                }

                                // 6. Featured Card ("كلمة اليوم") at bottom
                                item(key = "word_of_day") {
                                    WordOfTheDayCard(
                                        modifier = Modifier.padding(top = 8.dp)
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
private fun DuaCategoryChips(
    allDuas: List<Dua>,
    selectedCategory: String?,
    showFavoritesOnly: Boolean,
    onCategorySelected: (String?) -> Unit,
    onFavoritesSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors
    val chipItems = listOf(null to "الكل",
        DuaSections.titles[0] to "الصباح", DuaSections.titles[1] to "المساء",
        DuaSections.titles[2] to "من القرآن", DuaSections.titles[3] to "نبوية",
        "FAVORITES" to "المفضلة")

    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(chipItems, key = { it.first ?: "ALL" }) { (catId, label) ->
            val isSelected = if (catId == "FAVORITES") {
                showFavoritesOnly
            } else {
                !showFavoritesOnly && selectedCategory == catId
            }

            com.example.designsystem.component.IhsanFilterChip(label, isSelected, {
                if (catId == "FAVORITES") onFavoritesSelected() else onCategorySelected(catId)
            })
        }
    }
}


@Composable
private fun DuaCardItem(
    dua: Dua,
    isDark: Boolean,
    surfaceMint: Color,
    primaryColor: Color,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors
    val iconStyle = remember(dua.category, dua.title, isDark, surfaceMint, primaryColor) {
        getDuaIconStyle(
            category = dua.category,
            title = dua.title,
            isDark = isDark,
            surfaceMint = surfaceMint,
            primaryColor = primaryColor
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = colors.surfaceElevated),
        border = BorderStroke(1.dp, colors.borderSubtle.copy(alpha = 0.7f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon Circle
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(iconStyle.backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = iconStyle.icon,
                    contentDescription = null,
                    tint = iconStyle.iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Title & Subtitle Info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = dua.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = dua.text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )

                if (dua.source.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = dua.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondaryMuted,
                        fontSize = 11.sp
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // End Actions: Favorite toggle & Chevron
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onToggleFavorite,
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = if (dua.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (dua.isFavorite) "إزالة من المفضلة" else "إضافة للمفضلة",
                        tint = if (dua.isFavorite) colors.favorite else colors.textSecondaryMuted,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateBefore,
                    contentDescription = "عرض التفاصيل",
                    tint = colors.textSecondaryMuted,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun WordOfTheDayCard(
    modifier: Modifier = Modifier
) {
    val colors = IhsanTheme.colors
    val isDark = IhsanTheme.isDark

    val cardBgBrush = if (isDark) {
        Brush.horizontalGradient(
            colors = listOf(
                colors.surfaceMint,
                colors.surfaceElevated
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                Color(0xFFE8F5E9),
                Color(0xFFE0F2F1)
            )
        )
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        border = BorderStroke(1.dp, if (isDark) colors.borderSubtle else Color(0xFFA5D6A7).copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgBrush)
                .padding(12.dp)
        ) {
            Column {
                // Badge
                Surface(
                    color = IhsanTheme.colors.accentContent.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Spa,
                            contentDescription = null,
                            tint = IhsanTheme.colors.accentContent,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "كلمة اليوم",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = IhsanTheme.colors.accentContent
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "الدعاء صلة بين العبد وربه",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "معًا ... لنعيش حياة أقرب إلى الله",
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }
        }
    }
}

private data class DuaIconStyle(
    val icon: ImageVector,
    val backgroundColor: Color,
    val iconTint: Color
)

private fun getDuaIconStyle(
    category: String,
    title: String,
    isDark: Boolean,
    surfaceMint: Color,
    primaryColor: Color
): DuaIconStyle {
    return when {
        category.contains("قرآن") || title.contains("قرآن") -> DuaIconStyle(
            icon = Icons.AutoMirrored.Filled.MenuBook,
            backgroundColor = if (isDark) surfaceMint else Color(0xFFE8F5E9),
            iconTint = if (isDark) primaryColor else Color(0xFF2E7D32)
        )
        category.contains("سنة") || category.contains("نبوية") || title.contains("استخارة") -> DuaIconStyle(
            icon = Icons.Default.Mosque,
            backgroundColor = if (isDark) surfaceMint else Color(0xFFE0F2F1),
            iconTint = if (isDark) primaryColor else Color(0xFF00695C)
        )
        category.contains("صباح") || title.contains("صباح") -> DuaIconStyle(
            icon = Icons.Default.WbSunny,
            backgroundColor = if (isDark) DarkSurfaceWarm else Color(0xFFFFF8E1),
            iconTint = if (isDark) DarkGoldAccent else Color(0xFFF57F17)
        )
        category.contains("مساء") || category.contains("نوم") || title.contains("مساء") -> DuaIconStyle(
            icon = Icons.Default.NightsStay,
            backgroundColor = if (isDark) surfaceMint else Color(0xFFE3F2FD),
            iconTint = if (isDark) primaryColor else Color(0xFF1976D2)
        )
        category.contains("هم") || category.contains("حزن") || category.contains("شدائد") -> DuaIconStyle(
            icon = Icons.Default.VolunteerActivism,
            backgroundColor = if (isDark) surfaceMint else Color(0xFFEDE7F6),
            iconTint = if (isDark) primaryColor else Color(0xFF5E35B1)
        )
        category.contains("سفر") -> DuaIconStyle(
            icon = Icons.Default.Explore,
            backgroundColor = if (isDark) DarkSurfaceWarm else Color(0xFFFFF3E0),
            iconTint = if (isDark) DarkWarning else Color(0xFFE65100)
        )
        category.contains("رزق") -> DuaIconStyle(
            icon = Icons.Default.Spa,
            backgroundColor = if (isDark) surfaceMint else Color(0xFFF1F8E9),
            iconTint = if (isDark) primaryColor else Color(0xFF558B2F)
        )
        else -> DuaIconStyle(
            icon = Icons.Default.FormatQuote,
            backgroundColor = surfaceMint,
            iconTint = primaryColor
        )
    }
}

@Composable
private fun DuaSectionCard(title: String, onClick: () -> Unit) {
    val icons = listOf(Icons.Default.WbSunny, Icons.Default.NightsStay, Icons.AutoMirrored.Filled.MenuBook,
        Icons.Default.Mosque, Icons.Default.People, Icons.Default.VolunteerActivism, Icons.Default.MoreHoriz)
    val descriptions = listOf("أدعية لبدء يومك بذكر الله", "أدعية المساء", "أدعية وردت في كتاب الله تعالى",
        "أدعية من السنة النبوية", "أدعية لمن تحب", "أدعية عند الكرب والهم", "مجموعة مختارة من الأدعية")
    val index = DuaSections.titles.indexOf(title)
    Surface(onClick = onClick, color = IhsanTheme.colors.surfaceElevated, shape = RoundedCornerShape(20.dp),
        shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(color = IhsanTheme.colors.surfaceMint, shape = CircleShape, modifier = Modifier.size(52.dp)) {
                Box(contentAlignment = Alignment.Center) { Icon(icons[index], null, tint = if(index == 0)
                    IhsanTheme.colors.goldAccent else IhsanTheme.colors.accentContent, modifier = Modifier.size(28.dp)) }
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(title, fontSize = 17.sp, lineHeight = 24.sp, fontWeight = FontWeight.Bold, color = IhsanTheme.colors.accentContent)
                Text(descriptions[index], fontSize = 12.sp, lineHeight = 18.sp, color = IhsanTheme.colors.textSecondary)
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, null, tint = IhsanTheme.colors.accentContent)
        }
    }
}
