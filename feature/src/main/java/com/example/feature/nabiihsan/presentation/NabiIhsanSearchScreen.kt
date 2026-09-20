package com.example.feature.nabiihsan.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanErrorState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.component.IhsanSearchBar
import com.example.feature.nabiihsan.presentation.components.NabiEpisodeCard
import com.example.feature.nabiihsan.presentation.components.NabiRecipeCard
import com.example.feature.nabiihsan.presentation.components.NabiSectionHeader
import org.koin.androidx.compose.koinViewModel

@Composable
fun NabiIhsanSearchScreen(
    initialQuery: String = "",
    viewModel: NabiIhsanViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onEpisodeClick: (String) -> Unit = {},
    onRecipeClick: (String) -> Unit = {}
) {
    val searchState by viewModel.searchUiState.collectAsStateWithLifecycle()

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            viewModel.performSearch(initialQuery, searchState.filterState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
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

            Text(
                text = "نتائج البحث",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Search Input Field
            IhsanSearchBar(
                query = searchState.query,
                onQueryChange = { q ->
                    viewModel.performSearch(q, searchState.filterState)
                },
                placeholder = "ابحث عن حلقة، وصفة، موضوع..."
            )

            if (searchState.isLoading) {
                IhsanLoadingState(modifier = Modifier.fillMaxSize())
            } else if (searchState.errorMessage != null) {
                IhsanErrorState(
                    title = "حدث خطأ",
                    message = searchState.errorMessage ?: "تعذر تحميل نتائج البحث",
                    retryLabel = "إعادة المحاولة",
                    onRetry = { viewModel.performSearch(searchState.query, searchState.filterState) },
                    modifier = Modifier.fillMaxSize()
                )
            } else if (searchState.query.isNotBlank() && searchState.searchResult.isEmpty) {
                IhsanEmptyState(
                    title = "لا توجد نتائج",
                    message = "لم يتم العثور على أي حلقات أو وصفات تدعم كلمات البحث مدخلة.",
                    primaryActionLabel = "إعادة المحاولة",
                    onPrimaryAction = { viewModel.performSearch("", searchState.filterState) },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // Episodes Results
                if (searchState.searchResult.episodes.isNotEmpty()) {
                    NabiSectionHeader(title = "الحلقات (${searchState.searchResult.episodes.size})")
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        searchState.searchResult.episodes.forEach { episode ->
                            NabiEpisodeCard(
                                episode = episode,
                                onEpisodeClick = onEpisodeClick,
                                onFavoriteToggle = { viewModel.toggleEpisodeFavorite(it) }
                            )
                        }
                    }
                }

                // Recipes Results
                if (searchState.searchResult.recipes.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    NabiSectionHeader(title = "الوصفات (${searchState.searchResult.recipes.size})")
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        searchState.searchResult.recipes.forEach { recipe ->
                            NabiRecipeCard(
                                recipe = recipe,
                                onRecipeClick = onRecipeClick
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
