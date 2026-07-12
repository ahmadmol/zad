package com.example.feature.ihsanplus.prayerassist.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.ihsanplus.prayerassist.domain.model.IhsanPlusPrayerNotificationPreferences

@Composable
fun IhsanPlusPrayerNotificationPreferencesCard(
    preferences: IhsanPlusPrayerNotificationPreferences,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(IhsanTheme.spacing.medium)
    ) {
        Column(
            modifier = Modifier.padding(IhsanTheme.spacing.medium),
            verticalArrangement = Arrangement.spacedBy(IhsanTheme.spacing.small)
        ) {
            Text(
                text = "إعدادات التنبيهات",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            PreferenceRow(label = "الأذان (المنبه الرئيسي)", isEnabled = preferences.adhanEnabled)
            PreferenceRow(label = "تنبيه قبل الصلاة (${preferences.prePrayerReminderMinutes} د)", isEnabled = preferences.prePrayerReminderEnabled)
            PreferenceRow(label = "تنبيه الإقامة (${preferences.iqamaReminderMinutes} د)", isEnabled = preferences.iqamaReminderEnabled)
            
            HorizontalDivider(modifier = Modifier.padding(vertical = IhsanTheme.spacing.extraSmall))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "الوضع الصامت التلقائي", style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = preferences.silentModeLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun PreferenceRow(label: String, isEnabled: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
        Switch(checked = isEnabled, onCheckedChange = null, enabled = false)
    }
}
