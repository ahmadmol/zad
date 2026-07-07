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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanSearchBar
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
    var selectedTab by remember { mutableIntStateOf(0) } // 0: All, 1: Favorites

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
            containerColor = Color(0xFFF9F9F9)
        ) { padding ->
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                
                if (uiState.searchQuery.isEmpty()) {
                    TabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = Color.White,
                        contentColor = Color(0xFF0D4D3D),
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                                color = Color(0xFF0D4D3D)
                            )
                        }
                    ) {
                        Tab(selected = selectedTab == 0, onClick = { 
                            selectedTab = 0 
                            viewModel.onAction(DuaAction.OnToggleFavoritesOnly(false))
                        }) {
                            Text("الكل", modifier = Modifier.padding(16.dp))
                        }
                        Tab(selected = selectedTab == 1, onClick = { 
                            selectedTab = 1 
                            viewModel.onAction(DuaAction.OnToggleFavoritesOnly(true))
                        }) {
                            Text("المفضلة", modifier = Modifier.padding(16.dp))
                        }
                    }

                    if (selectedTab == 0) {
                        CategoryChips(
                            selectedCategory = uiState.selectedCategory,
                            onCategorySelected = { viewModel.onAction(DuaAction.OnCategorySelected(it)) }
                        )
                    }
                }

                Box(modifier = Modifier.weight(1f)) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(Modifier.align(Alignment.Center), color = Color(0xFF0D4D3D))
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DuaTopBar(onBack: () -> Unit, onSearch: (String) -> Unit, searchQuery: String, title: String) {
    Column(modifier = Modifier.background(Color.White)) {
        CenterAlignedTopAppBar(
            title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
            },
            actions = {
                IconButton(onClick = { /* Menu */ }) {
                    Icon(Icons.Default.Menu, contentDescription = "Menu")
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

    ScrollableTabRow(
        selectedTabIndex = categories.indexOfFirst { it.first == selectedCategory }.coerceAtLeast(0),
        containerColor = Color.Transparent,
        contentColor = Color(0xFF0D4D3D),
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
                    .background(if (isSelected) Color(0xFF0D4D3D) else Color.White)
                    .border(1.dp, if (isSelected) Color(0xFF0D4D3D) else Color.LightGray.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (isSelected) Color.White else Color.Gray,
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

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    color = Color(0xFFE8F5E9),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = dua.category.split(" ").last(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        color = Color(0xFF0D4D3D),
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
                color = Color.DarkGray
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onToggleFavorite, modifier = Modifier.size(32.dp)) {
                        Icon(
                            imageVector = if (dua.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorite",
                            tint = if (dua.isFavorite) Color.Red else Color.Gray,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(onClick = {
                        clipboardManager.setText(AnnotatedString(dua.text))
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                    IconButton(onClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, "${dua.title}\n\n${dua.text}\n\nالمصدر: ${dua.source}")
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
                
                Text(
                    text = dua.source,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun EmptyState(query: String, isFavorite: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(Color(0xFFF5F5F5), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (query.isNotEmpty()) Icons.Default.SearchOff else if (isFavorite) Icons.Default.FavoriteBorder else Icons.Default.Info,
                contentDescription = null,
                modifier = Modifier.size(60.dp),
                tint = Color.LightGray
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
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        if (query.isNotEmpty()) {
            Text(
                text = "جرب كلمة بحث أخرى أو تصفح الأقسام المختلفة",
                style = MaterialTheme.typography.bodySmall,
                color = Color.LightGray,
                modifier = Modifier.padding(top = 8.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}
