package com.example.feature.sanhya.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.feature.sanhya.presentation.components.QuranStoryCard
import com.example.feature.sanhya.presentation.components.StoryEmptyState
import com.example.feature.sanhya.presentation.components.StorySearchBar

@Composable
fun SanhyaSearchResultsScreen(
    initialQuery: String,
    viewModel: SanhyaViewModel,
    onNavigateBack: () -> Unit,
    onStoryClick: (storyId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.mainUiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty()) {
            viewModel.onSearchQueryChanged(initialQuery)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Top Bar & Search Input
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            StorySearchBar(
                query = uiState.searchQuery,
                onQueryChange = viewModel::onSearchQueryChanged,
                onFilterClick = { viewModel.setFilterSheetVisible(true) },
                placeholderText = "ابحث عن قصة، سورة، شخصية أو معنى...",
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (uiState.filteredStories.isEmpty()) {
            StoryEmptyState(
                title = "لا توجد نتائج",
                message = "لم نجد أي قصص مطابقة لبحثك حاول استخدام كلمات أخرى",
                onRetryClick = { viewModel.onSearchQueryChanged("") }
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
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
