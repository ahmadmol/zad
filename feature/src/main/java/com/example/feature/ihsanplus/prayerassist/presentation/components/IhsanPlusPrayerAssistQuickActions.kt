package com.example.feature.ihsanplus.prayerassist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerAssistQuickAction

@Composable
fun IhsanPlusPrayerAssistQuickActions(
    actions: List<IhsanPlusPrayerAssistQuickAction>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium)
    ) {
        Text(
            text = "إجراءات سريعة",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = IhsanTheme.spacing.small)
        )
        actions.forEach { action ->
            QuickActionItem(action)
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
        }
    }
}

@Composable
private fun QuickActionItem(action: IhsanPlusPrayerAssistQuickAction) {
    Surface(
        onClick = {},
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(IhsanTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = action.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                Text(text = action.subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(imageVector = Icons.Default.ChevronLeft, contentDescription = null)
        }
    }
}
