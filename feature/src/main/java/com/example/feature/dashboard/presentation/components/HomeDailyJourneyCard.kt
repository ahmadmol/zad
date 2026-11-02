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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.example.designsystem.component.DailyActivityItemData
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
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
            androidx.compose.foundation.layout.FlowRow(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalArrangement = Arrangement.spacedBy(12.dp), maxItemsInEachRow = 3) {
                // Home does not expose prayer completion counts: show an honest unavailable value.
                JourneyProgress(stringResource(R.string.polish_service_prayer), Icons.Outlined.Mosque, null, null,
                    onPrayerClick, Modifier.widthIn(min = (84 * androidx.compose.ui.platform.LocalDensity.current.fontScale).dp).weight(1f))
                JourneyProgress(stringResource(R.string.correction_quran_portion), Icons.AutoMirrored.Outlined.MenuBook,
                    quran.takeIf { it.isNotEmpty() }?.sumOf { it.currentCount },
                    quran.takeIf { it.isNotEmpty() }?.sumOf { it.targetCount }, onQuranClick, Modifier.widthIn(min = (84 * androidx.compose.ui.platform.LocalDensity.current.fontScale).dp).weight(1f))
                JourneyProgress(stringResource(R.string.polish_service_azkar), Icons.Outlined.VolunteerActivism,
                    azkar.takeIf { it.isNotEmpty() }?.sumOf { it.currentCount },
                    azkar.takeIf { it.isNotEmpty() }?.sumOf { it.targetCount }, onAzkarClick, Modifier.widthIn(min = (84 * androidx.compose.ui.platform.LocalDensity.current.fontScale).dp).weight(1f))
            }
        }
    }
}

@Composable
private fun JourneyProgress(title: String, icon: ImageVector, current: Int?, target: Int?,
    onClick: () -> Unit, modifier: Modifier) {
    val summary = if (current == null || target == null) "$title، لا يتوفر تقدم مسجل" else "$title، $current من $target"
    Surface(onClick, modifier.semantics(mergeDescendants = true) { contentDescription = summary },
        shape = RoundedCornerShape(12.dp), color = IhsanTheme.colors.surfaceMuted,
        border = BorderStroke(.5.dp, IhsanTheme.colors.borderSubtle)) {
        Column(Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(64.dp), contentAlignment = Alignment.Center) {
                if (current != null && target != null && target > 0)
                    com.example.designsystem.component.IhsanProgressIndicator(current.toFloat() / target, Modifier.fillMaxSize())
                Icon(icon, null, modifier = Modifier.size(24.dp), tint = IhsanTheme.colors.accentContent)
            }
            Text(title, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
            Text(if (current == null || target == null) "—" else "$current/$target", style = MaterialTheme.typography.labelMedium)
        }
    }
}
