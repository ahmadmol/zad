package com.example.feature.dashboard.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.designsystem.component.*

/** Public service keys are presentation keys; stored learning IDs and routes stay intact. */
data class ServiceItemData(val title: String, val icon: ImageVector, val route: String, val group: String = "عبادتك")
val ihsanServices = listOf(
    ServiceItemData("القرآن الكريم", Icons.AutoMirrored.Outlined.MenuBook, "quran"),
    ServiceItemData("الأذكار", Icons.Outlined.WbSunny, "azkar"),
    ServiceItemData("اتجاه القبلة", Icons.Outlined.Explore, "qibla"),
    ServiceItemData("الأدعية", Icons.Outlined.VolunteerActivism, "dua"),
    ServiceItemData("التقويم", Icons.Outlined.CalendarMonth, "calendar"),
    ServiceItemData("الأسماء الحسنى", Icons.Outlined.TextFields, "asma"),
    ServiceItemData("مواقيت الصلاة", Icons.Outlined.Mosque, "prayer"),
    ServiceItemData("التسبيح", Icons.Outlined.TouchApp, "tasbih"),
    ServiceItemData("الأحاديث", Icons.Outlined.AutoStories, "hadith"),
    ServiceItemData("سجل الصلاة", Icons.Outlined.FactCheck, "prayer_journal"),
    ServiceItemData("سجل الصيام", Icons.Outlined.EventAvailable, "fasting"),
    ServiceItemData("مركز رمضان", Icons.Outlined.NightsStay, "ramadan"),
    ServiceItemData("الحفظ والمراجعة", Icons.Outlined.BookmarkBorder, "memorization"),
    ServiceItemData("رحلتي مع القرآن", Icons.Outlined.Route, "reading_journey"),
    ServiceItemData("الفهم عن الله", Icons.Outlined.School, "fahm", "تعلّم واكتشف"),
    ServiceItemData("سنحيا بالقرآن", Icons.Outlined.HistoryEdu, "sanhya", "تعلّم واكتشف"),
    ServiceItemData("نبي الإحسان", Icons.Outlined.AutoAwesome, "nabi_ihsan", "تعلّم واكتشف"),
    ServiceItemData("البث المباشر", Icons.Outlined.LiveTv, "live_chooser", "تعلّم واكتشف"),
    ServiceItemData("زيارة الحرمين", Icons.Outlined.TravelExplore, "visit", "تعلّم واكتشف"),
    ServiceItemData("المساجد القريبة", Icons.Outlined.Map, "mosques", "تعلّم واكتشف"),
    ServiceItemData("أهدافي اليومية", Icons.Outlined.TaskAlt, "goals", "يومك وإحسانك"),
    ServiceItemData("مسيرتك كل يوم", Icons.Outlined.Checklist, "daily", "يومك وإحسانك"),
    ServiceItemData("الإحصائيات", Icons.Outlined.BarChart, "statistics", "يومك وإحسانك"),
    ServiceItemData("مجتمع إحسان", Icons.Outlined.FavoriteBorder, "donations", "يومك وإحسانك"),
    ServiceItemData("التذكيرات", Icons.Outlined.Notifications, "reminders", "يومك وإحسانك"),
    ServiceItemData("البحث", Icons.Outlined.Search, "search", "يومك وإحسانك"),
    ServiceItemData("الإعدادات", Icons.Outlined.Settings, "settings", "يومك وإحسانك"),
    ServiceItemData("الدعم والمساعدة", Icons.Outlined.HelpOutline, "support", "يومك وإحسانك")
)

@Composable
fun HomeDiscoverServicesGrid(onServiceClick: (String) -> Unit, modifier: Modifier = Modifier, onViewAll: () -> Unit = {}) {
    BoxWithConstraints(modifier.fillMaxWidth()) {
        val columns = serviceGridColumns(maxWidth.value, LocalDensity.current.fontScale)
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            IhsanSectionHeader("في متناولك", "مساحة لعبادتك كل يوم", "عرض الكل", onViewAll)
            ihsanServices.take(6).chunked(columns).forEach { services ->
                Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    services.forEach { service ->
                        IhsanServiceCard(service.title, service.icon, { onServiceClick(service.route) }, Modifier.weight(1f).fillMaxHeight())
                    }
                    repeat(columns - services.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
    }
}

@Composable
fun AllServicesScreen(onServiceClick: (String) -> Unit, onBack: () -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var liveChooser by rememberSaveable { mutableStateOf(false) }
    val visible = remember(query) { ihsanServices.filter { it.title.contains(query.trim(), ignoreCase = true) } }
    Column(Modifier.fillMaxSize()) {
        IhsanTopBar("كل خدمات إحسان", onNavigateBack = onBack)
        OutlinedTextField(query, { query = it }, Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp).testTag("service_search"),
            label = { Text("ابحث في الخدمات") }, leadingIcon = { Icon(Icons.Outlined.Search, null) }, singleLine = true,
            shape = MaterialTheme.shapes.large)
        if (visible.isEmpty()) IhsanEmptyState(title = "لا توجد نتائج", message = "جرّب اسم خدمة أخرى")
        else LazyVerticalGrid(GridCells.Adaptive((104 * LocalDensity.current.fontScale.coerceAtLeast(1f)).dp),
            state = rememberLazyGridState(), modifier = Modifier.weight(1f).navigationBarsPadding(),
            contentPadding = PaddingValues(20.dp), horizontalArrangement = Arrangement.spacedBy(10.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            visible.groupBy { it.group }.forEach { (group, services) ->
                item(key = "group_$group", span = { GridItemSpan(maxLineSpan) }) { IhsanSectionHeader(group) }
                items(services, key = { it.route }) { service ->
                    IhsanServiceCard(service.title, service.icon, {
                        if (service.route == "live_chooser") liveChooser = true else onServiceClick(service.route)
                    }, Modifier.fillMaxWidth().testTag("service_${service.route}"))
                }
            }
        }
    }
    if (liveChooser) AlertDialog(onDismissRequest = { liveChooser = false }, title = { Text("البث من الحرمين") },
        text = { Text("اختر المصدر الرسمي. يعتمد التشغيل على توفر البث وسياسة الناشر.") },
        confirmButton = { TextButton({ liveChooser = false; onServiceClick("haram") }) { Text("المسجد الحرام") } },
        dismissButton = { TextButton({ liveChooser = false; onServiceClick("nabawi") }) { Text("المسجد النبوي") } })
}

internal fun serviceGridColumns(widthDp: Float, fontScale: Float): Int =
    (widthDp / (96f * fontScale.coerceAtLeast(1f))).toInt().coerceIn(1, 3)
