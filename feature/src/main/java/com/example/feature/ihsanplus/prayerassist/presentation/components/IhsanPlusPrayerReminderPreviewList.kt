package com.example.feature.ihsanplus.prayerassist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerReminderPreviewItem

@Composable
fun IhsanPlusPrayerReminderPreviewList(
    items: List<IhsanPlusPrayerReminderPreviewItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium)
    ) {
        Text(
            text = "معاينة تنبيهات اليوم",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = IhsanTheme.spacing.small)
        )
        items.forEach { item ->
            ReminderPreviewRow(item)
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
        }
    }
}

@Composable
private fun ReminderPreviewRow(item: IhsanPlusPrayerReminderPreviewItem) {
    OutlinedCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(IhsanTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(IhsanTheme.spacing.medium))
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = item.prayerName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = item.prayerTimeLabel, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                }
                Text(text = item.adhanNotificationLabel, style = MaterialTheme.typography.labelSmall)
                Text(text = item.prePrayerReminderLabel, style = MaterialTheme.typography.labelSmall)
                if (item.iqamaReminderLabel.isNotBlank()) {
                    Text(text = item.iqamaReminderLabel, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
