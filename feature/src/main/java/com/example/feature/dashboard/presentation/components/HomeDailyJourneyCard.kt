package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.Mosque
import androidx.compose.material.icons.outlined.VolunteerActivism
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.DailyActivityItemData
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@Composable
fun HomeDailyJourneyCard(onGoToChecklist: () -> Unit, modifier: Modifier = Modifier,
    activities: List<DailyActivityItemData> = emptyList(), onPrayerClick: () -> Unit = onGoToChecklist,
    onQuranClick: () -> Unit = onGoToChecklist, onAzkarClick: () -> Unit = onGoToChecklist) {
    val quran = activities.filter { it.route == "quran_list" }
    val azkar = activities.filter { it.route == "azkar_screen" }
    Surface(modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = IhsanTheme.colors.surfaceElevated) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(stringResource(R.string.correction_journey_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.correction_journey_subtitle), style = MaterialTheme.typography.bodySmall,
                        color = IhsanTheme.colors.textSecondary)
                }
                TextButton(onGoToChecklist, contentPadding = PaddingValues(4.dp), colors = com.example.designsystem.theme.ihsanTextButtonColors()) {
                    Text(stringResource(R.string.correction_view_all), style = MaterialTheme.typography.labelMedium)
                }
            }
            Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Home does not expose prayer completion counts: show an honest unavailable value.
                JourneyProgress(stringResource(R.string.polish_service_prayer), Icons.Outlined.Mosque, null, null,
                    onPrayerClick, Modifier.weight(1f).fillMaxHeight())
                JourneyProgress(stringResource(R.string.correction_quran_portion), Icons.AutoMirrored.Outlined.MenuBook,
                    quran.takeIf { it.isNotEmpty() }?.sumOf { it.currentCount },
                    quran.takeIf { it.isNotEmpty() }?.sumOf { it.targetCount }, onQuranClick, Modifier.weight(1f).fillMaxHeight())
                JourneyProgress(stringResource(R.string.polish_service_azkar), Icons.Outlined.VolunteerActivism,
                    azkar.takeIf { it.isNotEmpty() }?.sumOf { it.currentCount },
                    azkar.takeIf { it.isNotEmpty() }?.sumOf { it.targetCount }, onAzkarClick, Modifier.weight(1f).fillMaxHeight())
            }
        }
    }
}

@Composable
private fun JourneyProgress(title: String, icon: ImageVector, current: Int?, target: Int?,
    onClick: () -> Unit, modifier: Modifier) {
    Surface(onClick, modifier, shape = RoundedCornerShape(12.dp), color = IhsanTheme.colors.surfaceMuted,
        border = BorderStroke(.5.dp, IhsanTheme.colors.borderSubtle)) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Icon(icon, null, modifier = Modifier.size(20.dp), tint = IhsanTheme.colors.accentContent)
                CircularProgressIndicator(progress = { if (target != null && target > 0) (current ?: 0).toFloat() / target else 0f },
                    modifier = Modifier.size(28.dp), strokeWidth = 3.dp, strokeCap = StrokeCap.Round,
                    trackColor = IhsanTheme.colors.progressTrack, color = IhsanTheme.colors.progressActive)
            }
            Text(title, style = MaterialTheme.typography.labelMedium)
            Text(if (current == null || target == null) "—" else "$current/$target", style = MaterialTheme.typography.labelMedium)
        }
    }
}
