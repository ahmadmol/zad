package com.example.feature.goals

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import java.time.DayOfWeek
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun GoalsHomeSummary(onOpen: () -> Unit, viewModel: DailyGoalsViewModel = org.koin.androidx.compose.koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    when {
        state.loading -> IhsanActionCard("أهدافك اليومية", "جارٍ قراءة أهدافك", Icons.Outlined.TaskAlt, onOpen)
        state.readError != null -> IhsanActionCard("أهدافك اليومية", "تعذر قراءة الأهداف • افتح للمحاولة", Icons.Outlined.TaskAlt, onOpen)
        state.todayGoals.isEmpty() -> IhsanActionCard("أهدافك اليومية", "اختر خطوة صغيرة ليومك", Icons.Outlined.TaskAlt, onOpen)
        else -> IhsanDailyGoalCard("أهدافك اليومية",
            state.todayGoals.count { state.ledger!!.count(it.id, state.date) >= it.target }, state.todayGoals.size, onOpen)
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyGoalsScreen(onBack: () -> Unit, viewModel: DailyGoalsViewModel = org.koin.androidx.compose.koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var editor by rememberSaveable { mutableStateOf(false) }
    var editorId by rememberSaveable { mutableStateOf<String?>(null) }
    var template by rememberSaveable { mutableStateOf("") }
    var deleteId by rememberSaveable { mutableStateOf<String?>(null) }
    var savedRevision by remember { mutableIntStateOf(state.saved) }
    LaunchedEffect(state.saved) {
        if (state.saved > savedRevision) { editor = false; savedRevision = state.saved }
    }
    val active = state.ledger?.goals?.filterNot { it.archived }.orEmpty()
    LaunchedEffect(state.ledger, deleteId) {
        if (state.ledger?.goals?.any { it.id == deleteId && it.archived } == true) deleteId = null
    }
    fun create(title: String = "") { editorId = null; template = title; editor = true }
    Column(Modifier.fillMaxSize()) {
        IhsanTopBar("أهدافي اليومية", onNavigateBack = onBack, actions = {
            IconButton({ create() }, enabled = state.ledger != null && !state.busy && active.size < 50) { Icon(Icons.Outlined.Add, "إنشاء هدف", tint = IhsanTheme.colors.accentContent) }
        })
        LazyColumn(Modifier.weight(1f).navigationBarsPadding(), contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            item(key = "goals_summary") {
                IhsanGlassCard(Modifier.fillMaxWidth()) {
                    Text("خطوتك، على مهل", style = MaterialTheme.typography.headlineSmall)
                    Text("أهداف شخصية بتسجيلك الصريح، مستقلة عن سجل الصلاة والصيام.", color = IhsanTheme.colors.textSecondary)
                    Text(state.date.toString(), style = MaterialTheme.typography.labelMedium)
                    if (state.ledger != null) {
                        val completed = state.todayGoals.count { state.ledger!!.count(it.id, state.date) >= it.target }
                        Text("$completed من ${state.todayGoals.size} أهداف اليوم مكتملة", style = MaterialTheme.typography.titleMedium)
                        val history = state.ledger!!.entries
                        Text("${history.count { it.count >= it.target }} إكمال مسجّل • ${history.filter { it.count > 0 }.map { it.date }.distinct().size} أيام بها تسجيل",
                            style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
                    }
                }
            }
            if (state.loading) item(key = "goals_loading") { IhsanLoadingState() }
            state.readError?.let { error -> item(key = "goals_error") {
                IhsanErrorState("تعذر قراءة الأهداف", message = error, onRetry = viewModel::retry)
            } }
            state.actionError?.let { error -> item(key = "goals_write_error") { Text(error, color = MaterialTheme.colorScheme.error) } }
            if (state.ledger != null) {
                item(key = "goals_templates") {
                    IhsanContentCard {
                        IhsanSectionHeader("خطوة تختارها", "قوالب يمكنك تعديلها قبل الحفظ")
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            listOf("قراءة القرآن", "أذكار الصباح", "أذكار المساء", "الاستغفار", "الدعاء", "ركعتا نافلة", "التسبيح").forEach { title ->
                                SuggestionChip({ create(title) }, label = { Text(title) }, enabled = !state.busy && active.size < 50,
                                    modifier = Modifier.heightIn(min = 48.dp))
                            }
                        }
                        IhsanPrimaryButton({ create() }, Modifier.fillMaxWidth(), enabled = !state.busy && active.size < 50) { Text("إضافة هدف شخصي") }
                    }
                }
                if (active.isEmpty()) item(key = "goals_empty") {
                    IhsanEmptyState("اختر ما يناسب يومك. لا يُسجّل أي إنجاز تلقائيًا.", title = "مساحة لخطوتك الأولى", icon = Icons.Outlined.TaskAlt)
                }
                items(active, key = { it.id }) { goal ->
                    val count = state.ledger!!.count(goal.id, state.date)
                    val scheduled = goal.scheduledOn(state.date)
                    IhsanContentCard {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(goal.title, Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                            IconButton({ editorId = goal.id; template = ""; editor = true }, enabled = !state.busy) {
                                Icon(Icons.Outlined.Edit, "تعديل ${goal.title}", tint = IhsanTheme.colors.accentContent)
                            }
                            IconButton({ deleteId = goal.id }, enabled = !state.busy) { Icon(Icons.Outlined.DeleteOutline, "حذف ${goal.title}") }
                        }
                        Text(if (goal.weekdays.size == 7) "يوميًا" else goal.weekdays.sorted().joinToString("، ") {
                            DayOfWeek.of(it).getDisplayName(TextStyle.FULL, Locale("ar"))
                        }, style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
                        if (!scheduled) Text("غير مجدول اليوم", style = MaterialTheme.typography.bodySmall, color = IhsanTheme.colors.textSecondary)
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Checkbox(count >= goal.target, { viewModel.complete(goal.id, state.date, it) }, enabled = scheduled && !state.busy,
                                modifier = Modifier.semantics { contentDescription = "تسجيل إكمال ${goal.title}" })
                            Text(if (count >= goal.target) "مكتمل بتسجيلك" else "تسجيل الإكمال", Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            Text("$count / ${goal.target}", style = MaterialTheme.typography.titleMedium)
                        }
                        if (goal.target > 1) Row(verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            OutlinedIconButton({ viewModel.increment(goal.id, state.date, -1) }, enabled = scheduled && !state.busy && count > 0,
                                modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.Remove, "إنقاص تسجيل ${goal.title}") }
                            OutlinedIconButton({ viewModel.increment(goal.id, state.date, 1) }, enabled = scheduled && !state.busy && count < goal.target,
                                modifier = Modifier.size(48.dp)) { Icon(Icons.Outlined.Add, "زيادة تسجيل ${goal.title}") }
                            Text("يمكنك تعديل التسجيل أو التراجع عنه", Modifier.weight(1f), style = MaterialTheme.typography.bodySmall,
                                color = IhsanTheme.colors.textSecondary)
                        }
                    }
                }
            }
        }
    }
    if (editor) GoalEditor(state.ledger?.goals?.firstOrNull { it.id == editorId }, template, state.busy, state.actionError,
        onDismiss = { if (!state.busy) editor = false }, onSave = { title, target, days -> viewModel.save(editorId, title, target, days) })
    deleteId?.let { id -> AlertDialog(onDismissRequest = { if (!state.busy) deleteId = null },
        title = { Text("حذف الهدف من قائمتك؟") }, text = { Column {
            Text("سيختفي الهدف، وتبقى تسجيلاتك السابقة في الإحصائيات المحلية.")
            state.actionError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        } },
        confirmButton = { TextButton({ viewModel.archive(id) }, enabled = !state.busy, colors = com.example.designsystem.theme.ihsanTextButtonColors()) { Text("حذف الهدف") } },
        dismissButton = { TextButton({ deleteId = null }, enabled = !state.busy) { Text("إلغاء") } }) }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun GoalEditor(goal: DailyGoal?, template: String, busy: Boolean, error: String?, onDismiss: () -> Unit,
    onSave: (String, Int, Set<Int>) -> Unit) {
    var title by rememberSaveable(goal?.id, template) { mutableStateOf(goal?.title ?: template) }
    var target by rememberSaveable(goal?.id, template) { mutableStateOf(goal?.target?.toString() ?: if (template == "الاستغفار") "100" else if (template == "التسبيح") "33" else "1") }
    var days by rememberSaveable(goal?.id, template) { mutableStateOf(goal?.weekdays?.sorted().orEmpty().ifEmpty { (1..7).toList() }) }
    val number = target.toIntOrNull()
    ModalBottomSheet(onDismiss, containerColor = IhsanTheme.colors.surfaceElevated) {
        Column(Modifier.fillMaxWidth().imePadding().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(if (goal == null) "خطوة جديدة" else "تعديل الهدف", style = MaterialTheme.typography.headlineSmall)
            OutlinedTextField(title, { title = it.take(100) }, Modifier.fillMaxWidth(), enabled = !busy,
                label = { Text("اسم الهدف") }, shape = MaterialTheme.shapes.medium)
            OutlinedTextField(target, { target = it.take(5) }, Modifier.fillMaxWidth(), enabled = !busy,
                label = { Text("العدد المستهدف • 1 إلى 10000") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = number == null || number !in 1..10_000, singleLine = true, shape = MaterialTheme.shapes.medium)
            Text("أيام التكرار", style = MaterialTheme.typography.titleMedium)
            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                (1..7).forEach { day -> FilterChip(day in days, {
                    days = if (day in days) days - day else days + day
                }, enabled = !busy, label = { Text(DayOfWeek.of(day).getDisplayName(TextStyle.FULL, Locale("ar"))) }, modifier = Modifier.heightIn(min = 48.dp)) }
            }
            if (days.isEmpty()) Text("اختر يومًا واحدًا على الأقل", color = MaterialTheme.colorScheme.error)
            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            IhsanPrimaryButton({ onSave(title, number!!, days.toSet()) }, Modifier.fillMaxWidth(),
                enabled = !busy && title.isNotBlank() && number != null && number in 1..10_000 && days.isNotEmpty()) { Text(if (busy) "جارٍ الحفظ" else "حفظ الهدف") }
            Spacer(Modifier.height(16.dp))
        }
    }
}
