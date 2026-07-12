package com.example.feature.ihsanplus.charitytrust.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusBeneficiaryPrivacyGuideline
import com.example.feature.ihsanplus.charitytrust.domain.model.IhsanPlusContactSafetyGuideline

@Composable
fun IhsanPlusPrivacySafetyCards(
    privacyGuidelines: List<IhsanPlusBeneficiaryPrivacyGuideline>,
    safetyGuidelines: List<IhsanPlusContactSafetyGuideline>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium)
    ) {
        // Privacy Section
        SectionHeader(title = "خصوصية المستفيدين", icon = Icons.Default.Lock)
        privacyGuidelines.forEach { guideline ->
            GuidelineItem(title = guideline.title, description = guideline.description)
        }
        
        Spacer(modifier = Modifier.height(IhsanTheme.spacing.medium))
        
        // Safety Section
        SectionHeader(title = "أمان التواصل", icon = Icons.Default.Security)
        safetyGuidelines.forEach { guideline ->
            GuidelineItem(title = guideline.title, description = guideline.description)
        }
    }
}

@Composable
private fun SectionHeader(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(imageVector = icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(IhsanTheme.spacing.small))
        Text(text = title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
}

@Composable
private fun GuidelineItem(title: String, description: String) {
    Column(modifier = Modifier.padding(bottom = IhsanTheme.spacing.small)) {
        Text(text = title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
        Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
