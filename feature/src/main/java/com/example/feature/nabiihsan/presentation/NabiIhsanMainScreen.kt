package com.example.feature.nabiihsan.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.component.IhsanSearchBar
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.nabiihsan.presentation.components.NabiEpisodeCard
import com.example.feature.nabiihsan.presentation.components.NabiFilterSheet
import com.example.feature.nabiihsan.presentation.components.NabiHeroHeader
import com.example.feature.nabiihsan.presentation.components.NabiSectionHeader
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NabiIhsanMainScreen(
    viewModel: NabiIhsanViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onEpisodeClick: (String) -> Unit = {},
    onNavigateToEpisodes: () -> Unit = {},
    onNavigateToRecipes: () -> Unit = {},
    onNavigateToFavorites: () -> Unit = {},
    onNavigateToWatchLater: () -> Unit = {},
    onNavigateToSearch: (String) -> Unit = {},
    onNavigateToSettings: () -> Unit = {}
) {
    val uiState by viewModel.mainUiState.collectAsStateWithLifecycle()

    val taxonomyList = listOf("الكل", "السيرة", "الأخلاق", "العبادة", "الأسرة", "القيادة")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Header
        NabiHeroHeader(
            title = "نبي الإحسان",
            subtitle = "رحلة في السيرة والوصفات النبوية",
            onBackClick = onNavigateBack,
            onSettingsClick = onNavigateToSettings
        )

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Search Bar + Filter Trigger
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        IhsanSearchBar(
                            query = uiState.searchQuery,
                            onQueryChange = { q ->
                                viewModel.onSearchQueryChanged(q)
                                onNavigateToSearch(q)
                            },
                            placeholder = "ابحث عن حلقة، وصفة، موضوع..."
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = { viewModel.setFilterSheetVisible(true) },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(IhsanTheme.dimens.radiusLarge))
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "تصفية",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Taxonomy Filter Chips Row
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    items(taxonomyList) { tag ->
                        val isSelected = uiState.filterState.selectedCategory == tag
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(
                                    if (isSelected) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                                .clickable {
                                    viewModel.onFilterStateChanged(
                                        uiState.filterState.copy(selectedCategory = tag)
                                    )
                                }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tag,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                // Quick Navigation Cards (الوصفات النبوية / المفضلة / المشاهدة لاحقًا)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickNavCard(
                        title = "الوصفات النبوية",
                        icon = Icons.Default.VolunteerActivism,
                        onClick = onNavigateToRecipes,
                        modifier = Modifier.weight(1f)
                    )
                    QuickNavCard(
                        title = "المفضلة",
                        icon = Icons.Default.Favorite,
                        onClick = onNavigateToFavorites,
                        modifier = Modifier.weight(1f)
                    )
                    QuickNavCard(
                        title = "المشاهدة لاحقاً",
                        icon = Icons.Default.WatchLater,
                        onClick = onNavigateToWatchLater,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Episodes Section Header
                NabiSectionHeader(
                    title = "حلقات السلسلة",
                    actionLabel = "عرض الكل",
                    onActionClick = onNavigateToEpisodes
                )

                // Episodes List or Empty/Loading State
                if (uiState.isLoading) {
                    IhsanLoadingState()
                } else if (uiState.filteredEpisodes.isEmpty()) {
                    IhsanEmptyState(
                        title = "لا توجد حلقات",
                        message = "لم نتمكن من العثور على حلقات تطابق البحث أو التصفية الحالية.",
                        primaryActionLabel = "إعادة التعيين",
                        onPrimaryAction = {
                            viewModel.onSearchQueryChanged("")
                            viewModel.onFilterStateChanged(uiState.filterState.copy(selectedCategory = "الكل"))
                        }
                    )
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        uiState.filteredEpisodes.forEach { episode ->
                            NabiEpisodeCard(
                                episode = episode,
                                onEpisodeClick = onEpisodeClick,
                                onFavoriteToggle = { viewModel.toggleEpisodeFavorite(it) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (uiState.isFilterSheetVisible) {
        NabiFilterSheet(
            filterState = uiState.filterState,
            onApplyFilter = { newFilter ->
                viewModel.onFilterStateChanged(newFilter)
            },
            onResetFilter = {
                viewModel.onFilterStateChanged(uiState.filterState.copy(selectedCategory = "الكل"))
            },
            onDismiss = { viewModel.setFilterSheetVisible(false) }
        )
    }
}

@Composable
private fun QuickNavCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(72.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)
        ),
        border = BorderStroke(0.8.dp, IhsanTheme.colors.borderSubtle.copy(alpha = 0.4f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
