package com.example.feature.quran.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.feature.quran.domain.model.ReviewResult
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun MemorizationScreen(onBack: () -> Unit, onRead: (Int, Int) -> Unit,
    viewModel: MemorizationViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val canWrite = state.dataReady && !state.busy
    var editor by rememberSaveable { mutableStateOf(false) }
    var editId by rememberSaveable { mutableStateOf<String?>(null) }
    var surah by rememberSaveable { mutableStateOf(1) }
    var first by rememberSaveable { mutableStateOf("1") }
    var last by rememberSaveable { mutableStateOf("7") }
    var menu by rememberSaveable { mutableStateOf(false) }
    var confirmId by rememberSaveable { mutableStateOf<String?>(null) }
    var archiveId by rememberSaveable { mutableStateOf<String?>(null) }
    var testId by rememberSaveable { mutableStateOf<String?>(null) }
    var revealed by rememberSaveable { mutableStateOf(false) }
    var history by rememberSaveable { mutableStateOf(false) }
    val listState=androidx.compose.foundation.lazy.rememberLazyListState()
    LaunchedEffect(editor, editId, testId) { if(editor || testId != null) listState.animateScrollToItem(0) }
    LaunchedEffect(state.error) { if(state.error != null) listState.animateScrollToItem(0) }
    val test = state.plans.firstOrNull { it.id == testId }
    LaunchedEffect(test?.id) { test?.let { viewModel.loadVerses(it) } }
    LaunchedEffect(state.saved) { if (state.saved > 0) { editor=false; confirmId=null; archiveId=null; testId=null; viewModel.consumeSaved() } }
    androidx.activity.compose.BackHandler(enabled = testId != null || editor) {
        if (testId != null) testId = null else editor = false
    }
    val today by produceState(initialValue=viewModel.today()) {
        while(true) { value=viewModel.today(); kotlinx.coroutines.delay(1000) }
    }
    Scaffold(topBar = { IhsanTopBar(if (test != null) "اختبار ذاتي يدوي" else "الحفظ والمراجعة", onNavigateBack = {
        if (testId != null) testId=null else if(editor) editor=false else onBack()
    }) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding).imePadding(), state=listState, contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("بيانات خاصة على جهازك. الحفظ والنتيجة بتأكيدك فقط؛ فتح الآيات والتلاوة لا يسجّلان حفظًا.") }
            if (state.loading) item { IhsanLoadingState() }
            state.error?.let { item { IhsanErrorState("تعذر إكمال العملية", message=it, onRetry={viewModel.reload(); test?.let(viewModel::loadVerses)}) } }
            if (test != null) {
                item { IhsanContentCard {
                    Text("السورة ${test.surahId} • الآيات ${test.firstAyah}–${test.lastAyah}")
                    Text("استرجع الآيات من ذاكرتك، ثم اكشفها للمقارنة. لا تقييم آلي.")
                    IhsanSecondaryButton(onClick={ revealed=!revealed }) { Text(if (revealed) "إخفاء النص" else "كشف الآيات كاملة") }
                    IhsanSecondaryButton(onClick={ onRead(test.surahId,test.firstAyah) }) { Text("فتح المصحف والتكرار") }
                } }
                if (revealed) {
                    if (state.verses.isEmpty() && state.error == null) item { IhsanLoadingState() }
                    items(state.verses, key={it.id}) { verse -> IhsanContentCard {
                        Text("الآية ${verse.verseNumber}", style=MaterialTheme.typography.labelLarge)
                        Text(verse.text, style=MaterialTheme.typography.headlineSmall)
                    } }
                }
                item { IhsanContentCard {
                    if (test.dueDay != null && test.dueDay <= today && !test.archived) {
                        Text("بعد المقارنة، سجّل نتيجتك:")
                        ReviewResult.entries.forEach { result ->
                            IhsanSecondaryButton(onClick={viewModel.review(test.id,result)}, enabled=revealed && state.verses.isNotEmpty() && canWrite) { Text(result.label) }
                        }
                    } else Text("تدريب فقط؛ تسجيل النتيجة يُتاح عند موعد المراجعة.")
                    IhsanSecondaryButton(onClick={testId=null}) { Text("إنهاء الاختبار") }
                } }
            } else {
                item { IhsanContentCard {
                    Text("${state.plans.count { !it.archived && it.confirmedDay != null }} نطاقات حفظ مؤكدة • ${state.plans.filter { !it.archived && it.confirmedDay != null }.sumOf { it.lastAyah-it.firstAyah+1 }} آية ضمن النطاقات (قد تتداخل)")
                    Text("المراجعات المستحقة: ${state.plans.count { !it.archived && it.dueDay != null && it.dueDay <= today }}")
                    Text("بعد تأكيد الحفظ: غدًا. نتيجة جيدة: 3 ثم 7 ثم 14 ثم 30 يومًا؛ صعبة: غدًا مع ثبات المرحلة؛ إعادة: غدًا وعودة للبداية. موعد المتأخر يبدأ من يوم تسجيلك.", style=MaterialTheme.typography.bodySmall)
                    IhsanButton(onClick={editId=null; editor=true}, enabled=canWrite) { Text("إنشاء خطة حفظ") }
                    IhsanSecondaryButton(onClick={history=!history}) { Text(if(history) "عرض الخطط النشطة" else "الأرشيف وسجل النتائج") }
                } }
                if (editor) item { IhsanContentCard {
                    Text(if(editId==null) "نطاق حفظ جديد" else "تعديل النطاق يؤرشف النسخة السابقة ويحفظ تاريخها")
                    Box {
                        IhsanSecondaryButton(onClick={menu=true}) { Text(state.surahs.firstOrNull { it.id==surah }?.name ?: "اختيار السورة") }
                        DropdownMenu(expanded=menu,onDismissRequest={menu=false}) {
                            state.surahs.forEach { s -> DropdownMenuItem(text={Text("${s.id}. ${s.name} • ${s.totalVerses} آية")},onClick={surah=s.id; first="1"; last="${s.totalVerses}"; menu=false}) }
                        }
                    }
                    OutlinedTextField(first,{first=it},label={Text("من الآية")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth(),singleLine=true)
                    OutlinedTextField(last,{last=it},label={Text("إلى الآية")},keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Number),modifier=Modifier.fillMaxWidth(),singleLine=true)
                    IhsanButton(onClick={viewModel.save(editId,surah,first.toIntOrNull() ?: 0,last.toIntOrNull() ?: 0)},enabled=canWrite && state.surahs.isNotEmpty()) { Text("حفظ النطاق") }
                    IhsanSecondaryButton(onClick={editor=false},enabled=canWrite) { Text("إلغاء") }
                    if(state.surahs.isEmpty()) Text("افتح قسم القرآن لإكمال تهيئة النص المعتمد ثم أعد المحاولة.")
                } }
                val plans = state.plans.filter { it.archived == history }.sortedWith(compareBy({it.dueDay ?: Long.MAX_VALUE},{it.createdDay}))
                if (!state.loading && plans.isEmpty()) item { Text(if(history) "لا خطط مؤرشفة بعد." else "لا خطط نشطة. اختر سورة ونطاق آيات للبدء.") }
                items(plans,key={it.id}) { plan -> IhsanContentCard {
                    Text("${state.surahs.firstOrNull {it.id==plan.surahId}?.name ?: plan.surahId} • ${plan.firstAyah}–${plan.lastAyah}",style=MaterialTheme.typography.titleMedium)
                    Text(if(plan.confirmedDay==null) "بانتظار تأكيد حفظك" else "حفظ مؤكد بتاريخ ${LocalDate.ofEpochDay(plan.confirmedDay)}")
                    plan.dueDay?.let { Text("${if(it<=today) "مستحقة" else "المراجعة التالية"}: ${LocalDate.ofEpochDay(it)}") }
                    IhsanSecondaryButton(onClick={onRead(plan.surahId,plan.firstAyah)}) { Text("فتح النطاق في المصحف") }
                    if(!plan.archived) {
                        if(plan.confirmedDay==null) IhsanButton(onClick={confirmId=plan.id},enabled=canWrite) { Text("تأكيد أنني حفظت هذا النطاق") }
                        else IhsanButton(onClick={revealed=false; testId=plan.id},enabled=canWrite) { Text("بدء الاختبار الذاتي") }
                        if(plan.confirmedDay != null) {
                            var scheduling by rememberSaveable(plan.id) { mutableStateOf(false) }
                            var dueText by rememberSaveable(plan.id) { mutableStateOf("") }
                            var invalidDate by rememberSaveable(plan.id) { mutableStateOf(false) }
                            IhsanSecondaryButton(onClick={scheduling=!scheduling;dueText=LocalDate.ofEpochDay(plan.dueDay ?: today+1).toString()}) { Text("تعديل موعد المراجعة") }
                            if(scheduling) {
                                OutlinedTextField(dueText,{dueText=it;invalidDate=false},label={Text("الموعد الميلادي YYYY-MM-DD")},
                                    modifier=Modifier.fillMaxWidth(),singleLine=true,isError=invalidDate)
                                if(invalidDate) Text("أدخل تاريخًا صحيحًا من الغد وحتى سنة",color=MaterialTheme.colorScheme.error)
                                IhsanSecondaryButton(onClick={
                                    val day=runCatching{LocalDate.parse(dueText.trim()).toEpochDay()}.getOrNull()
                                    if(day==null || day !in today+1..today+365) invalidDate=true
                                    else viewModel.reschedule(plan.id,day)
                                },enabled=canWrite) { Text("حفظ الموعد") }
                            }
                        }
                        IhsanSecondaryButton(onClick={editId=plan.id; surah=plan.surahId; first="${plan.firstAyah}"; last="${plan.lastAyah}"; editor=true},enabled=canWrite) { Text("تعديل النطاق") }
                        IhsanSecondaryButton(onClick={archiveId=plan.id},enabled=canWrite) { Text("أرشفة مع حفظ التاريخ") }
                    }
                    val events = state.reviews.filter { it.planId==plan.id }
                    Text("${events.size} مراجعة مسجلة")
                    events.forEach { event -> Text("${LocalDate.ofEpochDay(event.reviewedDay)} • ${ReviewResult.entries.firstOrNull {it.name==event.result}?.label ?: event.result} • التالي ${LocalDate.ofEpochDay(event.nextDueDay)}",style=MaterialTheme.typography.bodySmall) }
                } }
            }
            item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
        }
    }
    if(confirmId!=null || archiveId!=null) AlertDialog(onDismissRequest={if(!state.busy){confirmId=null;archiveId=null}},
        title={Text(if(confirmId!=null) "تأكيد الحفظ الفعلي" else "أرشفة الخطة")},
        text={Column {
            Text(if(confirmId!=null) "أؤكد أنني حفظت جميع الآيات المحددة. تبدأ المراجعة غدًا." else "تتوقف المراجعات لهذه الخطة وتبقى الآيات والنتائج السابقة محفوظة.")
            state.error?.let { Text(it,color=MaterialTheme.colorScheme.error) }
        }},
        confirmButton={TextButton(onClick={confirmId?.let(viewModel::confirm) ?: archiveId?.let(viewModel::archive)},enabled=canWrite) {Text("تأكيد")}},
        dismissButton={TextButton(onClick={confirmId=null;archiveId=null},enabled=canWrite){Text("إلغاء")}})
}
