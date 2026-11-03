package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun HomeContextualHero(
    location: String,
    hijriDate: String,
    currentPrayerName: String,
    countdownText: String,
    nextPrayerName: String,
    nextPrayerTime: String,
    @Suppress("UNUSED_PARAMETER") dailyVerseText: String,
    @Suppress("UNUSED_PARAMETER") dailyVerseSource: String,
    onNotificationClick: () -> Unit,
    onSearchClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onQiblaClick: () -> Unit,
    modifier: Modifier = Modifier,
    onPrayerClick: () -> Unit = {},
    onLocationClick: () -> Unit = {},
    userName: String = ""
) {
    val colors = IhsanTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Unified Expanded Header Top Row: Brand + Greeting on Start, 48dp Actions on End
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IhsanTextWithKaabaIcon("إحسان", colors.accentContent, logoHeight = 36.dp)
                Text(
                    text = if (userName.isBlank() || userName == "مستخدم إحسان") "أهلًا بك، طاب يومك" else "أهلًا ${userName.substringBefore(' ').take(24)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textSecondary
                )
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IhsanHeaderAction(
                    icon = Icons.Outlined.Search,
                    contentDescription = "بحث",
                    onClick = onSearchClick
                )
                IhsanHeaderAction(
                    icon = Icons.Outlined.Notifications,
                    contentDescription = "التذكيرات",
                    onClick = onNotificationClick
                )
            }
        }

        // Compact Metadata Row: Hijri Date + Location Pill (48dp touch target)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalArrangement = Arrangement.Center
        ) {
            if (hijriDate.isNotBlank()) {
                Box(
                    modifier = Modifier.heightIn(min = 36.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = hijriDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            TextButton(
                onClick = onLocationClick,
                modifier = Modifier.heightIn(min = 48.dp),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(Icons.Outlined.LocationOn, null, Modifier.size(16.dp), tint = colors.accentContent)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = location.ifBlank { "اختر موقعك" },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textPrimary
                )
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, Modifier.size(16.dp), tint = colors.accentContent)
            }
        }

        // Approved Prayer Hero Card — Preserved intact
        Surface(onPrayerClick, Modifier.fillMaxWidth(), shape = RoundedCornerShape(28.dp), color = colors.brandElevated) {
            Box {
                IhsanIllustration(IhsanScene.AQSA, Modifier.align(Alignment.TopEnd).fillMaxWidth(.68f).height(220.dp))
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, colors.brandElevated.copy(alpha = .96f)))))
                Column(Modifier.fillMaxWidth().padding(24.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("مساحة للصلاة", style = MaterialTheme.typography.labelLarge, color = Color(0xFFE4C78E))
                    Text(if (currentPrayerName.isBlank() || currentPrayerName == "—") "في يومك خير" else "وقت $currentPrayerName",
                        style = MaterialTheme.typography.headlineMedium, color = Color.White)
                    Spacer(Modifier.height(90.dp))
                    Text(if (nextPrayerName.isBlank() || nextPrayerName == "—") "جهّز مواقيت يومك" else "الصلاة القادمة • $nextPrayerName",
                        Modifier.semantics { heading() }, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    if (countdownText.isNotBlank()) Text("الوقت المتبقي للصلاة", style = MaterialTheme.typography.labelMedium, color = Color(0xFFE2EBE5))
                    Text(countdownText.ifBlank { "اختر الموقع لعرض المواقيت" },
                        style = if (countdownText.isBlank()) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.displaySmall.copy(textDirection = TextDirection.Ltr),
                        color = Color.White)
                    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Text(if (nextPrayerTime.isBlank() || nextPrayerTime == "—") "مواقيت الصلاة والإعدادات" else "موعدها $nextPrayerTime",
                            Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium, color = Color(0xFFE2EBE5))
                        Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, tint = Color(0xFFE4C78E))
                    }
                    Text("رسم مستوحى من قبة الصخرة • القدس", style = MaterialTheme.typography.labelSmall, color = Color(0xFFE2EBE5))
                }
            }
        }
    }
}
