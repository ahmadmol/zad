package com.example.feature.sanhya.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.sanhya.data.datasource.SanhyaFixtureData
import com.example.feature.sanhya.domain.model.StoryFilterState
import com.example.feature.sanhya.presentation.components.QuranStoryCard
import com.example.feature.sanhya.presentation.components.StoryEmptyState
import com.example.feature.sanhya.presentation.components.StorySearchBar
import com.example.feature.sanhya.presentation.components.StoryTagChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SanhyaMainScreen(
    viewModel: SanhyaViewModel,
    onNavigateBack: () -> Unit,
    onStoryClick: (storyId: String) -> Unit,
    onNavigateToFavorites: () -> Unit,
    onNavigateToWatchLater: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.mainUiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(if (IhsanTheme.isDark) IhsanTheme.colors.surfaceBase else MaterialTheme.colorScheme.surface)
    ) {
        com.example.designsystem.component.IhsanTopBar(
            title = "سنحيا بالقرآن",
            subtitle = "قصص من القرآن.. نفهمها ونعيش معانيها",
            style = com.example.designsystem.component.IhsanTopBarStyle.Hero,
            onNavigateBack = onNavigateBack,
            actions = {
                IconButton(onNavigateToWatchLater) { Icon(Icons.Default.WatchLater, "المشاهدة لاحقًا", tint = IhsanTheme.colors.accentContent) }
                IconButton(onNavigateToFavorites) { Icon(Icons.Default.Favorite, "المفضلة", tint = IhsanTheme.colors.accentContent) }
                IconButton(onNavigateToSettings) { Icon(Icons.Default.Settings, "الإعدادات", tint = IhsanTheme.colors.accentContent) }
            }
        )

        // Main Surface with Rounded Top Corners
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
            ) {
                // Search Bar
                Box(modifier = Modifier.padding(horizontal = 16.dp)) {
                    StorySearchBar(
                        query = uiState.searchQuery,
                        onQueryChange = viewModel::onSearchQueryChanged,
                        onFilterClick = { viewModel.setFilterSheetVisible(true) },
                        placeholderText = "ابحث عن قصة، سورة، شخصية أو معنى..."
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Category Chips Row
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(SanhyaFixtureData.categories) { category ->
                        StoryTagChip(
                            text = category,
                            isSelected = uiState.filterState.selectedCategory == category,
                            onClick = {
                                viewModel.onFilterStateChanged(
                                    uiState.filterState.copy(selectedCategory = category)
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Stories List
                if (uiState.filteredStories.isEmpty()) {
                    StoryEmptyState(
                        title = "لا توجد نتائج",
                        message = "لم نجد أي قصص مطابقة لبحثك، حاول استخدام كلمات أو تصنيفات أخرى.",
                        onRetryClick = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.onFilterStateChanged(StoryFilterState())
                        }
                    )
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(
                            items = uiState.filteredStories,
                            key = { it.id }
                        ) { story ->
                            QuranStoryCard(
                                story = story,
                                onClick = { onStoryClick(story.id) },
                                onFavoriteClick = { viewModel.toggleFavorite(story.id) }
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.isFilterSheetVisible) {
        SanhyaFilterSheet(
            filterState = uiState.filterState,
            sheetState = sheetState,
            onDismiss = { viewModel.setFilterSheetVisible(false) },
            onApply = { newFilter ->
                viewModel.onFilterStateChanged(newFilter)
                viewModel.setFilterSheetVisible(false)
            }
        )
    }
}
