package com.example.feature.prayer.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.IhsanButton
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.core.util.HijriDateFormatter
import com.example.feature.prayer.PrayerTime
import org.koin.compose.koinInject
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerDetailsBottomSheet(
    selectedPrayer: PrayerTime,
    allPrayers: List<PrayerTime>,
    location: String,
    countdown: String,
    onDismiss: () -> Unit,
    onNavigateToFullPrayer: () -> Unit,
    onSettingsClick: () -> Unit = {},
    onUpdatePrePrayer: (Int) -> Unit = {},
    onUpdateIqamah: (Int) -> Unit = {},
    modifier: Modifier = Modifier,
    settingsManager: SettingsManager = koinInject()
) {
    val prePrayerMins by settingsManager.prePrayerNotificationMinutesFlow.collectAsState(initial = 0)
    val iqamahMins by settingsManager.iqamahNotificationMinutesFlow.collectAsState(initial = 0)

    val colors = IhsanTheme.colors

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = colors.borderSubtle)
        },
        containerColor = colors.surfaceElevated
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = selectedPrayer.nameAr,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = if (selectedPrayer.isActive) "الصلاة القادمة" else "وقت الصلاة",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedPrayer.isActive) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                colors.textSecondary
                            }
                        )
                    }
                    Text(
                        text = selectedPrayer.time,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (selectedPrayer.isActive) {
                    CountdownSection(countdown)
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Info Section
                InfoRow(Icons.Default.LocationOn, location)
                InfoRow(Icons.Default.CalendarMonth, "${SimpleDateFormat("d MMMM yyyy", Locale("ar")).format(Date())} - ${HijriDateFormatter.nowFormatted()}")

                Spacer(modifier = Modifier.height(24.dp))

                // Full Schedule
                Text(
                    text = "مواقيت اليوم",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Surface(
                    color = colors.surfaceMuted,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        allPrayers.forEachIndexed { index, prayer ->
                            PrayerItemRow(prayer, isNext = prayer.isActive)
                            if (index < allPrayers.size - 1) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 12.dp),
                                    color = colors.divider
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Notifications Section
                Text(
                    text = "التنبيهات",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                
                NotificationSettings(
                    prePrayerMins = prePrayerMins,
                    iqamahMins = iqamahMins,
                    onUpdatePrePrayer = onUpdatePrePrayer,
                    onUpdateIqamah = onUpdateIqamah
                )

                Spacer(modifier = Modifier.height(32.dp))

                IhsanButton(
                    onClick = onNavigateToFullPrayer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("فتح عرض الجدول الزمني الكامل", fontWeight = FontWeight.Bold)
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                TextButton(
                    onClick = onSettingsClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("إعدادات طريقة الحساب", color = colors.textSecondary)
                }
            }
        }
    }
}

@Composable
fun CountdownSection(countdown: String) {
    val colors = IhsanTheme.colors

    Surface(
        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("الوقت المتبقي للأذان", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
            Text(
                text = countdown,
                style = MaterialTheme.typography.displaySmall,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
fun InfoRow(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    val colors = IhsanTheme.colors

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = colors.textSecondary)
    }
}

@Composable
fun PrayerItemRow(prayer: PrayerTime, isNext: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isNext) {
                Box(modifier = Modifier.size(8.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text(
                text = prayer.nameAr,
                fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
                color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = prayer.time,
            fontWeight = if (isNext) FontWeight.Bold else FontWeight.Normal,
            color = if (isNext) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun NotificationSettings(
    prePrayerMins: Int,
    iqamahMins: Int,
    onUpdatePrePrayer: (Int) -> Unit,
    onUpdateIqamah: (Int) -> Unit
) {
    val colors = IhsanTheme.colors

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("تنبيه قبل الصلاة بـ:", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0, 5, 10, 15, 30).forEach { mins ->
                val label = if (mins == 0) "إيقاف" else "$mins د"
                val isSelected = prePrayerMins == mins
                
                Surface(
                    onClick = { onUpdatePrePrayer(mins) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else colors.surfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else colors.borderSubtle
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            colors.textSecondary
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        Text("تنبيه الإقامة بعد:", style = MaterialTheme.typography.labelSmall, color = colors.textSecondary)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(0, 5, 10, 15, 20).forEach { mins ->
                val label = if (mins == 0) "إيقاف" else "$mins د"
                val isSelected = iqamahMins == mins
                
                Surface(
                    onClick = { onUpdateIqamah(mins) },
                    color = if (isSelected) MaterialTheme.colorScheme.primary else colors.surfaceElevated,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary else colors.borderSubtle
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = label,
                        modifier = Modifier.padding(vertical = 8.dp),
                        textAlign = TextAlign.Center,
                        fontSize = 12.sp,
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.onPrimary
                        } else {
                            colors.textSecondary
                        },
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }
        }
    }
}
