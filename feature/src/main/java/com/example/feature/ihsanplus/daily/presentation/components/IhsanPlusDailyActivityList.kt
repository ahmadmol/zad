package com.example.feature.ihsanplus.daily.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.daily.domain.model.IhsanPlusDailyActivityItem

@Composable
fun IhsanPlusDailyActivityList(
    activities: List<IhsanPlusDailyActivityItem>,
    onActivityClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium)
    ) {
        Text(
            text = "نشاطك اليومي",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = IhsanTheme.spacing.small)
        )
        activities.forEach { activity ->
            IhsanPlusDailyActivityRow(
                activity = activity,
                onClick = { onActivityClick(activity.id) }
            )
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
        }
    }
}

@Composable
private fun IhsanPlusDailyActivityRow(
    activity: IhsanPlusDailyActivityItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier.padding(IhsanTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = activity.subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.width(IhsanTheme.spacing.medium))
            CircularProgressIndicator(
                progress = { activity.progressPercent / 100f },
                modifier = Modifier.size(32.dp),
                strokeWidth = 3.dp,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
