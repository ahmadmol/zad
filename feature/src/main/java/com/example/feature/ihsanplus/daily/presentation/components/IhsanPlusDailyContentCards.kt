package com.example.feature.ihsanplus.daily.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusCharitySuggestion
import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDailyDua
import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDailyHadith
import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDhikrProgress

@Composable
fun IhsanPlusDailyDuaCard(
    dua: IhsanPlusDailyDua,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium),
        onClick = onShare,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.tertiaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(IhsanTheme.spacing.medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FormatQuote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary
                )
                Spacer(modifier = Modifier.width(IhsanTheme.spacing.small))
                Text(
                    text = dua.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onTertiaryContainer
                )
            }
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
            Text(
                text = dua.content,
                style = MaterialTheme.typography.bodyLarge,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
            Text(
                text = "المصدر: ${dua.source}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.6f)
            )
        }
    }
}

@Composable
fun IhsanPlusDailyHadithCard(
    hadith: IhsanPlusDailyHadith,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium),
        onClick = onShare
    ) {
        Column(
            modifier = Modifier.padding(IhsanTheme.spacing.medium)
        ) {
            Text(
                text = "حديث اليوم",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
            Text(
                text = hadith.content,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.extraSmall))
            Text(
                text = hadith.source,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun IhsanPlusDhikrProgressCard(
    progress: IhsanPlusDhikrProgress,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium)
    ) {
        Row(
            modifier = Modifier.padding(IhsanTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = progress.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${progress.currentCount} / ${progress.targetCount}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Box(contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    progress = { progress.currentCount.toFloat() / progress.targetCount },
                    modifier = Modifier.size(48.dp),
                    strokeWidth = 4.dp
                )
                IconButton(onClick = onAddClick) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                }
            }
        }
    }
}

@Composable
fun IhsanPlusCharitySuggestionCard(
    suggestion: IhsanPlusCharitySuggestion,
    onDonateClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium),
        onClick = onDonateClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(IhsanTheme.spacing.medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.VolunteerActivism,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(IhsanTheme.spacing.small))
                Text(
                    text = suggestion.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Badge { Text(suggestion.urgencyLabel) }
            }
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
            Text(
                text = suggestion.description,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
            Text(
                text = "الفئة: ${suggestion.categoryLabel}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}
