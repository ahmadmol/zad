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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanEmptyState
import com.example.designsystem.component.IhsanLoadingState
import com.example.feature.nabiihsan.presentation.components.NabiEpisodeCard
import org.koin.androidx.compose.koinViewModel

@Composable
fun NabiIhsanEpisodesScreen(
    viewModel: NabiIhsanViewModel = koinViewModel(),
    onNavigateBack: () -> Unit = {},
    onEpisodeClick: (String) -> Unit = {}
) {
    val uiState by viewModel.mainUiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
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
                text = "حلقات السلسلة",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        if (uiState.isLoading) {
            IhsanLoadingState(modifier = Modifier.fillMaxSize())
        } else if (uiState.episodes.isEmpty()) {
            IhsanEmptyState(
                title = "لا توجد حلقات",
                message = "لم يتم العثور على حلقات متوفرة حالياً.",
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                uiState.episodes.forEach { episode ->
                    NabiEpisodeCard(
                        episode = episode,
                        onEpisodeClick = onEpisodeClick,
                        onFavoriteToggle = { viewModel.toggleEpisodeFavorite(it) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}
