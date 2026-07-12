package com.example.feature.ihsanplus.charitytrust.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusTrustSummary

@Composable
fun IhsanPlusTrustSummaryCard(
    summary: IhsanPlusTrustSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(IhsanTheme.spacing.medium)
        ) {
            Text(
                text = "ملخص الثقة",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                TrustIndicator(label = summary.verifiedCasesCountLabel, modifier = Modifier.weight(1f))
                TrustIndicator(label = summary.pendingReviewCountLabel, modifier = Modifier.weight(1f))
                TrustIndicator(label = summary.completedCasesCountLabel, modifier = Modifier.weight(1f))
            }
            
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.medium))
            
            Text(
                text = summary.trustMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun TrustIndicator(
    label: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium
        )
    }
}
