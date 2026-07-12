package com.example.feature.ihsanplus.charitytrust.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusTransparencyItem

@Composable
fun IhsanPlusTransparencyList(
    items: List<IhsanPlusTransparencyItem>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium)
    ) {
        Text(
            text = "الشفافية والوضوح",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
        
        items.forEach { item ->
            ListItem(
                headlineContent = { Text(item.title, fontWeight = FontWeight.Bold) },
                supportingContent = { Text(item.description) },
                trailingContent = { 
                    Text(
                        item.valueLabel, 
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = ListItemDefaults.colors(containerColor = androidx.compose.ui.graphics.Color.Transparent)
            )
        }
    }
}
