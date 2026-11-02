package com.example.feature.quran.presentation

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.quran.domain.model.PrivateAyahNote
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

/** Uses the approved 604-page index; it never guesses pages from average verse counts. */
internal fun readPageAnchors(context: android.content.Context): List<Pair<Int, Int>> {
    val array = JSONArray(context.assets.open("mushaf_page_starts.json").bufferedReader().use { it.readText() })
    require(array.length() == 604)
    return (0 until array.length()).map { index -> array.getJSONArray(index).let { it.getInt(0) to it.getInt(1) } }
}

@Composable
fun ReadingJourneyScreen(onBack: () -> Unit, onRead: (Int, Int) -> Unit,
    initialSurah: Int = 0, initialAyah: Int = 0, initialTab: Int = 0,
    viewModel: ReadingJourneyViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var tab by rememberSaveable { mutableIntStateOf(initialTab.coerceIn(0, 1)) }
    var planEditor by rememberSaveable { mutableStateOf(false) }
    var noteEditor by rememberSaveable { mutableStateOf(initialSurah > 0 && initialAyah > 0) }
    var noteId by rememberSaveable { mutableStateOf<String?>(null) }
    var surah by rememberSaveable { mutableStateOf(if (initialSurah > 0) "$initialSurah" else "") }
    var ayah by rememberSaveable { mutableStateOf(if (initialAyah > 0) "$initialAyah" else "") }
    var noteText by rememberSaveable { mutableStateOf("") }
    var selectedIds by rememberSaveable { mutableStateOf(arrayListOf<String>()) }
    LaunchedEffect(state.notes, state.loading, state.error) {
        if (!state.loading && state.error == null) {
            val available = state.notes.map { it.id }.toSet()
            selectedIds = ArrayList(selectedIds.filter { it in available })
        }
    }
    var noteQuery by rememberSaveable { mutableStateOf("") }
    val visibleNotes = remember(state.notes, noteQuery) {
        state.notes.filter { com.example.feature.core.search.ArabicSearch.matches(it.text, noteQuery) }
    }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    LaunchedEffect(state.deletedNoteId) {
        state.deletedNoteId?.let { id ->
            selectedIds = ArrayList(selectedIds - id)
            if (deleteId == id) deleteId = null
            viewModel.consumeDeletedNote()
        }
    }
    var exportPreview by rememberSaveable { mutableStateOf(false) }
    var extending by rememberSaveable { mutableStateOf(false) }
    val export = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
        if (uri == null) { viewModel.cancelExport(); return@rememberLauncherForActivityResult }
        val content = viewModel.pendingExport
        scope.launch {
            val success = if (content == null) false else withContext(Dispatchers.IO) {
                runCatching {
                    val stream = requireNotNull(context.contentResolver.openOutputStream(uri, "wt"))
                    stream.bufferedWriter(Charsets.UTF_8).use { it.write(content) }
                }.isSuccess
            }
            viewModel.finishExport(success)
        }
    }
    LaunchedEffect(state.message) {
        if (state.message == "حُفظ التغيير على هذا الجهاز") {
            noteEditor = false; planEditor = false; extending = false
        }
    }
    var anchors by remember { mutableStateOf<List<Pair<Int, Int>>>(emptyList()) }
    LaunchedEffect(context) {
        anchors = withContext(Dispatchers.IO) { runCatching { readPageAnchors(context) }.getOrDefault(emptyList()) }
    }
    Scaffold(topBar = { IhsanTopBar("رحلتي مع القرآن", onNavigateBack = onBack) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 }, text = { Text("خطة القراءة") }, selectedContentColor = IhsanTheme.colors.accentContent)
                Tab(selected = tab == 1, onClick = { tab = 1 }, text = { Text("ملاحظاتي الخاصة") }, selectedContentColor = IhsanTheme.colors.accentContent)
            }
            LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { Text("محفوظة على جهازك فقط. الإنجاز تأكيد منك؛ موضع القراءة لا يثبت الإتمام.",
                    style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary) }
                if (state.loading) item { IhsanLoadingState() }
                state.error?.let { error -> item { IhsanErrorState(title = "تعذر إكمال العملية", message = error,
                    retryLabel = "إعادة قراءة البيانات", onRetry = viewModel::reload) } }
                state.message?.let { message -> item { Text(message, color = IhsanTheme.colors.accentContent) } }
                if (!state.loading && state.dataReady && tab == 0) {
                    item {
                        IhsanButton(onClick = { viewModel.clearMessage(); planEditor = true }, enabled = !state.busy) {
                            Text(if (state.activePlan == null) "إنشاء خطة قراءة" else "إنشاء خطة جديدة مع حفظ السجل السابق")
                        }
                    }
                    val plan = state.activePlan
                    if (plan == null) item { IhsanEmptyState("اختر نطاق صفحات ومدة تناسبك. لن تُسجّل أي صفحة تلقائيًا.", title = "لم تبدأ خطة بعد") }
                    else {
                        val completed = state.completed[plan.id].orEmpty()
                        val today = LocalDate.now()
                        val suggested = plan.suggestedPages(today, completed)
                        item { IhsanContentCard {
                            Text("${completed.count { it in plan.firstPage..plan.lastPage }} / ${plan.pageCount} صفحة مؤكدة", style = MaterialTheme.typography.titleMedium)
                            LinearProgressIndicator(progress = { (completed.size.toFloat() / plan.pageCount).coerceIn(0f, 1f) },
                                modifier = Modifier.fillMaxWidth(), color = IhsanTheme.colors.progressActive, trackColor = IhsanTheme.colors.progressTrack)
                            Text("من ${plan.firstPage} إلى ${plan.lastPage} • الموعد ${LocalDate.ofEpochDay(plan.dueEpochDay)}")
                            Text(if (suggested.isEmpty()) "أتممت نطاق الخطة بتأكيدك" else "اقتراح اليوم: ${suggested.take(12).joinToString("، ")}" + if (suggested.size > 12) "… (${suggested.size} صفحة)" else "", color = IhsanTheme.colors.textSecondary)
                            Text("حدد الصفحة التي أتممتها. يمكنك إلغاء التأكيد لاحقًا.", style = MaterialTheme.typography.bodySmall)
                            TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { viewModel.clearMessage(); extending = true }) { Text("تمديد الموعد مع حفظ الإنجاز") }
                        } }
                        items((plan.firstPage..plan.lastPage).toList(), key = { "page_$it" }) { page ->
                            Row(Modifier.fillMaxWidth().heightIn(min = 56.dp), verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(checked = page in completed, enabled = !state.busy, colors = readingJourneyCheckboxColors(),
                                    modifier = Modifier.semantics { contentDescription = "تأكيد قراءة الصفحة $page" },
                                    onCheckedChange = { checked -> if (checked) viewModel.confirm(plan.id, setOf(page)) else viewModel.clear(plan.id, page) })
                                Text("الصفحة $page", Modifier.weight(1f))
                                TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { anchors.getOrNull(page - 1)?.let { onRead(it.first, it.second) } },
                                    enabled = anchors.size == 604) { Text("قراءة") }
                            }
                        }
                        if (anchors.isEmpty()) item { Text("تعذر تجهيز انتقال الصفحات؛ أعد فتح الشاشة. تأكيد القراءة ما زال متاحًا.", color = IhsanTheme.colors.textSecondary) }
                    }
                    items(state.plans.filterNot { it.active }, key = { it.id }) { previous ->
                        IhsanContentCard { Text("خطة سابقة • ${state.completed[previous.id].orEmpty().size}/${previous.pageCount} صفحة مؤكدة")
                            Text("${LocalDate.ofEpochDay(previous.startEpochDay)} — ${LocalDate.ofEpochDay(previous.dueEpochDay)}", color = IhsanTheme.colors.textSecondary) }
                    }
                } else if (!state.loading && state.dataReady && tab == 1) {
                    item { Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        IhsanButton(onClick = { viewModel.clearMessage(); noteId = null; surah = ""; ayah = ""; noteText = ""; noteEditor = true }, enabled = !state.busy) { Text("إضافة ملاحظة خاصة") }
                        IhsanSecondaryButton(onClick = { exportPreview = true }, enabled = selectedIds.isNotEmpty() && !state.busy) { Text("معاينة تصدير المحدد (${selectedIds.size})") }
                        IhsanSearchBar(query = noteQuery, onQueryChange = { noteQuery = it }, placeholder = "البحث في ملاحظاتي الخاصة")
                    } }
                    if (state.notes.isEmpty()) item { IhsanEmptyState("أضف ملاحظتك من هنا أو من قائمة الآية في المصحف. الملاحظات شخصية وليست تفسيرًا موثقًا.", title = "لا توجد ملاحظات") }
                    if (state.notes.isNotEmpty() && visibleNotes.isEmpty()) item {
                        IhsanEmptyState("جرّب كلمة أخرى أو امسح البحث.", title = "لا توجد ملاحظات مطابقة",
                            primaryActionLabel = "مسح البحث", onPrimaryAction = { noteQuery = "" })
                    }
                    items(visibleNotes, key = { it.id }) { note ->
                        IhsanContentCard {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(note.id in selectedIds, colors = readingJourneyCheckboxColors(), onCheckedChange = { checked -> selectedIds = ArrayList(if (checked) selectedIds + note.id else selectedIds - note.id) },
                                    modifier = Modifier.semantics { contentDescription = "تحديد الملاحظة للتصدير، سورة ${note.surahId} آية ${note.ayahNumber}" })
                                Text("سورة ${note.surahId} • آية ${note.ayahNumber}", Modifier.weight(1f), style = MaterialTheme.typography.titleSmall)
                            }
                            Text(note.text)
                            // Flow layout is unnecessary: stacked full-width actions remain readable at 200%.
                            TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { onRead(note.surahId, note.ayahNumber) }) { Text("فتح الآية") }
                            TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { viewModel.clearMessage(); noteId = note.id; surah = "${note.surahId}"; ayah = "${note.ayahNumber}"; noteText = note.text; noteEditor = true }) { Text("تعديل الملاحظة") }
                            TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { deleteId = note.id }, enabled = !state.busy) { Text("حذف الملاحظة") }
                        }
                    }
                }
                item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
            }
        }
    }
    if (planEditor) PlanEditor(state.busy, state.error, state.activePlan != null, onDismiss = { planEditor = false }, onSave = viewModel::create)
    if (extending) ExtendPlanDialog(state.busy, state.error, onDismiss = { extending = false }, onExtend = { days -> state.activePlan?.let { viewModel.extend(it.id, days) } })
    if (noteEditor) AlertDialog(onDismissRequest = { if (!state.busy) noteEditor = false },
        title = { Text("ملاحظة خاصة — ليست تفسيرًا") },
        text = { Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            NumberField("رقم السورة", surah, { surah = it }, enabled = noteId == null && !state.busy)
            NumberField("رقم الآية", ayah, { ayah = it }, enabled = noteId == null && !state.busy)
            OutlinedTextField(value = noteText, onValueChange = { if (it.length <= 4000) noteText = it }, label = { Text("ملاحظتك الشخصية") }, modifier = Modifier.fillMaxWidth(), enabled = !state.busy, minLines = 3)
            state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } },
        confirmButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { viewModel.saveNote(noteId, surah.toIntOrNull() ?: 0, ayah.toIntOrNull() ?: 0, noteText) }, enabled = state.dataReady && !state.busy && noteText.isNotBlank()) { Text("حفظ محليًا") } },
        dismissButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { noteEditor = false }, enabled = !state.busy) { Text("إلغاء") } })
    deleteId?.let { id -> AlertDialog(onDismissRequest = { if (!state.busy) deleteId = null }, title = { Text("حذف هذه الملاحظة؟") }, text = { Column {
        Text("سيُحذف نص الملاحظة من جهازك. تبقى الآية والمفضلة وموضع القراءة محفوظة.")
        state.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    } },
        confirmButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { viewModel.deleteNote(id) }, enabled = state.dataReady && !state.busy) { Text("حذف") } },
        dismissButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { deleteId = null }, enabled = !state.busy) { Text("إلغاء") } }) }
    if (exportPreview) AlertDialog(onDismissRequest = { exportPreview = false }, title = { Text("تصدير ملاحظاتك الخاصة") },
        text = { Text("سيحتوي الملف على ${selectedIds.size} ملاحظة محددة ونصوصها ومراجع آياتها فقط. يصبح الملف متاحًا خارج التطبيق في المكان الذي تختاره. لن تُرسل بيانات إلى خادم.") },
        confirmButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { exportPreview = false; if (viewModel.prepareExport(selectedIds.toSet())) export.launch("ihsan-private-notes.json") }) { Text("اختيار ملف التصدير") } },
        dismissButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { exportPreview = false }) { Text("إلغاء") } })
}

