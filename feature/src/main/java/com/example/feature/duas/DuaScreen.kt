package com.example.feature.duas

import android.content.Intent
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanSearchBar
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.duas.domain.model.Dua
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
    val selectedTab = if (uiState.showFavoritesOnly) 1 else 0

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                DuaTopBar(
                    onBack = onBack,
                    onSearch = { viewModel.onAction(DuaAction.OnSearchQueryChanged(it)) },
                    searchQuery = uiState.searchQuery,
                    title = if (selectedTab == 1) "المفضلة" else "الأدعية"
                )
            },
            containerColor = colors.surfaceMuted
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                
                if (uiState.searchQuery.isEmpty()) {
                    DuaSegmentedTabs(
                        showFavoritesOnly = uiState.showFavoritesOnly,
                        onSelectionChanged = {
                            viewModel.onAction(DuaAction.OnToggleFavoritesOnly(it))
                        }
                    )

                    if (selectedTab == 0) {
                        CategoryChips(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { viewModel.onAction(DuaAction.OnCategorySelected(it)) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center), color = MaterialTheme.colorScheme.primary)
                    } else if (uiState.errorMessage != null) {
                        ErrorState(
                            message = stringResource(com.example.feature.R.string.dua_load_failed),
                            onRetry = { viewModel.onAction(DuaAction.Refresh) },
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else if (uiState.duas.isEmpty()) {
                        EmptyState(
                            query = uiState.searchQuery,
                            isFavorite = selectedTab == 1,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(uiState.duas, key = { it.id }) { dua ->
                                DuaCard(
                                    dua = dua,
                                    onToggleFavorite = { viewModel.onAction(DuaAction.OnToggleFavorite(dua.id, dua.isFavorite)) },
                                    onClick = { onDuaClick(dua.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuaSegmentedTabs(
    showFavoritesOnly: Boolean,
    onSelectionChanged: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth()
            .height(52.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(IhsanTheme.colors.surfaceElevated)
            .border(1.dp, IhsanTheme.colors.borderSubtle, RoundedCornerShape(18.dp))
            .padding(4.dp)
    ) {
        listOf(
            false to stringResource(com.example.feature.R.string.dua_tab_all),
            true to stringResource(com.example.feature.R.string.dua_tab_favorites)
        ).forEach { (favorites, label) ->
            val selected = showFavoritesOnly == favorites
            Surface(
                onClick = { onSelectionChanged(favorites) },
                modifier = Modifier.weight(1f).fillMaxHeight(),
                shape = RoundedCornerShape(14.dp),
                color = if (selected) IhsanTheme.colors.selectedContainer else Color.Transparent,
                contentColor = if (selected) IhsanTheme.colors.selectedContent else IhsanTheme.colors.textSecondary
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(label, fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuaTopBar(onBack: () -> Unit, onSearch: (String) -> Unit, searchQuery: String, title: String) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        CenterAlignedTopAppBar(
            title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                }
            },
            colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
        )
        IhsanSearchBar(
            query = searchQuery,
            onQueryChange = onSearch,
            placeholder = "ابحث في الأدعية...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategoryChips(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = listOf(
        null to "الكل",
        "أدعية قرآنية" to "من القرآن",
        "أدعية من السنة" to "من السنة",
        "النوم" to "النوم",
        "السفر" to "السفر",
        "الرزق" to "الرزق",
        "الهم والحزن" to "الهم"
    )

    val colors = IhsanTheme.colors

    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp,
        divider = {},
        indicator = {}
    ) {
        categories.forEach { (id, label) ->
            val isSelected = selectedCategory == id
            Tab(
                selected = isSelected,
                onClick = { onCategorySelected(id) },
                modifier = Modifier
                    .padding(vertical = 8.dp, horizontal = 4.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                    .border(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else colors.borderSubtle,
                        RoundedCornerShape(20.dp)
                    )
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else colors.textSecondaryMuted,
                    fontSize = 13.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun DuaCard(
    dua: Dua,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val colors = IhsanTheme.colors

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dua.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    color = colors.surfaceMint,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = dua.category.split(" ").last(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = dua.text,
                style = MaterialTheme.typography.bodyMedium.copy(
                    lineHeight = 26.sp,
                    textAlign = TextAlign.Start
                ),
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(48.dp)) {
                        Icon(
                            imageVector = if (dua.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (dua.isFavorite) "إزالة من المفضلة" else "إضافة إلى المفضلة",
                            tint = if (dua.isFavorite) colors.favorite else colors.textSecondaryMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(dua.text))
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "نسخ", tint = colors.textSecondaryMuted, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${dua.title}\n\n${dua.text}\n\nالمصدر: ${dua.source}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "مشاركة", tint = colors.textSecondaryMuted, modifier = Modifier.size(18.dp))
                    }
                }
                
                Text(
                    text = dua.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.textSecondaryMuted,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(message, textAlign = TextAlign.Center, color = IhsanTheme.colors.textSecondary)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry, modifier = Modifier.heightIn(min = 48.dp)) {
            Text(stringResource(com.example.feature.R.string.common_retry))
        }
    }
}

@Composable
private fun EmptyState(query: String, isFavorite: Boolean, modifier: Modifier = Modifier) {
    val colors = IhsanTheme.colors

    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(colors.quickActionSurface, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (query.isNotEmpty()) Icons.Default.SearchOff else if (isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = colors.textSecondaryMuted.copy(alpha = 0.6f)
            )
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = when {
                query.isNotEmpty() -> "لم يتم العثور على نتائج لـ \"$query\""
                isFavorite -> "لا توجد أدعية في المفضلة بعد"
                else -> "لا توجد أدعية متاحة حالياً"
            },
            style = MaterialTheme.typography.titleMedium,
            color = colors.textSecondaryMuted,
            textAlign = TextAlign.Center
        )
        if (query.isNotEmpty()) {
            Text(
                text = "جرب كلمة بحث أخرى أو تصفح الأقسام المختلفة",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondaryMuted.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
