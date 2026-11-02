package com.example.feature.fasting.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.ihsanTextButtonColors
import com.example.feature.calendar.HijriMonthGrid
import com.example.feature.core.util.HijriDateProvider
import com.example.feature.core.util.currentCivilDate
import com.example.feature.fasting.domain.*
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate
import java.time.chrono.HijrahDate
import java.time.temporal.ChronoField
import java.time.temporal.ChronoUnit

@Composable
fun FastingScreen(onBack: () -> Unit, onCalendar: () -> Unit,
    viewModel: FastingViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val today = currentCivilDate()
    val provider = remember { HijriDateProvider() }
    var anchorDay by rememberSaveable { mutableStateOf<Long?>(null) }
    var selectedDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    var dialog by rememberSaveable { mutableStateOf<String?>(null) }
    var editDay by rememberSaveable { mutableStateOf(today.toEpochDay()) }
    var draftType by rememberSaveable { mutableStateOf("") }
    var draftStatus by rememberSaveable { mutableStateOf(FastingStatus.FASTED.name) }
    val listState=androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(today) { if(selectedDay>today.toEpochDay()) selectedDay=today.toEpochDay() }
    LaunchedEffect(state.saved) {
        if(state.saved) { dialog=null;viewModel.acknowledgeSaved() }
    }
    LaunchedEffect(state.error) { if(state.error!=null && dialog==null) listState.animateScrollToItem(0) }
    val selectedDate=LocalDate.ofEpochDay(selectedDay)
    val selected=state.records.firstOrNull { it.date==selectedDate }
    val anchor=runCatching { anchorDay?.let { HijrahDate.from(LocalDate.ofEpochDay(it)) }
        ?: provider.date(today,state.offset) }.getOrNull()
    val days=remember(anchor,state.offset) { runCatching {
        anchor?.let { provider.month(it.get(ChronoField.YEAR),it.get(ChronoField.MONTH_OF_YEAR),state.offset) }.orEmpty()
    }.getOrDefault(emptyList()) }
    val canEdit=state.dataReady && !state.busy
    Scaffold(topBar={IhsanTopBar("سجل الصيام",onNavigateBack=onBack)}) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding),state=listState,contentPadding=PaddingValues(16.dp),
            verticalArrangement=Arrangement.spacedBy(12.dp)) {
            item { Text("سجل خاص على جهازك. «غير مسجل» لا يعني أنك صمت أو لم تصم. لا يسجّل التطبيق شيئًا تلقائيًا.") }
            if(state.loading) item { IhsanLoadingState() }
            state.error?.let { item { IhsanErrorState("تعذر تحديث سجل الصيام",message=it,onRetry=viewModel::reload) } }
            if(!state.loading && state.error==null && days.isNotEmpty() && anchor!=null) {
                item { IhsanContentCard {
                    Text("${HijriDateProvider.months[anchor.get(ChronoField.MONTH_OF_YEAR)-1]} ${anchor.get(ChronoField.YEAR)}",style=MaterialTheme.typography.titleLarge)
                    Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween) {
                        TextButton(colors=ihsanTextButtonColors(),onClick={anchorDay=anchor.minus(1,ChronoUnit.MONTHS).toEpochDay()},
                            enabled=anchor.get(ChronoField.YEAR)>1300 || anchor.get(ChronoField.MONTH_OF_YEAR)>1) { Text("السابق") }
                        TextButton(colors=ihsanTextButtonColors(),onClick={anchorDay=null;selectedDay=today.toEpochDay()}) { Text("اليوم") }
                        TextButton(colors=ihsanTextButtonColors(),onClick={anchorDay=anchor.plus(1,ChronoUnit.MONTHS).toEpochDay()},
                            enabled=days.last().civil<today) { Text("التالي") }
                    }
                    Text("كل خلية: هجري ثم ميلادي. حالة اليوم من تسجيلك فقط.",style=MaterialTheme.typography.bodySmall)
                    HijriMonthGrid(days,today,selectedDay,
                        annotations=state.records.associate { it.date.toEpochDay() to it.label },
                        isDayEnabled={it<=today}) { selectedDay=it }
                    Text("تاريخ هجري حسابي بتعديل ${state.offset} يوم؛ قد يختلف عن الرؤية الرسمية. التعديل يغيّر العرض ولا يغيّر تاريخ أي سجل.",style=MaterialTheme.typography.bodySmall)
                    IhsanSecondaryButton(onClick=onCalendar) { Text("فتح التقويم وإعداد التعديل") }
                } }
                item { IhsanContentCard {
                    Text("اليوم المحدد: $selectedDate ميلادي",style=MaterialTheme.typography.titleMedium)
                    Text(runCatching { HijriDateProvider.format(provider.date(selectedDate,state.offset)) }.getOrDefault("التاريخ الهجري غير متاح"))
                    Text(selected?.label ?: "غير مسجل")
                    selected?.typeLabel?.takeIf { it.isNotBlank() }?.let { Text("وصفك الشخصي: $it") }
                    IhsanButton(onClick={editDay=selectedDay;draftType=selected?.typeLabel.orEmpty();
                        draftStatus=selected?.status?.name ?: FastingStatus.FASTED.name;dialog="edit"},enabled=canEdit) {
                        Text(if(selected==null) "تسجيل هذا اليوم بتأكيدك" else "تعديل التسجيل")
                    }
                    if(selected!=null) IhsanSecondaryButton(onClick={editDay=selectedDay;dialog="delete"},enabled=canEdit) { Text("حذف تسجيل هذا اليوم") }
                } }
                item { IhsanContentCard {
                    val stats=FastingStats.calculate(state.records,days.first().civil,days.last().civil,today)
                    Text("ملخص الشهر المعروض حتى اليوم",style=MaterialTheme.typography.titleMedium)
                    Text("${stats.fasted} صمت • ${stats.notFasted} لم أصم بتسجيلك • ${stats.unrecorded} غير مسجلة")
                    if(stats.unknown>0) Text("${stats.unknown} تسجيلات بحالة غير معروفة؛ لا تُحتسب صيامًا.")
                    Text("النوع وصفك الشخصي فقط؛ لا تصنيف فقهي أو حساب قضاء أو فدية.",style=MaterialTheme.typography.bodySmall)
                } }
            }
            if(!state.loading && state.error==null) item { IhsanContentCard {
                if(state.records.isEmpty()) Text("لم تسجّل أي يوم بعد. اختر اليوم ثم أكّد حالته.")
                Text("كل السجلات: ${state.records.count {it.date<=today && it.status==FastingStatus.FASTED}} أيام سجّلت فيها «صمت»")
                Text("سجلك يبقى محفوظًا عند تسجيل الخروج من الملف المحلي.",style=MaterialTheme.typography.bodySmall)
                IhsanSecondaryButton(onClick={dialog="clear"},enabled=canEdit && state.records.isNotEmpty()) { Text("مسح سجل الصيام فقط") }
            } }
            item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
        }
    }
    dialog?.let { mode -> AlertDialog(onDismissRequest={if(!state.busy) dialog=null},
        title={Text(when(mode){"edit"->"تأكيد تسجيل ${LocalDate.ofEpochDay(editDay)}";"delete"->"حذف تسجيل اليوم";else->"مسح سجل الصيام فقط"})},
        text={Column(Modifier.verticalScroll(rememberScrollState()),verticalArrangement=Arrangement.spacedBy(8.dp)) {
            when(mode) {
                "edit" -> {
                    Text("التاريخ الميلادي: ${LocalDate.ofEpochDay(editDay)}. اختر الحالة التي تؤكدها بنفسك.")
                    FastingStatus.entries.forEach { status ->
                        Row(Modifier.fillMaxWidth().heightIn(min=48.dp).selectable(
                            selected=draftStatus==status.name,role=Role.RadioButton,enabled=!state.busy,
                            onClick={draftStatus=status.name}),verticalAlignment=androidx.compose.ui.Alignment.CenterVertically) {
                            RadioButton(selected=draftStatus==status.name,onClick=null)
                            Text(status.label,Modifier.padding(start=8.dp))
                        }
                    }
                    OutlinedTextField(draftType,{draftType=it},label={Text("نوع الصيام — وصف اختياري منك")},
                        supportingText={Text("حتى 80 حرفًا؛ لا تصنيف فقهي تلقائي")},isError=draftType.length>80,
                        enabled=!state.busy,modifier=Modifier.fillMaxWidth())
                    Text("سيحل هذا التسجيل محل أي تسجيل سابق لليوم نفسه.",style=MaterialTheme.typography.bodySmall)
                }
                "delete" -> Text("سيُحذف تسجيل ${LocalDate.ofEpochDay(editDay)}، ويصبح اليوم «غير مسجل». بقية الأيام لا تتغير.")
                else -> Text("سيُمسح سجل الصيام كله (${state.records.size} يومًا) من هذا الجهاز. لا يمكن التراجع. لن يُمس سجل الصلاة أو الختمة أو الحفظ أو الملاحظات أو الملف الشخصي.")
            }
            state.error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
            if(state.busy) CircularProgressIndicator(Modifier.size(24.dp))
        }},
        confirmButton={TextButton(colors=ihsanTextButtonColors(),enabled=state.dataReady && !state.busy && (mode!="edit" || draftType.length<=80),onClick={
            when(mode) {
                "edit" -> viewModel.save(LocalDate.ofEpochDay(editDay),FastingStatus.valueOf(draftStatus),draftType)
                "delete" -> viewModel.delete(LocalDate.ofEpochDay(editDay))
                else -> viewModel.clearFastingOnly()
            }
        }) {Text(if(mode=="edit") "أؤكد التسجيل" else if(mode=="clear") "أؤكد مسح الصيام فقط" else "تأكيد الحذف")}},
        dismissButton={TextButton(colors=ihsanTextButtonColors(),onClick={dialog=null},enabled=!state.busy){Text("إلغاء")}})
    }
}
