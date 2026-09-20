package com.example.feature.fahmanallah.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.fahmanallah.presentation.components.FahmEpisodeCard
import com.example.feature.fahmanallah.presentation.components.FahmStationCard

@Composable
fun FahmEpisodesScreen(
    viewModel: FahmViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToStations: () -> Unit,
    onNavigateToSearch: () -> Unit,
    onNavigateToSaved: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.episodesState.collectAsStateWithLifecycle()
    var filterQuery by remember { mutableStateOf("") }
    val primary = MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        // Top Bar Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "رجوع",
                        tint = IhsanTheme.colors.textPrimary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = "الفهم عن الله",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = IhsanTheme.colors.textPrimary
                    )
                    Text(
                        text = "الجزء الأول • رمضان 2023",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )
                }
            }

            Row {
                IconButton(onClick = onNavigateToSaved) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = "المحفوظات",
                        tint = primary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Tabs Row: جميع الدروس | منازل الروح | المحفوظة
        val tabs = listOf("جميع الدروس", "منازل الروح", "المحفوظة")
        TabRow(
            selectedTabIndex = state.selectedTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = primary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[state.selectedTab]),
                    color = primary
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = state.selectedTab == index,
                    onClick = {
                        if (index == 1) {
                            onNavigateToStations()
                        } else {
                            viewModel.setEpisodesTab(index)
                        }
                    },
                    text = {
                        Text(
                            text = title,
                            fontSize = 13.sp,
                            fontWeight = if (state.selectedTab == index) FontWeight.Bold else FontWeight.Medium
                        )
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Input Field
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = filterQuery,
                onValueChange = { filterQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = {
                    Text(
                        text = "ابحث في الدروس...",
                        fontSize = 13.sp,
                        color = IhsanTheme.colors.textSecondaryMuted
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "بحث",
                        tint = IhsanTheme.colors.textSecondaryMuted
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primary,
                    unfocusedBorderColor = IhsanTheme.colors.borderSubtle.copy(alpha = 0.6f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Episode List (Displaying all 29 episodes or tab selection)
        val displayedEpisodes = remember(state.episodes, state.savedEpisodes, state.selectedTab, filterQuery) {
            val baseList = if (state.selectedTab == 2) state.savedEpisodes else state.episodes
            if (filterQuery.isBlank()) {
                baseList
            } else {
                baseList.filter { ep ->
                    ep.title.contains(filterQuery, ignoreCase = true) ||
                    (ep.subtitle?.contains(filterQuery, ignoreCase = true) == true) ||
                    ep.number.toString() == filterQuery.trim()
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(
                items = displayedEpisodes,
                key = { episode -> episode.id }
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
