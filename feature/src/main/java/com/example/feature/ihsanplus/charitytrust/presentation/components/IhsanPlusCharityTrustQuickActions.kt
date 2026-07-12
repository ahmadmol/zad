package com.example.feature.ihsanplus.charitytrust.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusCharityTrustQuickAction

@Composable
fun IhsanPlusCharityTrustQuickActions(
    actions: List<IhsanPlusCharityTrustQuickAction>,
    onActionClick: (String) -> Unit,
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
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
        
        actions.forEach { action ->
            OutlinedButton(
                onClick = { onActionClick(action.id) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = IhsanTheme.spacing.extraSmall)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(text = action.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                    Text(text = action.subtitle, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
