package com.example.feature.ihsanplus.integration.presentation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.IhsanLoadingState
import com.example.feature.ihsanplus.integration.flags.IhsanPlusFeatureFlags
import org.koin.androidx.compose.koinViewModel

/**
 * Read-only Prayer Assist panel embedded in the canonical Prayer screen.
 * Hidden when [IhsanPlusFeatureFlags.prayerAssistEnabled] is false.
 */
@Composable
fun ControlledPrayerAssistSection(
    viewModel: ControlledPrayerAssistViewModel = koinViewModel()
) {
    if (!IhsanPlusFeatureFlags.prayerAssistEnabled) return

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "مساعد الصلاة (قراءة فقط)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "يعتمد على نفس مصدر أوقات الصلاة في التطبيق — بدون جدولة إضافية",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(10.dp))

            when (val state = uiState) {
                ControlledPrayerAssistUiState.Loading -> IhsanLoadingState()
                is ControlledPrayerAssistUiState.Error -> Text("تعذر تحميل حالة النظام")
                is ControlledPrayerAssistUiState.Content -> {
                    val m = state.model
                    AssistLine("الصلاة التالية", m.nextPrayerArabic ?: "غير محددة")
                    AssistLine("الموقع", m.locationLabel)
                    AssistLine("التنبيهات", m.alarmPermissionLabel)
                    AssistLine(
                        "آخر مزامنة",
                        when (m.lastReconcileSuccess) {
                            true -> "نجحت"
                            false -> "فشلت أو تخطت"
                            null -> "لم تُسجَّل بعد"
                        }
                    )
                    AssistLine("التنبيهات المجدولة", m.scheduledAlarmCount.toString())
                }
            }
        }
    }
}

@Composable
private fun AssistLine(label: String, value: String) {
    Text(
        text = "$label: $value",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(vertical = 2.dp)
    )
}
