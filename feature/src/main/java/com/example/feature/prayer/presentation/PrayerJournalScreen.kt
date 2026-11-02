package com.example.feature.prayer.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.designsystem.component.*
import com.example.designsystem.theme.IhsanTheme
import com.example.feature.prayer.domain.model.ManualPrayerStatus
import org.koin.androidx.compose.koinViewModel
import java.time.LocalDate

@Composable
fun PrayerJournalScreen(onBack: () -> Unit, viewModel: ManualPrayerTrackerViewModel = koinViewModel()) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var dateText by rememberSaveable { mutableStateOf(state.date.toString()) }
    var dateError by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(state.date) { dateText = state.date.toString() }
    Scaffold(topBar = { IhsanTopBar("سجل الصلاة اليدوي", onNavigateBack = onBack) }) { padding ->
        LazyColumn(Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item { Text("هذا سجل شخصي على جهازك. لا تُسجّل أي صلاة تلقائيًا، وعدم التسجيل لا يعني عدم الصلاة.", color = IhsanTheme.colors.textSecondary) }
            item { IhsanContentCard {
                OutlinedTextField(dateText, { dateText = it; dateError = false }, label = { Text("التاريخ الميلادي: YYYY-MM-DD") }, isError = dateError, modifier = Modifier.fillMaxWidth(), singleLine = true)
                if (dateError) Text("اختر تاريخًا صحيحًا لا يتجاوز اليوم", color = MaterialTheme.colorScheme.error)
                IhsanSecondaryButton(onClick = {
                    val date = runCatching { LocalDate.parse(dateText.trim()) }.getOrNull()
                    if (date == null || date > LocalDate.now()) dateError = true else viewModel.selectDate(date)
                }, enabled = !state.busy) { Text("عرض اليوم المحدد") }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    TextButton(onClick = { viewModel.selectDate(state.date.minusDays(1)) }, enabled = !state.busy, colors = com.example.designsystem.theme.ihsanTextButtonColors()) { Text("اليوم السابق") }
                    TextButton(onClick = { viewModel.selectDate(state.date.plusDays(1)) }, enabled = state.date < LocalDate.now() && !state.busy, colors = com.example.designsystem.theme.ihsanTextButtonColors()) { Text("اليوم التالي") }
                }
            } }
            if (state.loading) item { IhsanLoadingState() }
            state.error?.let { error -> item { IhsanErrorState("تعذر تحديث السجل", message = error, retryLabel = "إعادة المحاولة", onRetry = viewModel::retry) } }
            if (!state.loading && state.error == null) state.trackable.forEach { prayer ->
                item(key = prayer.name) {
                    var menu by rememberSaveable { mutableStateOf(false) }
                    val status = state.logs[prayer]
                    val label = when (status) {
                        ManualPrayerStatus.PERFORMED -> "أديتها"
                        ManualPrayerStatus.LATE -> "أديتها متأخرًا"
                        ManualPrayerStatus.NOT_PERFORMED -> "لم أؤدّها — بتسجيلي"
                        null -> "غير مسجلة"
                    }
                    IhsanContentCard {
                        Text(prayer.arabic, style = MaterialTheme.typography.titleMedium)
                        Text(label, color = IhsanTheme.colors.textSecondary)
                        Box {
                            IhsanSecondaryButton(onClick = { menu = true }, enabled = !state.busy,
                                modifier = Modifier.semantics { contentDescription = "تعديل تسجيل ${prayer.arabic}، $label" }) { Text("تعديل الحالة") }
                            DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                                listOf("أديتها" to ManualPrayerStatus.PERFORMED, "أديتها متأخرًا" to ManualPrayerStatus.LATE,
                                    "لم أؤدّها" to ManualPrayerStatus.NOT_PERFORMED, "مسح التسجيل" to null).forEach { (title, value) ->
                                    DropdownMenuItem(text = { Text(title) }, onClick = { menu = false; viewModel.setStatus(prayer, value) })
                                }
                            }
                        }
                    }
                }
            }
            if (!state.loading && state.error == null) item { IhsanContentCard {
                Text("الأيام السبعة المنتهية بالتاريخ المحدد", style = MaterialTheme.typography.titleMedium)
                Text("${state.stats.performedThisWeek} صلاة أديتها بحسب تسجيلك • ${state.stats.loggedThisWeek} تسجيلًا من أصل 35")
                Text("${state.stats.daysWithAnyLog} أيام فيها تسجيل. الأسبوع السابق: ${state.stats.performedLastWeek} صلاة أديتها.", color = IhsanTheme.colors.textSecondary)
                Text("الإحصاء لا يصنّف الصلوات غير المسجلة على أنها فائتة.", style = MaterialTheme.typography.bodySmall)
            } }
            if (!state.loading && state.error == null) {
                item { IhsanContentCard {
                    Text("ملخص شهر ${state.date.monthValue}/${state.date.year}", style = MaterialTheme.typography.titleMedium)
                    Text("حتى اليوم فقط: ${state.month.performed} أديتها • ${state.month.recorded} تسجيلًا • ${state.month.unrecorded} غير مسجلة")
                    Text("${state.month.explicitlyNotPerformed} لم أؤدّها بتسجيل صريح. لا يُستنتج شيء من غياب التسجيل.")
                    IhsanSecondaryButton(onClick = { viewModel.selectDate(state.date.withDayOfMonth(1).minusDays(1)) }, enabled = !state.busy) { Text("الشهر السابق") }
                    val nextMonth = state.date.withDayOfMonth(1).plusMonths(1)
                    IhsanSecondaryButton(onClick = { viewModel.selectDate(minOf(nextMonth, LocalDate.now())) }, enabled = !state.busy && nextMonth <= LocalDate.now()) { Text("الشهر التالي") }
                } }
                state.month.days.forEach { day -> item(key = "day:${day.date}") { IhsanContentCard {
                    Text("${day.date} • ${day.loggedCount} مسجلة • ${day.performedCount} أديتها • ${day.unloggedCount} غير مسجلة")
                    IhsanSecondaryButton(onClick = { viewModel.selectDate(day.date) }, enabled = !state.busy) { Text("عرض وتعديل هذا اليوم") }
                } } }
            }
            item { Spacer(Modifier.navigationBarsPadding().height(16.dp)) }
        }
    }
}
