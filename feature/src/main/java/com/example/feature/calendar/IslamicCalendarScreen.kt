package com.example.feature.calendar

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.feature.core.preferences.UserPreferences
import com.example.feature.core.util.HijriDateProvider
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import org.koin.androidx.compose.koinViewModel
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

class IslamicCalendarViewModel(private val preferences: UserPreferences) : ViewModel() {
    private val mutable = MutableStateFlow(CalendarPreferenceState())
    val state = mutable.asStateFlow()
    private var observer: Job? = null
    init { reload() }
    fun reload() {
        observer?.cancel()
        mutable.update { it.copy(loading=true,error=null) }
        observer=viewModelScope.launch {
            preferences.hijriDayOffset.catch { e ->
                if(e is CancellationException) throw e
                mutable.update { it.copy(loading=false,error="تعذر قراءة تفضيل التقويم") }
            }.collect { value -> mutable.update { it.copy(offset=value,loading=false) } }
        }
    }
    fun offset(value: Int) {
        if(mutable.value.busy) return
        mutable.update { it.copy(busy=true,error=null) }
        viewModelScope.launch {
            try { preferences.setHijriDayOffset(value) }
            catch(e: Exception) {
                if(e is CancellationException) throw e
                mutable.update { it.copy(error="تعذر حفظ تعديل التاريخ؛ بقي التفضيل السابق") }
            } finally { mutable.update { it.copy(busy=false) } }
        }
    }
}
data class CalendarPreferenceState(val offset: Int=0,val loading: Boolean=true,val busy: Boolean=false,val error: String?=null)

@Composable
fun IslamicCalendarScreen(onBack: () -> Unit, viewModel: IslamicCalendarViewModel=koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val provider=remember { HijriDateProvider() }
    val today by produceState(initialValue=provider.today()) { while(true){value=provider.today();delay(1000)} }
    var monthAnchor by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedDay by rememberSaveable { mutableStateOf<Long?>(null) }
    val current = if(state.loading || state.error != null) null else runCatching { provider.date(today,state.offset) }.getOrNull()
    val anchor = runCatching { monthAnchor?.let { HijrahDate.from(java.time.LocalDate.ofEpochDay(it)) } ?: current }.getOrNull()
    val days = remember(anchor,state.offset) { runCatching {
        anchor?.let { provider.month(it.get(ChronoField.YEAR),it.get(ChronoField.MONTH_OF_YEAR),state.offset) }.orEmpty()
    }.getOrDefault(emptyList()) }
    val selected = days.firstOrNull { it.civil.toEpochDay()==selectedDay }
    Scaffold(topBar={IhsanTopBar("التقويم الهجري",onNavigateBack=onBack)}) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),contentPadding=PaddingValues(16.dp),verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { IhsanContentCard {
                Text("اليوم: $today ميلادي")
                Text(when {
                    state.loading -> "جارٍ قراءة تفضيل التاريخ…"
                    state.error != null -> "التاريخ الهجري غير متاح حاليًا"
                    else -> current?.let(HijriDateProvider::format) ?: "التاريخ الهجري خارج النطاق المدعوم"
                })
                Text("تقويم أم القرى حسابي؛ قد يختلف عن الرؤية الرسمية. اليوم المعروض يتبع منتصف الليل المدني والمنطقة الزمنية للجهاز. لا يغيّر مواقيت الصلاة.",style=MaterialTheme.typography.bodySmall)
            } }
            if(state.loading) item { IhsanLoadingState() }
            state.error?.let { item { IhsanErrorState("تعذر تحديث التقويم",message=it,onRetry=viewModel::reload) } }
            if(!state.loading && anchor!=null && days.isNotEmpty()) {
                item { IhsanContentCard {
                    Text("${HijriDateProvider.months[anchor.get(ChronoField.MONTH_OF_YEAR)-1]} ${anchor.get(ChronoField.YEAR)}",style=MaterialTheme.typography.titleLarge)
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                        TextButton(colors=com.example.designsystem.theme.ihsanTextButtonColors(), onClick={monthAnchor=anchor.minus(1,ChronoUnit.MONTHS).toEpochDay();selectedDay=null},enabled=anchor.get(ChronoField.YEAR)>1300 || anchor.get(ChronoField.MONTH_OF_YEAR)>1) { Text("السابق") }
                        TextButton(colors=com.example.designsystem.theme.ihsanTextButtonColors(), onClick={monthAnchor=null;selectedDay=today.toEpochDay()}) { Text("اليوم") }
                        TextButton(colors=com.example.designsystem.theme.ihsanTextButtonColors(), onClick={monthAnchor=anchor.plus(1,ChronoUnit.MONTHS).toEpochDay();selectedDay=null},enabled=anchor.get(ChronoField.YEAR)<1600 || anchor.get(ChronoField.MONTH_OF_YEAR)<12) { Text("التالي") }
                    }
                    Text("الأسبوع من السبت إلى الجمعة • كل خلية: هجري ثم ميلادي",style=MaterialTheme.typography.bodySmall)
                    HijriMonthGrid(days,today,selectedDay) { selectedDay=it }

                } }
                selected?.let { day -> item { IhsanContentCard { Text(HijriDateProvider.format(day.hijri));Text("${day.civil} ميلادي") } } }
            }
            item { IhsanContentCard {
                Text("تعديل محلي: ${state.offset} يوم")
                Row(Modifier.fillMaxWidth(), horizontalArrangement=Arrangement.SpaceBetween) {
                    TextButton(colors=com.example.designsystem.theme.ihsanTextButtonColors(), onClick={viewModel.offset(state.offset-1)},enabled=!state.loading && state.error==null && !state.busy && state.offset > -HijriDateProvider.MAX_LOCAL_OFFSET) { Text("− يوم") }
                    TextButton(colors=com.example.designsystem.theme.ihsanTextButtonColors(), onClick={viewModel.offset(0)},enabled=!state.loading && state.error==null && !state.busy) { Text("بلا تعديل") }
                    TextButton(colors=com.example.designsystem.theme.ihsanTextButtonColors(), onClick={viewModel.offset(state.offset+1)},enabled=!state.loading && state.error==null && !state.busy && state.offset < HijriDateProvider.MAX_LOCAL_OFFSET) { Text("+ يوم") }
                }
            } }
            item { IhsanContentCard {
                Text("المناسبات الإسلامية",style=MaterialTheme.typography.titleMedium)
                Text("لا توجد قائمة مناسبات معتمدة حاليًا. يتطلب نشرها مصدرًا وإصدارًا ومراجعة مختصة وفق D-09؛ لا نعتمد التاريخ الحسابي إعلانًا رسميًا.")
            } }
            item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
        }
    }
}