@Composable
private fun readingJourneyCheckboxColors() = CheckboxDefaults.colors(
    checkedColor = IhsanTheme.colors.accentContent,
    checkmarkColor = if (IhsanTheme.isDark) IhsanTheme.colors.surfaceBase else IhsanTheme.colors.onBrand,
    uncheckedColor = IhsanTheme.colors.textSecondary
)

@Composable
internal fun NumberField(label: String, value: String, onChange: (String) -> Unit, enabled: Boolean = true) {
    OutlinedTextField(value, onChange, label = { Text(label) }, singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth(), enabled = enabled)
}

@Composable
private fun PlanEditor(busy: Boolean, error: String?, replaces: Boolean, onDismiss: () -> Unit, onSave: (Int, Int, Int) -> Unit) {
    var first by rememberSaveable { mutableStateOf("1") }
    var last by rememberSaveable { mutableStateOf("604") }
    var days by rememberSaveable { mutableStateOf("30") }
    val valid = (first.toIntOrNull() ?: 0) in 1..604 && (last.toIntOrNull() ?: 0) in (first.toIntOrNull() ?: 605)..604 && (days.toIntOrNull() ?: 0) in 1..3650
    AlertDialog(onDismissRequest = { if (!busy) onDismiss() }, title = { Text("خطة قراءة جديدة") },
        text = { Column(Modifier.heightIn(max = 400.dp).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("تبدأ اليوم وتشمل يوم البداية والنهاية. إتمام الصفحات تأكيد يدوي منك.")
            if (replaces) Text("ستُؤرشف الخطة الحالية مع إبقاء جميع تأكيداتها.")
            NumberField("الصفحة الأولى (1–604)", first, { first = it }, !busy)
            NumberField("الصفحة الأخيرة", last, { last = it }, !busy)
            NumberField("المدة بالأيام (1–3650)", days, { days = it }, !busy)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } }, confirmButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { onSave(first.toInt(), last.toInt(), days.toInt()) }, enabled = valid && !busy) { Text("إنشاء وحفظ") } },
        dismissButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = onDismiss, enabled = !busy) { Text("إلغاء") } })
}

@Composable
private fun ExtendPlanDialog(busy: Boolean, error: String?, onDismiss: () -> Unit, onExtend: (Int) -> Unit) {
    var days by rememberSaveable { mutableStateOf("30") }
    AlertDialog(onDismissRequest = onDismiss, title = { Text("تمديد الموعد") }, text = { Column {
        NumberField("عدد الأيام ابتداءً من اليوم", days, { days = it }, !busy)
        Text("لن تتغير الصفحات المؤكدة. يجب أن يكون الموعد الجديد مساويًا للحالي أو لاحقًا له.")
        error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    } }, confirmButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = { days.toIntOrNull()?.let(onExtend) }, enabled = !busy && (days.toIntOrNull() ?: 0) in 1..3650) { Text("تمديد") } }, dismissButton = { TextButton(colors = com.example.designsystem.theme.ihsanTextButtonColors(), onClick = onDismiss, enabled = !busy) { Text("إلغاء") } })
}
