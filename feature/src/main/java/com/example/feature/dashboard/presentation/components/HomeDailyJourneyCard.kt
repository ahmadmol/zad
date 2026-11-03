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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.DailyActivityItemData
import com.example.designsystem.component.IhsanProgressIndicator
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.R

@Composable
fun HomeDailyJourneyCard(
    onGoToChecklist: () -> Unit,
    modifier: Modifier = Modifier,
    activities: List<DailyActivityItemData> = emptyList(),
    onPrayerClick: () -> Unit = onGoToChecklist,
    onQuranClick: () -> Unit = onGoToChecklist,
    onAzkarClick: () -> Unit = onGoToChecklist
) {
    val quranActivities = activities.filter { it.route == "quran_list" }
    val azkarActivities = activities.filter { it.route == "azkar_screen" }

    val quranCurrent = quranActivities.takeIf { it.isNotEmpty() }?.sumOf { it.currentCount }
    val quranTarget = quranActivities.takeIf { it.isNotEmpty() }?.sumOf { it.targetCount }?.takeIf { it > 0 }

    val azkarCurrent = azkarActivities.takeIf { it.isNotEmpty() }?.sumOf { it.currentCount }
    val azkarTarget = azkarActivities.takeIf { it.isNotEmpty() }?.sumOf { it.targetCount }?.takeIf { it > 0 }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Section Header: "مسيرتك اليوم" / "خطوات صغيرة تصنع أثرًا كبيرًا" / "عرض الكل"
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.correction_journey_title),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = IhsanTheme.colors.textPrimary
                )
                Text(
                    text = stringResource(R.string.correction_journey_subtitle),
                    style = MaterialTheme.typography.bodySmall,
                    color = IhsanTheme.colors.textSecondary
                )
            }
            TextButton(
                onClick = onGoToChecklist,
                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 2.dp),
                colors = com.example.designsystem.theme.ihsanTextButtonColors()
            ) {
                Text(
                    text = stringResource(R.string.correction_view_all),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
                )
            }
        }

        // Three standalone cards: Prayer, Quran Portion, Azkar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            JourneyProgressCard(
                title = stringResource(R.string.polish_service_prayer),
                icon = Icons.Outlined.Mosque,
                current = null,
                target = null,
                onClick = onPrayerClick,
                modifier = Modifier.weight(1f)
            )
            JourneyProgressCard(
                title = stringResource(R.string.correction_quran_portion),
                icon = Icons.AutoMirrored.Outlined.MenuBook,
                current = quranCurrent,
                target = quranTarget,
                onClick = onQuranClick,
                modifier = Modifier.weight(1f)
            )
            JourneyProgressCard(
                title = stringResource(R.string.polish_service_azkar),
                icon = Icons.Outlined.VolunteerActivism,
                current = azkarCurrent,
                target = azkarTarget,
                onClick = onAzkarClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun JourneyProgressCard(
    title: String,
    icon: ImageVector,
    current: Int?,
    target: Int?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = IhsanTheme.isDark
    val summary = if (current == null || target == null) "$title، لا يتوفر تقدم مسجل" else "$title، $current من $target"
    Surface(
        onClick = onClick,
        modifier = modifier
            .semantics(mergeDescendants = true) { contentDescription = summary }
            .heightIn(min = 116.dp),
        shape = RoundedCornerShape(18.dp),
        color = if (isDark) IhsanTheme.colors.surfaceElevated else Color.White,
        border = BorderStroke(1.dp, if (isDark) IhsanTheme.colors.borderSubtle else Color(0xFFF0F0F0))
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 12.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically)
        ) {
            Box(
                modifier = Modifier.size(52.dp),
                contentAlignment = Alignment.Center
            ) {
                if (current != null && target != null && target > 0) {
                    IhsanProgressIndicator(
                        progress = current.toFloat() / target,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        shape = RoundedCornerShape(26.dp),
                        color = if (isDark) Color(0xFF1E302C) else Color(0xFFEDF4FA)
                    ) {}
                }
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp),
                    tint = if (isDark) Color(0xFF6ED9C5) else Color(0xFF2C6D9E)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                textAlign = TextAlign.Center,
                color = IhsanTheme.colors.textPrimary,
                maxLines = 1
            )
            Text(
                text = if (current == null || target == null) "—" else "$current/$target",
                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                textAlign = TextAlign.Center,
                color = IhsanTheme.colors.textPrimary
            )
        }
    }
}

