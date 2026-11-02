package com.example.feature.ramadan

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.core.util.*
import com.example.feature.fasting.domain.FastingStatus
import com.example.feature.prayer.presentation.PrayerTimeFormatter
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.*
import java.time.ZoneId
import java.time.temporal.ChronoField
import java.util.Locale

@Composable
fun RamadanScreen(onBack: () -> Unit, onPrayer: () -> Unit, onQuran: () -> Unit,
    onReadingPlan: () -> Unit, onAzkar: () -> Unit, onDuas: () -> Unit, onFasting: () -> Unit,
    onCalendar: () -> Unit, viewModel: RamadanViewModel=koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today=currentCivilDate()
    val prayer=RamadanSummary.prayer(state.day,state.location,today,ZoneId.systemDefault())
    val calculatedMonth=if(state.preferencesLoading || state.preferencesError!=null) null else
        runCatching { HijriDateProvider().date(today,state.offset).get(ChronoField.MONTH_OF_YEAR) }.getOrNull()
    Scaffold(topBar={IhsanTopBar("مركز رمضان",onNavigateBack=onBack)}) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { IhsanContentCard {
                Text("رحلتك في مكان واحد",style=MaterialTheme.typography.headlineSmall,color=IhsanTheme.colors.accentContent)
                Text(currentHijriDateText())
                Text("$today ميلادي")
                Text(if(calculatedMonth==9) "يعرض التقويم الحسابي رمضان حاليًا؛ يُرجع إلى الجهة المختصة لإثبات البداية الرسمية."
                    else "المركز متاح طوال العام. التاريخ الهجري حسابي وقد يختلف عن الرؤية الرسمية.",style=MaterialTheme.typography.bodySmall)
                IhsanSecondaryButton(onClick=onCalendar) { Text("فتح التقويم") }
            } }
            item { IhsanContentCard {
                Text("مواقيت اليوم",style=MaterialTheme.typography.titleLarge)
                Text("الفجر والمغرب من محرك المواقيت وإعداداته الحالية. لا وقت إمساك مستقلًا أو توقيت إضافيًا.",style=MaterialTheme.typography.bodySmall)
                if(prayer.loading || state.prayerBusy) IhsanLoadingState()
                if(prayer.locationName.isNotBlank()) Text(prayer.locationName)
                prayer.zoneId?.let { Text("المنطقة الزمنية: $it",style=MaterialTheme.typography.labelMedium) }
                listOf("الفجر" to prayer.fajrMillis,"المغرب" to prayer.maghribMillis).forEach { (name,time) ->
                    val label=if(time==null || prayer.zoneId==null) "غير متاح" else
                        PrayerTimeFormatter.format(time,ZoneId.of(prayer.zoneId),Locale("ar"))
                    Text("$name: $label",style=MaterialTheme.typography.titleMedium)
                }
                prayer.message?.let { Text(it) }
                state.prayerError?.let { Text(it,color=MaterialTheme.colorScheme.error) }
                IhsanSecondaryButton(onClick=viewModel::refreshPrayer,enabled=!state.prayerBusy) { Text("إعادة محاولة تحديث الموقع") }
                IhsanSecondaryButton(onClick=onPrayer) { Text("المواقيت وإعدادات المدينة") }
            } }
            item { IhsanContentCard {
                Text("خطة الختمة",style=MaterialTheme.typography.titleLarge)
                when {
                    state.readingLoading -> IhsanLoadingState()
                    state.readingError!=null -> IhsanErrorState("تعذر عرض التقدم",message=state.readingError!!,onRetry=viewModel::reloadData)
                    state.plan==null -> Text("لا توجد خطة ختمة نشطة. يمكنك إنشاء خطة من رحلتي مع القرآن.")
                    else -> {
                        val plan=state.plan!!
                        val confirmed=RamadanSummary.confirmedPages(plan,state.pages)
                        Text("$confirmed من ${plan.pageCount} صفحة مؤكدة يدويًا")
                        LinearProgressIndicator(progress={confirmed.toFloat()/plan.pageCount},modifier=Modifier.fillMaxWidth(),
                            color=IhsanTheme.colors.progressActive,trackColor=IhsanTheme.colors.progressTrack)
                        Text("الصفحات ${plan.firstPage}–${plan.lastPage} • الموعد ${java.time.LocalDate.ofEpochDay(plan.dueEpochDay)}")
                    }
                }
                IhsanSecondaryButton(onClick=onReadingPlan) { Text("فتح خطة الختمة") }
            } }
            item { IhsanContentCard {
                Text("الصيام المسجل",style=MaterialTheme.typography.titleLarge)
                when {
                    state.fastingLoading -> IhsanLoadingState()
                    state.fastingError!=null -> IhsanErrorState("تعذر عرض سجل الصيام",message=state.fastingError!!,onRetry=viewModel::reloadData)
                    else -> {
                        val past=state.fasting.filter { it.date<=today }
                        if(past.isEmpty()) Text("لم تُسجّل أيامًا بعد؛ لا يُستنتج الصيام من التاريخ.")
                        else Text("${past.count {it.status==FastingStatus.FASTED}} أيام سجّلت فيها «صمت» • ${past.size} يومًا مسجلًا في كامل سجلك")
                    }
                }
                IhsanSecondaryButton(onClick=onFasting) { Text("فتح تقويم الصيام وتعديل السجلات") }
            } }
            item { IhsanActionCard(title="القرآن الكريم",subtitle="المصحف والتلاوة والحفظ في قسم القرآن الموجود",icon=Icons.AutoMirrored.Filled.MenuBook,onClick=onQuran) }
            item { IhsanActionCard(title="الأذكار",subtitle="الوصول إلى الأذكار الموجودة بمصادرها",icon=Icons.Default.Favorite,onClick=onAzkar) }
            item { IhsanActionCard(title="الأدعية",subtitle="الوصول إلى مكتبة الأدعية الحالية",icon=Icons.Default.Bookmark,onClick=onDuas) }
            item { IhsanContentCard {
                if(state.preferencesLoading) IhsanLoadingState()
                state.preferencesError?.let { IhsanErrorState("تعذر تحديث التفضيل",message=it,onRetry=viewModel::reloadData) }
                Row(Modifier.fillMaxWidth().heightIn(min=48.dp).toggleable(state.highlight,role=Role.Switch,
                    enabled=!state.preferencesLoading && !state.preferencesBusy && state.preferencesError==null,
                    onValueChange=viewModel::highlight),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                    Text("إبراز المركز في الرئيسية خلال رمضان الحسابي",Modifier.weight(1f))
                    IhsanSwitch(state.highlight,onCheckedChange=null)
                }
                Text("اختياري ومعطّل افتراضيًا؛ إيقافه لا يحذف السجلات أو يخفي رابط المركز الدائم.",style=MaterialTheme.typography.bodySmall)
            } }
            item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
        }
    }
}

@Composable
fun RamadanHomeEntry(onOpen: () -> Unit) {
    val preferences: com.example.feature.core.preferences.UserPreferences=koinInject()
    val flow=remember(preferences) {
        combine(preferences.ramadanHighlightEnabled,preferences.hijriDayOffset) { enabled,offset -> enabled to offset }
            .catch { e -> if(e is CancellationException) throw e;emit(false to 0) }
    }
    val setting by flow.collectAsStateWithLifecycle(initialValue=false to 0)
    val today=currentCivilDate()
    val month=runCatching { HijriDateProvider().date(today,setting.second).get(ChronoField.MONTH_OF_YEAR) }.getOrNull()
    if(RamadanSummary.shouldHighlight(setting.first,month)) IhsanActionCard(title="مركز رمضان",
        subtitle="رمضان بحسب التقويم الحسابي • مواقيت وختمة وصيام مسجل",icon=Icons.Default.Star,onClick=onOpen)
    else IhsanSecondaryButton(onClick=onOpen) { Text("مركز رمضان وسجل الصيام") }
}
