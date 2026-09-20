package com.example.feature.fahmanallah.presentation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.fahmanallah.presentation.components.FahmJourneyItem
import com.example.feature.fahmanallah.presentation.components.FahmProgressRing

@Composable
fun FahmJourneyScreen(
    viewModel: FahmViewModel,
    onNavigateToDetail: (String) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.mainState.collectAsStateWithLifecycle()
    val primary = MaterialTheme.colorScheme.primary

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

            Column {
                Text(
                    text = "رحلتي في الفهم عن الله",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary
                )

                Text(
                    text = "الجزء الأول • رمضان 2023",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = IhsanTheme.colors.textSecondaryMuted
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Progress Ring Component
            item {
                FahmProgressRing(progress = state.journeyProgress)
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "خط سير الرحلة (29 درسًا)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary
                )
            }

            // Timeline of Episodes 01 to 29
            items(
                items = state.episodes,
                key = { ep -> ep.id }
            ) { episode ->
                FahmJourneyItem(
                    episode = episode,
                    onEpisodeClick = { onNavigateToDetail(it) }
                )
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
