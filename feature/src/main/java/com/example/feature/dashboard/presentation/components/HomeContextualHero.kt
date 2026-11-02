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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextDirection
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme

@Composable
fun HomeContextualHero(location: String, hijriDate: String, currentPrayerName: String,
    countdownText: String, nextPrayerName: String, nextPrayerTime: String,
    @Suppress("UNUSED_PARAMETER") dailyVerseText: String, @Suppress("UNUSED_PARAMETER") dailyVerseSource: String,
    onNotificationClick: () -> Unit, onSearchClick: () -> Unit,
    @Suppress("UNUSED_PARAMETER") onQiblaClick: () -> Unit, modifier: Modifier = Modifier,
    onPrayerClick: () -> Unit = {}, onLocationClick: () -> Unit = {}, userName: String = "") {
    val colors = IhsanTheme.colors
    Column(modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                IhsanTextWithKaabaIcon("إحسان", colors.accentContent, logoHeight = 40.dp)
                Text(if (userName.isBlank() || userName == "مستخدم إحسان") "أهلًا بك، طاب يومك" else "أهلًا ${userName.substringBefore(' ').take(24)}",
                    Modifier.padding(top = 6.dp), style = MaterialTheme.typography.bodyMedium, color = colors.textSecondary)
            }
            FilledTonalIconButton(onSearchClick, Modifier.size(48.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colors.surfaceElevated, contentColor = colors.textPrimary)) { Icon(Icons.Outlined.Search, "بحث") }
            Spacer(Modifier.width(8.dp))
            FilledTonalIconButton(onNotificationClick, Modifier.size(48.dp), colors = IconButtonDefaults.filledTonalIconButtonColors(
                containerColor = colors.surfaceElevated, contentColor = colors.textPrimary)) { Icon(Icons.Outlined.Notifications, "التذكيرات") }
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(hijriDate, style = MaterialTheme.typography.bodyMedium, color = colors.textPrimary)
            TextButton(onLocationClick, contentPadding = PaddingValues(0.dp)) {
                Icon(Icons.Outlined.LocationOn, null, Modifier.size(18.dp), tint = colors.accentContent)
                Spacer(Modifier.width(4.dp))
                Text(location.ifBlank { "اختر موقعك" }, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
                Icon(Icons.AutoMirrored.Outlined.KeyboardArrowLeft, null, Modifier.size(18.dp), tint = colors.accentContent)
            }
        }
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
