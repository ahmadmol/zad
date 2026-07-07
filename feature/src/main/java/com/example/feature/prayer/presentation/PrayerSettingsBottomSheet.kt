package com.example.feature.prayer.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.designsystem.component.IhsanButton
import com.example.feature.azkar.data.local.SettingsManager
import com.example.feature.prayer.util.BatteryOptimizationHelper
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerSettingsBottomSheet(
    onDismiss: () -> Unit,
    onUpdateMethod: (String) -> Unit,
    onUpdateMadhab: (String) -> Unit,
    onUpdateLocationMode: (Boolean) -> Unit,
    onUpdateSound: (String) -> Unit,
    onSelectCityClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    settingsManager: SettingsManager = koinInject()
) {
    val context = LocalContext.current
    val method by settingsManager.calculationMethodFlow.collectAsState(initial = "MUSLIM_WORLD_LEAGUE")
    val madhab by settingsManager.madhabFlow.collectAsState(initial = "SHAFI")
    val autoLocation by settingsManager.useAutoLocationFlow.collectAsState(initial = true)
    val cityName by settingsManager.manualLocationCityFlow.collectAsState(initial = "حلب")
    val soundType by settingsManager.notificationSoundTypeFlow.collectAsState(initial = "DEFAULT_ATHAN")
    
    val isIgnoringOptimizations = remember { BatteryOptimizationHelper.isIgnoringBatteryOptimizations(context) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = modifier,
        shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
        dragHandle = {
            BottomSheetDefaults.DragHandle(color = Color.LightGray.copy(alpha = 0.5f))
        },
        containerColor = Color.White
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .padding(bottom = 32.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = "إعدادات الصلاة",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Battery Optimization Card
                if (!isIgnoringOptimizations) {
                    BatteryOptimizationCard(onAction = {
                        BatteryOptimizationHelper.requestIgnoreBatteryOptimizations(context)
                    })
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Sound Settings
                SettingsSectionTitle("صوت التنبيه")
                val sounds = listOf(
                    "DEFAULT_ATHAN" to "أذان افتراضي",
                    "SHORT_TONE" to "نغمة قصيرة",
                    "SILENT" to "صامت / اهتزاز فقط"
                )
                sounds.forEach { (key, label) ->
                    SelectableItem(
                        label = label,
                        isSelected = soundType == key,
                        onClick = { onUpdateSound(key) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Calculation Method
                SettingsSectionTitle("طريقة الحساب")
                val methods = listOf(
                    "MUSLIM_WORLD_LEAGUE" to "رابطة العالم الإسلامي",
                    "UMM_AL_QURA" to "أم القرى (مكة)",
                    "EGYPTIAN" to "الهيئة المصرية العامة للمساحة",
                    "KARACHI" to "جامعة العلوم الإسلامية بكراتشي",
                    "NORTH_AMERICA" to "ISNA (أمريكا الشمالية)",
                    "DUBAI" to "دبي",
                    "KUWAIT" to "الكويت",
                    "QATAR" to "قطر",
                    "TURKEY" to "تركيا"
                )
                
                methods.forEach { (key, label) ->
                    SelectableItem(
                        label = label,
                        isSelected = method == key,
                        onClick = { onUpdateMethod(key) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Madhab
                SettingsSectionTitle("المذهب")
                val madhabs = listOf(
                    "SHAFI" to "شافعي، مالكي، حنبلي",
                    "HANAFI" to "حنفي"
                )
                madhabs.forEach { (key, label) ->
                    SelectableItem(
                        label = label,
                        isSelected = madhab == key,
                        onClick = { onUpdateMadhab(key) }
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Location
                SettingsSectionTitle("الموقع")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("استخدام الموقع التلقائي (GPS)", style = MaterialTheme.typography.bodyMedium)
                    Switch(
                        checked = autoLocation,
                        onCheckedChange = { onUpdateLocationMode(it) }
                    )
                }
                
                if (!autoLocation) {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedCard(
                        onClick = onSelectCityClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.LocationCity, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("المدينة المختارة", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                Text(cityName, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, tint = Color.Gray)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                IhsanButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("حفظ وإغلاق", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun BatteryOptimizationCard(onAction: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.BatteryAlert,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    "تحسين دقة التنبيهات",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "لضمان وصول تنبيهات الصلاة بدقة، يُفضّل السماح للتطبيق بالعمل في الخلفية وعدم تقييده بتحسين البطارية.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(
                onClick = onAction,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("فتح الإعدادات", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = Color.Black,
        modifier = Modifier.padding(bottom = 12.dp)
    )
}

@Composable
fun SelectableItem(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
        RadioButton(selected = isSelected, onClick = onClick)
    }
}
