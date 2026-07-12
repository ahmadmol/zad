package com.example.feature.ihsanplus.prayerassist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CompassCalibration
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusQiblaAssist

@Composable
fun IhsanPlusQiblaAssistCard(
    assist: IhsanPlusQiblaAssist,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier.padding(IhsanTheme.spacing.medium)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CompassCalibration,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.width(IhsanTheme.spacing.small))
                Text(
                    text = "مساعد القبلة",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(IhsanTheme.spacing.small))
            AssistRow(label = "اتجاه القبلة:", value = assist.qiblaDirectionLabel)
            AssistRow(label = "دقة البوصلة:", value = assist.compassAccuracyLabel)
            AssistRow(label = "دعم الجهاز:", value = assist.deviceSupportLabel)
            AssistRow(label = "حالة الاتصال:", value = assist.offlineStatusLabel)
            AssistRow(label = "آخر موقع معروف:", value = assist.lastKnownLocationLabel)
        }
    }
}

@Composable
private fun AssistRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = IhsanTheme.spacing.extraSmall),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall)
        Text(text = value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
    }
}
