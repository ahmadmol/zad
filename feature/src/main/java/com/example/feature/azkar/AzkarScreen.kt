package com.example.feature.azkar

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanSearchBar
import com.example.feature.azkar.domain.model.Zikr
import com.example.feature.azkar.presentation.AzkarAction
import com.example.feature.azkar.presentation.AzkarUiState
import com.example.feature.azkar.presentation.AzkarViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AzkarScreen(
    viewModel: AzkarViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onOpenSebha: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Scaffold(
            topBar = {
                AzkarTopBar(
                    onBack = onNavigateBack,
                    onSearch = { viewModel.onAction(AzkarAction.OnSearchQueryChanged(it)) },
                    searchQuery = uiState.searchQuery,
                    onToggleFavorites = { viewModel.onAction(AzkarAction.OnToggleShowFavorites(!uiState.showFavoritesOnly)) },
                    isFavoritesOnly = uiState.showFavoritesOnly
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                CategoryTabs(
                    selectedCategory = uiState.selectedCategory,
                    onCategorySelected = { viewModel.onAction(AzkarAction.OnCategorySelected(it)) }
                )
                
                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    } else if (uiState.azkarList.isEmpty()) {
                        EmptyState(
                            message = if (uiState.showFavoritesOnly) "لا توجد أذكار في المفضلة" else "لا توجد أذكار متاحة",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    } else {
                        if (uiState.selectedCategory == "سبحة حرة") {
                            val free = uiState.azkarList.find { it.category == "سبحة حرة" }
                            if (free != null) {
                                FreeCounterScreen(free, viewModel::onAction)
                            } else {
                                Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                    Text("لا توجد مسبحة حرة. افتح المسبحات لإدارة المزيد.")
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(onClick = onOpenSebha) { Text("فتح المسبحات") }
                                }
                            }
                        } else {
                            AzkarList(uiState.azkarList, uiState.selectedCategory, uiState.fontSize, viewModel::onAction)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AzkarTopBar(
    onBack: () -> Unit,
    onSearch: (String) -> Unit,
    searchQuery: String,
    onToggleFavorites: () -> Unit,
    isFavoritesOnly: Boolean
) {
    Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
        CenterAlignedTopAppBar(
            title = { Text("الأذكار", fontWeight = FontWeight.Bold) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = onToggleFavorites) {
                    Icon(
                        imageVector = if (isFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorites",
                        tint = if (isFavoritesOnly) Color.Red else Color.Gray
                    )
                }
            }
        )
        IhsanSearchBar(
            query = searchQuery,
            onQueryChange = onSearch,
            placeholder = "بحث في الأذكار...",
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@Composable
private fun CategoryTabs(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit
) {
    val categories = listOf(
        null to "الكل",
        "أذكار الصباح" to "الصباح",
        "أذكار المساء" to "المساء",
        "أذكار بعد الصلاة" to "بعد الصلاة",
        "أذكار النوم" to "النوم",
        "تسابيح عامة" to "تسابيح",
        "سبحة حرة" to "سبحة"
    )

    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
        containerColor = Color.Transparent,
        contentColor = MaterialTheme.colorScheme.primary,
        edgePadding = 16.dp,
        divider = {}
    ) {
        categories.forEach { (id, label) ->
            Tab(
                selected = selectedCategory == id,
                onClick = { onCategorySelected(id) }
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                    fontWeight = if (selectedCategory == id) FontWeight.Bold else FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun AzkarList(
    azkar: List<Zikr>,
    category: String?,
    fontSize: Float,
    onAction: (AzkarAction) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (category != null) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = { onAction(AzkarAction.OnResetCategory(category)) }) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("إعادة ضبط الكل", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
        
        items(azkar, key = { it.id }) { zikr ->
            ZikrCard(zikr = zikr, fontSize = fontSize, onAction = onAction)
        }
    }
}

@Composable
private fun ZikrCard(zikr: Zikr, fontSize: Float, onAction: (AzkarAction) -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (zikr.isCompleted) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (zikr.isCompleted) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
            else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = zikr.title,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = { onAction(AzkarAction.OnToggleFavorite(zikr.id)) }) {
                    Icon(
                        imageVector = if (zikr.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (zikr.isFavorite) Color(0xFFE91E63) else Color.Gray,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = zikr.text,
                style = MaterialTheme.typography.titleLarge.copy(
                    lineHeight = (fontSize * 1.5).sp,
                    textAlign = TextAlign.Center,
                    fontSize = fontSize.sp
                ),
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Box(contentAlignment = Alignment.Center) {
                LinearProgressIndicator(
                    progress = { if (zikr.targetCount > 0) zikr.currentCount.toFloat() / zikr.targetCount.toFloat() else 0f },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = zikr.currentCount.toString(),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = " / ${zikr.targetCount}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onAction(AzkarAction.OnReset(zikr.id)) },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(
                            Icons.Default.RestartAlt, 
                            contentDescription = "Reset", 
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (!zikr.isCompleted) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onAction(AzkarAction.OnIncrement(zikr.id))
                            }
                        },
                        enabled = !zikr.isCompleted,
                        shape = RoundedCornerShape(16.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                        )
                    ) {
                        if (zikr.isCompleted) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اكتمل")
                        } else {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("تسبيح", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FreeCounterScreen(zikr: Zikr, onAction: (AzkarAction) -> Unit) {
    val haptic = LocalHapticFeedback.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Surface(
            modifier = Modifier.padding(bottom = 32.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text(
                "المسبحة الحرة",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Box(
            modifier = Modifier
                .size(300.dp)
                .clip(CircleShape)
                .background(
                    brush = androidx.compose.ui.graphics.Brush.radialGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    )
                )
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onAction(AzkarAction.OnIncrement(zikr.id))
                },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = zikr.currentCount.toString(),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 80.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White
                    )
                )
                Text(
                    "اضغط للتسبيح",
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(48.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = { onAction(AzkarAction.OnReset(zikr.id)) },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.RestartAlt, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تصفير العداد", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(text = message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
