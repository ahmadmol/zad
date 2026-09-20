package com.example.feature.fahmanallah.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.fahmanallah.presentation.components.FahmEpisodeCard

@Composable
fun FahmSavedScreen(
    initialTab: Int = 0,
    viewModel: FahmViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.savedState.collectAsStateWithLifecycle()
    val primary = MaterialTheme.colorScheme.primary

    LaunchedEffect(initialTab) {
        viewModel.setSavedTab(initialTab)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "رجوع",
                    tint = IhsanTheme.colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            Text(
                text = "المحفوظات",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tabs: الدروس المحفوظة (0) | مشاهدة لاحقًا (1)
        val tabs = listOf("الدروس المحفوظة", "مشاهدة لاحقًا")
        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab.coerceIn(0, tabs.lastIndex)]),
                    color = primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = { viewModel.setSavedTab(index) },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.5.sp,
                            fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        val currentList = if (state.selectedTab == 0) state.favoriteEpisodes else state.watchLaterEpisodes

        if (currentList.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IhsanEmptyState(
                    title = if (state.selectedTab == 0) "لا توجد دروس محفوظة" else "قائمة المشاهدة لاحقًا فارغة",
                    message = "احفظ الدروس أثناء المتابعة للرجوع إليها بسهولة"
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = currentList,
                    key = { ep -> ep.id }
                ) { episode ->
                    FahmEpisodeCard(
                        episode = episode,
                        onEpisodeClick = { onNavigateToDetail(it) },
                        onFavoriteToggle = { viewModel.toggleFavorite(it) },
                        onWatchLaterToggle = { viewModel.toggleWatchLater(it) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
