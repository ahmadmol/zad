package com.example.feature.fahmanallah.presentation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.example.designsystem.component.IhsanLoadingState
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.fahmanallah.presentation.components.FahmCompletionCard

@Composable
fun FahmCompletionScreen(
    episodeId: String,
    viewModel: FahmViewModel,
    onNavigateToDetail: (String) -> Unit,
    onNavigateToJourney: () -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.detailState.collectAsStateWithLifecycle()
    val primary = MaterialTheme.colorScheme.primary

    LaunchedEffect(episodeId) {
        viewModel.loadEpisodeDetail(episodeId)
    }

    if (state.isLoading || state.episode == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            IhsanLoadingState()
        }
        return
    }

    val ep = state.episode!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 10.dp)
    ) {
        // Top Navigation Header
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

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "إتمام الدرس",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )

            Spacer(modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Completion Card Layout
        FahmCompletionCard(
            episode = ep,
            onNextEpisodeClick = {
                if (state.nextEpisodeId != null) {
                    onNavigateToDetail(state.nextEpisodeId!!)
                } else {
                    onNavigateToJourney()
                }
            },
            onReturnToJourneyClick = onNavigateToJourney
        )
    }
}
